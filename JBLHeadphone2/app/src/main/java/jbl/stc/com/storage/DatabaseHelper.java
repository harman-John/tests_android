package jbl.stc.com.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import java.util.ArrayList;

import jbl.stc.com.constant.EqDbKey;
import jbl.stc.com.entity.GraphicEQPreset;


/**
 * DatabaseHelper
 * Created by darren.lu on 08/09/2017.
 */
public class DatabaseHelper extends SQLiteOpenHelper implements EqDbKey {
    private final static String DATABASE_NAME = "AKG.db";
    private final static int version = 1;
    private final static String TAG = DatabaseHelper.class.getName();

    private String CREATE_TABLE = "create table " + AKG_EQ + " (" +
            ID + " INTEGER ," +
            INDEX + " INTEGER ," +
            POINT_X + " TEXT ," +
            POINT_Y + " TEXT ," +
            EQ_NAME + " TEXT PRIMARY KEY ," +
            EQ_TYPE + " INTEGER ," +
            VALUE_32 + " TEXT ," +
            VALUE_64 + " TEXT ," +
            VALUE_125 + " TEXT ," +
            VALUE_250 + " TEXT ," +
            VALUE_500 + " TEXT ," +
            VALUE_1000 + " TEXT ," +
            VALUE_2000 + " TEXT ," +
            VALUE_4000 + " TEXT ," +
            VALUE_8000 + " TEXT ," +
            VALUE_16000 + " TEXT, " +
            DEVICE_NAME + " TEXT " +
            ");";

    public static ArrayList<String> presetEqNames = new ArrayList<>();

    static {
        presetEqNames.add(GraphicEQPreset.Off.name());
        presetEqNames.add(GraphicEQPreset.Vocal.name());
        presetEqNames.add(GraphicEQPreset.Bass.name());
        presetEqNames.add(GraphicEQPreset.Jazz.name());
    }

    /**
     * <p>Constructor</p>
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTableEqSettings(db);
    }

    /**
     * <p>Creates table named AKG_EQ </p>
     */
    private void createTableEqSettings(SQLiteDatabase db) {
        Log.d(TAG, "createTableEqSettings");
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade newVersion=" + newVersion + ",oldVersion=" + oldVersion);
        if (oldVersion < newVersion) {
            String deleteSql = "drop table " + AKG_EQ;
            db.execSQL(deleteSql);
            onCreate(db);
        }

        /*if (oldVersion == 4) {
            String rename_sql = "alter table " + AKG_EQ + " rename to  AKG_EQ_Temp";
            db.execSQL(rename_sql);

            String sql_message = "create table " + AKG_EQ + "(_id int,_index int,POINT_X Text,POINT_Y Text,EQ_NAME Text primary key," +
                    "EQ_TYPE int,VALUE_32 Text,VALUE_64 Text,VALUE_125 Text,VALUE_250 Text,VALUE_500 Text,VALUE_1000 Text," +
                    "VALUE_2000 Text,VALUE_4000 Text,VALUE_8000 Text,VALUE_16000 Text)";
            db.execSQL(sql_message);

            String sql_copy = "insert into AKG_EQ select _id,_index,POINT_X,POINT_Y,EQ_NAME,EQ_TYPE,VALUE_32,VALUE_64,VALUE_125," +
                    "VALUE_250,VALUE_500,VALUE_1000,VALUE_2000,VALUE_4000,VALUE_8000,VALUE_16000 from AKG_EQ_Temp";
            db.execSQL(sql_copy);

            String alter_sql = "alter table " + AKG_EQ + " add column DEVICE_NAME Text";
            db.execSQL(alter_sql);

            String drop_sql = "drop table if exists AKG_EQ_Temp";
            db.execSQL(drop_sql);

        } else if (oldVersion < 4) {
            String deleteSql = "drop table " + AKG_EQ;
            db.execSQL(deleteSql);
            onCreate(db);
        }*/

    }
}
