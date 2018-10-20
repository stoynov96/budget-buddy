package com.budget_buddy;


import android.util.Log;

import com.budget_buddy.config.DataConfig;
import com.budget_buddy.exception.InvalidDataLabelException;
import com.budget_buddy.utils.Data.DataNode;
import com.budget_buddy.utils.Data.MyCallback;
import com.budget_buddy.utils.Data.TableReader;
import com.budget_buddy.utils.Data.TableWriter;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class BBUser implements DataNode {
    // The singleton class object used for referencing throughout the program.
    private static final BBUser ourInstance = new BBUser();
    // Used to write user data to Firebase
    private final TableWriter tableWriter;
    // Used to read user data from Firebase
    private final TableReader tableReader;
    // Path in the database to the users section
    private final List<String> userPath;
    // Used for signing in and other functions that need to authenticate a user's credentials.
    private FirebaseAuth authentication;
    // This is the user in the database, used directly to make Firebase function calls on a Firebase user.
    private FirebaseUser user;
    // The user's name as entered in the database.
    private String userName;
    // Current level of the user (NYI)
    private long budgetLevel = -1;
    // Current score of the user (NYI)
    private long budgetScore = -1;
    // Monthly Savings Goal (this is how much the user hopes to save throughout the month)
    private long savingsGoal = -1;
    // Rent (in dollars, should we worry about cents at all?)
    private long rent = -1;
    // other expenses (maybe we should store as an array but simpler as a lump sum)
    private long otherExpenses = -1;
    // Primary income
    private long primaryIncome = -1;
    // Other income
    private long otherIncome = -1;
    // Suggested daily spending amount
    // ( primaryIncome + otherIncome - rent - otherExpenses) / daysInMonthOfSavingsGoal
    private long suggestedSpendingAmount = -1;

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

    public void Initialize() throws InvalidDataLabelException {
        user = authentication.getCurrentUser();
        if(user != null) {
            userName = user.getDisplayName();
            // TODO: Add other initialization her as appropriate
        }
        try {
            LatchToDatabase();
        }
        catch (InvalidDataLabelException idle) {
            throw new InvalidDataLabelException(
                    "Could not initialize user. It could be that the username was empty?",
                    idle);
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

    public long getBudgetLevel() {
        return this.budgetLevel;
    }
    public long getBudgetScore() {
        return this.budgetScore;
    }
    public long getSavingsGoal() {
        return savingsGoal;
    }
    public long getRent() {
        return rent;
    }
    public long getOtherExpenses() {
        return otherExpenses;
    }
    public long getPrimaryIncome() {
        return primaryIncome;
    }
    public long getOtherIncome() {
        return otherIncome;
    }
    public long getSuggestedSpendingAmount() {
        return suggestedSpendingAmount;
    }

    public boolean updateSavingsGoal(int newGoal) {
        return false;
    }

    /**
     * Writes user info to the database
     * @throws InvalidDataLabelException thrown if userpath contains invalid labels
     */
    public void WriteUserInfo() throws InvalidDataLabelException {
        // Todo: We really need to check if user already exists,
        // but I am not sure this should be done here
        tableWriter.WriteData(userPath, this, userName);
    }

    /**
     * This function writes a new expenditure to the database. The current structure is to write
     * to the Users/'username'/Purchases section, and the items are stored by their purchase date.
     * For example, if you buy a Nintendo Switch on 10/10/18 and your username is Budget Buddy, it will
     * be in the database at Users/Budget Buddy/Purchases/'unique key'/.
     * @param name The name of the item purchased.
     * @param date The date the item was purchased.
     * @param amount The amount the item cost.
     * @throws InvalidDataLabelException thrown if userpath contains invalid label.
     */
    public void WriteNewExpenditure(String name, String date, String amount, String note) throws InvalidDataLabelException {
        Map<String, Object> userData = new HashMap<>();
        date = date.replace("/", "-");
        userData.put("Item Name", name);
        userData.put("Purchase Date", date);
        userData.put("Purchase Amount", amount);
        userData.put("Purchase Note", note);
        tableWriter.WriteExpenditure(userPath.get(0), userData, "/"+userName+"/Purchases/"+date);
    }

    public void GetWeeklySpending(final MyCallback callback) {
        String path = userPath.get(0) + "/" + userName + "/Purchases/";

        MyCallback callbackInner = new MyCallback() {
            @Override
            public void onCallback(String key, String value) {
                Log.d("BBDATA: ", key + ", " + value);
                callback.onCallback(0, 1);
            }

            @Override
            public void onCallback(int index, int key) {

            }
        };

        tableReader.WeeklyExpenditures(path, callbackInner);
    }

    /**
     * Latches onto the database so that every time data is changed over there
     * those changes are reflected in this object
     * @throws InvalidDataLabelException thrown if userpath contains invalid labels
     */
    public void LatchToDatabase() throws InvalidDataLabelException {
        List<String> latchPath = new ArrayList<>(userPath);
        latchPath.add(userName);
        tableReader.Latch(latchPath, this);
    }


    @Override
    public Map<String, Object> ToMap() {
        return new HashMap<String, Object>() {{
            put("BudgetLevel", budgetLevel);
            put("BudgetScore", budgetScore);
            put("SavingsGoal", savingsGoal);
            put("Rent", rent);
            put("OtherExpenses", otherExpenses);
            put("PrimaryIncome", primaryIncome);
            put("OtherIncome", otherIncome);
            // suggestedSpending should probably be calculated on the spot
        }};
    }

    @Override
    public void GetFromMap(Map<String, Object> map) {
        budgetLevel = (long) map.get("BudgetLevel");
        budgetScore = (long) map.get("BudgetScore");
        savingsGoal = (long) map.get("SavingsGoal");
        rent        = (long) map.get("Rent");
        otherExpenses = (long) map.get("OtherExpenses");
        otherIncome = (long) map.get("OtherIncome");
    }

    @Override
    public void OnDataChange() {
        // Add custom logic here to be executed when user data changes
        // as a result of a database read
        // Do not add anything if this is expected to be overridden
    }

    // Only for testing purposes
    BBUser SetUsername(String userName) {
        this.userName = userName;
        return this;
    }
    BBUser SetBudgetLevel(long budgetLevel) {
        this.budgetLevel = budgetLevel;
        return this;
    }

    private BBUser() {
        authentication = FirebaseAuth.getInstance();
        userName = "";
        tableWriter = new TableWriter();
        tableReader = new TableReader();
        userPath = new ArrayList<String>() {{
            add(DataConfig.DataLabels.USERS);
        }};
    }
}