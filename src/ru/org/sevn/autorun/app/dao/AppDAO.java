/*
 * Copyright 2016 Veronica Anokhina.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.org.sevn.autorun.app.dao;

import java.util.ArrayList;
import java.util.List;

import ru.org.sevn.alib.data.app.AppDetail;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class AppDAO {

    private static final String TEXT_TYPE = " TEXT ";
    
    public static abstract class Columns implements BaseColumns {
        public static final String TABLE_NAME_APPS = "autorun_apps";
        
        public static final String COLUMN_NAME_SORT_ORDER = "sort_order";
        public static final String COLUMN_NAME_PACKAGE = "package_name";
    }
    private static final String SQL_DELETE_APPS =
            "DROP TABLE IF EXISTS " + Columns.TABLE_NAME_APPS;

    private static final String SQL_CREATE_APPS =
            "CREATE TABLE " + Columns.TABLE_NAME_APPS + " (" +
                    Columns._ID + " INTEGER PRIMARY KEY," +
                    Columns.COLUMN_NAME_SORT_ORDER + " INTEGER," +
                    Columns.COLUMN_NAME_PACKAGE + TEXT_TYPE + 
                    " )";
    public static class AppEntryReaderDbHelper extends SQLiteOpenHelper {
        // Increment in case you change the database schema
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "SevnAutorun.db";

        public AppEntryReaderDbHelper(final Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(final SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_APPS);
        }

        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            db.execSQL(SQL_DELETE_APPS);
            onCreate(db);
        }

        public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
    public static AppDetail selectAppDetail(final SQLiteOpenHelper mDbHelper,  final String colName, final String title) {
        List<AppDetail> ret = selectAppDetails(mDbHelper, colName, title, 1);
        if (ret.size() > 0) {
            return ret.get(0);
        }
        return null;
    }
    public static List<AppDetail> selectAppDetails(final SQLiteOpenHelper mDbHelper,  final String colName, final String title, int size) {
        ArrayList<AppDetail> ret = null;
        if (size > 0) {
            ret = new ArrayList<>(size);
        } else {
            ret = new ArrayList<>();
        }
        AppDetail pm = null;
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = null;
        try {
            String[] projection = {
                    Columns._ID,
                    Columns.COLUMN_NAME_SORT_ORDER,
                    Columns.COLUMN_NAME_PACKAGE
            };

            String sortOrder = null;

            String selection = null;
            String[] selectionArgs = null;
            
            if (colName != null) {
                selection = colName + " = ? ";
                selectionArgs = new String[]{title};
            }

            c = db.query(
                    Columns.TABLE_NAME_APPS,
                    projection,                               // The columns receiver return
                    selection,                                // The columns for the WHERE clause
                    selectionArgs,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    sortOrder                                 // The sort order
            );
            if (c.moveToFirst()) {
                int i = 0;
                do {
                    i++;
                    pm = new AppDetail();
                    pm.setId(c.getLong(c.getColumnIndex(Columns._ID)));
                    pm.setSortOrder(c.getInt(c.getColumnIndex(Columns.COLUMN_NAME_SORT_ORDER)));
                    pm.setPackageName(c.getString(c.getColumnIndex(Columns.COLUMN_NAME_PACKAGE)));
                    ret.add(pm);
                } while (c.moveToNext() && !(size > 0 && i >= size));
            }
        } finally {
            closeCursor(c);
            closeDB(db);
        }
        return ret;
    }
    private static boolean isEmpty(final String s) {
        if (s == null || s.trim().length() == 0) {
            return true;
        }
        return false;
    }
    
    public static long insertAppDetail(final SQLiteOpenHelper mDbHelper, final AppDetail pm) {
        if (isEmpty(pm.getPackageName())) {
            return 0;
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(Columns.COLUMN_NAME_SORT_ORDER, pm.getSortOrder());
            values.put(Columns.COLUMN_NAME_PACKAGE, pm.getPackageName());

            long newRowId;
            newRowId = db.insert(
                    Columns.TABLE_NAME_APPS,
                    null,
                    values);
            pm.setId(newRowId);
            return newRowId;
        } finally {
            closeDB(db);
        }
    }

    public static int updateAppDetail(SQLiteOpenHelper mDbHelper, AppDetail pm) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(Columns.COLUMN_NAME_SORT_ORDER, pm.getSortOrder());
            return db.update(Columns.TABLE_NAME_APPS, values, Columns._ID + " = ? ", new String[]{""+pm.getId()});
        } finally {
            closeDB(db);
        }
    }
    
    public static int removeAppDetail(SQLiteOpenHelper mDbHelper, AppDetail pm) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            return db.delete(Columns.TABLE_NAME_APPS, Columns._ID + " = ? ", new String[]{""+pm.getId()});
        } finally {
            closeDB(db);
        }
    }
    
    public static int updateOrInsertAppDetail(SQLiteOpenHelper mDbHelper, AppDetail pm) {
        AppDetail pmStored = selectAppDetail(mDbHelper, Columns.COLUMN_NAME_PACKAGE, pm.getPackageName());
        if (pmStored != null) {
            pm.setId(pmStored.getId());
            updateAppDetail(mDbHelper, pm);
        } else {
            if (insertAppDetail(mDbHelper, pm) != 0) {
                return 1;
            }
        }
        return 0;
    }
    
    public static void closeDB(SQLiteDatabase db) {
        if (db != null) {
            try {
                db.close();
            } catch (Exception e) {}
        }
    }
    public static void closeCursor(Cursor c) {
        if ( c != null ) {
            try {
                c.close();
            } catch (Exception e) {}
        }
    }    
}
