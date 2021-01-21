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

                    //calls for list of Class from DB
                    ArrayList<String> classArray = stringCallable(idNo, Class, 1);
                    classArray = processingClass(classArray, idNo,1);
                    //classArray.removeAll(classArray);

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
                                //TODO Send Join Command
                                Toast.makeText(getActivity().getBaseContext(), ("TEST12"), Toast.LENGTH_SHORT).show();
                            }
                        });

                        SearchList.addView(classBtn, params);
                    }

                    final Button subscribeBtn = new Button(root.getContext());
                    subscribeBtn.setText("Know when " + Class + " groups are made.");
                    subscribeBtn.setId(classArray.size());
                    subscribeBtn.setBackgroundColor(Color.GRAY);
                    subscribeBtn.setTextColor(Color.WHITE);

                    subscribeBtn.setOnClickListener(new View.OnClickListener(){
                       @Override
                       public void onClick(View view){
                           //TODO Send Subscribe Request
                           Toast.makeText(getActivity().getBaseContext(), ("Subscribe pressed."), Toast.LENGTH_SHORT).show();
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
        if (pathID == 0) {
            //changes input DB string to readable details of Study Party
            //Input Format: (Sample)
            //{ "class": "Testingology 112", "party": [ 6 ] }
            //{ "class": "Testingology 110", "party": [ 8, 9, 7 ] }
            //Output Format:
            //
            return nameInput.substring(12, nameInput.indexOf(",") - 1);
        }
        else if (pathID == 1){
            //Input Format: (Sample)
            //{ "party": { "partyID": 5, "class": "Testingology 3", "size": 2, "purpose": "Quiz", "location": "Online", "meetTime": 14, "hostID": 9 }
            //{ "party": { "partyID": 6, "class": "Testingology 112", "size": 5, "purpose": "Test", "location": "Online", "meetTime": 18, "hostID": 9 }
            //
            //{ "party": { "partyID": 1, "class": "Testingism", "size": 6, "purpose": "Final", "location": "Online", "meetTime": 11, "hostID": 8 }
            //Output Format:
            //
            nameInput = nameInput.substring(12, nameInput.length() - 2);

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
                placeHold = "ALREADY IN THIS GROUP";
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
            Log.d("debug", "In ProfileFragment: in stringCallable");
            ending1 = ending;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        executor.shutdown();

        if (ending1.indexOf("success") != -1)
            endinG = asterixDBReturn(ending1);

        return endinG;
    }

    private ArrayList<String> asterixDBReturn(String input){
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
            String data = "use StudyParty; " +
                    "SELECT class.class AS class, " +
                    "(SELECT VALUE party.partyID FROM Party party " +
                    "WHERE party.class = class.class) AS party " +
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
            String data = "use StudyParty; " +
                    "SELECT party AS party, " +
                    "(SELECT VALUE guest FROM isGuest guest " +
                    "WHERE guest.partyID = party.partyID " +
                    "AND guest.idNO != " + idNo + ") AS guest " +
                    "FROM Party party " +
                    "WHERE party.class = \"" + Class + "\";";

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
}