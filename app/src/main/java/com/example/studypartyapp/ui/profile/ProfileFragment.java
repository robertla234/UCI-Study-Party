package com.example.studypartyapp.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.util.List;
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
        ArrayList<String> profileData = stringCallable(idNo);
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
        Log.d("debug", Integer.toString(start) + " " + Integer.toString(end));
        start = input.indexOf(":", end);
        end = input.indexOf(",", end + 1);
        Log.d("debug", Integer.toString(start) + " " + Integer.toString(end));
        start = input.indexOf(":", end);
        end = input.indexOf(",", end + 1);
        Log.d("debug", Integer.toString(start) + " " + Integer.toString(end));
        result = input.substring(start + 3, end - 1);
        Log.d("debug", "results are " + result);

        start = input.indexOf(":", end);
        end = input.indexOf(",", end + 1);
        Log.d("debug", Integer.toString(start) + " " + Integer.toString(end));
        result = result + " " + input.substring(start + 3, end - 1);
        Log.d("debug", "results are " + result);

        return result;
    }


    private ArrayList<String> stringCallable(String idNo){
        //setup for Socket retrieval
        ArrayList<String> endinG = new ArrayList<String>();

        ThreadPoolExecutor executor= (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        List<Future<String>> retList = new ArrayList<>();
        socketGrabCallableProfile trying = new socketGrabCallableProfile(idNo);
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

class socketGrabCallableProfile implements Callable<String> {
    private String retString;

    public socketGrabCallableProfile(String idNo){
        this.retString = idNo;
    }

    @Override
    public String call() throws Exception {
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
}