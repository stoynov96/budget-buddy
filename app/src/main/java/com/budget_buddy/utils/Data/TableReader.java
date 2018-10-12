package com.budget_buddy.utils.Data;

import android.support.annotation.NonNull;

import com.budget_buddy.config.DataConfig;
import com.budget_buddy.exception.InvalidDataLabelException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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
     * Latches onto a particular child given as a list of labels.
     * As data in the database is changed, so will the values in that object
     * @param labels List of labels to reference to the database child
     * @param data {@link DataNode} to latch to the database
     * @throws InvalidDataLabelException Thrown if an invalid label is used
     */
    public void Latch(List<String> labels, final DataNode data)
            throws InvalidDataLabelException {
        // Todo: we should validate if there actually is data at this label.
        // Todo: (cont.) the problem with doing it the obvious way is outlined here:
        // https://stackoverflow.com/questions/37397205/google-firebase-check-if-child-exists
        String label = joinLabels(labels);
        mDatabase.child(label).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map)dataSnapshot.getValue();
                data.GetFromMap(map);
                data.OnDataChange();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // todo
            }
        });
    }

    /**
     * Latches onto a particular child given as a label under the root.
     * As data in the database is changed, so will the values in that object
     * @param label Label to reference the database child
     * @param data {@link DataNode} to latch to the database
     * @throws InvalidDataLabelException Thrown if an invalid label is used
     */
    public void Latch(final String label, final DataNode data)
            throws InvalidDataLabelException {
        List<String> labels = new ArrayList<String>() {{ add(label); }};
        Latch(labels, data);
    }

    // Todo: this is code repetition from TableWriter. Maybe we should define this method externally
    private String joinLabels(List<String> labels) throws InvalidDataLabelException {
        StringBuilder labelsSb = new StringBuilder();
        String deliminator = "";

        for(String label : labels) {
            DataConfig.DataLabels.VerifyLabel(label);
            labelsSb.append(deliminator);
            deliminator = "/";
            labelsSb.append(label);
        }
        return labelsSb.toString();
    }
}
