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

public class SearchFragment extends Fragment { //implements SearchView.OnQueryTextListener{

    private SearchViewModel searchViewModel;
    private EditText outputEditText;
    private Button SearchBtn;
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

        SearchList = root.findViewById(R.id.search_fragment_searchlistlist);
        outputEditText = root.findViewById(R.id.search_fragment_searchinput);
        SearchBtn = root.findViewById(R.id.search_fragment_searchbutton);
        SearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = outputEditText.getText().toString();
                //textView.setText(str);

                //TODO restructure for differing sets from DB
                String stri = sendString(str);
                SearchList.removeAllViews(); //clears for new search query
                for (int i = 0; i < 20; i++) {
                        //Create New Button
                        final Button joinBtn = new Button(root.getContext());

                        //Set Margins and Parameters between Buttons
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(0, 10, 0, 10);

                        //Set Text and Content Settings for Button
                        String tempRayString = (stri + Integer.toString(i));
                        joinBtn.setText(stri + " " + i); // replace w/ setString
                        joinBtn.setId(i); //Button with Unique ID
                        joinBtn.setBackgroundColor(Color.DKGRAY);
                        joinBtn.setTextColor(Color.WHITE);

                        //Control what Button does on Click
                        joinBtn.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View view){
                                //TODO Join Requests
                                //TODO Possible new ID with distinct URL/Port data
                                Toast.makeText(getActivity().getBaseContext(), ("Working. " + tempRayString + "."), Toast.LENGTH_SHORT).show();
                            }
                        });

                    //Add New Button with Parameters to LinearLayout
                    SearchList.addView(joinBtn, params);
                }
            }
        });

        return root;
    }

    private static String sendString(String str){
        //TODO sends string Query to DB and receives and processes it to return to needed type for list
        return str;
    }
}