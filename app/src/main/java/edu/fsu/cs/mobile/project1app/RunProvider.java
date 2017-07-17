package edu.fsu.cs.mobile.project1app;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;


public class RunProvider extends ContentProvider{
    public final static String DBNAME = "pastRuns3";
    public final static String RUN_TABLE = "runs";
    public final static String RUN_ID = "_id";
    public final static String RUN_STEPS = "steps";
    public final static String RUN_DISTANCE = "distance";
    public final static String START_TIME = "startTime";
    public final static String END_TIME = "endTime";
    public final static String RUN_MINUTES = "minutes";
    public final static String RUN_SECONDS = "seconds";
    public final static String RUN_MILISECONDS = "miliseconds";



    public static final Uri CONTENT_URI = Uri.parse(
            "content://edu.fsu.cs.mobile.project1app.provider/" + RUN_TABLE);

    private static final String SQL_CREATE = "CREATE TABLE "
            + RUN_TABLE + " ( "
            + RUN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + RUN_STEPS + " REAL,"
            + RUN_DISTANCE + " REAL,"
            + RUN_MINUTES + " INTEGER, "
            + RUN_SECONDS + " INTEGER, "
            + RUN_MILISECONDS + " INTEGER )";

    private MainDatabaseHelper mOpenHelper;
    private static UriMatcher sUriMatcher;

    @Override
    public boolean onCreate(){
        mOpenHelper = new MainDatabaseHelper(getContext());
        return true;
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        return mOpenHelper.getWritableDatabase().update(RUN_TABLE, values, selection, selectionArgs);
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        return mOpenHelper.getReadableDatabase().query(RUN_TABLE, projection, selection, selectionArgs,
                null, null, sortOrder);
    }

    public Uri insert(Uri uri, ContentValues values){

        //String _id = values.getAsString(RUN_ID);
        //String steps = values.getAsString(RUN_STEPS);
        //String distance = values.getAsString(RUN_DISTANCE);

        Log.i("Insert", "About to insert");


        long id = mOpenHelper.getWritableDatabase().insert(RUN_TABLE, null, values);
        Log.i("Insert", "Inserted into database");
        return Uri.withAppendedPath(CONTENT_URI, "" + id);

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs){
        return mOpenHelper.getWritableDatabase().delete(RUN_TABLE, selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri){
        return null;
    }

    protected static final class MainDatabaseHelper extends SQLiteOpenHelper {

        MainDatabaseHelper(Context context){
            super(context, DBNAME, null, 1);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + RUN_TABLE);
            db.execSQL(SQL_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + RUN_TABLE);

        }
    }



}
