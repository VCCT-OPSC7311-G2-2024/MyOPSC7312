package com.example.myopsc7312;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.example.myopsc7312.Account;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;

public class SyncManager {
    private DatabaseHelper databaseHelper;
    private DatabaseReference databaseReference;


    public SyncManager(Context context) {
        databaseHelper = new DatabaseHelper(context);
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    // Fetch data from Firebase and save to SQLite
    public void syncFirebaseToSQLite() {
        databaseReference.child("accounts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot accountSnapshot : dataSnapshot.getChildren()) {
                    String accountId = accountSnapshot.getKey();
                    String userId = accountSnapshot.child("user_id").getValue(String.class);
                    double balance = accountSnapshot.child("balance").getValue(Double.class);
                    String name = accountSnapshot.child("name").getValue(String.class);
                    String type = accountSnapshot.child("type").getValue(String.class);

                    // Insert account data into SQLite
                    databaseHelper.insertAccount(accountId, userId, balance, name, type);

                    // Insert budgets data into SQLite
                    DataSnapshot budgetsSnapshot = accountSnapshot.child("budgets");
                    for (DataSnapshot budgetSnapshot : budgetsSnapshot.getChildren()) {
                        String budgetId = budgetSnapshot.getKey();
                        double amount = budgetSnapshot.child("amount").getValue(Double.class);
                        String budgetName = budgetSnapshot.child("name").getValue(String.class);
                        databaseHelper.insertBudget(budgetId, accountId, amount, budgetName);
                    }

                    // Insert expenses data into SQLite
                    DataSnapshot expensesSnapshot = accountSnapshot.child("expenses");
                    for (DataSnapshot expenseSnapshot : expensesSnapshot.getChildren()) {
                        String expenseId = expenseSnapshot.getKey();
                        double amount = expenseSnapshot.child("amount").getValue(Double.class);
                        String expenseName = expenseSnapshot.child("name").getValue(String.class);
                        databaseHelper.insertExpense(expenseId, accountId, amount, expenseName);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }


    private class SaveDataTask extends AsyncTask<DataSnapshot, Void, Void> {
        @Override
        protected Void doInBackground(DataSnapshot... dataSnapshots) {
            DataSnapshot dataSnapshot = dataSnapshots[0];
            for (DataSnapshot accountSnapshot : dataSnapshot.getChildren()) {
                String accountId = accountSnapshot.getKey();
                String userId = accountSnapshot.child("user_id").getValue(String.class);
                double balance = accountSnapshot.child("balance").getValue(Double.class);
                String name = accountSnapshot.child("name").getValue(String.class);
                String type = accountSnapshot.child("type").getValue(String.class);

                // Insert account data into SQLite
                try {
                    databaseHelper.insertAccount(accountId, userId, balance, name, type);
                } catch (Exception e) {
                    // Handle insertion error
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}