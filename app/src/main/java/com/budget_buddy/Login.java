package com.budget_buddy;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;
    private BBUser currentUser = BBUser.GetInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))  // may need to add the server's client ID here, or get the value dynamically.
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public void onStart() {
        super.onStart();
        // TODO: Check if user is signed in (non-null) and update UI accordingly. I.E. if they are no longer signed in, display the sign in page.
        if(currentUser.GetUser() != null) {
            gotoDashboard(currentUser);
        }
        else {
            // TODO: If they are no longer signed in, go to the sign in page
        }
    }

    public void signIn(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void signOut(View view) {
        currentUser.SignOut();
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

        task.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Sign in message:", "signInWithCredential:success");
                    currentUser.Initialize();

                    gotoDashboard(currentUser);

                } else {
                    // TODO: If sign in fails, display a message to the user.
                    Log.w("Sign in message:", "signInWithCredential:failure", task.getException());
                    // updateUI(null);
                }
            }});
    }


    private void gotoDashboard(BBUser user) {
        if (user.GetUser() != null) {
            Intent dashboardIntent = new Intent(this, Dashboard.class);

            startActivity(dashboardIntent);
        }
    }
}
