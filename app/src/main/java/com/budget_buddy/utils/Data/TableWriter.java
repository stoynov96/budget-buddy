package com.budget_buddy.utils.Data;

import android.support.annotation.NonNull;

import com.budget_buddy.config.DataConfig;
import com.budget_buddy.exception.InvalidDataLabelException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableWriter {

    private DatabaseReference mDatabase;

    /**
     * Initializes a {@link TableWriter} with a default instance
     * of a {@link DatabaseReference}
     */
    public TableWriter() {
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
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
            mDatabase.child(fullPath).child(label).setValue(data.ToMap());
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

    /**
     * This function adds a purchase to the database, saved based on the purchase date.
     * @param path The path to the Users in the database.
     * @param map Hashmap containing the data to add to the database. Maps an identifier to a value, such
     *            as Item Name : Nintendo Swtich.
     * @param label The sub-path to write the data to. Currently this is going to /UserName/Purchases/'Data'/'Item'.
     * @throws InvalidDataLabelException
     */
    // TODO: Probably needs some error checking at some point.
    public void WriteExpenditure(String path, Map<String, Object> map, String label) throws InvalidDataLabelException {
        mDatabase.child(path).child(label).push().setValue(map);
    }
}
