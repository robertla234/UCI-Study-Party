package com.example.studypartyapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.studypartyapp.ui.groups.GroupsFragment;
import com.example.studypartyapp.ui.profile.ProfileFragment;
import com.example.studypartyapp.ui.search.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            this.getSupportActionBar().hide();
        } catch (NullPointerException e) {}
        setContentView(R.layout.activity_second);

        Log.d("debug", "In SecondActivity: in onCreate before Intent");

        Intent intent = getIntent();
        String currentAccIDNum = intent.getStringExtra("idNumber");

        Bundle bundle = new Bundle();
        bundle.putString("idSecondAct", currentAccIDNum);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        Log.d("debug", "In SecondActivity: in onCreate before bottomNav");
        bottomNav.setSelectedItemId(R.id.bnmnavigation_groups);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        GroupsFragment groupsFrag = new GroupsFragment();
        groupsFrag.setArguments(bundle);
        getSupportFragmentManager().
                beginTransaction().replace(R.id.fragment_container,
                groupsFrag).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Intent intent = getIntent();
                    String currentAccIDNum = intent.getStringExtra("idNumber");


                    Fragment selectedFragment = null;

                    Bundle bundle = new Bundle();
                    bundle.putString("idSecondAct", currentAccIDNum);

                    switch(item.getItemId()) { //referenced in bottom_nav_menu.xml
                        case R.id.bnmnavigation_groups:
                            selectedFragment = new GroupsFragment();
                            selectedFragment.setArguments(bundle);
                            break;
                        case R.id.bnmnavigation_profile:
                            selectedFragment = new ProfileFragment();
                            selectedFragment.setArguments(bundle);
                            break;
                        case R.id.bnmnavigation_search:
                            selectedFragment = new SearchFragment();
                            selectedFragment.setArguments(bundle);
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();

                    return true;
                }
            };

}
