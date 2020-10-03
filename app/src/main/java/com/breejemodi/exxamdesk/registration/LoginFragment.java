package com.breejemodi.exxamdesk.registration;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.breejemodi.exxamdesk.MainActivity;
import com.breejemodi.exxamdesk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.regex.Pattern;

public class LoginFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private EditText edtPhoneorEmail, edtPasswordLogin;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView txtSignup,txtForgotPassword;

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

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
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(edtPhoneorEmail.getText().toString().isEmpty()){
                    progressBar.setVisibility(View.VISIBLE);
                    edtPhoneorEmail.setError("Required!");
                    return;
                }
                if(edtPasswordLogin.getText().toString().isEmpty()){
                    progressBar.setVisibility(View.VISIBLE);
                    edtPasswordLogin.setError("Required!");
                    return;
                }

                if(VALID_EMAIL_ADDRESS_REGEX.matcher(edtPhoneorEmail.getText().toString()).find()){

                    login(edtPhoneorEmail.getText().toString());

                }else if(edtPhoneorEmail.getText().toString().matches("\\d{10}")){

                    FirebaseFirestore.getInstance().collection("StudentUsers").whereEqualTo("PhoneNo",edtPhoneorEmail.getText().toString()).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful()){
                                        List<DocumentSnapshot> document = task.getResult().getDocuments();
                                        if(document.isEmpty()){
                                            edtPhoneorEmail.setError("Phone Number Not Found!");
                                            progressBar.setVisibility(View.INVISIBLE);
                                            return;
                                        }else{

                                            String email = document.get(0).get("Email").toString();
                                            login(email);

                                        }
                                    }else{

                                        String error = task.getException().getMessage();
                                        Toast.makeText(getContext(),error,Toast.LENGTH_LONG);
                                        progressBar.setVisibility(View.INVISIBLE);

                                    }
                                }
                            });

                }else{
                    edtPhoneorEmail.setError("Please Enter Valid Email or Phone Number!");
                }

            }
        });

        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((RegisterActivity)getActivity()).setFragment(new ForgotPasswordFragment());
            }
        });

        txtSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((RegisterActivity)getActivity()).setFragment(new CreateNewAccount());
            }
        });
    }

    private void init(View view) {
        edtPhoneorEmail = view.findViewById(R.id.edtEmailorPhone);
        edtPasswordLogin = view.findViewById(R.id.edtPasswordLogin);
        btnLogin = view.findViewById(R.id.btnLogin);
        progressBar = view.findViewById(R.id.progressBar);
        txtSignup = view.findViewById(R.id.txtSignup);
        txtForgotPassword = view.findViewById(R.id.txtForgotPassword);
    }

    private void login(String email){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(email,edtPasswordLogin.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    Intent mIntent = new Intent(getContext(), UsernameActivity.class);
                    startActivity(mIntent);
                    getActivity().finish();

                }else{
                    String error = task.getException().getMessage();
                    Toast.makeText(getContext(),error,Toast.LENGTH_LONG);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

}