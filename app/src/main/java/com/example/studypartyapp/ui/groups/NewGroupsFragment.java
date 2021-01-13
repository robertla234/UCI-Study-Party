package com.example.studypartyapp.ui.groups;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.studypartyapp.R;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class NewGroupsFragment extends Fragment {

    private EditText Name;
    private EditText Class;
    private EditText Purpose;
    private EditText DateTime;
    private EditText Location;
    private EditText PartySize;
    private Button NewGroupBtn;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fragment_groups, container, false);
        //saves account ID Number for use
        String idNo = ( getArguments().getString("idGroup") );

        Name = root.findViewById(R.id.groupgroup_GroupName);
        Class = root.findViewById(R.id.groupgroup_Class);
        Purpose = root.findViewById(R.id.groupgroup_Purpose);
        DateTime = root.findViewById(R.id.groupgroup_DateTime);
        Location = root.findViewById(R.id.groupgroup_Location);
        PartySize = root.findViewById(R.id.groupgroup_PartySize);
        NewGroupBtn = root.findViewById(R.id.groupgroup_CreateBtn);
        NewGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String NAME = Name.getText().toString();
                String CLSS = Class.getText().toString();
                String PRPS = Purpose.getText().toString();
                String DTTM = DateTime.getText().toString();
                String LCTN = Location.getText().toString();
                String PTYS = PartySize.getText().toString();

                if ( CreateNew(NAME, CLSS, PRPS, DTTM, LCTN, PTYS, idNo) ){
                    GroupsFragment oldGroup = new GroupsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("idGroup", idNo);
                    oldGroup.setArguments(bundle);
                    getParentFragmentManager().beginTransaction().
                            replace(R.id.fragment_container, oldGroup).commit();
                }
                else{
                    Toast.makeText(getActivity().getBaseContext(), "Incomplete study group information.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        return root;
    }

    private boolean CreateNew(String name, String classs, String purpose,
                                     String datetime, String location, String partysize, String idNo) {
        if (classs.length() <= 20 && classs.length() > 0
        && partysize.length() <= 20 && partysize.length() > 0
        && location.length() <= 25 && location.length() > 0
        && name.length() <= 20 && name.length() > 0
        && datetime.length() <= 20 && datetime.length() > 0
        && purpose.length() <= 20 && purpose.length() > 0) {
            //Randomly creates and verifies partyID not taken
            //final int random = new Random().nextInt((max - min) + 1) + min;
            String partyID = Integer.toString(0);
            do {
                int random = new Random().nextInt((10000 - 2) + 1) + 2;
                partyID = Integer.toString(random);
            } while (exists(partyID));

            //TODO randomly create and verify partyID not taken
            
            Log.d("debug", "In NewGroupsFragment: in CreateNew after exists call");
            Log.d("debug", "idNo: " + idNo);
            ArrayList<String> tray = stringCallable(partyID, 1, classs, partysize, purpose,
                    location, datetime, idNo);
            if (tray == null) {
                Log.d("debug", "In NewGroupsFragment: in CreateNew - tray == null");
                return false;
            }
            return true;
        }
        else {
            return false;
        }
    }

    private boolean exists(String partyID) {
        //TODO check if partyID exists already (false = NOT exist, true = exists)
        String msgToSend = "USE StudyParty; SELECT guests.partyID FROM isGuest guests WHERE guests.partyID = " + partyID + ";";
        ArrayList<String> existsRes = stringCallable(partyID, 0, "", "", "", "", "", "");
        if (existsRes.size() != 0)
            return true;

        return false;
    }

    private ArrayList<String> stringCallable(String idNo, int pathID, String Class, String size, String purpose,
                                             String location, String meetTime, String hostID){
        //setup for Socket retrieval
        ArrayList<String> endinG = new ArrayList<String>();

        ThreadPoolExecutor executor= (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        List<Future<String>> retList = new ArrayList<>();
        socketGrabCallableNewGroup trying = new socketGrabCallableNewGroup(idNo, pathID, Class, size, purpose, location, meetTime, hostID);
        Future<String> reting = executor.submit(trying);
        retList.add(reting);

        String ending1 = "";
        try {
            String ending = reting.get();
            Log.d("debug", "In NewGroupsFragment: in stringCallable");
            ending1 = ending;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        executor.shutdown();

        endinG = asterixDBReturn(ending1, pathID);

        return endinG;
    }

    private ArrayList<String> asterixDBReturn(String input, int pathID){
        if (pathID == 1){
            //Takes input from AsterixDB and parses it
            //Itemizes parts into ArrayList
            ArrayList<String> results = new ArrayList<String>();

            Log.d("debug", "In MainFragNew: in asterixDBReturn");
            Log.d("debug", "In:" + input);
            if (input == null){
                Log.d("debug", "Returning null...");
                return null;
            }
            int ParanIndex1 = input.indexOf("\"status\"");
            results.add(input.substring(ParanIndex1, ParanIndex1 + 20));

            return results;
        }

        //Takes input from AsterixDB and parses it
        //Itemizes parts into ArrayList
        ArrayList<String> results = new ArrayList<String>();

        Log.d("debug", "In NewGroupsFragment: in asterixDBReturn");

        int ParanIndex1 = input.indexOf("[");
        int ParanIndex2 = input.indexOf("]");

        //resultOnly removes all other parts of DB String
        //  and leaves the results only
        String resultOnly = input.substring(ParanIndex1 + 2, ParanIndex2 - 1);

        //This section does the parsing of the { }
        String whatsLeft = resultOnly;
        int finwhatsLeft = whatsLeft.length() - 1;
        int openParIndex = whatsLeft.indexOf("{");
        int closeParIndex = whatsLeft.indexOf("}");
        String saveStr = "";
        int iteration = 1;

        while (openParIndex != -1 && closeParIndex != -1){
            saveStr = whatsLeft.substring(openParIndex, closeParIndex + 1);

            results.add(saveStr.substring(1,saveStr.length() - 2));
            if (whatsLeft.indexOf("{", closeParIndex) != -1)
                whatsLeft = whatsLeft.substring(whatsLeft.indexOf("{", closeParIndex));
            else {
                whatsLeft = "";
            }
            openParIndex = whatsLeft.indexOf("{");
            closeParIndex = whatsLeft.indexOf("}");
            finwhatsLeft = whatsLeft.length() - 1;
        }

        for (int i = 0; i < results.size(); i++){
            Log.d("debug", "in:" + results.get(i));
        }

        return results;
    }
}

class socketGrabCallableNewGroup implements Callable<String> {
    private String retString;
    private String Class;
    private String Size;
    private String Purpose;
    private String Location;
    private String meetTime;
    private String hostId;

    private int pathId;

    public socketGrabCallableNewGroup(String partyID, int pathID, String Class, String size, String purpose,
                                      String location, String meetTime, String hostID){
        this.retString = partyID;
        this.pathId = pathID;

        this.Class = Class;
        this.Size = size;
        this.Purpose = purpose;
        this.Location = location;
        this.meetTime = meetTime;
        this.hostId = hostID;
    }

    @Override
    public String call() throws Exception {
        if (pathId == 0)
            return idCheckString(retString);
        else if (pathId == 1)
            return startString(retString, Class, Size, Purpose, Location, meetTime, hostId);
        return startString(retString, Class, Size, Purpose, Location, meetTime, hostId);
    }

    private String startString(String partyid, String Class, String Size, String Purpose,
                               String Location, String meetTime, String hostId) throws InterruptedException {
        Log.d("debug", "In NewGroupsFragment: in startString Thread run");
        Socket s;
        DataOutputStream dos;
        DataInputStream dis;
        BufferedReader input;
        String Endresult = null;

        try {
            //TODO 10.0.2.2 is apparently PC localhost port
            Log.d("debug", "partyid:" + partyid);
            String data = "use StudyParty; INSERT INTO Party ([ " +
                    "{\"partyID\": " + partyid +
                    ", \"class\": \"" + Class +
                    "\" , \"size\": " + Size +
                    " , \"purpose\": \"" + Purpose +
                    "\", \"location\": \"" + Location +
                    "\", \"meetTime\": " + meetTime +
                    ", \"hostID\": " + hostId + " }"
                    + " ]);" +
                    "INSERT INTO isGuest ([ " +
                    "{\"idNo\": " + hostId +
                    ", \"partyID\": " + partyid + " }"
                    + " ]);";

            String params = "statement=" + URLEncoder.encode(data, "UTF-8")
                    + "&pretty=" + URLEncoder.encode("False", "UTF-8");

            String result = new String();

            URL url = new URL("http://10.0.2.2:19002/query/service");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setDoOutput(true);

            PrintWriter out = new PrintWriter(con.getOutputStream());
            out.println(params);
            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream(), "UTF-8"));

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                result = result.concat(inputLine);
            }
            //Log.d("debug", result);
            in.close();

            Endresult = result;
            Log.d("debug", "In NewGroupsFragment: in socketGrabCallableNewGroup startString");
            Log.d("debug", "socketGrabCallableNewGroup: " + Endresult);

        } catch (IOException e) {
            Log.d("debug", "In NewGroupsFragment: in startString IOException");
            e.printStackTrace();
        } catch (Exception e) {
            Log.d("debug", "In NewGroupsFragment: in startString Exception");
        }
        return Endresult;
    }

    private String idCheckString(String idNo) throws InterruptedException {
        Log.d("debug", "In NewGroupsFragment: in idCheckString Thread run");
        Socket s;
        DataOutputStream dos;
        DataInputStream dis;
        BufferedReader input;
        String Endresult = null;

        try {
            //TODO 10.0.2.2 is apparently PC localhost port
            Log.d("debug", "idNo:" + idNo);
            String data = "use StudyParty; SELECT VALUE party FROM Party party WHERE party.partyID = " + idNo + ";";

            String params = "statement=" + URLEncoder.encode(data, "UTF-8")
                    + "&pretty=" + URLEncoder.encode("False", "UTF-8");

            String result = new String();

            URL url = new URL("http://10.0.2.2:19002/query/service");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setDoOutput(true);

            PrintWriter out = new PrintWriter(con.getOutputStream());
            out.println(params);
            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream(), "UTF-8"));

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                result = result.concat(inputLine);
            }
            //Log.d("debug", result);
            in.close();

            Endresult = result;
            Log.d("debug", "In NewGroupsFragment: in socketGrabCallableNewGroup idCheckString");
            Log.d("debug", "socketGrabCallableNewGroup: " + Endresult);

        } catch (IOException e) {
            Log.d("debug", "In NewGroupsFragment: in idCheckString IOException");
            e.printStackTrace();
        } catch (Exception e) {
            Log.d("debug", "In NewGroupsFragment: in idCheckString Exception");
        }
        return Endresult;
    }
}
