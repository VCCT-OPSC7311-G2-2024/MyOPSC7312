package com.example.myopsc7312;

import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.net.NetworkInfo;
import android.content.Intent;
import com.example.myopsc7312.Account;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;

public class SyncManager {
    private DatabaseHelper databaseHelper;
    private DatabaseReference databaseReference;
    private Context context;


    public SyncManager(Context context) {
        this.context = context;
        databaseHelper = new DatabaseHelper(context);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        registerNetworkReceiver(context);
    }

    private void registerNetworkReceiver(Context context) {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(networkReceiver, filter);
    }

    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            if (isConnected) {
                syncSQLiteToFirebase(context);
            }
        }
    };

    // Fetch data from Firebase and save to SQLite
    public void syncFirebaseToSQLite() {
        databaseReference.child("accounts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                try {
                    for (DataSnapshot accountSnapshot : dataSnapshot.getChildren()) {
                        String accountId = accountSnapshot.getKey();
                        for (DataSnapshot accountDetailSnapshot : accountSnapshot.getChildren()) {
                            if (accountDetailSnapshot.hasChild("balance")) {
                                String userId = accountId; // Assuming userId is the same as accountId
                                double balance = Double.parseDouble(accountDetailSnapshot.child("balance").getValue(String.class));
                                String name = accountDetailSnapshot.child("name").getValue(String.class);
                                String type = accountDetailSnapshot.child("type").getValue(String.class);

                                // Insert account data into SQLite
                                databaseHelper.insertAccount(accountDetailSnapshot.getKey(), userId, balance, name, type, 0);
                            }

                            // Insert budgets data into SQLite
                            DataSnapshot budgetsSnapshot = accountDetailSnapshot.child("budgets");
                            for (DataSnapshot budgetSnapshot : budgetsSnapshot.getChildren()) {
                                String budgetId = budgetSnapshot.getKey();
                                double amount = Double.parseDouble(budgetSnapshot.child("amount").getValue(String.class));
                                String budgetName = budgetSnapshot.child("name").getValue(String.class);
                                databaseHelper.insertBudget(budgetId, accountDetailSnapshot.getKey(), amount, budgetName, 0);
                            }

                            // Insert expenses data into SQLite
                            DataSnapshot expensesSnapshot = accountDetailSnapshot.child("expenses");
                            for (DataSnapshot expenseSnapshot : expensesSnapshot.getChildren()) {
                                String expenseId = expenseSnapshot.getKey();
                                double amount = expenseSnapshot.child("amount").getValue(Double.class);
                                String expenseName = expenseSnapshot.child("name").getValue(String.class);
                                databaseHelper.insertExpense(expenseId, accountDetailSnapshot.getKey(), amount, expenseName, 0);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.close();
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
                while (accountCursor.moveToNext()) {
                    int accountIdIndex = accountCursor.getColumnIndex("account_id");
                    int userIdIndex = accountCursor.getColumnIndex("user_id");
                    int balanceIndex = accountCursor.getColumnIndex("balance");
                    int nameIndex = accountCursor.getColumnIndex("name");
                    int typeIndex = accountCursor.getColumnIndex("type");
                    if (accountIdIndex != -1 && userIdIndex != -1 && balanceIndex != -1 && nameIndex != -1 && typeIndex != -1) {
                        String accountId = accountCursor.getString(accountIdIndex);
                        String userId = accountCursor.getString(userIdIndex);
                        double balance = accountCursor.getDouble(balanceIndex);
                        String name = accountCursor.getString(nameIndex);
                        String type = accountCursor.getString(typeIndex);
                        DatabaseReference accountRef = databaseReference.child("accounts").child(accountId);
                        accountRef.child("user_id").setValue(userId);
                        accountRef.child("balance").setValue(balance);
                        accountRef.child("name").setValue(name);
                        accountRef.child("type").setValue(type);

                        ContentValues accountValues = new ContentValues();
                        accountValues.put("synced", 1);
                        databaseHelper.updateAccount(accountId, accountValues);
                    }
                    accountCursor.close();
                }
                Cursor budgetCursor = databaseHelper.getAllUnsyncedBudgets();
                while (budgetCursor.moveToNext()) {
                    int budgetIdIndex = budgetCursor.getColumnIndex("budget_id");
                    int budgetAccountIdIndex = budgetCursor.getColumnIndex("account_id");
                    int amountIndex = budgetCursor.getColumnIndex("amount");
                    int budgetNameIndex = budgetCursor.getColumnIndex("name");

                    if (budgetIdIndex != -1 && budgetAccountIdIndex != -1 && amountIndex != -1 && budgetNameIndex != -1) {
                        String budgetId = budgetCursor.getString(budgetIdIndex);
                        String accountId = budgetCursor.getString(budgetAccountIdIndex);
                        double amount = budgetCursor.getDouble(amountIndex);
                        String name = budgetCursor.getString(budgetNameIndex);

                        DatabaseReference budgetRef = databaseReference.child("accounts").child(accountId).child("budgets").child(budgetId);
                        budgetRef.child("amount").setValue(amount);
                        budgetRef.child("name").setValue(name);

                        ContentValues budgetValues = new ContentValues();
                        budgetValues.put("synced", 1);
                        databaseHelper.updateBudget(budgetId, budgetValues);
                    }
                }
                budgetCursor.close();

                Cursor expenseCursor = databaseHelper.getAllUnsyncedExpenses();
                Cursor unsyncedExpenseCursor = databaseHelper.getAllUnsyncedExpenses();
                while (expenseCursor.moveToNext()) {
                    int expenseIdIndex = expenseCursor.getColumnIndex("expense_id");
                    int expenseAccountIdIndex = expenseCursor.getColumnIndex("account_id");
                    int expenseAmountIndex = expenseCursor.getColumnIndex("amount");
                    int expenseNameIndex = expenseCursor.getColumnIndex("name");

                    if (expenseIdIndex != -1 && expenseAccountIdIndex != -1 && expenseAmountIndex != -1 && expenseNameIndex != -1) {
                        String expenseId = expenseCursor.getString(expenseIdIndex);
                        String accountId = expenseCursor.getString(expenseAccountIdIndex);
                        double amount = expenseCursor.getDouble(expenseAmountIndex);
                        String name = expenseCursor.getString(expenseNameIndex);

                        DatabaseReference expenseRef = databaseReference.child("accounts").child(accountId).child("expenses").child(expenseId);
                        expenseRef.child("amount").setValue(amount);
                        expenseRef.child("name").setValue(name);

                        ContentValues expenseValues = new ContentValues();
                        expenseValues.put("synced", 1);
                        databaseHelper.updateExpense(expenseId, expenseValues);
                    }
                    unsyncedExpenseCursor.close();
                    expenseCursor.close();
                }
            }
        else {
            // Device is offline, handle accordingly
            Log.d("SyncManager", "Device is offline, cannot sync data.");
        }
    }
}
