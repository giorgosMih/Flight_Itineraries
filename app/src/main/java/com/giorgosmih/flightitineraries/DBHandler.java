package com.giorgosmih.flightitineraries;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "flightsDB";

    private static final String TABLE_AIRLINES = "airlines";
    private static final String AIRLINES_ID = "ID";
    private static final String AIRLINES_CODE = "CODE";
    private static final String AIRLINES_NAME = "NAME";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_AIRLINES,null,null);
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_AIRLINES + "("
        + AIRLINES_ID + " INTEGER PRIMARY KEY," + AIRLINES_CODE + " TEXT,"
        + AIRLINES_NAME + " TEXT" + ")";

        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AIRLINES);
        onCreate(db);
    }

    public void addAirline(String[] data) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AIRLINES_CODE, data[0]);
        values.put(AIRLINES_NAME, data[1]);

        db.insert(TABLE_AIRLINES, null, values);
        db.close();
    }

    public String getAirlineByCode(String code) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_AIRLINES, new String[]{AIRLINES_NAME}, AIRLINES_CODE + "=?",new String[]{code}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        String val = cursor.getString(0);
        cursor.close();
        return val;
    }
}