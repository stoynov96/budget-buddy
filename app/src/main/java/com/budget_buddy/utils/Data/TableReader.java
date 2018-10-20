package com.budget_buddy.utils.Data;

import android.support.annotation.NonNull;
import android.util.Log;

import com.budget_buddy.config.DataConfig;
import com.budget_buddy.exception.InvalidDataLabelException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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

    public void WeeklyExpenditures(String path, final MyCallback callback) {
        Calendar calendar = GregorianCalendar.getInstance();
        DateFormat formatter = new SimpleDateFormat("MM-dd-yy");
        String date;
        Date weekOld;

        for (int i = 6; i >= 0; i--) {
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_YEAR, -i);
            weekOld = calendar.getTime();
            date = formatter.format(weekOld);
            Log.d("WEEK OLD: ", date);
            Query myQueryReference = mDatabase.child(path).child(date);
            myQueryReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("WEEK OLD: ", dataSnapshot.toString());
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Log.d("VALUES: ", snapshot.getValue().toString());
                        callback.onCallback(snapshot.getRef().getParent().getKey(), snapshot.getValue().toString());
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });
        }
    }
}
