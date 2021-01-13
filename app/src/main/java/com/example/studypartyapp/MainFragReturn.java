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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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

public class MainFragReturn extends Fragment {

    private Button ReturnSignInBtn;
    private EditText IDNumberEdTxt;
    private EditText PasswordEdTxt;

    private Button ReturnBackBtn;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main_return, container, false);
        Log.d("debug", "In MainFragReturn: onCreate setup");
        /*
        ReturnBackBtn = root.findViewById(R.id.return_user_backBtn);
        ReturnBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        */

        IDNumberEdTxt = root.findViewById(R.id.return_user_IDNum);
        PasswordEdTxt = root.findViewById(R.id.return_user_Password);
        ReturnSignInBtn = root.findViewById(R.id.return_user_signin);
        ReturnSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String IDNO = IDNumberEdTxt.getText().toString();
                String PASS = PasswordEdTxt.getText().toString();

                //Check input paramaters
                if (ParamReturnUser(IDNO, PASS)) {
                    //Send New User Info to DB
                    boolean isSent = SendReturnUser(IDNO, PASS);
                    //Confirm DATA SENT and Continue to Rest of App
                    if (isSent) {
                        Log.d("debug", "In MainFragReturn: before Intent/StartActivity");
                        Intent intent = new Intent(getActivity(), SecondActivity.class);
                        intent.putExtra("idNumber", IDNO);
                        Log.d("debug", "In MainFragReturn: after Intent/StartActivity");
                        startActivity(intent);
                        Log.d("debug", "In MainFragReturn: after startActivity");
                    }
                    else{
                        Toast.makeText(getActivity().getBaseContext(), "Something went wrong. Please try again. ERROR CODE: MFR_SRU", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                else{
                    Toast.makeText(getActivity().getBaseContext(), "Incorrect login. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        return root;
    }

    private boolean ParamReturnUser(String idNo, String password) {
        //checks input parameters to those in DB
        if (idNo == "8") {
            Toast.makeText(getActivity().getBaseContext(), "ADMIN SIGNIN ENTERED.", Toast.LENGTH_LONG).show();
            return true;
        }
        else
            return SendReturnUser(idNo, password);
    }

    private boolean SendReturnUser(String idNo, String password){
        //checks for data sent
        ArrayList<String> userData = stringCallable(idNo, password);
        Log.d("debug", "In MainFragReturn: in SendReturnUser userData.size() = " + userData.size());
        //return is null (size 0) if no matches are found
        if (userData.size() == 0){
            return false;
        }
        return true;
    }

    private ArrayList<String> stringCallable(String idNo, String password){
        //setup for Socket retrieval
        ArrayList<String> endinG = new ArrayList<String>();

        ThreadPoolExecutor executor= (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        List<Future<String>> retList = new ArrayList<>();
        socketGrabCallableReturn trying = new socketGrabCallableReturn(idNo, password);
        Future<String> reting = executor.submit(trying);
        retList.add(reting);

        String ending1 = "";
        try {
            String ending = reting.get();
            Log.d("debug", "In MainFragReturn: in stringCallable");
            ending1 = ending;
        } catch (InterruptedException | ExecutionException e) {
            Log.d("debug", "In MainFragReturn: in stringCallable catchExceptions");
            e.printStackTrace();
        }
        executor.shutdown();

        if (ending1 == null) {
            Log.d("debug", "In MainFragReturn: in stringCallable -> empty results");
            //endinG = null;
        }
        else {
            Log.d("debug", "In MainFragReturn: in stringCallable -> ending1 has value");
            endinG = asterixDBReturn(ending1);
        }

        return endinG;
    }

    private ArrayList<String> asterixDBReturn(String input){
        //Takes input from AsterixDB and parses it
        //Itemizes parts into ArrayList
        ArrayList<String> results = new ArrayList<String>();

        Log.d("debug", "In MainFragReturn: in asterixDBReturn");

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

        return results;
    }
}

class socketGrabCallableReturn implements Callable<String> {
    private String idNo;
    private String passWord;

    public socketGrabCallableReturn(String idNo, String passWord){
        this.passWord = passWord;
        this.idNo = idNo;
    }

    @Override
    public String call() throws Exception {
        return startString(idNo, passWord);
    }

    private String startString(String idNo, String passWord) throws InterruptedException {
        Log.d("debug", "In MainFragReturn: in startString Thread run");
        Socket s;
        DataOutputStream dos;
        DataInputStream dis;
        BufferedReader input;
        String Endresult = null;

        try {
            //TODO 10.0.2.2 is apparently PC localhost port
            Log.d("debug", "idNo:" + idNo);
            String data = "use StudyParty; SELECT VALUE user FROM User user WHERE user.idNo = " + idNo + " AND user.passWord = \"" + passWord + "\";";

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
            Log.d("debug", "In MainFragReturn: in socketGrabCallableReturn startString");
            Log.d("debug", "socketGrabCallableReturn: " + Endresult);

        } catch (IOException e) {
            Log.d("debug", "In MainFragReturn: in startString IOException");
            e.printStackTrace();
        } catch (Exception e) {
            Log.d("debug", "In MainFragReturn: in startString Exception");
        }
        return Endresult;
    }
}