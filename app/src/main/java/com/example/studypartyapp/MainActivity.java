package com.example.studypartyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.studypartyapp.ui.groups.GroupsFragment;
import com.example.studypartyapp.ui.profile.ProfileFragment;
import com.example.studypartyapp.ui.search.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button ReturnBtn;
    private Button NewUserBtn;
    private boolean passedUserCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            this.getSupportActionBar().hide();
        } catch (NullPointerException e) {}
        setContentView(R.layout.activity_main);
        Log.d("debug", "In MainActivity: onCreate setup");

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        ReturnBtn = findViewById(R.id.mainReturnUser);
        NewUserBtn = findViewById(R.id.mainNewUser);

        NewUserBtn.setOnClickListener(this);
        ReturnBtn.setOnClickListener(this);

    }

    //loadFragment helps load NEW/RETURNING USER fragments
    public void loadFragment(Fragment frag, String tag)
    {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if(fragment == null)
        {
            ft.add(R.id.fragment_container, frag, tag);
        } else
        {
            ft.replace(R.id.fragment_container, frag, tag);
        }
        ft.addToBackStack(null);

        ft.commit();
    }

    //Handles Bottom Navigation and Switching
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch(item.getItemId()) { //referenced in bottom_nav_menu.xml
                        case R.id.bnmnavigation_groups:
                            selectedFragment = new GroupsFragment();
                            break;
                        case R.id.bnmnavigation_profile:
                            selectedFragment = new ProfileFragment();
                            break;
                        case R.id.bnmnavigation_search:
                            selectedFragment = new SearchFragment();
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();

                    return true;
                }
            };

    //Used for NEW/RETURNING USER buttons
    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.mainReturnUser:
                MainFragReturn newFragReturn = new MainFragReturn();
                loadFragment(newFragReturn, "ReturningUser");
                NewUserBtn.setVisibility(View.GONE);
                ReturnBtn.setVisibility(View.GONE);
                break;

            case R.id.mainNewUser:
                MainFragNew newFragNew = new MainFragNew();
                loadFragment(newFragNew, "NewUser");
                NewUserBtn.setVisibility(View.GONE);
                ReturnBtn.setVisibility(View.GONE);
                break;
        }
    }
}