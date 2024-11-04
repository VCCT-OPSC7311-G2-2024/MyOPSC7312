package com.example.myopsc7312;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "app.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_ACCOUNTS = "accounts";
    private static final String TABLE_BUDGETS = "budgets";
    private static final String TABLE_EXPENSES = "expenses";
    private static final String TABLE_USERS = "users";

    // Accounts Table Columns
    private static final String ACCOUNT_ID = "account_id";
    private static final String USER_ID = "user_id";
    private static final String BALANCE = "balance";
    private static final String NAME = "name";
    private static final String TYPE = "type";

    // Budgets Table Columns
    private static final String BUDGET_ID = "budget_id";
    private static final String AMOUNT = "amount";

    // Expenses Table Columns
    private static final String EXPENSE_ID = "expense_id";

    // Users Table Columns
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create accounts table
        String CREATE_ACCOUNTS_TABLE = "CREATE TABLE IF NOT EXISTS accounts (" +
                "account_id TEXT PRIMARY KEY, " +
                "user_id TEXT, " +
                "balance REAL, " +
                "name TEXT, " +
                "type TEXT, " +
                "synced INTEGER DEFAULT 0, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                ")";
        db.execSQL(CREATE_ACCOUNTS_TABLE);

        // Create budgets table
        String CREATE_BUDGETS_TABLE = "CREATE TABLE IF NOT EXISTS budgets (" +
                "budget_id TEXT PRIMARY KEY, " +
                "account_id TEXT, " +
                "amount REAL, " +
                "name TEXT, " +
                "FOREIGN KEY (account_id) REFERENCES accounts(account_id)" +
                ")";
        db.execSQL(CREATE_BUDGETS_TABLE);

        // Create expenses table
        String CREATE_EXPENSES_TABLE = "CREATE TABLE IF NOT EXISTS expenses (" +
                "expense_id TEXT PRIMARY KEY, " +
                "account_id TEXT, " +
                "amount REAL, " +
                "name TEXT, " +
                "FOREIGN KEY (account_id) REFERENCES accounts(account_id)" +
                ")";
        db.execSQL(CREATE_EXPENSES_TABLE);

        // Create users table
        String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS users (" +
                "user_id TEXT PRIMARY KEY, " +
                "email TEXT, " +
                "password TEXT" +
                ")";
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    //
    public void insertAccount(String accountId, String userId, double balance, String name, String type, int synced) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("account_id", accountId);
        values.put("user_id", userId);
        values.put("balance", balance);
        values.put("name", name);
        values.put("type", type);
        values.put("synced", synced);
        db.insert("accounts", null, values);
        db.close();
    }

    // Insert Budget
    public void insertBudget(String budgetId, String accountId, double amount, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(BUDGET_ID, budgetId);
            values.put(ACCOUNT_ID, accountId);
            values.put(AMOUNT, amount);
            values.put(NAME, name);
            db.insert(TABLE_BUDGETS, null, values);
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    // Insert Expense
    public void insertExpense(String expenseId, String accountId, double amount, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(EXPENSE_ID, expenseId);
            values.put(ACCOUNT_ID, accountId);
            values.put(AMOUNT, amount);
            values.put(NAME, name);
            db.insert(TABLE_EXPENSES, null, values);
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    // Insert User
    public void insertUser(String userId, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("email", email);
            values.put("password", password);
            db.insert("users", null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    // Get Accounts for User
    public Cursor getAccountsForUser(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ACCOUNTS + " WHERE " + USER_ID + " = ?", new String[]{userId});
    }

    // Get Budgets for Account
    public Cursor getBudgetsForAccount(String accountId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_BUDGETS + " WHERE " + ACCOUNT_ID + " = ?", new String[]{accountId});
    }

    // Get Expenses for Account
    public Cursor getExpensesForAccount(String accountId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_EXPENSES + " WHERE " + ACCOUNT_ID + " = ?", new String[]{accountId});
    }

    // Get All Users
    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS, null);
    }

    // Get All Unsynced Accounts
    public Cursor getAllUnsyncedAccounts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ACCOUNTS + " WHERE synced = 0", null);
    }

    public void updateBudget(String budgetId, ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(TABLE_BUDGETS, values, BUDGET_ID + " = ?", new String[]{budgetId});
    }

    public void updateExpense(String expenseId, ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(TABLE_EXPENSES, values, EXPENSE_ID + " = ?", new String[]{expenseId});
    }
    public Cursor getAllUnsyncedExpenses() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM expenses WHERE synced = 0", null);
    }

    public void updateAccount(String accountId, ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.update("accounts", values, "account_id = ?", new String[]{accountId});
        db.close();
    }

    public Cursor getAllUnsyncedBudgets() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM budgets WHERE synced = 0", null);
    }

    public void saveUserCredentials(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("password", password);
        db.insert("users", null, values);
        db.close();
    }

    // In DatabaseHelper.java
    public boolean checkUserCredentials(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE email = ? AND password = ?", new String[]{email, password});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    // DatabaseHelper.java
    public List<User> getAllUserList() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users", null);
        if (cursor.moveToFirst()) {
            do {
                int emailIndex = cursor.getColumnIndex("email");
                int passwordIndex = cursor.getColumnIndex("password");

                if (emailIndex != -1 && passwordIndex != -1) {
                    String email = cursor.getString(emailIndex);
                    String password = cursor.getString(passwordIndex);
                    userList.add(new User(email, password, new ArrayList<>()));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return userList;
    }

    public void deleteUser(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("users", "email = ?", new String[]{email});
        db.close();
    }

    public List<Account> getAccounts(String userId) {
        List<Account> accounts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM accounts WHERE user_id = ?", new String[]{userId});

        if (cursor.moveToFirst()) {
            do {
                int accountIdIndex = cursor.getColumnIndex("account_id");
                int balanceIndex = cursor.getColumnIndex("balance");
                int nameIndex = cursor.getColumnIndex("name");
                int typeIndex = cursor.getColumnIndex("type");

                if (accountIdIndex != -1 && balanceIndex != -1 && nameIndex != -1 && typeIndex != -1) {
                    String accountId = cursor.getString(accountIdIndex);
                    double balance = cursor.getDouble(balanceIndex);
                    String name = cursor.getString(nameIndex);
                    String type = cursor.getString(typeIndex);
                    accounts.add(new Account(accountId, name, type, balance, new ArrayList<>()));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return accounts;
    }


}