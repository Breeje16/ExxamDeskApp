package com.breejemodi.exxamdesk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.breejemodi.exxamdesk.clientfragments.ExamFragment;
import com.breejemodi.exxamdesk.clientfragments.HomeFragment;
import com.breejemodi.exxamdesk.clientfragments.ProfileFragment;
import com.breejemodi.exxamdesk.clientfragments.SettingFragment;
import com.breejemodi.exxamdesk.registration.ForgotPasswordFragment;
import com.breejemodi.exxamdesk.registration.OTPFragment;
import com.breejemodi.exxamdesk.registration.RegisterActivity;
import com.breejemodi.exxamdesk.registration.UsernameActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FrameLayout frameLayout;
    private TabLayout tabLayout;
    private List<Fragment> fragmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();

//        checkUsername();

        fragmentList = new ArrayList<>();
        fragmentList.add(new HomeFragment());
        fragmentList.add(new ExamFragment());
        fragmentList.add(new ProfileFragment());
        fragmentList.add(new SettingFragment());

        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getIcon().setTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setTintList(ColorStateList.valueOf(Color.parseColor("#BDBDBD")));

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabLayout.getTabAt(0).getIcon().setTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        setFragment(0);
    }

    private void init(){
        frameLayout = findViewById(R.id.frameLayout);
        tabLayout = findViewById(R.id.tablayout);

    }

    public void setFragment(int position){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        fragmentTransaction.replace(frameLayout.getId(), fragmentList.get(position));
        fragmentTransaction.commit();
    }

    private void checkUsername(){

        firestore.collection("StudentUsers").document(firebaseAuth.getCurrentUser().getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){

                            if(task.getResult().exists()){
                                if(!task.getResult().contains("Username")){
                                    Intent usernameIntent = new Intent(MainActivity.this, UsernameActivity.class);
                                    startActivity(usernameIntent);
                                    finish();
                                }
                            }else{
                                Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
                                startActivity(registerIntent);
                                finish();
                            }

                        }else{
                            String error = task.getException().getMessage();
                            Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG);

                        }
                    }
                });

    }
}