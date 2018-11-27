package com.budget_buddy;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.budget_buddy.exception.InvalidDataLabelException;

import com.budget_buddy.utils.Data.MyCallback;
import com.budget_buddy.utils.Data.TableReader;
import com.budget_buddy.utils.Data.TableWriter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import java.util.HashMap;

public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;
    private BBUser currentUser;
    private ProgressBar wheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        wheel = findViewById(R.id.loginProgressWheel);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))  // may need to add the server's client ID here, or get the value dynamically.
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser = BBUser.GetInstance();

        fromDashboard();
        checkLoggedIn();
        onLaunchSignIn();

        // TODO: Check if user is signed in (non-null) and update UI accordingly. I.E. if they are no longer signed in, display the sign in page.
        if(currentUser.IsLoggedIn()) {
            //gotoDashboard(currentUser);

        }
        else {
            // TODO: If they are no longer signed in, go to the sign in page

        }
    }


    public void signIn(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // TODO: Google Sign In failed, update UI appropriately
                Log.w("Sign in message:", "Google sign in failed: " + e.getMessage(), e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d("Sign in message:", "firebaseAuthWithGoogle: " + account.getId());
        Task task = currentUser.SignIn(account);
        startProgressWheel();
        task.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Sign in message:", "signInWithCredential:success");
                    try {
                        MyCallback newUserCallback = new MyCallback() {
                            @Override
                            public void OnCallback(float[] weeklySpending) {

                            }

                            @Override
                            public void OnCallback(HashMap<String, Object> map) {

                            }

                            @Override
                            public void OnProfileSet() {

                            }

                            @Override
                            public void CreateNewUser() {
                                gotoNewUser(currentUser);
                            }

                            @Override
                            public void UserExists() {
                                gotoDashboard(currentUser);
                            }
                        };
                        currentUser.Initialize(newUserCallback);
                    }
                    catch (InvalidDataLabelException idle) {
                        // TODO: IMPORTANT: Handle this exception
                    }
                } else {
                    // TODO: If sign in fails, display a message to the user.
                    Log.w("Sign in message:", "signInWithCredential:failure", task.getException());
                    // updateUI(null);
                    closeProgressWheel();
                }
            }
        });
    }

    private void gotoNewUser(BBUser user) {
        if (user.GetUser() != null) {
            Intent newUserIntent = new Intent(this, UserProfileActivity.class);
            closeProgressWheel();
            startActivity(newUserIntent);
        }
    }

    private void gotoDashboard(BBUser user) {
        if (user.GetUser() != null) {

            // TODO do this Kevin
            //currentUser.loginCount += 1;
            try {
                currentUser.IncLogin();
            } catch (InvalidDataLabelException e) {
                Log.i("Error", "" + e);
            }
//            try {
//                currentUser.WriteStats("99");
//            } catch (InvalidDataLabelException e) {
//                Log.i("Error", "" + e);
//            }


            Intent dashboardIntent = new Intent(this, Dashboard.class);
            closeProgressWheel();
            startActivity(dashboardIntent);
        }
    }

    private void onLaunchSignIn() {
        Task<GoogleSignInAccount> task = mGoogleSignInClient.silentSignIn();
        startProgressWheel();
        if(task.isSuccessful()) {
            GoogleSignInAccount account = task.getResult();
            firebaseAuthWithGoogle(account);
        } else {
            task.addOnCompleteListener(this, new OnCompleteListener<GoogleSignInAccount>() {
                @Override
                public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account);
                    } catch(ApiException apiException) {
                        // TODO: handle exception
                        closeProgressWheel();
                    }
                }
            });
        }
    }

    private void startProgressWheel() {
        wheel.setVisibility(View.VISIBLE);
    }

    private void closeProgressWheel() {
        wheel.setVisibility(View.GONE);
    }

    private void checkLoggedIn(){
        if (!currentUser.IsLoggedIn()){
            findViewById(R.id.button).setVisibility(View.VISIBLE);
        }
    }

    private void fromDashboard(){
        // Check if we are coming from dashboard logout
        Bundle extras = getIntent().getExtras();
        if(extras != null && extras.getBoolean("dashboard", false)){
            currentUser.SignOut();
            mGoogleSignInClient.signOut();
        }
    }

    @Override
    public void onBackPressed() {
        // do nothing - prevent going back to dashboard if logged out
        return;
    }

    public void incStats(){

    }

}