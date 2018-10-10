package com.budget_buddy.utils;

import com.budget_buddy.config.DataConfig;
import com.budget_buddy.exception.InvalidDataLabelException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class TableWriter {

    private DatabaseReference mDatabase;

    /**
     * Initializes a TableWriter with a default instance of a {@link DatabaseReference}
     */
    public TableWriter() {
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Writes data to the database with a specified label
     * @param label Label under which to write the data
     * @param data Data to write
     * @param push Whether to push with a new key
     *             (use false if the key is already provided in labels)
     * @throws InvalidDataLabelException Thrown if an invalid label is used
     */
    public void WriteData(final String label, Object data, boolean push)
            throws InvalidDataLabelException{
        this.WriteData(new ArrayList<String>() {{ add(label); }}, data, push);
    }

    /**
     * Writes data nested under a number of labels
     * @param labels List of labels to nest data under
     * @param data Data to write
     * @param push Whether to push with a new key
     *             (use false if the key is already provided in labels)
     * @throws InvalidDataLabelException Thrown if an invalid label is used
     */
    public void WriteData(List<String> labels, Object data, boolean push)
            throws InvalidDataLabelException{
        String label = joinLabels(labels);
        if (push)
            mDatabase.child(label).push().setValue(data);
        else
            mDatabase.child(label).setValue(data);
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
}
