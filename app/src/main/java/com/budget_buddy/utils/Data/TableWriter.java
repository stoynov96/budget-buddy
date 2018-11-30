package com.budget_buddy.utils.Data;

import android.support.annotation.NonNull;

import com.budget_buddy.config.DataConfig;
import com.budget_buddy.exception.InvalidDataLabelException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TableWriter {

    private DatabaseReference mDatabase;

    /**
     * Initializes a {@link TableWriter} with a default instance
     * of a {@link DatabaseReference}
     */
    public TableWriter() {
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void SetData(List<String> path, String label, DataNode data) throws InvalidDataLabelException {
        String fullPath = joinLabels(path);

        if(label == null) {
            mDatabase.child(fullPath).setValue(data.ToMap());
        } else {
            mDatabase.child(fullPath).child(label).setValue(data.ToMap());
        }
    }

    /**
     * Writes data to the database with a specified path
     * @param path Path under which to write the data
     * @param data {@link DataNode} to write to the database
     * @param label Label to write data under
     *             (leave null if one should be generated automatically)
     * @throws InvalidDataLabelException Thrown if an invalid path is used
     */
    public void WriteData(final String path, DataNode data, String label)
            throws InvalidDataLabelException{
        this.WriteData(new ArrayList<String>() {{ add(path); }}, data, label);
    }

    /**
     * Writes data nested under a number of labels
     * @param path Path of labels to nest data under
     * @param data {@link DataNode} to write to the database
     * @param label Label to write data under
     *              (leave null if one should be generated automatically)
     * @throws InvalidDataLabelException Thrown if an invalid label is used
     */
    public void WriteData(List<String> path, DataNode data, String label)
            throws InvalidDataLabelException {
        String fullPath = joinLabels(path);

        OnCompleteListener<Void> listener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isCanceled()) {
                    // Todo: add logic here to handle cancelled writes
                }
            }
        };

        // Write the data
        if (label == null)
            mDatabase.child(fullPath).push().setValue(data.ToMap());
        else
            mDatabase.child(fullPath).child(label).push().setValue(data.ToMap());
    }

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

    public void Increment(final List<String> path, final String path2, final MyCallback callback) throws InvalidDataLabelException{
        String fullPath = joinLabels(path) + path2;

        final DatabaseReference ref = mDatabase.child(fullPath);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = dataSnapshot.getValue(Integer.class);
                count++;
                callback.OnIncrement(count); // increment before write to db
                ref.setValue(count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void IncrementBy(final int amount, final List<String> path, final String path2, final MyCallback callback) throws InvalidDataLabelException {
        String fullPath = joinLabels(path) + path2;

        final DatabaseReference ref = mDatabase.child(fullPath);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //HashMap<String, Object>  map = new HashMap<>();
                int count = dataSnapshot.getValue(Integer.class);
                count += amount;
                callback.OnIncrement(count); // increment before write to db
                ref.setValue(count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}