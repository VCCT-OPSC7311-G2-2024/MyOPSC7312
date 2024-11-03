package com.example.myopsc7312;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
        db.execSQL("CREATE TABLE " + TABLE_ACCOUNTS + " ("
                + ACCOUNT_ID + " TEXT PRIMARY KEY, "
                + USER_ID + " TEXT, "
                + BALANCE + " REAL, "
                + NAME + " TEXT, "
                + TYPE + " TEXT);");

        // Create budgets table
        db.execSQL("CREATE TABLE " + TABLE_BUDGETS + " ("
                + BUDGET_ID + " TEXT PRIMARY KEY, "
                + ACCOUNT_ID + " TEXT, "
                + AMOUNT + " REAL, "
                + NAME + " TEXT, "
                + "FOREIGN KEY (" + ACCOUNT_ID + ") REFERENCES " + TABLE_ACCOUNTS + "(" + ACCOUNT_ID + "));");

        // Create expenses table
        db.execSQL("CREATE TABLE " + TABLE_EXPENSES + " ("
                + EXPENSE_ID + " TEXT PRIMARY KEY, "
                + ACCOUNT_ID + " TEXT, "
                + AMOUNT + " REAL, "
                + NAME + " TEXT, "
                + "FOREIGN KEY (" + ACCOUNT_ID + ") REFERENCES " + TABLE_ACCOUNTS + "(" + ACCOUNT_ID + "));");

        // Create users table
        db.execSQL("CREATE TABLE " + TABLE_USERS + " ("
                + USER_ID + " TEXT PRIMARY KEY, "
                + EMAIL + " TEXT, "
                + PASSWORD + " TEXT);");

        // Modify the create table statement for accounts
        db.execSQL("CREATE TABLE " + TABLE_ACCOUNTS + " ("
                + ACCOUNT_ID + " TEXT PRIMARY KEY, "
                + USER_ID + " TEXT, "
                + BALANCE + " REAL, "
                + NAME + " TEXT, "
                + TYPE + " TEXT, "
                + "synced INTEGER DEFAULT 0, "
                + "FOREIGN KEY (" + USER_ID + ") REFERENCES " + TABLE_USERS + "(" + USER_ID + "));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Insert Account
    public void insertAccount(String accountId, String userId, double balance, String name, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ACCOUNT_ID, accountId);
        values.put(USER_ID, userId);
        values.put(BALANCE, balance);
        values.put(NAME, name);
        values.put(TYPE, type);
        values.put("synced", 0); // Set synced status to 0 (unsynced)
        db.insert(TABLE_ACCOUNTS, null, values);
    }

    // Insert Budget
    public void insertBudget(String budgetId, String accountId, double amount, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BUDGET_ID, budgetId);
        values.put(ACCOUNT_ID, accountId);
        values.put(AMOUNT, amount);
        values.put(NAME, name);
        db.insert(TABLE_BUDGETS, null, values);
    }

    // Insert Expense

    public void insertExpense(String expenseId, String accountId, double amount, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EXPENSE_ID, expenseId);
        values.put(ACCOUNT_ID, accountId);
        values.put(AMOUNT, amount);
        values.put(NAME, name);
        db.insert(TABLE_EXPENSES, null, values);
    }

    // Insert User
    public void insertUser(String userId, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_ID, userId);
        values.put(EMAIL, email);
        values.put(PASSWORD, password);
        db.insert(TABLE_USERS, null, values);
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
}