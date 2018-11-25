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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
    private long budgetLevel = 1;
    // Current score of the user (NYI)
    private long budgetScore = 0;
    // Monthly Savings Goal (this is how much the user hopes to save throughout the month)
    private long savingsGoal = 0;
    // Rent (in dollars, should we worry about cents at all?)
    private long rent = 0;
    // other expenses (maybe we should store as an array but simpler as a lump sum)
    private long otherExpenses = 0;
    // Primary income
    private long primaryIncome = 0;
    // Array of categories for purchases
    private ArrayList<String> spendingCategories;
    // HashMap of purchases
    private HashMap<String, ArrayList<Expenditure>> purchases = new HashMap<>();
    // Other income
    // Suggested daily spending amount
    // ( primaryIncome + otherIncome - rent - otherExpenses) / daysInMonthOfSavingsGoal
    private long suggestedSpendingAmount = -1;
    // holds callbacks relevant to the UI, triggered on data loads
    private MyCallback userInterfaceCallback;
    private List<MyCallback> UICallbacks = new ArrayList<>();
    static private String BUDGET_LEVEL_KEY = "Budget Level";
    static private String BUDGET_SCORE_KEY = "Budget Score";
    static private String SAVINGS_GOAL_KEY = "Savings Goal";
    static private String RENT_KEY = "Rent";
    static private String OTHER_EXPENSES_KEY = "Other Expenses";
    static private String PRIMARY_INCOME_KEY = "Primary Income";
    static private String USERNAME_KEY = "User Name";
    static private String SPENDING_CATEGORIES = "Spending Categories";

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

    public void Initialize(final MyCallback newUserCallback) throws InvalidDataLabelException {
        user = authentication.getCurrentUser();
        if(user != null) {
            userName = user.getDisplayName();
            tableReader.CheckForExistingUser(userPath.get(0), user.getUid(), userName, newUserCallback);
            // TODO: Add other initialization here as appropriate
        } else {  // user does not exist, create new user

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
    public double getSavingsGoal() {
        return (new Long(savingsGoal).doubleValue() / 100.0);
    }
    public double getRent() { return (new Long(rent).doubleValue() / 100.0); }
    public double getOtherExpenses() {
        return (new Long(otherExpenses).doubleValue() / 100.0);
    }
    public double getPrimaryIncome() {
        return (new Long(primaryIncome).doubleValue()) / 100.0;
    }
    public ArrayList<String> GetSpendingCategories() {
        return this.spendingCategories;
    }

    public void AddToSpendingCategories(String newItem) {
        this.spendingCategories.add(newItem);
        try {
            UpdateUserParameters();
        }
        catch (InvalidDataLabelException e) {
            e.printStackTrace();
        }
    }

    public void UpdateUserParameters() throws InvalidDataLabelException {
        tableWriter.SetData(userPath, "/" + user.getUid() + "/User Parameters/", this);
    }

    public void SetSpendingCategories(ArrayList<String> newCategories) {
        this.spendingCategories = newCategories;
    }

    public void setBudgetLevel(long l) { budgetLevel = l; }
    public void setBudgetScore(long s) { budgetScore = s; }
    public void setSavingsGoal(double g) { savingsGoal = (new Double(g * 100.0).longValue()); }
    public void setRent(double r) { rent = (new Double(r * 100.0).longValue()); }
    public void setOtherExpenses(double o) { otherExpenses = (new Double(o * 100.0).longValue()); }
    public void setPrimaryIncome(double p) { primaryIncome = (new Double(p * 100.0).longValue()); }


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
        tableWriter.SetData(userPath, "/" + user.getUid() + "/User Parameters/", this);
    }

    /**
     * This function writes a new expenditure to the database. The current structure is to write
     * to the Users/'username'/Purchases section, and the items are stored by their purchase date.
     * For example, if you buy a Nintendo Switch on 10/10/18 and your username is Budget Buddy, it will
     * be in the database at Users/Budget Buddy/Purchases/'unique key'/.
     * @param name The name of the item purchased.
     * @param date The date the item was purchased.
     * @param amount The amount the item cost.
     * @param note Any optional note the user enters for the purchase.
     * @throws InvalidDataLabelException thrown if userpath contains invalid label.
     */
    public void WriteNewExpenditure(String name, String date, String amount, String note, String type) throws InvalidDataLabelException {
        DateFormat inputFormat = new SimpleDateFormat("MMM dd, yyyy");
        DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date oldDate = inputFormat.parse(date);
            String formattedDate = outputFormat.format(oldDate);
            Expenditure expenditure = new Expenditure(name, formattedDate, amount, note, type);
            tableWriter.WriteData(userPath, expenditure, "/"+user.getUid()+"/Purchases/"+formattedDate);}
        catch (ParseException e1) {
            Log.d("Parse error", e1.toString());
        }
    }

    public void AcquireAllPurchases() {
        user = authentication.getInstance().getCurrentUser();
        String path = userPath.get(0) + "/" + user.getUid() + "/";

        MyCallback purchaseCallback = new MyCallback() {
            @Override
            public void OnCallback(float[] weeklySpending) {

            }

            @Override
            public void OnCallback(HashMap<String, Object> map) {
                Iterator iterator = map.entrySet().iterator();
                Expenditure expenditure = new Expenditure("","","","", "");

                while (iterator.hasNext()) {
                    Map.Entry pair = (Map.Entry)iterator.next();
                    HashMap<String, Object> expenditureMap = (HashMap<String, Object>)pair.getValue();
                    expenditure.GetFromMap(expenditureMap);
                    ArrayList<Expenditure> purchaseList = purchases.get(expenditure.GetDate());
                    if (purchaseList == null) {
                        purchaseList = new ArrayList<>();
                    }
                    purchaseList.add(expenditure);
                    purchases.put(expenditure.GetDate(), purchaseList);
                }

            }

            @Override
            public void OnProfileSet() {

            }

            @Override
            public void CreateNewUser() {

            }

            @Override
            public void UserExists() {

            }
        };

        tableReader.WeeklyExpenditures(path, purchaseCallback);
    }

    /**
     * This function parses the returned Purchases data from Firebase. It gets only items that fall
     * within the last 7 days and adds the expenditure amount for each day to an array. The array
     * is sent back to Dashboard where the data can be used to populate the bar chart.
     * @param callback The callback used to return the array of expenditures to the calling procedure.
     */
    public void GetWeeklySpending(final MyCallback callback) {
        user = authentication.getInstance().getCurrentUser();
        String path = userPath.get(0) + "/" + user.getUid() + "/";

        MyCallback callbackInner = new MyCallback() {
            // Stores a list of valid dates based on the last 7 days.
            String [] validDates = CreateValidDates();

            // This function creates an array of dates for the previous week, i.e. 1-1-01 to 1-7-01
            private String [] CreateValidDates() {
                Calendar calendar = GregorianCalendar.getInstance();
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String date;
                Date weekOld;
                String [] dates = new String[7];

                for(int i = 0; i < 7; i++){
                    calendar.setTime(new Date());
                    calendar.add(Calendar.DAY_OF_YEAR, -i);
                    weekOld = calendar.getTime();
                    date = formatter.format(weekOld);
                    dates[i] = date;
                }
                return dates;
            }

            // This function finds which day a purchase occurred on, relative to the last 7 days.
            // It returns the index to use when adding a purchase amount to the expenditures array
            // if the purchase happened within the last 7 days. Otherwise, it returns -1 to indicate
            // that we need to ignore this purchase.
            private int FindRelativeDay(String [] validDates, String date) {
                int location = -1;

                for(int i = 0; i < validDates.length; i++) {
                    if (validDates[i].equals(date)) {
                        location = i;
                    }
                }

                return location;
            }

            @Override
            public void OnCallback(HashMap<String, Object> map) {
                Iterator iterator = map.entrySet().iterator();
                float [] expenditures = {0, 0, 0, 0, 0, 0, 0};
                Expenditure expenditure = new Expenditure("","","","", "");

                while (iterator.hasNext()) {
                    Map.Entry pair = (Map.Entry)iterator.next();
                    HashMap<String, Object> expenditureMap = (HashMap<String, Object>)pair.getValue();
                    expenditure.GetFromMap(expenditureMap);
                    int index = FindRelativeDay(validDates, expenditure.GetDate());
                    // Data is part of the last 7 days, add it to the array
                    if(index != -1) {
                        expenditures[index] = expenditures[index] + Float.valueOf(expenditure.GetAmount());
                    }
                }

                callback.OnCallback(expenditures);

            }

            @Override
            public void OnCallback(float [] expenditures) {

            }

            @Override
            public void OnProfileSet() {}

            @Override
            public void CreateNewUser() {

            }

            @Override
            public void UserExists() {

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
        latchPath.add(user.getUid());
        tableReader.Latch(latchPath, this);
    }

    @Override
    public Map<String, Object> ToMap() {
        return new HashMap<String, Object>() {{
            put(BUDGET_LEVEL_KEY, budgetLevel);
            put(BUDGET_SCORE_KEY, budgetScore);
            put(SAVINGS_GOAL_KEY, savingsGoal);
            put(RENT_KEY, rent);
            put(OTHER_EXPENSES_KEY, otherExpenses);
            put(PRIMARY_INCOME_KEY, primaryIncome);
            put(USERNAME_KEY, GetUser().getDisplayName());
            put(SPENDING_CATEGORIES, spendingCategories);
            // suggestedSpending should probably be calculated on the spot
        }};
    }

    @Override
    public void GetFromMap(Map<String, Object> map) {
        if(map == null) {
            return;
        }
        Object temp = map.get(BUDGET_LEVEL_KEY);
        budgetLevel = temp != null ? (long) temp : budgetLevel;
        temp = map.get(BUDGET_SCORE_KEY);
        budgetScore = temp != null ? (long) temp : budgetScore;
        temp = map.get(SAVINGS_GOAL_KEY);
        savingsGoal = temp != null ? (long) temp : savingsGoal;
        temp = map.get(RENT_KEY);
        rent = temp != null ? (long) temp : rent;
        temp = map.get(OTHER_EXPENSES_KEY);
        otherExpenses = temp != null ? (long) temp : otherExpenses;
        temp = map.get(PRIMARY_INCOME_KEY);
        primaryIncome = temp != null ? (long) temp : primaryIncome;
        temp = map.get(SPENDING_CATEGORIES);
        spendingCategories = temp != null ? (ArrayList<String>) temp : spendingCategories;
        //userInterfaceCallback.OnProfileSet();
        for(MyCallback callback: UICallbacks) {
            callback.OnProfileSet();
        }
    }

    public void addUICallback(MyCallback callback) {
        UICallbacks.add(callback);
        user = authentication.getInstance().getCurrentUser();
        String path = userPath.get(0) + "/" + user.getUid() + "/";

        tableReader.addListener(path, callback);
    }

    public void setUserInterfaceCallback(MyCallback callback) {
        userInterfaceCallback = callback;
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