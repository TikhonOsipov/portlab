package com.tixon.portlab.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MyDataHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = "myLogs";

    private static final String UID = "_id";
    private static final String TABLE_VARIABLES_NAME = "table_variables";
    private static final String TABLE_FUNCTIONS_NAME = "table_functions";
    private static final String TABLE_ARRAYS_NAME = "table_arrays";
    private static final String TABLE_HISTORY_NAME = "table_history";
    private static final String TABLE_EXPRESSIONS_NAME = "table_expressions";

    private static final String FIELD_VARIABLE_NAME = "variable_name";
    private static final String FIELD_VARIABLE_VALUE = "variable_value";
    private static final String FIELD_FUNCTION_NAME = "function_name";
    private static final String FIELD_FUNCTION_ARGUMENTS = "function_arguments";
    private static final String FIELD_FUNCTION_EXPRESSION = "function_expression";
    private static final String FIELD_ARRAY_NAME = "array_name";
    private static final String FIELD_ARRAY_VALUES = "array_values";
    private static final String FIELD_HISTORY_ITEM = "history_item";
    private static final String FIELD_EXPRESSIONS_ITEM = "expressions_item";

    private static final String DATABASE_NAME = "port_lab_database";
    private static final int DATABASE_VERSION = 4;//last stable version: 3

    private static final String SQL_CREATE_TABLE_VARIABLES = "create table " + TABLE_VARIABLES_NAME + " (" +
            UID + " integer primary key autoincrement, " + FIELD_VARIABLE_NAME + " text, " + FIELD_VARIABLE_VALUE + " real" + ");";
    private static final String SQL_CREATE_TABLE_FUNCTIONS = "create table " + TABLE_FUNCTIONS_NAME + " (" +
            UID + " integer primary key autoincrement, " + FIELD_FUNCTION_NAME + " text, " + FIELD_FUNCTION_ARGUMENTS + " text, " +
            FIELD_FUNCTION_EXPRESSION + " text" + ");";
    private static final String SQL_CREATE_TABLE_ARRAYS = "create table " + TABLE_ARRAYS_NAME + " (" +
            UID + " integer primary key autoincrement, " + FIELD_ARRAY_NAME + " text, " + FIELD_ARRAY_VALUES + " text" + ");";
    private static final String SQL_CREATE_TABLE_HISTORY = "create table " + TABLE_HISTORY_NAME + " (" +
            UID + " integer primary key autoincrement, " + FIELD_HISTORY_ITEM + " text" + ");";
    private static final String SQL_CREATE_TABLE_EXPRESSIONS = "create table " + TABLE_EXPRESSIONS_NAME + " (" +
            UID + " integer primary key autoincrement, " + FIELD_EXPRESSIONS_ITEM + " text" + ");";
    private static final String SQL_DELETE_VARIABLES = "DROP TABLE IF EXISTS "
            + TABLE_VARIABLES_NAME;
    private static final String SQL_DELETE_FUNCTIONS = "DROP TABLE IF EXISTS "
            + TABLE_FUNCTIONS_NAME;
    private static final String SQL_DELETE_ARRAYS = "DROP TABLE IF EXISTS "
            + TABLE_ARRAYS_NAME;
    private static final String SQL_DELETE_HISTORY = "DROP TABLE IF EXISTS "
            + TABLE_HISTORY_NAME;
    private static final String SQL_DELETE_EXPRESSIONS = "DROP TABLE IF EXISTS "
            + TABLE_EXPRESSIONS_NAME;

    public MyDataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "--- onCreate database ---");
        db.execSQL(SQL_CREATE_TABLE_VARIABLES);
        db.execSQL(SQL_CREATE_TABLE_FUNCTIONS);
        db.execSQL(SQL_CREATE_TABLE_HISTORY);
        db.execSQL(SQL_CREATE_TABLE_EXPRESSIONS);
        db.execSQL(SQL_CREATE_TABLE_ARRAYS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "--- onUpgrade database: old v. = " + oldVersion + ", new v. = " + newVersion + " ---");

        ArrayList<String> historyItems = new ArrayList<>();
        ArrayList<String> expressionItems = new ArrayList<>();
        HashMap<String, Double> variables = new HashMap<>();
        HashMap<String, FunctionsDefinitions> functions = new HashMap<>();

        readHistory(db, historyItems, expressionItems);
        readVariables(db, variables);
        readFunctions(db, functions);

        historyItems = reverse(historyItems);
        expressionItems = reverse(expressionItems);

        db.execSQL(SQL_DELETE_VARIABLES);
        db.execSQL(SQL_DELETE_FUNCTIONS);
        db.execSQL(SQL_DELETE_HISTORY);
        db.execSQL(SQL_DELETE_EXPRESSIONS);

        onCreate(db);

        for(int i = 0; i < historyItems.size(); i++) {
            insertHistory(db, historyItems.get(i), expressionItems.get(i));
        }
        insertVariables(db, variables);
        insertFunctions(db, functions);

        Log.d(LOG_TAG, "The database has been upgraded successfully");
    }

    public void insertVariables(SQLiteDatabase db, HashMap<String, Double> varMap) {
        Log.d(LOG_TAG, "--- Insert in '" + TABLE_VARIABLES_NAME + "': ---");
        ContentValues cv = new ContentValues();
        String varName;
        double varValue;
        for(Map.Entry entry : varMap.entrySet()) {
            varName = entry.getKey().toString();
            varValue = Double.parseDouble(entry.getValue().toString());
            cv.put(FIELD_VARIABLE_NAME, varName);
            cv.put(FIELD_VARIABLE_VALUE, varValue);
            long rowID = db.insert(TABLE_VARIABLES_NAME, null, cv);
            Log.d(LOG_TAG, TABLE_VARIABLES_NAME + ": row inserted, ID = " + rowID);
        }
    }

    public void readVariables(SQLiteDatabase db, HashMap<String, Double> varMap) {
        Log.d(LOG_TAG, "--- Read from '" + TABLE_VARIABLES_NAME + "': ---");
        Cursor c = db.query(TABLE_VARIABLES_NAME, null, null, null, null, null, null);
        if(c.moveToFirst()) {
            int idColIndex = c.getColumnIndex(UID);
            int varNameColIndex = c.getColumnIndex(FIELD_VARIABLE_NAME);
            int varValueColIndex = c.getColumnIndex(FIELD_VARIABLE_VALUE);

            do {
                Log.d(LOG_TAG, UID + " = " + c.getInt(idColIndex) + ", " + FIELD_VARIABLE_NAME + " = " + c.getString(varNameColIndex) + ", " + FIELD_VARIABLE_VALUE + " = " + c.getDouble(varValueColIndex));
                varMap.put(c.getString(varNameColIndex), c.getDouble(varValueColIndex));
            } while(c.moveToNext());
        } else
            Log.d(LOG_TAG, "0 rows");
        c.close();
    }

    public int findVariableByName(SQLiteDatabase db, String name, HashMap<String, Double> varMap) {
        int id_result = 0;
        Log.d(LOG_TAG, "--- Read from '" + TABLE_VARIABLES_NAME + "': ---");
        Cursor c = db.query(TABLE_VARIABLES_NAME, null, null, null, null, null, null);
        if(c.moveToFirst()) {
            int idColIndex = c.getColumnIndex(UID);
            int varNameColIndex = c.getColumnIndex(FIELD_VARIABLE_NAME);
            int varValueColIndex = c.getColumnIndex(FIELD_VARIABLE_VALUE);

            do {
                int id = c.getInt(idColIndex);
                String varName = c.getString(varNameColIndex);
                if(varName.equals(name)) {
                    Log.d(LOG_TAG, UID + " = " + id + ", " + FIELD_VARIABLE_NAME + " = " + c.getString(varNameColIndex) + ", " + FIELD_VARIABLE_VALUE + " = " + c.getDouble(varValueColIndex));
                    if(varMap != null) varMap.put(c.getString(varNameColIndex), c.getDouble(varValueColIndex));
                    id_result = id;
                    break;
                }
            } while(c.moveToNext());
        } else
            Log.d(LOG_TAG, "0 rows");
        c.close();
        return id_result;
    }

    public void deleteVariables(SQLiteDatabase db) {
        int clearCount = db.delete(TABLE_VARIABLES_NAME, null, null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + TABLE_VARIABLES_NAME + "'"); //Resetting ID
        Log.d(LOG_TAG, "Deleted from '" + TABLE_VARIABLES_NAME + "': " + clearCount);
    }

    public void insertFunctions(SQLiteDatabase db, HashMap<String, FunctionsDefinitions> funcMap) {
        Log.d(LOG_TAG, "--- Insert in '" + TABLE_FUNCTIONS_NAME + "': ---");
        ContentValues cv = new ContentValues();
        String funcName, funcExpr;
        for(Map.Entry entry : funcMap.entrySet()) {
            FunctionsDefinitions FD; //= new FunctionsDefinitions();
            StringBuilder funcArgs = new StringBuilder();
            funcName = entry.getKey().toString();
            FD = (FunctionsDefinitions) entry.getValue();
            funcExpr = FD.expression;
            //Making arguments string from arrayList
            if(FD.arguments.size() > 1) {
                for(int i = 0; i < FD.arguments.size(); i++) {
                    funcArgs.append(FD.arguments.get(i));
                    funcArgs.append(",");
                }
                funcArgs.deleteCharAt(funcArgs.length()-1);
            } else {
                funcArgs.append(FD.arguments.get(0));
            }
            //Putting into contentValues
            cv.put(FIELD_FUNCTION_NAME, funcName);
            cv.put(FIELD_FUNCTION_ARGUMENTS, funcArgs.toString());
            cv.put(FIELD_FUNCTION_EXPRESSION, funcExpr);
            long rowID = db.insert(TABLE_FUNCTIONS_NAME, null, cv);
            Log.d(LOG_TAG, TABLE_FUNCTIONS_NAME + ": row inserted, ID = " + rowID);
        }
    }

    public void readFunctions(SQLiteDatabase db, HashMap<String, FunctionsDefinitions> funcMap) {
        Log.d(LOG_TAG, "--- Read from '" + TABLE_FUNCTIONS_NAME + "': ---");
        Cursor c = db.query(TABLE_FUNCTIONS_NAME, null, null, null, null, null, null);
        if(c.moveToFirst()) {
            int idColIndex = c.getColumnIndex(UID);
            int funcNameColIndex = c.getColumnIndex(FIELD_FUNCTION_NAME);
            int funcArgsColIndex = c.getColumnIndex(FIELD_FUNCTION_ARGUMENTS);
            int funcExprColIndex = c.getColumnIndex(FIELD_FUNCTION_EXPRESSION);

            do {
                FunctionsDefinitions FD = new FunctionsDefinitions();
                FD.arguments = new ArrayList<String>();
                int id = c.getInt(idColIndex);
                String funcName = c.getString(funcNameColIndex);
                String funcArgs = c.getString(funcArgsColIndex);
                String funcExpr = c.getString(funcExprColIndex);
                Log.d(LOG_TAG, UID + " = " + id + ", " + FIELD_FUNCTION_NAME + " = " + funcName + ", " + FIELD_FUNCTION_ARGUMENTS + " = " + funcArgs + ", " + FIELD_FUNCTION_EXPRESSION + " = " + funcExpr);
                String[] args_mas = funcArgs.split(",");
                FD.expression = funcExpr;//
                Collections.addAll(FD.arguments, args_mas);
                funcMap.put(funcName, FD);
            } while(c.moveToNext());
        } else Log.d(LOG_TAG, "0 rows in " + TABLE_FUNCTIONS_NAME);
        c.close();
    }

    public int findFunctionByName(SQLiteDatabase db, String name, HashMap<String, FunctionsDefinitions> funcMap) {
        int id_result = 0;
        Log.d(LOG_TAG, "--- Read from '" + TABLE_FUNCTIONS_NAME + "': ---");
        Cursor c = db.query(TABLE_FUNCTIONS_NAME, null, null, null, null, null, null);
        if(c.moveToFirst()) {
            int idColIndex = c.getColumnIndex(UID);
            int funcNameColIndex = c.getColumnIndex(FIELD_FUNCTION_NAME);
            int funcArgsColIndex = c.getColumnIndex(FIELD_FUNCTION_ARGUMENTS);
            int funcExprColIndex = c.getColumnIndex(FIELD_FUNCTION_EXPRESSION);

            do {
                int id = c.getInt(idColIndex);
                String funcName = c.getString(funcNameColIndex);
                if(funcName.equals(name)) {
                    id_result = id;
                    FunctionsDefinitions FD = new FunctionsDefinitions();
                    FD.arguments = new ArrayList<String>();
                    String funcArgs = c.getString(funcArgsColIndex);
                    String funcExpr = c.getString(funcExprColIndex);
                    Log.d(LOG_TAG, UID + " = " + id + ", " + FIELD_FUNCTION_NAME + " = " + funcName + ", " + FIELD_FUNCTION_ARGUMENTS + " = " + funcArgs + ", " + FIELD_FUNCTION_EXPRESSION + " = " + funcExpr);
                    String[] args_mas = funcArgs.split(",");
                    FD.expression = funcExpr;//
                    Collections.addAll(FD.arguments, args_mas);
                    if(funcMap != null) funcMap.put(funcName, FD);
                }
            } while(c.moveToNext());
        } else Log.d(LOG_TAG, "0 rows in " + TABLE_FUNCTIONS_NAME);
        c.close();
        return id_result;
    }

    public void deleteFunctions(SQLiteDatabase db) {
        int clearCount = db.delete(TABLE_FUNCTIONS_NAME, null, null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + TABLE_FUNCTIONS_NAME + "'"); //Resetting ID
        Log.d(LOG_TAG, "Deleted from '" + TABLE_FUNCTIONS_NAME + "': " + clearCount);
    }

    public void insertArrays(SQLiteDatabase db, ArrayList<Array> arrays) {
        Log.d(LOG_TAG, "--- Insert in '" + TABLE_ARRAYS_NAME + "': ---");
        ContentValues cv = new ContentValues();
        String arrayName;
        String arrayValues;
        for(int a = 0; a < arrays.size(); a++) {
            arrayName = arrays.get(a).getName();
            arrayValues = makeStringFromArray(arrays.get(a).getValues());
            cv.put(FIELD_ARRAY_NAME, arrayName);
            cv.put(FIELD_ARRAY_VALUES, arrayValues);
            long rowID = db.insert(TABLE_ARRAYS_NAME, null, cv);
            Log.d(LOG_TAG, TABLE_ARRAYS_NAME + ": row inserted, ID = " + rowID);
        }
    }

    public void readArrays(SQLiteDatabase db, ArrayList<Array> arrays) {
        Log.d(LOG_TAG, "--- Read from '" + TABLE_ARRAYS_NAME + "': ---");
        Cursor c = db.query(TABLE_ARRAYS_NAME, null, null, null, null, null, null);
        ArrayList<Double> tempArrayValues;
        Array array;
        if(c.moveToFirst()) {
            int idColIndex = c.getColumnIndex(UID);
            int arrayNameColIndex = c.getColumnIndex(FIELD_ARRAY_NAME);
            int arrayValuesColIndex = c.getColumnIndex(FIELD_ARRAY_VALUES);

            do {
                Log.d(LOG_TAG, UID + " = " + c.getInt(idColIndex) + ", " + FIELD_ARRAY_NAME + " = " + c.getString(arrayNameColIndex) + ", " + FIELD_ARRAY_VALUES + " = " + c.getString(arrayValuesColIndex));
                array = new Array();
                tempArrayValues = new ArrayList<>();
                String[] values = c.getString(arrayValuesColIndex).split(";");
                StringMasToDoubleList(values, tempArrayValues);
                array.setName(c.getString(arrayNameColIndex));
                array.setValues(tempArrayValues);
                arrays.add(array);
            } while(c.moveToNext());
        } else {
            Log.d(LOG_TAG, "0 rows");
        }
        c.close();
    }

    public int findArrayByName(SQLiteDatabase db, String name, Array array) {
        int id_result = 0;
        Log.d(LOG_TAG, "--- Read from '" + TABLE_ARRAYS_NAME + "': ---");
        Cursor c = db.query(TABLE_ARRAYS_NAME, null, null, null, null, null, null);
        if(c.moveToFirst()) {
            int idColIndex = c.getColumnIndex(UID);
            int arrayNameColIndex = c.getColumnIndex(FIELD_ARRAY_NAME);
            int arrayValuesColIndex = c.getColumnIndex(FIELD_ARRAY_VALUES);

            do {
                int id = c.getInt(idColIndex);
                String arrayName = c.getString(arrayNameColIndex);
                if(arrayName.equals(name)) {
                    Log.d(LOG_TAG, UID + " = " + c.getInt(idColIndex) + ", " + FIELD_ARRAY_NAME + " = " + c.getString(arrayNameColIndex) + ", " + FIELD_ARRAY_VALUES + " = " + c.getString(arrayValuesColIndex));
                    id_result = id;
                    if(array != null) {
                        array.setName(c.getString(arrayNameColIndex));
                        array.setValuesString(c.getString(arrayValuesColIndex));
                    }
                    break;
                }
            } while(c.moveToNext());
        } else
            Log.d(LOG_TAG, "0 rows");
        c.close();
        return id_result;
    }

    public void deleteArrays(SQLiteDatabase db) {
        int clearCount = db.delete(TABLE_ARRAYS_NAME, null, null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + TABLE_ARRAYS_NAME + "'"); //Resetting ID
        Log.d(LOG_TAG, "Deleted from '" + TABLE_ARRAYS_NAME + "': " + clearCount);
    }

    public void deleteHistoryAndExpressions(SQLiteDatabase db) {
        int clearCount = db.delete(TABLE_HISTORY_NAME, null, null);
        int clearExprCount = db.delete(TABLE_EXPRESSIONS_NAME, null, null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + TABLE_HISTORY_NAME + "'");
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + TABLE_EXPRESSIONS_NAME + "'");
        Log.d(LOG_TAG, "Deleted from '" + TABLE_HISTORY_NAME + "': " + clearCount);
        Log.d(LOG_TAG, "Deleted from '" + TABLE_EXPRESSIONS_NAME + "': " + clearExprCount);
    }

    public void insertHistory(SQLiteDatabase db, String history_item, String expressions_item) {
        ContentValues cv_history = new ContentValues();
        ContentValues cv_expressions = new ContentValues();
        cv_history.put(FIELD_HISTORY_ITEM, history_item);
        cv_expressions.put(FIELD_EXPRESSIONS_ITEM, expressions_item);
        long history_rowID = db.insert(TABLE_HISTORY_NAME, null, cv_history);
        long expressions_rowID = db.insert(TABLE_EXPRESSIONS_NAME, null, cv_expressions);
        Log.d(LOG_TAG, "row inserted in " + TABLE_HISTORY_NAME + ": ID = " + history_rowID);
        Log.d(LOG_TAG, "row inserted in " + TABLE_EXPRESSIONS_NAME + ": ID = " + expressions_rowID);
    }

    public void readHistory(SQLiteDatabase db, ArrayList<String> history_list, ArrayList<String> expressions_list) {
        history_list.clear();
        expressions_list.clear();
        Cursor c_history = db.query(TABLE_HISTORY_NAME, null, null, null, null, null, null);
        Cursor c_expressions = db.query(TABLE_EXPRESSIONS_NAME, null, null, null, null, null, null);
        //for history:
        if(c_history.moveToLast()) {
            int idCI = c_history.getColumnIndex(UID);
            int historyItemCI = c_history.getColumnIndex(FIELD_HISTORY_ITEM);
            do {
                Log.d(LOG_TAG, "reading history: ID = " + c_history.getInt(idCI) + "; history_item = " + c_history.getString(historyItemCI));
                history_list.add(c_history.getString(historyItemCI));
            } while(c_history.moveToPrevious());
        } else Log.d(LOG_TAG, "0 rows in " + TABLE_HISTORY_NAME);
        //for expressions:
        if(c_expressions.moveToLast()) {
            int idCI = c_expressions.getColumnIndex(UID);
            int expressionsItemCI = c_expressions.getColumnIndex(FIELD_EXPRESSIONS_ITEM);
            do {
                Log.d(LOG_TAG, "reading expressions: ID = " + c_expressions.getInt(idCI) + "; expressions_item = " + c_expressions.getString(expressionsItemCI));
                expressions_list.add(c_expressions.getString(expressionsItemCI));
            } while(c_expressions.moveToPrevious());
        } else Log.d(LOG_TAG, "0 rows in " + TABLE_EXPRESSIONS_NAME);
    }

    public String findHistoryByID(SQLiteDatabase db, int needed_id) {
        String result = "";
        Log.d(LOG_TAG, "--- Read from '" + TABLE_HISTORY_NAME + "', find history by ID: ---");
        Cursor c = db.query(TABLE_HISTORY_NAME, null, null, null, null, null, null);
        if(c.moveToFirst()) {
            int idColIndex = c.getColumnIndex(UID);
            int historyItemColIndex = c.getColumnIndex(FIELD_HISTORY_ITEM);

            do {
                int id = c.getInt(idColIndex);
                if(id == needed_id) {
                    Log.d(LOG_TAG, "IN DATABASE: " + UID + " = " + id + ", " + FIELD_HISTORY_ITEM + " = " + c.getString(historyItemColIndex));
                    result = c.getString(historyItemColIndex);
                    break;
                }
            } while(c.moveToNext());
        }
        c.close();
        return result;
    }

    public int findHistoryByName(SQLiteDatabase db, String item) {
        int id_result = 0;
        Log.d(LOG_TAG, "--- Read from '" + TABLE_HISTORY_NAME + "': ---");
        Cursor c = db.query(TABLE_HISTORY_NAME, null, null, null, null, null, null);
        if(c.moveToFirst()) {
            int idColIndex = c.getColumnIndex(UID);
            int historyItemColIndex = c.getColumnIndex(FIELD_HISTORY_ITEM);

            do {
                int id = c.getInt(idColIndex);
                String historyItem = c.getString(historyItemColIndex);
                if(historyItem.equals(item)) {
                    Log.d(LOG_TAG, UID + " = " + c.getInt(idColIndex) + ", " + FIELD_HISTORY_ITEM + " = " + c.getString(historyItemColIndex));
                    id_result = id;
                    break;
                }
            } while(c.moveToNext());
        } else
            Log.d(LOG_TAG, "0 rows");
        c.close();
        return id_result;
    }

    public void updateVariables(SQLiteDatabase db, int id, String name, Double value) {
        ContentValues cv = new ContentValues();
        cv.put(FIELD_VARIABLE_NAME, name);
        cv.put(FIELD_VARIABLE_VALUE, value);
        int updCount = db.update(TABLE_VARIABLES_NAME, cv, UID + " = ?", new String[] {String.valueOf(id)});
        Log.d(LOG_TAG, "UPDATE_VARiABLES: updated rows count = " + updCount);
    }

    public void updateFunctions(SQLiteDatabase db, int id, String name, String args, String expr) {
        ContentValues cv = new ContentValues();
        cv.put(FIELD_FUNCTION_NAME, name);
        cv.put(FIELD_FUNCTION_ARGUMENTS, args);
        cv.put(FIELD_FUNCTION_EXPRESSION, expr);
        int updCount = db.update(TABLE_FUNCTIONS_NAME, cv, UID + " = ?", new String[] {String.valueOf(id)});
        Log.d(LOG_TAG, "UPDATE_FUNCTIONS: updated rows count = " + updCount);
    }

    public void updateArrays(SQLiteDatabase db, int id, String name, String values) {
        ContentValues cv = new ContentValues();
        cv.put(FIELD_ARRAY_NAME, name);
        cv.put(FIELD_ARRAY_VALUES, values);
        int updCount = db.update(TABLE_ARRAYS_NAME, cv, UID + " = ?", new String[] {String.valueOf(id)});
        Log.d(LOG_TAG, "UPDATE_ARRAYS: updated rows count = " + updCount);
    }

    public void deleteVariable(SQLiteDatabase db, int id) {
        int delCount;
        delCount = db.delete(TABLE_VARIABLES_NAME, UID + " = " + id, null);
        /*try {
            //delCount = db.delete(TABLE_VARIABLES_NAME, "id = " + id, null);
        } catch(Exception e) {
            //
        }*/
        Log.d("myLogs", "deleteVariable: id = " + id + "; count = " + delCount);
    }

    public void deleteFunction(SQLiteDatabase db, int id) {
        int delCount;
        delCount = db.delete(TABLE_FUNCTIONS_NAME, UID + " = " + id, null);
        /*try {
            //delCount = db.delete(TABLE_FUNCTIONS_NAME, "id = " + id, null);
        } catch (Exception e) {
            //
        }*/
        Log.d("myLogs", "deleteFunction: id = " + id + "; count = " + delCount);
    }

    public void deleteArray(SQLiteDatabase db, int id) {
        int delCount;
        delCount = db.delete(TABLE_ARRAYS_NAME, UID + " = " + id, null);
        /*try {
            //delCount = db.delete(TABLE_ARRAYS_NAME, "id = " + id, null);
        } catch (Exception e) {
            //
        }*/
        Log.d("myLogs", "deleteArray: id = " + id + "; count = " + delCount);
    }

    public void deleteHistoryItem(SQLiteDatabase db, int id) {
        int delCount;
        delCount = db.delete(TABLE_HISTORY_NAME, UID + " = " + id, null);
        Log.d("myLogs", "deleteHistory: id = " + id + "; count = " + delCount);
    }

    private ArrayList<String> reverse(ArrayList<String> list) {
        ArrayList<String> result = new ArrayList<>();
        for(int i = 0; i < list.size(); i++) {
            result.add(0, list.get(i));
        }
        return result;
    }

    private String makeStringFromArray(ArrayList<Double> L) {
        String result = "";
        for(int i = 0; i < L.size()-1; i++) result += String.valueOf(L.get(i)) + ";";
        result += String.valueOf(L.get(L.size()-1));
        return result;
    }

    public void StringMasToDoubleList(String[] mas, ArrayList<Double> F) {
        for(int i = 0; i < mas.length; i++) {
            F.add(Double.parseDouble(mas[i]));
        }
    }
}
