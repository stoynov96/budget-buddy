package com.budget_buddy.utils.Data;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.budget_buddy.UserProfileActivity;
import com.budget_buddy.config.DataConfig;
import com.budget_buddy.exception.InvalidDataLabelException;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableReader {

    private DatabaseReference mDatabase;

    /**
     * Initializes a {@link TableReader} with a default instance
     * of a {@link DatabaseReference}
     */
    public TableReader() {
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Latches onto a particular child given as a list of path.
     * As data in the database is changed, so will the values in that object
     * @param path List of labels to reference to the database child
     * @param data {@link DataNode} to latch to the database
     * @throws InvalidDataLabelException Thrown if an invalid label is used
     */
    public void Latch(List<String> path, final DataNode data)
            throws InvalidDataLabelException {
        // Todo: we should validate if there actually is data at this label.
        // Todo: (cont.) the problem with doing it the obvious way is outlined here:
        // https://stackoverflow.com/questions/37397205/google-firebase-check-if-child-exists
        final String label = joinLabels(path);
        mDatabase.child(label).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map)dataSnapshot.getValue();
                if (map != null) {
                    map = (Map<String, Object>) map.get("User Parameters");
                    data.GetFromMap(map);
                    data.OnDataChange();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // todo
            }
        });
    }

    /**
     * Latches onto a particular child given as a path under the root.
     * As data in the database is changed, so will the values in that object
     * @param path Path of labels to reference the database child
     * @param data {@link DataNode} to latch to the database
     * @throws InvalidDataLabelException Thrown if an invalid path is used
     */
    public void Latch(final String path, final DataNode data)
            throws InvalidDataLabelException {
        List<String> labels = new ArrayList<String>() {{ add(path); }};
        Latch(labels, data);
    }

    // This won't check the last label - that is supposed to be the final object label
    private String joinLabels(List<String> labels) throws InvalidDataLabelException {
        StringBuilder labelsSb = new StringBuilder();
        String deliminator = "";
        String lastLabel = DataConfig.DataLabels.USERS; // some dummy valid label

        for(String label : labels) {
            DataConfig.DataLabels.VerifyLabel(lastLabel);

            labelsSb.append(deliminator);
            deliminator = "/";

            labelsSb.append(label);
            lastLabel = label;
        }
        return labelsSb.toString();
    }

    public void addListener(String path, final MyCallback callback) {
        Query mQueryReference = mDatabase.child(path);

        mQueryReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                callback.OnProfileSet();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                callback.OnProfileSet();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Currently pulls everything in the "Purchases" category of a Firebase user. We need either 1)
     * better callbacks on Dashboard/a way to update the graph or 2) some restructuring of the database.
     * Due to how the listeners work, if we try to get something from purchases on a specific date,
     * it will send a callback for each item. You cannot combine search criteria, i.e. you cannot do
     * equalTo("Name").startAt("date").endAt("date2");. So right now this is how it has to be done. I'm
     * not sure how we could structure the database differently to easily facilitate a more concise read,
     * and I think having update functionality for the graph makes more sense anyway.
     * @param path The path to the user in Firebase.
     * @param callback A callback object to return the retrieved data.
     */
    public void WeeklyExpenditures(String path, final MyCallback callback) {
        Query myQueryReference = mDatabase.child(path).orderByKey().equalTo("Purchases");

        myQueryReference.addChildEventListener(new ChildEventListener() {
            HashMap<String, Object>  map = new HashMap<>();

            private void handleData(DataSnapshot dataSnapshot) {
                // this iterates through purchases until it gets each date - purchases HashMap,
                // which is then added to map to be sent back to BBUser.
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for(DataSnapshot snapshot2 : snapshot.getChildren()) {
                        map.put(snapshot2.getKey(), snapshot2.getValue());
                    }
                }

                callback.OnCallback(map);
            }

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                handleData(dataSnapshot);
                Log.i("Child added!", "child added");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                handleData(dataSnapshot);
                Log.i("Child", "changed");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {

            }
        });
    }

    /**
     * This function checks if the user is already in the database or not. If not, it creates a new
     * user and gives it some data points with initial values of -1.
     * @param path The pathname to Users in the database
     * @param name The username (this should be the key eventually) to check for.
     */
    public void CheckForExistingUser(final String path, final String userID, final String name, final MyCallback newUserCallback) {
        Query myQueryReference = mDatabase.child(path);
        myQueryReference.orderByKey().equalTo(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) { // user not in the DB, create a new one
                    Map<String, Object> newUser = new HashMap<>();
                    Map<String, Object> userData = new HashMap<>();
                    Map<String, Object> userStats = new HashMap<>();

                    // Temp location for user stats
                    userStats.put("login count", 0);
                    userStats.put("purchase count", 0);

                    newUser.put(userID, userData);
                    userData.put("Suggested Spending Amount", 0);   // this should be moved to user parameters at some point

                    userData.put("User Stats", userStats); // should be moved as well

                    mDatabase.child(path).child(userID).setValue(userData);
                    // if the user is not in the database, then jump to the UserProfileActivity so they can create their budget
                    newUserCallback.CreateNewUser();
                }
                else {
                    //Log.i("FUCK", "THE PATH: " + dataSnapshot.getKey());
                    HashMap<String, Object> userParameters = new HashMap<>();
                    // Pull data from DB to set to our BBUser singleton on start up

                    for(DataSnapshot snapshot : dataSnapshot.child(userID).child("User Parameters").getChildren()) {
                        userParameters.put(snapshot.getKey(), snapshot.getValue());
                    }

                    newUserCallback.OnCallback(userParameters);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    /**
     * This function checks if the user is already in the database or not. If not, it creates a new
     * user and gives it some data points with initial values of -1.
     * @param path The pathname to Users in the database
     * @param path2 The path to lead to the specified path in the database
     * @param  callback A callback object to return the retrieved data
     */
    public void singleRead(final List<String> path, final String path2, final MyCallback callback) throws InvalidDataLabelException {
        String fullPath = joinLabels(path) + path2;

        //Log.i("FUCK", "singleRead: " + fullPath);

        final DatabaseReference ref = mDatabase.child(fullPath);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> map = new HashMap<>();

                // Get snapshot needed
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    map.put(snapshot.getKey(), snapshot.getValue());
                    //Log.i("FUCK", "String: " + snapshot.getKey());
                    //Log.i("FUCK", "Val: " + snapshot.getValue());
                }
                callback.OnCallback(map);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
