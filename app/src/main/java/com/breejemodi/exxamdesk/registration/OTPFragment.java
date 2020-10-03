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

import com.breejemodi.exxamdesk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class OTPFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public OTPFragment() {
        // Required empty public constructor
    }


    public OTPFragment(String edtPhoneNumber, String edtEmailID, String edtPasswordSignUp){
        this.edtPhoneNumber = edtPhoneNumber;
        this.edtEmailID = edtEmailID;
        this.edtPasswordSignUp = edtPasswordSignUp;
    }

    private TextView txtSentToPhoneNumber;
    private EditText edtOTP;
    private ProgressBar progressBar;
    private Button btnVerify, btnResendOTP;
    private String edtPhoneNumber,edtEmailID,edtPasswordSignUp;
    private Timer timer;
    private int count = 60;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack;
    private FirebaseAuth firebaseAuth;

    public static OTPFragment newInstance(String param1, String param2) {
        OTPFragment fragment = new OTPFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

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
        return inflater.inflate(R.layout.fragment_o_t_p, container, false);
    }
//-----------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);
        firebaseAuth = FirebaseAuth.getInstance();

        txtSentToPhoneNumber.setText("Verification Code has been Sent on number: " + edtPhoneNumber);

        sendOTP();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(count == 0){
                            btnResendOTP.setText("RESEND");
                            btnResendOTP.setEnabled(true);
                            btnResendOTP.setAlpha(1f);
                        }else{
                            btnResendOTP.setText("Resend in " + count);
                            count--;
                        }
                    }
                });
            }
        },0,1000);

        btnResendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendOTP();
                btnResendOTP.setEnabled(false);
                btnResendOTP.setAlpha(0.5f);
                count=60;
            }
        });

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edtOTP.getText() == null || edtOTP.getText().toString().isEmpty()){
                    return;
                }
                edtOTP.setError(null);
                String code = edtOTP.getText().toString().trim();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                signInWithPhoneAuthCredential(credential);
//                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }
//----------------------------------------------------------------------------------------------------------------------------
    private void init(View view) {
        edtOTP = view.findViewById(R.id.edtOTP);
        progressBar = view.findViewById(R.id.progressBar);
        btnVerify = view.findViewById(R.id.btnVerify);
        btnResendOTP = view.findViewById(R.id.btnResendOTP);
        txtSentToPhoneNumber = view.findViewById(R.id.txtSentToPhoneNumber);
    }

    private void sendOTP(){

        mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                //Log.d(TAG, "onVerificationCompleted:" + credential);

                //signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                //Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    edtOTP.setError(e.getMessage());
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    edtOTP.setError(e.getMessage());
                    // The SMS quota for the project has been exceeded
                    // ...
                }
//                progressBar.setVisibility(View.INVISIBLE);
                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                //Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // ...
            }
        };

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + edtPhoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallBack);

    }

    private void resendOTP(){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + edtPhoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallBack,mResendToken);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            AuthCredential credential = EmailAuthProvider.getCredential(edtEmailID,edtPasswordSignUp);
                            user.linkWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){

//                                        Intent mIntent = new Intent(getContext(), UsernameActivity.class);
//                                        startActivity(mIntent);
//                                        getActivity().finish();

                                        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

                                        Map<String,Object> map = new HashMap<>();
                                        map.put("Email", edtEmailID);
                                        map.put("PhoneNo",edtPhoneNumber);

                                        firebaseFirestore.collection("StudentUsers").document(firebaseAuth.getCurrentUser().getUid()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){

                                                    Intent mIntent = new Intent(getContext(), UsernameActivity.class);
                                                    startActivity(mIntent);
                                                    getActivity().finish();

                                                }else{
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(getContext(),error,Toast.LENGTH_LONG);
//                                                    progressBar.setVisibility(View.INVISIBLE);
                                                }
                                            }
                                        });

                                    }else{
                                        String error = task.getException().getMessage();
                                        Toast.makeText(getContext(),error,Toast.LENGTH_LONG);
//                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                edtOTP.setError("Invalid OTP!");
                                // The verification code entered was invalid
                            }
//                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }
}