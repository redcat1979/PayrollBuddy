package org.ryancutter.payrollbuddy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PayrollDbAdapter {
    public static final String KEY_EMPS_ROWID = "_id";
    public static final String KEY_EMPS_NAME = "name";
    public static final String KEY_EMPS_STATUS = "status"; // 0 = inactive, 1 = active
    public static final String KEY_EMPS_PAY = "pay"; // 0 = no pay, 1 = pay
    public static final String KEY_EMPS_PAYRATE = "payrate";
    public static final String KEY_EMPS_PAYUNIT = "payunit"; // 0 = hourly, 1 = daily
    public static final String KEY_EMPS_PRECISION = "precision"; // 15, 6, 1 mins (hourly only)
    public static final String KEY_EMPS_OVERTIME = "overtime"; // 0 = no, 1 = yes
    public static final String KEY_EMPS_YEAR = "year";
    public static final String KEY_EMPS_MONTH = "month";
    public static final String KEY_EMPS_DAY = "day";
    public static final String KEY_PROFILE_COUNTER = "counter";
    public static final String KEY_PROFILE_EMAIL = "email";
    public static final String KEY_TIME_ROWID = "_id";
    public static final String KEY_TIME_EMPID = "emp_id";
    public static final String KEY_TIME_DATE = "date";
    public static final String KEY_TIME_START = "start";
    public static final String KEY_TIME_STOP = "stop";
    
    private static final String TAG = "PayrollDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String EMPS_CREATE =
        "create table emps (_id integer primary key autoincrement, "
        + "name text not null, status integer, pay integer, payrate real, "
        + "payunit integer, precision integer, overtime integer);";

    private static final String TIME_CREATE =
        "create table time (_id integer primary key autoincrement, "
        + "emp_id integer, date long, start long, stop long);";
    
    private static final String PROFILE_CREATE =
        "create table profile (email text, counter integer);";
    
    private static final String DATABASE_NAME = "data";
    private static final String EMPS_TABLE = "emps";
    private static final String TIME_TABLE = "time";
    private static final String PROFILE_TABLE = "profile";
    private static final int DATABASE_VERSION = 21;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(EMPS_CREATE);
            db.execSQL(PROFILE_CREATE);
            db.execSQL(TIME_CREATE);
            
            ContentValues initialValues = new ContentValues();
            initialValues.put(KEY_PROFILE_COUNTER, 0);

            db.insert(PROFILE_TABLE, null, initialValues);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + EMPS_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + PROFILE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + TIME_TABLE);
            onCreate(db);
        }
    }

    public PayrollDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public PayrollDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public long createEmp(String name) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_EMPS_NAME, name);
        initialValues.put(KEY_EMPS_STATUS, 1);
        initialValues.put(KEY_EMPS_PAY, 1);
        initialValues.put(KEY_EMPS_PAYRATE, 0.00);
        initialValues.put(KEY_EMPS_PAYUNIT, 0);
        initialValues.put(KEY_EMPS_PRECISION, 15);
        initialValues.put(KEY_EMPS_OVERTIME, 0);

        return mDb.insert(EMPS_TABLE, null, initialValues);
    }

    public boolean deleteEmp(long rowId) {
        return mDb.delete(EMPS_TABLE, KEY_EMPS_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllEmps() {
        return mDb.query(EMPS_TABLE, new String[] {KEY_EMPS_ROWID, KEY_EMPS_NAME}, null, null, null, null, null);
    }

    public Cursor fetchEmp(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, EMPS_TABLE, new String[] {KEY_EMPS_ROWID,
                    KEY_EMPS_NAME, KEY_EMPS_STATUS, KEY_EMPS_PAY, 
                    KEY_EMPS_PAYRATE, KEY_EMPS_PAYUNIT, KEY_EMPS_PRECISION,
                    KEY_EMPS_OVERTIME}, KEY_EMPS_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    public Cursor fetchTime(long empId, long millis) throws SQLException {

        Cursor mCursor =

            mDb.query(true, TIME_TABLE, new String[] {KEY_TIME_ROWID,
                    KEY_TIME_EMPID, KEY_TIME_DATE, KEY_TIME_START, KEY_TIME_STOP}, 
                    KEY_TIME_EMPID + "=" + empId + " AND " +
            		KEY_TIME_DATE + "=" + millis, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    public Cursor fetchAllTime(long empId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, TIME_TABLE, new String[] {KEY_TIME_ROWID,
                    KEY_TIME_EMPID, KEY_TIME_DATE, KEY_TIME_START, KEY_TIME_STOP}, 
                    KEY_TIME_EMPID + "=" + empId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    public Cursor fetchTimes(long start, long stop) throws SQLException {

        Cursor mCursor =

            mDb.query(true, TIME_TABLE, new String[] {KEY_TIME_ROWID,
                    KEY_TIME_EMPID, KEY_TIME_DATE, KEY_TIME_START, KEY_TIME_STOP}, 
            		KEY_TIME_START + ">=" + start + " AND " +
            		KEY_TIME_STOP + "<=" + stop, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    public long saveTime(long empId, long date, long start, long stop) {
    	ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TIME_EMPID, empId);
        initialValues.put(KEY_TIME_DATE, date);
        initialValues.put(KEY_TIME_START, start);
        initialValues.put(KEY_TIME_STOP, stop);

        deleteTime(empId, date);
        return mDb.insert(TIME_TABLE, null, initialValues);
    }

    public boolean deleteTime(long empId, long date) {
        return mDb.delete(TIME_TABLE, KEY_TIME_EMPID + "=" + empId + " AND " + KEY_TIME_DATE + "=" + date, null) > 0;
    }

    public boolean updateEmp(long rowId, String name, int status, int pay, String payrate,
    		int payunit, int precision, int overtime) {
        ContentValues args = new ContentValues();
        args.put(KEY_EMPS_NAME, name);
        args.put(KEY_EMPS_STATUS, status);
        args.put(KEY_EMPS_OVERTIME, overtime);
        args.put(KEY_EMPS_PAY, pay);
        args.put(KEY_EMPS_PAYRATE, payrate);
        args.put(KEY_EMPS_PAYUNIT, payunit);
        args.put(KEY_EMPS_PRECISION, precision);

        return mDb.update(EMPS_TABLE, args, KEY_EMPS_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor fetchCounter() throws SQLException {

        Cursor mCursor =

            mDb.query(true, PROFILE_TABLE, new String[] {KEY_PROFILE_COUNTER}, null, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    public Cursor fetchProfile() throws SQLException {

        Cursor mCursor =

            mDb.query(true, PROFILE_TABLE, new String[] {KEY_PROFILE_EMAIL}, null, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public boolean updateProfile(String email) {
        ContentValues args = new ContentValues();
        args.put(KEY_PROFILE_EMAIL, email);

        return mDb.update(PROFILE_TABLE, args, null, null) > 0;
    }

    public boolean incCounter(long counter) {
        ContentValues args = new ContentValues();
        args.put(KEY_PROFILE_COUNTER, counter);

        return mDb.update(PROFILE_TABLE, args, null, null) > 0;
    }
}
