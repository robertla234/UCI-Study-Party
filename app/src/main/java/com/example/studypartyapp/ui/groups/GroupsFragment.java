package com.example.studypartyapp.ui.groups;

import android.app.Activity;
import android.content.Context;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.studypartyapp.MainActivity;
import com.example.studypartyapp.R;
import com.example.studypartyapp.SecondActivity;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class GroupsFragment extends Fragment {

    private GroupsViewModel groupsViewModel;
    private TextView YourGroupsTitle;
    private Button NewGroupBtn;
    private LinearLayout GroupList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        groupsViewModel =
                new ViewModelProvider(this).get(GroupsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_groups, container, false);
        final TextView textView = root.findViewById(R.id.text_groups);
        groupsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        //saves account ID Number for use
        String idNo = ( getArguments().getString("idSecondAct") );
        Log.d("debug", "In GroupsFragment: after onCreateView setup");

        YourGroupsTitle = root.findViewById(R.id.title_groups);
        GroupList = root.findViewById(R.id.groups_fragment_groupslistlist);
        NewGroupBtn = root.findViewById(R.id.groups_fragment_newgroupbutton);
        NewGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String str = "Button pressed.";
                //textView.setText(str);

                //Create new Fragment to do NewGroup
                NewGroupsFragment newGroup = new NewGroupsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("idGroup", idNo);
                newGroup.setArguments(bundle);
                getParentFragmentManager().beginTransaction().
                        replace(R.id.fragment_container, newGroup).commit();
            }
        });

        //calls DB for Groups before setting each individual one as buttons
        ArrayList<String> resultsArray = stringCallable(idNo);

        GroupList.removeAllViews(); //reset LinearLayout
        for (int i = 0; i < resultsArray.size(); i++){
            //Create New Button
            final Button groupBtn = new Button(root.getContext());

            //Set Margins and Parameters between Buttons
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 10, 0, 10);

            //Set Text and Content Settings for Button
            String processName[] = processingName(resultsArray.get(i));

            groupBtn.setText(processName[0]); // replace w/ setString
            groupBtn.setId(i); //Button with Unique ID
            groupBtn.setBackgroundColor(Color.DKGRAY);
            groupBtn.setTextColor(Color.WHITE);

            //Control what Button does on Click
            groupBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    //Group Info Requests
                    //TODO Possible new ID with distinct URL/Port data
                    Toast.makeText(getActivity().getBaseContext(), ("Working. " + processName[0] + "."), Toast.LENGTH_SHORT).show();
                    String partyID = processName[1]; //TODO parse partyID from tempArray[i]
                }
            });

            GroupList.addView(groupBtn, params);
        }

        return root;
    }

    private String[] processingName(String nameInput){
        //changes input DB string to readable details of Study Party
        //Input Format: (Sample)
        //  "partyID": 6, "class": "Test", "size": 6, "purpose": "Final", "location": "Test", "meetTime": 6, "hostID": 8
        //  "partyID": 123, "class": "Testing", "size": 3, "purpose": "Testing", "location": "Online", "meetTime": 23, "hostID": 8
        //Output Format:
        //  CLASS PURPOSE at TIME \n at LOCATION
        nameInput = nameInput.substring(0, nameInput.indexOf(" \"guests\":"));
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

        if (Class.length() != 1)
            Class = Class.substring(1, Class.length() - 1);
        if (Purpose.length() != 1)
            Purpose = Purpose.substring(1, Purpose.length() - 1);
        if (Location.length() != 1)
            Location = Location.substring(1, Location.length() - 1);

        Log.d("debug", Class + " " + Purpose + " " + MeetTime + " " + Location + " " + Size);

        output[0] = Class + " " + Purpose + " at " + MeetTime + ":00\n" +
         Location + "\n" +
         "People in Party: " + Size;
        output[1] = map.get("\"partyID\"");
        return output;
    }

    private ArrayList<String> stringCallable(String idNo){
        //setup for Socket retrieval
        ArrayList<String> endinG = new ArrayList<String>();

        ThreadPoolExecutor executor= (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        List<Future<String>> retList = new ArrayList<>();
        socketGrabCallableGroup trying = new socketGrabCallableGroup(idNo);
        Future<String> reting = executor.submit(trying);
        retList.add(reting);

        String ending1 = "";
        try {
            String ending = reting.get();
            Log.d("debug", "In GroupsFragment: in stringCallable");
            ending1 = ending;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        executor.shutdown();

        endinG = asterixDBReturn(ending1);

        return endinG;
    }

    private ArrayList<String> asterixDBReturn(String input){
        //Takes input from AsterixDB and parses it
        //Itemizes parts into ArrayList
        ArrayList<String> results = new ArrayList<String>();

        Log.d("debug", "In GroupsFragment: in asterixDBReturn");

        int ParanIndex1 = input.indexOf("[");
        int PlansIndex = input.indexOf("\"plans\"");
        int ParanIndex2 = input.lastIndexOf("]", PlansIndex);

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

class socketGrabCallableGroup implements Callable<String> {
    private String retString;

    public socketGrabCallableGroup(String idNo){
        this.retString = idNo;
    }

    @Override
    public String call() throws Exception {
        return startString(retString);
    }

    private String startString(String idNo) throws InterruptedException {
        Log.d("debug", "In GroupsFragment: in startString Thread run");
        Socket s;
        DataOutputStream dos;
        DataInputStream dis;
        BufferedReader input;
        String Endresult = null;

        try {
            //TODO 10.0.2.2 is apparently PC localhost port
            Log.d("debug", "idNo:" + idNo);
            String data = "use StudyParty1; " +
                    "SELECT VALUE party " +
                    "FROM Party party " +
                    "WHERE party.hostID = " + idNo + ";";

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
            Log.d("debug", "In GroupsFragment: in socketGrabCallableGroup startString");
            Log.d("debug", "socketGrabCallableGroup: " + Endresult);

        } catch (IOException e) {
            Log.d("debug", "In GroupsFragment: in startString IOException");
            e.printStackTrace();
        } catch (Exception e) {
            Log.d("debug", "In GroupsFragment: in startString Exception");
        }
        return Endresult;
    }
}