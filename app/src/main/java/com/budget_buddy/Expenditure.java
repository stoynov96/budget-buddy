package com.budget_buddy;

// Initially made this to try to map a key to this object in the database. Could not figure out
// how to make it work, but I still think it would be preferable to just storing a HashMap for everything.
// Leaving this in for now as I'd still like to be able to store objects and get objects from Firebase,
// then manipulate them using class functions.

public class Expenditure {
    // Item name
    String name = "";
    // Purchase date
    String date = "";
    // Purchase amount
    String amount = "";
    // Purchase note
    String note = "";

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
}
