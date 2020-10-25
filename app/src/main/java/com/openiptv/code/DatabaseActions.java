package com.openiptv.code;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * Contains the methods to save and load accounts to/from the database easily
 */
public class DatabaseActions extends SQLiteOpenHelper {

    private static final String TAG = DatabaseActions.class.getSimpleName();

    public static Bundle activeAccount;

    // Just stores the active account ID
    private static final String ACTIVE_ACCOUNT_TABLE = "activeAccountTable";

    // Stores the actual user data.
    private static final String TABLE_NAME = "userDatabase";
    private static final String COL1 = "ID";
    private static final String COL2 = "username";
    private static final String COL3 = "password";
    private static final String COL4 = "hostname";
    private static final String COL5 = "port";
    private static final String COL6 = "clientName";

    public DatabaseActions(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DatabaseActions(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    /**
     * When the application is opened for the first time, create the database(s) using these parameters
     *
     * @param sqLiteDatabase (Ignore automatically filled)
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" + COL1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL2 + " TEXT, " + COL3 + " TEXT, " + COL4 + " TEXT, " + COL5 + " TEXT, " + COL6 + " TEXT)";
        sqLiteDatabase.execSQL(createTable);

        createTable = "CREATE TABLE " + ACTIVE_ACCOUNT_TABLE + " (" + COL1 + " INTEGER)";
        sqLiteDatabase.execSQL(createTable);
    }

    /**
     * When the database is upgraded drop and recreate the databases.
     *
     * @param sqLiteDatabase
     * @param i
     * @param i1
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
        sqLiteDatabase.execSQL("DROP IF TABLE EXISTS " + ACTIVE_ACCOUNT_TABLE);
        onCreate(sqLiteDatabase);
    }

    /**
     * When an account is marked as inactive the table is dropped as a precaution to
     * make sure that only one entry is in the database at a time.
     * It is then recreated with no entries.
     */
    public void removeActiveAccount() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL("DROP TABLE " + ACTIVE_ACCOUNT_TABLE);

        String createTable = "CREATE TABLE " + ACTIVE_ACCOUNT_TABLE + " (" + COL1 + " INTEGER)";
        sqLiteDatabase.execSQL(createTable);
    }

    /**
     * Mark an account as active by using its id.
     * Drops the original table, recreates it with no entries, and then adds the account to it
     * (Marking it active)
     *
     * @param id The id of the account to mark as active
     * @return true if successful, otherwise return false
     */
    public boolean setActiveAccount(String id) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        // Drop and recreate table
        removeActiveAccount();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, id);
        // Get account using the id and then add to the active account database.
        try {
            sqLiteDatabase.insertOrThrow(ACTIVE_ACCOUNT_TABLE, null, contentValues);

        } catch (Exception e) {
            return false;
        }

