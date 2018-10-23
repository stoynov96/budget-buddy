package com.budget_buddy;

// Initially made this to try to map a key to this object in the database. Could not figure out
// how to make it work, but I still think it would be preferable to just storing a HashMap for everything.
// Leaving this in for now as I'd still like to be able to store objects and get objects from Firebase,
// then manipulate them using class functions.

import com.budget_buddy.utils.Data.DataNode;
import java.util.HashMap;
import java.util.Map;

public class Expenditure implements DataNode {
    // Item name
    String name;
    // Purchase date
    String date;
    // Purchase amount
    String amount;
    // Purchase note
    String note;

    // Constructor
    Expenditure(String newName, String newDate, String newAmount, String newNote) {
        name=newName;
        date=newDate;
        amount=newAmount;
        note=newNote;
    }

    // Currently unused since I can't return an Expenditure from Firebase. Self-explanatory.
    public String GetName() {return name;}
    public String GetDate() {return date;}
    public String GetAmount() {return amount;}
    public String GetNote() {return note;}

    @Override
    public Map<String, Object> ToMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("amount", amount);
        map.put("date", date);
        map.put("name", name);
        map.put("note", note);

        return map;
    }

    @Override
    public void GetFromMap(Map<String, Object> map) {
        name = (String)map.get("name");
        date = (String)map.get("date");
        amount = (String)map.get("amount");
        note = (String)map.get("note");
    }

    @Override
    public void OnDataChange() {

    }
}