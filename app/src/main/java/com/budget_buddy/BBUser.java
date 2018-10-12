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
    private int budgetLevel = -1;
    // Current score of the user (NYI)
    private int budgetScore = -1;
    // Monthly Savings Goal (this is how much the user hopes to save throughout the month)
    private int savingsGoal = -1;
    // Rent (in dollars, should we worry about cents at all?)
    private int rent = -1;
    // other expenses (maybe we should store as an array but simpler as a lump sum)
    private int otherExpenses = -1;
    // Primary income
    private int primaryIncome = -1;
    // Other income
    private int otherIncome = -1;
    // Suggested daily spending amount
    // ( primaryIncome + otherIncome - rent - otherExpenses) / daysInMonthOfSavingsGoal
    private int suggestedSpendingAmount = -1;

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

    public boolean IsLoggedIn() {
        user = authentication.getInstance().getCurrentUser();
        if(user != null) {
            return true;
        }
        else {
            return false;
        }
    }

    public int getSavingsGoal() {
        if (savingsGoal == -1) {
            // fetch from server
        }

        return savingsGoal;
    }

    public boolean updateSavingsGoal(int newGoal) {

        return false;
    }

    private BBUser() {
        authentication = FirebaseAuth.getInstance();
        userName = "";
    }
}