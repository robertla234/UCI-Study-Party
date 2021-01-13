package com.example.studypartyapp;

import android.content.Intent;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class MainFragNew extends Fragment {

    private Button NewCreateBtn;
    private EditText IDNumberEdTxt;
    private EditText PasswordEdTxt;
    private EditText FirstNameEdTxt;
    private EditText LastNameEdTxt;
    private EditText MajorEdTxt;

    private Button NewBackBtn;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main_new, container, false);
        Log.d("debug", "In MainFragNew: onCreate setup");
        /*
        NewBackBtn = root.findViewById(R.id.new_user_backBtn);
        NewBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        */

        IDNumberEdTxt = root.findViewById(R.id.new_user_IDNum);
        PasswordEdTxt = root.findViewById(R.id.new_user_Password);
        FirstNameEdTxt = root.findViewById(R.id.new_user_FirstName);
        LastNameEdTxt = root.findViewById(R.id.new_user_LastName);
        MajorEdTxt = root.findViewById(R.id.new_user_Major);
        NewCreateBtn = root.findViewById(R.id.new_user_CreateNew);
        NewCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String IDNO = IDNumberEdTxt.getText().toString();
                String PASS = PasswordEdTxt.getText().toString();
                String FNAM = FirstNameEdTxt.getText().toString();
                String LNAM = LastNameEdTxt.getText().toString();
                String MAJR = MajorEdTxt.getText().toString();

                //Check input paramaters
                if (ParamNewUser(IDNO, PASS, FNAM, LNAM, MAJR)) {
                    //Send New User Info to DB
                    boolean isSent = SendNewUser(IDNO, PASS, FNAM, LNAM, MAJR);
                    //Confirm DATA SENT and Continue to Rest of App
                    if (isSent) {
                        Log.d("debug", "In MainFragNew: before Intent/StartActivity");
                        Intent intent = new Intent(getActivity(), SecondActivity.class);
                        intent.putExtra("idNumber", IDNO);
                        Log.d("debug", "In MainFragNew: after Intent/StartActivity");
                        startActivity(intent);
                        Log.d("debug", "In MainFragNew: after StartActivity");
                    }
                    else{
                        Toast.makeText(getActivity().getBaseContext(), "ID Number has already been registered. ERROR CODE: MFN_SNU", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                else{
                    Toast.makeText(getActivity().getBaseContext(), "Login doesn't meet length requirements. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        return root;
    }

    private boolean ParamNewUser(String idNo, String password,
                                       String firstName, String lastName, String major){
        //TODO check input parameters in DB
        if (idNo.length() == 8 &&
        firstName.length() <= 20 && firstName.length() > 0 &&
        lastName.length() <= 20 && lastName.length() > 0 &&
        major.length() <= 20 && major.length() > 0) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean SendNewUser(String idNo, String password,
                                      String firstName, String lastName, String major){
        //TODO send data to DB
        //String msgToSend = "USE StudyParty; INSERT INTO User ([ {\"idNo\":" + idNo + ", \"passWord\":\"" + password + "\", \"firstName\":\"" + firstName + "\", \"lastName\":\"" + lastName + "\", \"major\":\"" + major + "\"} ]);";

        //TODO check for data sent
        //String msgToCheck = "USE StudyParty; SELECT VALUE user FROM User user WHERE user.idNo = " + idNo + " AND passWord = \"" + password + "\";";

        ArrayList<String> tray = stringCallable(idNo, password, firstName, lastName, major);
        if (tray == null)
            return false;

        return true;
    }

    private ArrayList<String> stringCallable(String idNo, String password, String firstName, String lastName, String major){
        //setup for Socket retrieval
        ArrayList<String> endinG = new ArrayList<String>();

        ThreadPoolExecutor executor= (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        List<Future<String>> retList = new ArrayList<>();
        socketGrabCallableNew trying = new socketGrabCallableNew(idNo, password, firstName, lastName, major);
        Future<String> reting = executor.submit(trying);
        retList.add(reting);

        String ending1 = "";
        try {
            String ending = reting.get();
            Log.d("debug", "In MainFragNew: in stringCallable");
            ending1 = ending;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        executor.shutdown();

        endinG = asterixDBReturn(ending1);
        Log.d("debug", "In MainFragNew: in stringCallable after asterixDBReturn");
        if(endinG == null){
            Log.d("debug", "In MainFragNew: in stringCallable after asterixDBReturn in if - returning null");
            return endinG;
        }
        Log.d("debug", "In MainFragNew: in stringCallable after asterixDBReturn after if");
        Log.d("debug", endinG.get(0));
        return endinG;
    }

    private ArrayList<String> asterixDBReturn(String input) {
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
}

class socketGrabCallableNew implements Callable<String> {
    private String idNo;
    private String password;
    private String firstName;
    private String lastName;
    private String major;

    public socketGrabCallableNew(String idNo, String password, String firstName, String lastName, String major){
        this.idNo = idNo;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.major = major;
    }

    @Override
    public String call() throws Exception {
        return startString(idNo, password, firstName, lastName, major);
    }

    private String startString(String idNo, String password, String firstName, String lastName, String major) throws InterruptedException {
        Log.d("debug", "In MainFragNew: in startString Thread run");
        Socket s;
        DataOutputStream dos;
        DataInputStream dis;
        BufferedReader input;
        String Endresult = null;


        if (checkString(idNo)){
        try {
            //TODO 10.0.2.2 is apparently PC localhost port
            Log.d("debug", "idNo:" + idNo);
            String data = "use StudyParty; INSERT INTO User ([ " +
                    "{\"idNo\": " + idNo +
                    ", \"passWord\": \"" + password +
                    "\" , \"firstName\": \"" + firstName +
                    "\" , \"lastName\": \"" + lastName +
                    "\", \"major\": \"" + major + "\" }"
                    + " ]);";

            Log.d("debug", "sending:" + data);
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
            Log.d("debug", "In MainFragNew: in socketGrabCallableNew startString");
            Log.d("debug", "socketGrabCallableNew: " + Endresult);

        } catch (IOException e) {
            Log.d("debug", "In MainNewFrag: in startString IOException");
            e.printStackTrace();
        } catch (Exception e) {
            Log.d("debug", "In MainFragNew: in startString Exception");
        }
        }

        return Endresult;
    }

    private boolean checkString(String idNo){
        Log.d("debug", "In MainFragNew: in checkString Thread run");
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
            Log.d("debug", "In MainFragNew: in socketGrabCallableNew checkString");
            Log.d("debug", "socketGrabCallableNew: " + Endresult);

        } catch (IOException e) {
            Log.d("debug", "In MainFragNew: in checkString IOException");
            e.printStackTrace();
        } catch (Exception e) {
            Log.d("debug", "In MainFragNew: in checkString Exception");
        }

        //process response to see if idNo exists


        return checkStringReturn(Endresult);
    }

    private boolean checkStringReturn(String input){
        //Takes input from AsterixDB and parses it
        //Itemizes parts into ArrayList
        ArrayList<String> results = new ArrayList<String>();

        Log.d("debug", "In MainFragNew: in checkStringReturn");

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

        if (results.size() == 0){
            return true;
        }
        return false;
    }
}