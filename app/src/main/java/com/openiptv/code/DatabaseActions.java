package com.openiptv.code;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseActions extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseActions";

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

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" + COL1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL2 + " TEXT, " + COL3 + " TEXT, " + COL4 + " TEXT, " + COL5 + " TEXT, " + COL6 + " TEXT)";
        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    /**
     * Add and account to the database
     *
     * @param account
     * @return true on success, false on error thrown
     */
    public boolean addAccount(TVHeadendAccount account) {
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
        return true;
    }

    /**
     * Return all accounts
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
     * @param name
     * @return
     */
    public Cursor getAccountByID(String name) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + COL1 + " = '" + name + "'";
        Cursor account = sqLiteDatabase.rawQuery(query, null);
        return account;
    }

    /**
     * Returns an account after being passed the client name
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

}
