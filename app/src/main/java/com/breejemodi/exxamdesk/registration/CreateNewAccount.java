package com.breejemodi.exxamdesk.registration;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.breejemodi.exxamdesk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.concurrent.Executor;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;

public class CreateNewAccount extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public CreateNewAccount() {
        // Required empty public constructor
    }


    public static CreateNewAccount newInstance(String param1, String param2) {
        CreateNewAccount fragment = new CreateNewAccount();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private EditText edtPhoneNumber, edtEmailID, edtPasswordSignUp, edtConfirmPassword;
    private Button btnSignUp;
    private ProgressBar progressBar;
    private TextView txtLogin;
    private FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_new_account, container, false);
    }

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        firebaseAuth = FirebaseAuth.getInstance();

        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((RegisterActivity)getActivity()).setFragment(new LoginFragment());
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                edtPhoneNumber.setError(null);
                edtEmailID.setError(null);
                edtPasswordSignUp.setError(null);
                edtConfirmPassword.setError(null);

                if(edtPhoneNumber.getText().toString().isEmpty()){
                    edtPhoneNumber.setError("Required!");
                    return;
                }
                if(edtEmailID.getText().toString().isEmpty()){
                    edtEmailID.setError("Required!");
                    return;
                }
                if(edtPasswordSignUp.getText().toString().isEmpty()){
                    edtPasswordSignUp.setError("Required!");
                    return;
                }
                if(edtConfirmPassword.getText().toString().isEmpty()){
                    edtConfirmPassword.setError("Required!");
                    return;
                }
                if(edtPhoneNumber.getText().toString().length() != 10){
                    edtPhoneNumber.setError("Please Enter Valid Phone No.");
                    return;
                }
                if(!VALID_EMAIL_ADDRESS_REGEX.matcher(edtEmailID.getText().toString()).find()){
                    edtEmailID.setError("Please Enter Valid Email");
                    return;
                }
                if(!edtPasswordSignUp.getText().toString().equals(edtConfirmPassword.getText().toString())){
                    edtConfirmPassword.setError("Password Mismatched!");
                    return;
                }
                createAccount(view);
            }
        });
    }

    private void init(View view) {

        edtPhoneNumber = view.findViewById(R.id.edtPhoneNumber);
        edtEmailID = view.findViewById(R.id.edtEmailID);
        edtPasswordSignUp = view.findViewById(R.id.edtPasswordSignUp);
        edtConfirmPassword = view.findViewById(R.id.edtConfirmPassword);
        btnSignUp = view.findViewById(R.id.btnSignup);
        progressBar = view.findViewById(R.id.progressBar);
        txtLogin = view.findViewById(R.id.txtLogin);
    }

    private void createAccount(View view){

        progressBar.setVisibility(View.VISIBLE);

        firebaseAuth.fetchSignInMethodsForEmail(edtEmailID.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if(task.isSuccessful()){

                    if(task.getResult().getSignInMethods().isEmpty()){
                        registeringUser();
                        ((RegisterActivity)getActivity()).setFragment(new OTPFragment(edtPhoneNumber.getText().toString(),edtEmailID.getText().toString(),edtPasswordSignUp.getText().toString()));
                    }
                    else{
                        edtEmailID.setError("Email is Already Registered!");
                    }
                }
                else{
                    Toast.makeText(getContext(), task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    void registeringUser(){

//        firebaseAuth.createUserWithEmailAndPassword(edtEmailID.getText().toString(), edtPasswordSignUp.getText().toString())
//                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
////                            Log.d(TAG, "createUserWithEmail:success");
//                            FirebaseUser user = firebaseAuth.getCurrentUser();
//                            Toast.makeText(getContext(),"You will Receive An OTP!",Toast.LENGTH_SHORT).show();
////                            updateUI(user);
//                        } else {
//                            // If sign in fails, display a message to the user.
////                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                            Toast.makeText(getContext(), "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
////                            updateUI(null);
//                        }
//
//                        // ...
//                    }
//                });

    }

}