        // Mark the account active in a static variable for use by other classes
        activeAccount = this.accountToBundle(this.getAccountByID(id));
        return true;
    }

    /**
     * Gets the active account and adds it to the static variable
     * Used to make sure that the variable is accurate after the application is closed.
     *
     * @return
     */
    public boolean syncActiveAccount() {
        activeAccount = this.accountToBundle(this.getAccountByID(this.getActiveAccount()));

        return true;
    }

    /**
     * Returns the id of the active account currently stored in the database.
     *
     * @return id of the account marked active in the database
     */
    public String getActiveAccount() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "SELECT * FROM " + ACTIVE_ACCOUNT_TABLE;
        Cursor account = sqLiteDatabase.rawQuery(query, null);
        account.moveToFirst();
        return account.getString(0);
    }

    /**
     * This method converts a Cursor containing an account to a Bundle to simplify editing and
     * deleting entries
     *
     * @param account The account to convert to a Bundle
     * @return The Bundle with the account details
     */
    public Bundle accountToBundle(Cursor account) {
        account.moveToFirst();
        Bundle accountToBundle = new Bundle();

        accountToBundle.putString("id", account.getString(0));
        accountToBundle.putString("username", account.getString(1));
        accountToBundle.putString("password", account.getString(2));
        accountToBundle.putString("hostname", account.getString(3));
        accountToBundle.putString("port", account.getString(4));
        accountToBundle.putString("clientName", account.getString(5));

        return accountToBundle;
    }

    /**
     * Updates an account with new details
     *
     * @param id      Original account ID
     * @param account Account Details to replace it with
     */
    public Boolean updateAccount(String id, TVHeadendAccount account) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE " + COL1 + " = '" + id+"'";

        sqLiteDatabase.execSQL(query);

        checkAccountValid(account);

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, account.getUsername());
        contentValues.put(COL3, account.getPassword());
        contentValues.put(COL4, account.getHostname());
        contentValues.put(COL5, account.getPort());
        contentValues.put(COL6, account.getClientName());
        try {
            sqLiteDatabase.insertOrThrow(TABLE_NAME, null, contentValues);

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Boolean checkAccountValid(TVHeadendAccount account) {
        /**
         * If any entries are equal to "", return false
         */
        if (account.getUsername().equals("") || account.getPassword().equals("") ||
                account.getHostname().equals("") || account.getPort().equals("") ||
                account.getClientName().equals("")) {
            return false;
        }
        /**
         * Check port is a number if not return false
         */
        try {
            Integer.parseInt(account.getPort());
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Add and account to the database
     *
     * @param account
     * @return true on success, false on error thrown
     */
    public boolean addAccount(TVHeadendAccount account) {
        if (checkAccountValid(account)) {

            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put(COL2, account.getUsername());
            contentValues.put(COL3, account.getPassword());
            contentValues.put(COL4, account.getHostname());
            contentValues.put(COL5, account.getPort());
            contentValues.put(COL6, account.getClientName());
            try {
                sqLiteDatabase.insertOrThrow(TABLE_NAME, null, contentValues);

            } catch (Exception e) {
                return false;
            }

            // Get id of just added and set it to active account
            String query = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " + COL1 + " DESC";
            Cursor accountIDs = sqLiteDatabase.rawQuery(query, null);
            accountIDs.moveToFirst();
            setActiveAccount(accountIDs.getString(0));

            activeAccount = accountToBundle(accountIDs);

            return true;
        }
        return false;
    }

    /**
     * Return all accounts in the database
     *
     * @return
     */
    public Cursor getAccounts() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor accounts = sqLiteDatabase.rawQuery(query, null);
        return accounts;
    }

    /**
     * Returns an account after being passed an ID
     *
     * @param name id of the account
     * @return Cursor with the account
     */
    public Cursor getAccountByID(String name) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + COL1 + " = '" + name + "'";
        Cursor account = sqLiteDatabase.rawQuery(query, null);
        return account;
    }

    /**
     * Returns the first account after being passed the client name (Not reliable due to possible duplicates)
     *
     * @param name
     * @return
     */
    public Cursor getAccountByClientName(String name) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + COL6 + " = '" + name + "'";
        Cursor account = sqLiteDatabase.rawQuery(query, null);
        return account;
    }

    /**
     * Removes an account from the database. Requires the id and the client name.
     *
     * @param id
     * @param clientName
     */
    public void deleteAccount(int id, String clientName) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE " + COL1 + " = '" + id + "' AND " + COL6 + " = '" + clientName + "'";

        sqLiteDatabase.execSQL(query);
    }

    /**
     * Remove all accounts with a certain name rather than just the first.
     *
     * @param clientName
     */
    public void clearAccountsClientName(String clientName) {
        Cursor accounts = getAccountByClientName(clientName);

        while (accounts.moveToNext()) {
            deleteAccount(Integer.parseInt(accounts.getString(0)), accounts.getString(5));
        }
    }

    /**
     * Checks if the accounts database is empty
     *
     * @return If it is empty, return true, else return false.
     */
    public Boolean isAccountsEmpty() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor accounts = sqLiteDatabase.rawQuery(query, null);

        accounts.moveToFirst();

        int count = accounts.getInt(0);
        if (count <= 1) {
            return true;
        }
        return false;
    }
}
