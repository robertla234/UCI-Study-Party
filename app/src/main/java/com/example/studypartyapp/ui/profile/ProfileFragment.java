package com.example.studypartyapp.ui.profile;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private TextView name;
    private TextView idnum;
    private TextView major;
    private Button GroupsJoinedBtn;
    private LinearLayout ScrollList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        final TextView textView = root.findViewById(R.id.text_profile);
        profileViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        //saves account ID Number for use
        String idNo = ( getArguments().getString("idSecondAct") );
        Log.d("debug", "In ProfileFragment: after onCreateView setup");

        name = root.findViewById(R.id.profile_name);
        idnum = root.findViewById(R.id.profile_idnum);
        major = root.findViewById(R.id.profile_major);

        Log.d("debug", "In ProfileFragment: idNo:" + idNo);
        ArrayList<String> profileData = stringCallable(idNo, 0);
        String input = profileData.get(0);

        name.setText(getName(input));
        idnum.setText(getID(input));
        major.setText(getMajor(input));

        GroupsJoinedBtn = root.findViewById(R.id.profile_fragment_groupjoined);
        GroupsJoinedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String str = "Groups Joined Button pressed.";
                //textView.setText(str);
            }
        });

        ScrollList = root.findViewById(R.id.scroll_fragment_scrolllistlist);
        ArrayList<String> resultsArray = stringCallable(idNo, 1);
        ScrollList.removeAllViews();
        for (int i = 0; i < resultsArray.size(); i++){
            //Create New Button
            final Button partyBtn = new Button(root.getContext());

            //Set Margins and Parameters between Buttons
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 10, 0, 10);

            //Set Text and Content Settings for  //TODO remove err when fixed
            String processName[] = processingName(resultsArray.get(i), idNo);

            partyBtn.setText(processName[0]); // replace w/ setString
            partyBtn.setId(i); //Button with Unique ID
            partyBtn.setBackgroundColor(Color.DKGRAY);
            partyBtn.setTextColor(Color.WHITE);

            //Control what Button does on Click
            partyBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    //Group Info Requests
                    //TODO Possible new ID with distinct URL/Port data
                    Toast.makeText(getActivity().getBaseContext(), ("Working. " + processName[0] + "."), Toast.LENGTH_SHORT).show();
                    String partyID = processName[1]; //TODO parse partyID from tempArray[i]
                }
            });

            ScrollList.addView(partyBtn, params);
        }

        return root;
    }

    private String getID(String input){
        int start = input.indexOf(":") + 2;
        int end = input.indexOf(",");
        return input.substring(start, end);
    }
    private String getMajor(String input){
        int start = input.lastIndexOf(":") + 3;
        int end = input.lastIndexOf("}") - 2;
        return input.substring(start, end);
    }
    private String getName(String input){
        String result = "";
        int start = input.indexOf(":");
        int end = input.indexOf(",");
        //Log.d("debug", Integer.toString(start) + " " + Integer.toString(end));
        start = input.indexOf(":", end);
        end = input.indexOf(",", end + 1);
        //Log.d("debug", Integer.toString(start) + " " + Integer.toString(end));
        start = input.indexOf(":", end);
        end = input.indexOf(",", end + 1);
        //Log.d("debug", Integer.toString(start) + " " + Integer.toString(end));
        result = input.substring(start + 3, end - 1);
        //Log.d("debug", "results are " + result);

        start = input.indexOf(":", end);
        end = input.indexOf(",", end + 1);
        //Log.d("debug", Integer.toString(start) + " " + Integer.toString(end));
        result = result + " " + input.substring(start + 3, end - 1);
        //Log.d("debug", "results are " + result);

        return result;
    }

    private String[] processingName(String nameInput, String idNo){
        //TODO change to fit and REMOVE ERR
        //changes input DB string to readable details of Study Party
        //Input Format: (Sample)
        //  "partyID": 6, "class": "Test", "size": 6, "purpose": "Final", "location": "Test", "meetTime": 6, "hostID": 8
        //  "partyID": 123, "class": "Testing", "size": 3, "purpose": "Testing", "location": "Online", "meetTime": 23, "hostID": 8
        //Output Format:
        //  CLASS PURPOSE at TIME \n at LOCATION
        String nameInputParse = nameInput.substring(23);
        String[] output = new String[2];

        String[] keyValuePairs = nameInput.split(",");
        Map<String,String> map = new HashMap<>();
        for (String pair : keyValuePairs){
            String[] entry = pair.split(":");
            map.put(entry[0].trim(), entry[1].trim());
        }

        String Class = map.get("\"class\"");
        String Purpose = map.get("\"purpose\"");
        String MeetTime = map.get("\"meetTime\"");
        String Location = map.get("\"location\"");
        String Size = map.get("\"size\"");
        String hostID = map.get("\"hostID\"");

        if (Class.length() != 1)
            Class = Class.substring(1, Class.length() - 1);
        if (Purpose.length() != 1)
            Purpose = Purpose.substring(1, Purpose.length() - 1);
        if (Location.length() != 1)
            Location = Location.substring(1, Location.length() - 1);
        if (hostID.length() != 1)
            hostID = hostID.substring(0, hostID.length() - 2);

        Log.d("debug", "host ID: " + hostID);
        Log.d("debug", Class + " " + Purpose + " " + MeetTime + " " + Location + " " + Size);

        String placeHold = Class + " " + Purpose + " at " + MeetTime + ":00\n" +
                Location + "\n" +
                "People in Party: " + Size;
        if (hostID.equals(idNo))
            placeHold = Class + " " + Purpose + " at " + MeetTime + ":00 (HOST)\n" +
                    Location + "\n" +
                    "People in Party: " + Size;

        output[0] = placeHold;
        output[1] = map.get("\"partyID\"");
        return output;
    }

    private ArrayList<String> stringCallable(String idNo, int pathID){
        //setup for Socket retrieval
        ArrayList<String> endinG = new ArrayList<String>();

        ThreadPoolExecutor executor= (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        List<Future<String>> retList = new ArrayList<>();
        socketGrabCallableProfile trying = new socketGrabCallableProfile(idNo, pathID);
        Future<String> reting = executor.submit(trying);
        retList.add(reting);

        String ending1 = "";
        try {
            String ending = reting.get();
            Log.d("debug", "In ProfileFragment: in stringCallable");
            ending1 = ending;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        executor.shutdown();

        if (ending1.indexOf("success") != -1)
            endinG = asterixDBReturn(ending1, pathID);

        return endinG;
    }

    private ArrayList<String> asterixDBReturn(String input, int pathID){
        //Takes input from AsterixDB and parses it
        //Itemizes parts into ArrayList
        ArrayList<String> results = new ArrayList<String>();

        Log.d("debug", "In ProfileFragment: in asterixDBReturn");

        int ParanIndex1 = input.indexOf("[");
        int ParanIndex2 = input.indexOf("plans") - 4;

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

            results.add(saveStr);
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

class socketGrabCallableProfile implements Callable<String> {
    private String retString;
    private int pathId;

    public socketGrabCallableProfile(String idNo, int pathID){
        this.retString = idNo;
        this.pathId = pathID;
    }

    @Override
    public String call() throws Exception {
        if (pathId == 0)
            return startString(retString);
        else if (pathId == 1)
            return groupString(retString);
        return startString(retString);
    }

    private String startString(String idNo) throws InterruptedException {
        Log.d("debug", "In ProfileFragment: in startString Thread run");
        Socket s;
        DataOutputStream dos;
        DataInputStream dis;
        BufferedReader input;
        String Endresult = null;

        try {
            //TODO 10.0.2.2 is apparently PC localhost port
            Log.d("debug", "idNo:" + idNo);
            String data = "use StudyParty; SELECT VALUE user FROM User user WHERE user.idNo = " + idNo + ";";

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
            Log.d("debug", "In ProfileFragment: in socketGrabCallableProfile startString");
            Log.d("debug", "socketGrabCallableProfile: " + Endresult);

        } catch (IOException e) {
            Log.d("debug", "In ProfileFragment: in startString IOException");
            e.printStackTrace();
        } catch (Exception e) {
            Log.d("debug", "In ProfileFragment: in startString Exception");
        }
        return Endresult;
    }

    private String groupString(String idNo) throws InterruptedException {
        Log.d("debug", "In ProfileFragment: in groupString Thread run");
        Socket s;
        DataOutputStream dos;
        DataInputStream dis;
        BufferedReader input;
        String Endresult = null;

        try {
            //TODO 10.0.2.2 is apparently PC localhost port
            Log.d("debug", "idNo:" + idNo);
            String data = "use StudyParty; " +
                    "SELECT guest.idNo AS id, " +
                    "(SELECT VALUE party FROM Party party " +
                    "WHERE party.partyID = guest.partyID) AS party " +
                    "FROM isGuest guest " +
                    "WHERE guest.idNo = " + idNo + ";";

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
            Log.d("debug", result);
            in.close();

            Endresult = result;
            Log.d("debug", "In ProfileFragment: in socketGrabCallableProfile groupString");
            Log.d("debug", "socketGrabCallableProfile: " + Endresult);

        } catch (IOException e) {
            Log.d("debug", "In ProfileFragment: in groupString IOException");
            e.printStackTrace();
        } catch (Exception e) {
            Log.d("debug", "In ProfileFragment: in groupString Exception");
        }
        return Endresult;
    }
}