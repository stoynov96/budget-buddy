package com.budget_buddy;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

class BBUser {
    // The singleton class object used for referencing throughout the program.
    private static final BBUser ourInstance = new BBUser();
    // Used for signing in and other functions that need to authenticate a user's credentials.
    private FirebaseAuth authentication;
    // This is the user in the database, used directly to make Firebase function calls on a Firebase user.
    private FirebaseUser user;
    // The user's name as entered in the database.
    private String userName;
    // Current level of the user (NYI)
    public int budgetLevel;
    // Current score of the user (NYI)
    public int budgetScore;

    static BBUser GetInstance() {
        return ourInstance;
    }
    FirebaseUser GetUser() {return user;}
    String GetUserName() {return userName;}

    public Task SignIn(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        return authentication.signInWithCredential(credential);
    }

    public void SignOut() {
        authentication.signOut();
    }

    public void Initialize() {
        user = authentication.getCurrentUser();
        if(user != null) {
            userName = user.getDisplayName();
            // TODO: Add other initialization her as appropriate
        }
    }

    private BBUser() {
        authentication = FirebaseAuth.getInstance();
        userName = "";
    }
}