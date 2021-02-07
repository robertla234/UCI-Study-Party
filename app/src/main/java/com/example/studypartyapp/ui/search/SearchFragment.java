package com.example.studypartyapp.ui.search;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class SearchFragment extends Fragment { //implements SearchView.OnQueryTextListener{

    private TextView ProfileTitle;
    private SearchViewModel searchViewModel;
    private LinearLayout SearchList;

    public View onCreateView (@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        searchViewModel =
                new ViewModelProvider(this).get(SearchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        final TextView textView = root.findViewById(R.id.text_search);
        searchViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s); //setText(s) changes text to text in SearchViewModel
            }
        });
        //saves account ID Number for use
        String idNo = ( getArguments().getString("idSecondAct") );
        Log.d("debug", "In SearchFragment: after onCreateView setup");

        ProfileTitle = root.findViewById(R.id.title_profile);
        SearchList = root.findViewById(R.id.search_fragment_searchlistlist);

        //calls for list of Class from DB
        ArrayList<String> classArray = stringCallable(idNo, "", 0);
        classArray = processingClass(classArray, idNo,0);

        SearchList.removeAllViews();
        for (int i = 0; i < classArray.size(); i++){
            //CreateNewButton
            final Button classBtn = new Button(root.getContext());

            //Set Margins and Parameters between Buttons
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 10, 0, 10);

            //Set Text and Content Settings for Button
            String processName = classArray.get(i);

            classBtn.setText(processName); // replace w/ setString
            classBtn.setId(i); //Button with Unique ID
            classBtn.setBackgroundColor(Color.DKGRAY);
            classBtn.setTextColor(Color.WHITE);

            //Control what Button does on Click
            classBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    //Group Info Requests
                    //TODO Change View to All Groups of That Class
                    String Class = processName;
                    ProfileTitle.setText("Join a Group");

                    boolean classEmpty = false;
                    //calls for list of Class from DB
                    ArrayList<String> classArray = stringCallable(idNo, Class, 1);
                    if (classArray.isEmpty()) {
                        classArray.add("No new groups for " + Class);
                        classEmpty = true;
                        Log.d("Search-availGroups", "No new groups Indicator");
                    }
                    else {
                        classArray = processingClass(classArray, idNo, 1);
                        Log.d("Search-availGroups", "Existing unjoined groups Indicator");
                    }

                    SearchList.removeAllViews();
                    if (classEmpty){
                        final Button classBtn = new Button(root.getContext());
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(0, 10, 0, 10);

                        classBtn.setText(classArray.get(0)); // replace w/ setString
                        classBtn.setId(0); //Button with Unique ID
                        classBtn.setBackgroundColor(Color.DKGRAY);
                        classBtn.setTextColor(Color.WHITE);
                        classBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {}
                        });
                        SearchList.addView(classBtn, params);
                    }
                    else {
                        for (int i = 0; i < classArray.size(); i++) {
                            //CreateNewButton
                            final Button classBtn = new Button(root.getContext());

                            //Set Margins and Parameters between Buttons
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.setMargins(0, 10, 0, 10);

                            //Set Text and Content Settings for Button
                            String processInput = classArray.get(i);
                            String partyID = processInput.substring(0, processInput.indexOf("?"));
                            String processName = processInput.substring(processInput.indexOf("?") + 1);

                            classBtn.setText(processName); // replace w/ setString
                            classBtn.setId(i); //Button with Unique ID
                            classBtn.setBackgroundColor(Color.DKGRAY);
                            classBtn.setTextColor(Color.WHITE);

                            classBtn.setTag(0);
                            //Control what Button does on Click
                            classBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //Join Command
                                    final int status = (Integer) view.getTag();
                                    if(status == 0) {
                                        ArrayList<String> joinArray = stringCallable(idNo, partyID, 2);
                                        Toast.makeText(getActivity().getBaseContext(), ("partyID:" + partyID), Toast.LENGTH_SHORT).show();
                                        view.setTag(1);
                                        classBtn.setText("GROUP JOINED");
                                    }
                                }
                            });

                            SearchList.addView(classBtn, params);
                        }
                    }

                    final Button subscribeBtn = new Button(root.getContext());
                    subscribeBtn.setText("Know when " + Class + " groups are made.");
                    subscribeBtn.setId(classArray.size());
                    subscribeBtn.setBackgroundColor(Color.GRAY);
                    subscribeBtn.setTextColor(Color.WHITE);

                    subscribeBtn.setTag(3);
                    subscribeBtn.setOnClickListener(new View.OnClickListener(){
                       @Override
                       public void onClick(View view){
                           //TODO Send Subscribe Request
                           final int subscribeStatus = (Integer) view.getTag();
                           if(subscribeStatus == 3) {
                               ArrayList<String> subscribeArray = stringCallable(idNo, Class, 3);
                               Toast.makeText(getActivity().getBaseContext(), ("Subscribed to " + Class + "."), Toast.LENGTH_SHORT).show();
                               Toast.makeText(getActivity().getBaseContext(), Integer.toString(subscribeStatus), Toast.LENGTH_SHORT).show();
                               view.setTag(4);
                               subscribeBtn.setText("SUBSCRIBED!");
                           }
                       }
                    });
                    SearchList.addView(subscribeBtn, params);
                }
            });

            SearchList.addView(classBtn, params);
        }

        return root;
    }

    private String processingName(String nameInput, String idNo, int pathID){
        Log.d("debug", "In Search Fragment: in processingName -> path " + Integer.toString(pathID));
        if (pathID == 0) {
            //changes input DB string to readable details of Study Party
            //Input Format: (Sample)
            //{ "class": "Testingology 112", "party": [ 6 ] }
            //{ "class": "Testingology 110", "party": [ 8, 9, 7 ] }
            //Output Format:
            //
            Log.d("procName", nameInput);
            return nameInput;
        }
        else if (pathID == 1){
            //Input Format: (Sample)
            //{ "party": { "partyID": 5, "class": "Testingology 3", "size": 2, "purpose": "Quiz", "location": "Online", "meetTime": 14, "hostID": 9 }, "guest": [  ] }
            //{ "party": { "partyID": 7, "class": "Testingology 110", "size": 7, "purpose": "Test", "location": "Online", "meetTime": 8, "hostID": 9 }, "guest": [ { "idNo": 9, "partyID": 7 }
            //
            //{ "party": { "partyID": 1, "class": "Testingism", "size": 6, "purpose": "Final", "location": "Online", "meetTime": 11, "hostID": 8 }
            //Output Format:
            //
            String nameInputGuest = nameInput.substring(nameInput.indexOf("\"guests\"") - 1, nameInput.length() - 2);
            nameInput = nameInput.substring(2, nameInput.indexOf("\"guests\"") - 2);
            Log.d("asterixdbret", "pathID 1:" + nameInput);
            Log.d("asterixdbret", nameInputGuest);

            String[] keyValuePairs = nameInput.split(",");
            //for (int jkl = 0; jkl < keyValuePairs.length; jkl++) { Log.d("asterixdbret", keyValuePairs[jkl]); }
            Map<String,String> map = new HashMap<>();
            for (String pair : keyValuePairs){
                String[] entry = pair.split(":");
                map.put(entry[0].trim(), entry[1].trim());
            }
            String[] entry = nameInputGuest.split(":");
            map.put(entry[0].trim(), entry[1].trim());

            String guests = map.get("\"guests\"");
            Log.d("debug", "guests: " + guests);
            if (guests.contains(idNo)){
                Log.d("debug", "No guests. ALREADY IN GROUP");
                return "ALREADY IN THIS GROUP";
            }

            String partyID = map.get("\"partyID\"");
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

            Log.d("debug", "host ID: " + hostID);
            Log.d("debug", Class + " " + Purpose + " " + MeetTime + " " + Location + " " + Size);

            String placeHold = partyID + "?" + Class + " " + Purpose + " at " + MeetTime + ":00\n" +
                    Location + "\n" +
                    "People in Party: " + Size;
            return placeHold;
        }

        return "";
    }

    private ArrayList<String> processingClass(ArrayList<String> classArray, String idNo, int pathID){
        Log.d("debug", "In SearchFragment: in processingClass -> path " + Integer.toString(pathID));
        ArrayList<String> output = new ArrayList<>();

        output.add(processingName(classArray.get(0), idNo, pathID));
        for (int i = 1; i < classArray.size(); i++){
            String fill = processingName(classArray.get(i), idNo, pathID);
            if (!output.contains(fill)){
                output.add(fill);
                Log.d("debug", "In SearchFragment: in processingClass");
                Log.d("debug", "fill:"+ fill);
            }
        }

        return output;
    }

    private ArrayList<String> stringCallable(String use1, String use2, int pathID){
        //setup for Socket retrieval
        ArrayList<String> endinG = new ArrayList<String>();

        ThreadPoolExecutor executor= (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        List<Future<String>> retList = new ArrayList<>();
        socketGrabCallableSearch trying = new socketGrabCallableSearch(use1, use2, pathID);
        Future<String> reting = executor.submit(trying);
        retList.add(reting);

        String ending1 = "";
        try {
            String ending = reting.get();
            Log.d("debug", "In SearchFragment: in stringCallable");
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
        if (pathID == 2){
            //Takes input from AsterixDB and parses it
            //Itemizes parts into ArrayList
            ArrayList<String> results = new ArrayList<String>();

            Log.d("debug", "In SearchFragment: in asterixDBReturn");
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

        Log.d("debug", "In SearchFragment: in asterixDBReturn -> path " + Integer.toString(pathID));

        int ParanIndex1 = input.indexOf("[");
        int ParanIndex2 = input.indexOf("plans") - 4;

        //resultOnly removes all other parts of DB String
        //  and leaves the results only
        String resultOnly = input.substring(ParanIndex1 + 2, ParanIndex2 - 1);
        Log.d("asterixdbret", "resultOnly:" + resultOnly);

        if (pathID == 0){
            String whatsLeft = resultOnly.substring(0, resultOnly.length() - 1);
            //Log.d("asterixdbret", "whatsLeft:" + whatsLeft);
            String[] temp = whatsLeft.split(",");
            for (int i = 0; i < temp.length; i++){
                String addee = temp[i];
                //Log.d("asterixdbret", "addee 1:" + addee);
                int start = addee.indexOf("\"");
                int end = addee.indexOf("\"", start + 1);
                addee = addee.substring(start + 1, end);
                //Log.d("asterixdbret", Integer.toString(start) + " " + Integer.toString(end));
                results.add(addee);
            }
        }

        else if (pathID == 1){
            //This section does the parsing of the { }
            String whatsLeft = resultOnly;
            int openParIndex = whatsLeft.indexOf("{");
            int closeParIndex = whatsLeft.indexOf("}");
            //closeParIndex = whatsLeft.indexOf("}", closeParIndex + 1);
            String saveStr = "";

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
                //closeParIndex = whatsLeft.indexOf("}", closeParIndex + 1);
            }
        }

        for (int i = 0; i < results.size(); i++){
            Log.d("debug", "in:" + results.get(i));
        }

        return results;
    }
}

class socketGrabCallableSearch implements Callable<String> {
    private String retString;
    private int pathId;
    private String use2;

    public socketGrabCallableSearch(String idNo, String use2, int pathID){
        this.retString = idNo;
        this.pathId = pathID;
        this.use2 = use2;
    }

    @Override
    public String call() throws Exception {
        if (pathId == 0)
            return startString(retString);
        else if (pathId == 1)
            return groupString(retString, use2);
        else if(pathId == 2)
            return joinString(retString, use2);
        else if(pathId == 3)
            return subscribeString(retString, use2);
        return startString(retString);
    }

    private String startString(String idNo) throws InterruptedException {
        Log.d("debug", "In SearchFragment: in startString Thread run");
        Socket s;
        DataOutputStream dos;
        DataInputStream dis;
        BufferedReader input;
        String Endresult = null;

        try {
            //TODO 10.0.2.2 is apparently PC localhost port
            Log.d("debug", "idNo:" + idNo);
            String data = "use StudyParty1; " +
                    "SELECT VALUE class.class " +
                    "FROM Class class;";

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
            Log.d("debug", "In SearchFragment: in socketGrabCallableSearch startString");
            Log.d("debug", "socketGrabCallableSearch: " + Endresult);

        } catch (IOException e) {
            Log.d("debug", "In SearchFragment: in startString IOException");
            e.printStackTrace();
        } catch (Exception e) {
            Log.d("debug", "In SearchFragment: in startString Exception");
        }
        return Endresult;
    }

    private String groupString(String idNo, String Class) throws InterruptedException {
        Log.d("debug", "In SearchFragment: in groupString Thread run");
        Socket s;
        DataOutputStream dos;
        DataInputStream dis;
        BufferedReader input;
        String Endresult = null;

        try {
            //TODO 10.0.2.2 is apparently PC localhost port
            Log.d("debug", "idNo:" + idNo);
            String data = "use StudyParty1; " +
                    "SELECT VALUE p " +
                    "FROM Party p " +
                    "WHERE p.class = \"" + Class + "\" AND " + idNo + " NOT in p.guests;";


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
            Log.d("debug", "In SearchFragment: in socketGrabCallableSearch groupString");
            Log.d("debug", "socketGrabCallableSearch: " + Endresult);

        } catch (IOException e) {
            Log.d("debug", "In SearchFragment: in groupString IOException");
            e.printStackTrace();
        } catch (Exception e) {
            Log.d("debug", "In SearchFragment: in groupString Exception");
        }
        return Endresult;
    }

    private String joinString(String idNo, String partyID) throws InterruptedException {
        Log.d("debug", "In SearchFragment: in joinString Thread run");
        Socket s;
        DataOutputStream dos;
        DataInputStream dis;
        BufferedReader input;
        String Endresult = null;

        try {
            //TODO 10.0.2.2 is apparently PC localhost port
            Log.d("debug", "idNo:" + idNo);
            Log.d("debug", "partyID:" + partyID);
            String data = "use StudyParty1; " +
                    "UPSERT INTO Party ([{ " +
                    "\"partyID\": " + partyID + ", " +
                    "\"class\": (SELECT VALUE c.class FROM Party c WHERE c.partyID = " + partyID + ")[0], " +
                    "\"size\": (SELECT VALUE c.size FROM Party c WHERE c.partyID = " + partyID + ")[0], " +
                    "\"purpose\": (SELECT VALUE c.purpose FROM Party c WHERE c.partyID = " + partyID + ")[0], " +
                    "\"location\": (SELECT VALUE c.location FROM Party c WHERE c.partyID = " + partyID + ")[0], " +
                    "\"meetTime\": (SELECT VALUE c.meetTime FROM Party c WHERE c.partyID = " + partyID + ")[0], " +
                    "\"hostID\": (SELECT VALUE c.hostID FROM Party c WHERE c.partyID = " + partyID + ")[0], " +
                    "\"guests\": ARRAY_APPEND(" +
                    "(SELECT VALUE c.guests FROM Party c WHERE c.partyID = " + partyID + ")[0], " + idNo + ") " +
                    "}]);";

            //TODO Redo data command

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
            Log.d("debug", "In SearchFragment: in socketGrabCallableSearch joinString");
            Log.d("debug", "socketGrabCallableSearch: " + Endresult);

        } catch (IOException e) {
            Log.d("debug", "In SearchFragment: in joinString IOException");
            e.printStackTrace();
        } catch (Exception e) {
            Log.d("debug", "In SearchFragment: in joinString Exception");
        }
        return Endresult;
    }

    private String subscribeString(String idNo, String Class) throws InterruptedException {
        Log.d("debug", "In SearchFragment: in subscribeString Thread run");
        Socket s;
        DataOutputStream dos;
        DataInputStream dis;
        BufferedReader input;
        String Endresult = null;

        try {
            //TODO 10.0.2.2 is apparently PC localhost port
            Log.d("debug", "idNo:" + idNo);
            Log.d("debug", "class:" + Class);
            String data = "use StudyParty1; " +
                    "SUBSCRIBE to newClassChannel(\""+ Class +"\", " + idNo + ") on brokerC;";

            //TODO Redo data command

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
            Log.d("debug", "In SearchFragment: in socketGrabCallableSearch subscribeString");
            Log.d("debug", "socketGrabCallableSearch: " + Endresult);

        } catch (IOException e) {
            Log.d("debug", "In SearchFragment: in subscribeString IOException");
            e.printStackTrace();
        } catch (Exception e) {
            Log.d("debug", "In SearchFragment: in subscribeString Exception");
        }
        return Endresult;
    }
}