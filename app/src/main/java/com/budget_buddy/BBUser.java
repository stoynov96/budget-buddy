package com.budget_buddy;

import android.content.Context;
import android.icu.text.DateTimePatternGenerator;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    // Total spent in past month
    private long totalSpentPastMonth = 0;
    // used to keep of how many days the user has been entering purchases, max 30
    private long totalDaysBudgeted = 1; // sort of a hack to get meaningful averages
    // progress towards savings goal
    private long savingsGoalProgress = 0;
    // Array of categories for purchases
    private HashMap<String, String> spendingCategories;
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

    // Stats TODO Actually make achievement system - do this Kevin
    public UserStats userStats;
    //public int loginCount = 0;
    public Context currentContext;

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
            userStats = new UserStats();
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
    public HashMap<String, String> GetSpendingCategories() {
        return this.spendingCategories;
    }

    public void AddToSpendingCategories(String key, String value) {
        this.spendingCategories.put(key, value);
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

    public void SetSpendingCategories(HashMap<String, String> newCategories) {
        this.spendingCategories = newCategories;
    }

    public void setBudgetLevel(long l) { budgetLevel = l; }
    public void setBudgetScore(long s) { budgetScore = s; }
    public void setSavingsGoal(double g) { savingsGoal = (new Double(g * 100.0).longValue()); }
    public void setRent(double r) { rent = (new Double(r * 100.0).longValue()); }
    public void setOtherExpenses(double o) { otherExpenses = (new Double(o * 100.0).longValue()); }
    public void setPrimaryIncome(double p) { primaryIncome = (new Double(p * 100.0).longValue()); }


    /**
     * This function computes the suggestedSpendingAmount and then returns the result divided by 30,
     * the number of days in a month. I don't know how else to calculate this.
     * @return
     */
    public float GetSuggestedDailySpendingAmount() {
        suggestedSpendingAmount = (primaryIncome - rent - otherExpenses - savingsGoal);
        // converted between floats and longs is becoming a hacky mess :(
        return new Long(suggestedSpendingAmount).floatValue() / 100 / 30;
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

    /**
     * This function computes the average for the past month or the earliest day the user started budgeting.
     * @return
     */
    public float GetAveMonthSpent() {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        long runningTotal = 0L;
        for (long i = 0; i < 30; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(dateFormat);

            ArrayList<Expenditure> expenditures = purchases.get(dateStr);
            float dayTotal = 0.0f;
            if(expenditures != null) {
                for(Expenditure e: expenditures) {
                    dayTotal += (new Float(e.amount).floatValue());
                }
                runningTotal += new Float(dayTotal*100).longValue();
                if(i + 1 > totalDaysBudgeted) {
                    totalDaysBudgeted = i + 1;
                }
            }
        }

        totalSpentPastMonth = runningTotal;
        return new Float(runningTotal).floatValue() / 100 / totalDaysBudgeted;
    }

    /**
     * Computes the goal progress by taking the suggested spending allowance and subtracting the amount spent in a day.
     * Going over the spending allowance reduces the progress, while going under increases the progress.
     * Starts calculating from the first day the user started budgeting, but at most 30 days back.
     * @return
     */
    public float GetGoalProgress() {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        float progress = 0f;

        for(long i = 0;i<totalDaysBudgeted;i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(dateFormat);

            ArrayList<Expenditure> expenditures = purchases.get(dateStr);
            if(expenditures != null) {
                float totalForDay = 0f;
                for(Expenditure e: expenditures) {
                    totalForDay += (new Float(e.amount).floatValue());
                }
                // if the user goes above the average, increase the progress
                // if user goes below, decrease the progress
                progress += GetSuggestedDailySpendingAmount() - totalForDay;
            } else {
                progress += GetSuggestedDailySpendingAmount();
            }
        }

        savingsGoalProgress = new Float(progress * 100).longValue();
        return progress;
    }

    /**
     * This function populates the purchases hash table with lists of purchases for each day.
     * The purchases has table is indexed with date strings with format "yyyy-MM-dd"
     * @param userInterfaceCallback
     */
    public void AcquireAllPurchases(final MyCallback userInterfaceCallback) {
        user = authentication.getInstance().getCurrentUser();
        String path = userPath.get(0) + "/" + user.getUid() + "/";

        MyCallback purchaseCallback = new MyCallback() {
            @Override
            public void OnCallback(float[] weeklySpending) {

            }

            @Override
            public void OnPurchases(HashMap<String, ArrayList<Expenditure>> purchases) { }

            @Override
            public void OnCallback(HashMap<String, Object> map) {
                Iterator iterator = map.entrySet().iterator();

                purchases.clear();
                while (iterator.hasNext()) {
                    Map.Entry pair = (Map.Entry)iterator.next();
                    HashMap<String, Object> expenditureMap = (HashMap<String, Object>)pair.getValue();
                    Expenditure expenditure = new Expenditure("","","","", "");
                    expenditure.GetFromMap(expenditureMap);
                    ArrayList<Expenditure> purchaseList = purchases.get(expenditure.GetDate());
                    if (purchaseList == null) {
                        purchaseList = new ArrayList<>();
                    }
                    purchaseList.add(expenditure);
                    purchases.put(expenditure.GetDate(), purchaseList);
                }

                userInterfaceCallback.OnPurchases(purchases);

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

            @Override
            public void OnIncrement(int value) {

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
            public void OnPurchases(HashMap<String, ArrayList<Expenditure>> purchases) { }

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

            @Override
            public void OnIncrement(int value) {
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

        // Workaround for odd bug where rent from DB is of type Long?
        //temp = temp instanceof Long ? ((Long)temp).doubleValue() : temp;

        temp = map.get(RENT_KEY);
        rent = temp != null ? (long) temp : rent;
        temp = map.get(OTHER_EXPENSES_KEY);
        otherExpenses = temp != null ? (long) temp : otherExpenses;
        temp = map.get(PRIMARY_INCOME_KEY);
        primaryIncome = temp != null ? (long) temp : primaryIncome;
        temp = map.get(SPENDING_CATEGORIES);
        spendingCategories = temp != null ? (HashMap<String, String>) temp : spendingCategories;
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

        if (userInterfaceCallback != null) {
            userInterfaceCallback.OnProfileSet();
        }
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

    /***
     * Increments User Stat that corresponds to statToInc and handles callback
     * @param statToInc tag that corresponds to stat needed to be incremented
     ***/
    public void IncStat(UserStats.Counters statToInc) throws  InvalidDataLabelException {
        // Set path to stat
        String path = "";
        switch (statToInc) {
            case LOGIN_COUNT:
                path = "login count";
                userStats.loginCountCallBack(this);
                break;
            case PURCHASE_COUNT:
                path = "purchase count";
                userStats.purchaseCountCallBack(this);
                break;
            default:
                Log.e("UserStats", "Increment: invalid stat to increase!");
        }
        tableWriter.Increment(userPath, "/" + user.getUid() + "/User Stats/" + path, userStats.statCallBack);
    }

    /***
     * Increments BBUser's Score based on exp amount
     * @param exp amount of experience to increase by
     ***/
    public void IncBudgetScore(long exp){
        try {
            tableWriter.IncrementBy( (int)exp, userPath, "/" + user.getUid() + "/User Parameters/Budget Score", new MyCallback() {
                @Override
                public void OnCallback(float[] weeklySpending) {

                }

                @Override
                public void OnPurchases(HashMap<String, ArrayList<Expenditure>> purchases) {

                }

                @Override
                public void OnCallback(HashMap<String, Object> map) {

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

                @Override
                public void OnIncrement(int value) {
                    //Log.i("FUCK", "OnCallback: " + value);
                    setBudgetScore((long) value);
                }
            });
        } catch (InvalidDataLabelException e1) {
            Log.d("Parse error", e1.toString());
        }
    }

    public void IncrementDaily(UserStats.Dailies dailyToCheck){
        String path = "";
        switch (dailyToCheck) {
            case FIRST_PURCHASE:
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                path = "/Purchases/" + sdf.format(new Date());
                //Log.i("FUCK", "IncrementDaily: " + path + "/");
                userStats.purchaseCountCallBack(this);
                break;
            default:
                Log.e("UserStats", "Increment: invalid stat to increase!");
        }
        try {
            tableReader.singleRead(userPath, "/" + user.getUid() + path, userStats.statCallBack);
        } catch (InvalidDataLabelException e1) {
            Log.d("Parse error", e1.toString());
        }
    }

    public void CheckDaily(UserStats.Dailies dailyToCheck){
        String path = "";
        switch (dailyToCheck) {
            case FIRST_PURCHASE:
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                path = "/Purchases/" + sdf.format(new Date());
                //Log.i("FUCK", "IncrementDaily: " + path + "/");
                userStats.readCallBack(this);
                break;
            default:
                Log.e("UserStats", "Increment: invalid stat to increase!");
        }
        try {
            tableReader.singleRead(userPath, "/" + user.getUid() + path, userStats.statCallBack);
        } catch (InvalidDataLabelException e1) {
            Log.d("Parse error", e1.toString());
        }
    }
}
