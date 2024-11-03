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
    private static final String ACCOUNT_ID = "account_id";
    private static final String USER_ID = "user_id";
    private static final String BALANCE = "balance";
    private static final String NAME = "name";
    private static final String TYPE = "type";


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

    public void syncSQLiteToFirebase(Context context) {
        if (NetworkUtil.isNetworkAvailable(context)) {
            // Sync accounts
            Cursor accountCursor = databaseHelper.getAllUnsyncedAccounts();
            Cursor expenseCursor = databaseHelper.getAllUnsyncedExpenses();
            while (accountCursor.moveToNext()) {
                int accountIdIndex = accountCursor.getColumnIndex(ACCOUNT_ID);
                int userIdIndex = accountCursor.getColumnIndex(USER_ID);
                int balanceIndex = accountCursor.getColumnIndex(BALANCE);
                int nameIndex = accountCursor.getColumnIndex(NAME);
                int typeIndex = accountCursor.getColumnIndex(TYPE);
                if (accountIdIndex != -1 && userIdIndex != -1 && balanceIndex != -1 && nameIndex != -1 && typeIndex != -1) {
                    String accountId = accountCursor.getString(accountIdIndex);
                    String userId = accountCursor.getString(userIdIndex);
                    double balance = accountCursor.getDouble(balanceIndex);
                    String name = accountCursor.getString(nameIndex);
                    String type = accountCursor.getString(typeIndex);
                    accountCursor.close();
                    expenseCursor.close();
                    new SaveDataTask().execute(accountCursor);
                }
                if (accountIdIndex != -1 && userIdIndex != -1 && balanceIndex != -1 && nameIndex != -1 && typeIndex != -1) {
                    String accountId = accountCursor.getString(accountIdIndex);
                    String userId = accountCursor.getString(userIdIndex);
                    double balance = accountCursor.getDouble(balanceIndex);
                    String name = accountCursor.getString(nameIndex);
                    String type = accountCursor.getString(typeIndex);
                    accountCursor.close();
                    int amountIndex = expenseCursor.getColumnIndex("amount");
                    int expenseIdIndex = expenseCursor.getColumnIndex("expense_id");
                    if (amountIndex != -1 && nameIndex != -1 && expenseIdIndex != -1) {
                        ContentValues expenseValues = new ContentValues();
                        expenseValues.put("amount", expenseCursor.getDouble(amountIndex));
                        expenseValues.put("name", expenseCursor.getString(nameIndex));
                        String expenseId = expenseCursor.getString(expenseIdIndex);

                        databaseHelper.updateExpense(expenseId, expenseValues);
                    }
                }
                expenseCursor.close();
            }
        }
    }


    private class SaveDataTask extends AsyncTask<Cursor, Void, Void> {
        @Override
        protected Void doInBackground(Cursor... cursors) {
            Cursor cursor = cursors[0];
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int accountIdIndex = cursor.getColumnIndex(ACCOUNT_ID);
                    int userIdIndex = cursor.getColumnIndex(USER_ID);
                    int balanceIndex = cursor.getColumnIndex(BALANCE);
                    int nameIndex = cursor.getColumnIndex(NAME);
                    int typeIndex = cursor.getColumnIndex(TYPE);

                    try {
                    if (accountIdIndex != -1 && userIdIndex != -1 && balanceIndex != -1 && nameIndex != -1 && typeIndex != -1) {
                        String accountId = cursor.getString(accountIdIndex);
                        String userId = cursor.getString(userIdIndex);
                        double balance = cursor.getDouble(balanceIndex);
                        String name = cursor.getString(nameIndex);
                        String type = cursor.getString(typeIndex);
                        databaseHelper.insertAccount(accountId, userId, balance, name, type);
                    }
                    } catch (Exception e) {
                        // Handle insertion error
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
            return null;
        }
    }
}