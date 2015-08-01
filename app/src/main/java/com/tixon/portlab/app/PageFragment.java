package com.tixon.portlab.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PageFragment extends Fragment {
    Parser parser;
    MyDataHelper helper;
    private int idVariable, idFunction, idArray;
    private ArrayAdapter adapter1, adapter2, adapter3;
    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    ActionMode actionMode_deleteVariables, actionMode_deleteFunctions;

    int pageNumber;
    ArrayList<String> list_variables, list_functions, list_arrays;

    static PageFragment newInstance(int page) {
        PageFragment pageFragment = new PageFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
    }

    void createListOfVariables(ArrayList<String> variablesList, HashMap<String, Double> map) {
        String varName, varValue;
        for (Map.Entry entry : map.entrySet()) {
            varName = entry.getKey().toString();
            varValue = entry.getValue().toString();
            if(!(varName.equals("pi") | varName.equals("e"))) {
                variablesList.add(varName + " = " + varValue);
            }
        }
    }

    void createListOfFunctions(ArrayList<String> functionsList, HashMap<String, FunctionsDefinitions> funcMap) {
        String funcName, funcExpr, funcArgs;
        ArrayList<String> args;
        FunctionsDefinitions FD;
        for(Map.Entry entry : funcMap.entrySet()) {
            funcName = entry.getKey().toString();
            FD = new FunctionsDefinitions();
            FD.arguments = new ArrayList<String>();
            FD = (FunctionsDefinitions) entry.getValue();
            args = FD.arguments;
            funcExpr = FD.expression;
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg);
                sb.append(", ");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
            funcArgs = sb.toString();
            functionsList.add(funcName + "(" + funcArgs + ") = " + funcExpr);
        }
    }

    void createListOfArrays(ArrayList<String> arrayList, ArrayList<Array> arrays) {
        String arrayName, arrayValuesString;
        ArrayList<Double> arrayValues;
        Array array;
        for(int a = 0; a < arrays.size(); a++) {
            array = arrays.get(a);
            arrayName = array.getName();
            arrayValues = array.getValues();
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < arrayValues.size(); i++) {
                sb.append(arrayValues.get(i));
                sb.append("; ");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
            arrayValuesString = sb.toString();
            arrayList.add(arrayName + " = {" + arrayValuesString + "}");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = null;
        //final MyDataHelper helper1, helper2;
        parser = new Parser();
        switch(pageNumber) {
            //variables
            case 0:
                try {
                    view = inflater.inflate(R.layout.fragment_variables, null);
                    final ListView lv1 = (ListView) view.findViewById(R.id.listViewVariables);
                    TextView tv_variables = (TextView) view.findViewById(R.id.tv_variables);
                    list_variables = new ArrayList<String>();
                    //ToDo
                    helper = new MyDataHelper(getActivity().getApplicationContext());
                    final SQLiteDatabase db = helper.getWritableDatabase();
                    helper.readVariables(db, parser.vars);
                    createListOfVariables(list_variables, parser.vars);
                    if(list_variables.isEmpty()) tv_variables.setVisibility(View.VISIBLE);
                    else tv_variables.setVisibility(View.GONE);
                    adapter1 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_activated_1, list_variables);
                    lv1.setAdapter(adapter1);
                    lv1.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                    //lv1.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
                    if(Build.VERSION.SDK_INT < 21) {
                        lv1.setOverScrollMode(View.OVER_SCROLL_NEVER);
                    } else {
                        lv1.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
                    }
                    adapter1.notifyDataSetChanged();

                    lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Log.d("myLogs", "LV1 | position = " + position);
                            showVariablesDialog(savedInstanceState, list_variables.get(position));
                        }
                    });
                    break;
                } catch(Exception e) {
                    Log.d("myLogs", e.toString());
                }
                //functions
            case 1:
                try {
                    view = inflater.inflate(R.layout.fragment_functions, null);
                    ListView lv2 = (ListView) view.findViewById(R.id.listViewFunctions);
                    TextView tv_functions = (TextView) view.findViewById(R.id.tv_functions);
                    list_functions = new ArrayList<String>();
                    helper = new MyDataHelper(getActivity().getApplicationContext());
                    SQLiteDatabase db = helper.getWritableDatabase();
                    helper.readFunctions(db, parser.functions);
                    createListOfFunctions(list_functions, parser.functions);//get functions' data
                    if(list_functions.isEmpty()) tv_functions.setVisibility(View.VISIBLE);
                    else tv_functions.setVisibility(View.GONE);
                    adapter2 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_activated_1, list_functions);
                    lv2.setAdapter(adapter2);
                    //lv2.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                    if(Build.VERSION.SDK_INT < 21) {
                        lv2.setOverScrollMode(View.OVER_SCROLL_NEVER);
                    } else {
                        lv2.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
                    }
                    adapter2.notifyDataSetChanged();

                    lv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Log.d("myLogs", "LV2 | position = " + position);
                            showFunctionsDialog(savedInstanceState, list_functions.get(position));
                        }
                    });
                    break;
                } catch(Exception e) {
                    Log.d("myLogs", e.toString());
                }
            case 2:
                //arrays
                try {
                    view = inflater.inflate(R.layout.fragment_arrays, null);
                    ListView lv3 = (ListView) view.findViewById(R.id.listViewArrays);
                    TextView tv_arrays = (TextView) view.findViewById(R.id.tv_arrays);
                    list_arrays = new ArrayList<>();
                    helper = new MyDataHelper(getActivity().getApplicationContext());
                    SQLiteDatabase db = helper.getWritableDatabase();
                    helper.readArrays(db, parser.arrays);
                    createListOfArrays(list_arrays, parser.arrays);
                    if(list_arrays.isEmpty()) tv_arrays.setVisibility(View.VISIBLE);
                    else tv_arrays.setVisibility(View.GONE);
                    adapter3 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_activated_1, list_arrays);
                    lv3.setAdapter(adapter3);
                    //lv3.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                    if(Build.VERSION.SDK_INT < 21) {
                        lv3.setOverScrollMode(View.OVER_SCROLL_NEVER);
                    } else {
                        lv3.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
                    }
                    adapter3.notifyDataSetChanged();

                    lv3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Log.d("myLogs", "LV3 | position = " + position);
                            showArrayDialog(savedInstanceState, list_arrays.get(position));
                        }
                    });
                    break;
                } catch (Exception e) {
                    Log.d("myLogs", e.toString());
                }
        }
        return view;
    }

    void ConvertListToMap_variables(ArrayList<String> list, HashMap<String, Double> map) {
        String list_item = "";
        StringBuilder name, value;
        for(int i = 0; i < list.size(); i++) {
            list_item = list.get(i);
            name = new StringBuilder();
            value = new StringBuilder();
            name.append(deleteSpaces(list_item.substring(0, list_item.indexOf("="))));
            value.append(deleteSpaces(list_item.substring(list_item.indexOf("=") + 1)));
            map.put(name.toString(), Double.parseDouble(value.toString()));
        }
    }

    void showVariablesDialog(Bundle bundle, String listItem) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_launcher)
                .setTitle(getResources().getString(R.string.dialog_variables_title));
        View linearLayout = getLayoutInflater(bundle).inflate(R.layout.dialog_variables, null);
        builder.setView(linearLayout);

        final EditText ET_name = (EditText) linearLayout.findViewById(R.id.ET_V_name);
        final EditText ET_value = (EditText) linearLayout.findViewById(R.id.ET_V_value);

        String s = deleteSpaces(listItem);
        String varName = s.substring(0, s.indexOf("="));
        HashMap<String, Double> variable = new HashMap<String, Double>();
        helper = new MyDataHelper(getActivity().getApplicationContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        idVariable = helper.findVariableByName(db, varName, variable);
        for(Map.Entry entry : variable.entrySet()) {
            ET_name.setText(entry.getKey().toString());
            ET_name.setSelection(ET_name.getText().length());
            ET_value.setText(entry.getValue().toString());
        }
        Log.d("myLogs", "id = " + idVariable);
        builder.setNegativeButton(getResources().getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        })
                .setPositiveButton(getResources().getString(R.string.dialog_button_OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = deleteSpaces(ET_name.getText().toString());
                        String value = deleteSpaces(ET_value.getText().toString());
                        helper = new MyDataHelper(getActivity().getApplicationContext());
                        SQLiteDatabase db = helper.getWritableDatabase();
                        if(!(name.equals("") || value.equals(""))) {
                            helper.updateVariables(db, idVariable, name, Double.parseDouble(value));
                            helper.deleteArray(db, helper.findArrayByName(db, name, null));
                            parser.vars.clear();
                            parser = new Parser();
                            helper.readVariables(db, parser.vars);
                            list_variables.clear();
                            createListOfVariables(list_variables, parser.vars);
                            adapter1.notifyDataSetChanged();
                            helper.insertHistory(db, getResources().getString(R.string.variable_changed) + " " +  name + " = " + value, name);
                        }
                    }
                })
                .create()
                .show();
    }

    void showFunctionsDialog(Bundle bundle, String listItem) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_launcher)
                .setTitle(getResources().getString(R.string.dialog_functions_title));
        View linearLayout = getLayoutInflater(bundle).inflate(R.layout.dialog_functions, null);
        builder.setView(linearLayout);

        final EditText ET_name = (EditText) linearLayout.findViewById(R.id.ET_F_name);
        final EditText ET_args = (EditText) linearLayout.findViewById(R.id.ET_F_args);
        final EditText ET_expr = (EditText) linearLayout.findViewById(R.id.ET_F_expr);

        String s = deleteSpaces(listItem);
        s = s.substring(0, s.indexOf("="));
        String functionName = s.substring(0, s.indexOf("("));
        HashMap<String, FunctionsDefinitions> function = new HashMap<String, FunctionsDefinitions>();
        helper = new MyDataHelper(getActivity().getApplicationContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        idFunction = helper.findFunctionByName(db, functionName, function);
        for(Map.Entry entry : function.entrySet()) {
            ET_name.setText(entry.getKey().toString());
            ET_name.setSelection(ET_name.getText().length());
            FunctionsDefinitions FD = (FunctionsDefinitions) entry.getValue();
            ET_expr.setText(FD.expression);
            ET_args.setText(listToString(FD.arguments));
        }
        Log.d("myLogs", "id = " + idFunction);
        builder.setNegativeButton(getResources().getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton(getResources().getString(R.string.dialog_button_OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = deleteSpaces(ET_name.getText().toString());
                String args = deleteSpaces(ET_args.getText().toString());
                String expr = deleteSpaces(ET_expr.getText().toString());
                helper = new MyDataHelper(getActivity().getApplicationContext());
                SQLiteDatabase db = helper.getWritableDatabase();
                if(!(name.equals("") || args.equals("") || expr.equals(""))) {
                    helper.updateFunctions(db, idFunction, name, args, expr);
                    parser.functions.clear();
                    parser = new Parser();
                    helper.readFunctions(db, parser.functions);
                    list_functions.clear();
                    createListOfFunctions(list_functions, parser.functions);
                    adapter2.notifyDataSetChanged();
                    helper.insertHistory(db, getResources().getString(R.string.function_changed) + " " + name + "(" + args + ") = " + expr, name + "(");
                }
            }
        })
                .create()
                .show();
    }

    void showArrayDialog(Bundle bundle, String listItem) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_launcher)
                .setTitle(getResources().getString(R.string.dialog_arrays_title));
        View linearLayout = getLayoutInflater(bundle).inflate(R.layout.dialog_arrays, null);
        builder.setView(linearLayout);

        final EditText ET_name = (EditText) linearLayout.findViewById(R.id.ET_A_name);
        final EditText ET_values = (EditText) linearLayout.findViewById(R.id.ET_A_values);

        String s = deleteSpaces(listItem);
        String arrayName = s.substring(0, s.indexOf("="));
        Array array = new Array();
        helper = new MyDataHelper(getActivity().getApplicationContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        idArray = helper.findArrayByName(db, arrayName, array);
        ET_name.setText(array.getName());
        ET_name.setSelection(ET_name.getText().length());
        ET_values.setText(array.getValuesString());
        Log.d("myLogs", "id = " + idArray);

        builder.setNegativeButton(getResources().getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton(getResources().getString(R.string.dialog_button_OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = deleteSpaces(ET_name.getText().toString());
                String values = deleteSpaces(ET_values.getText().toString());
                helper = new MyDataHelper(getActivity().getApplicationContext());
                SQLiteDatabase db = helper.getWritableDatabase();
                if(!(name.equals("") || values.equals(""))) {
                    helper.updateArrays(db, idArray, name, values);
                    helper.deleteVariable(db, helper.findVariableByName(db, name, null));
                    parser.arrays.clear();
                    parser = new Parser();
                    helper.readArrays(db, parser.arrays);
                    list_arrays.clear();
                    createListOfArrays(list_arrays, parser.arrays);
                    adapter3.notifyDataSetChanged();
                    helper.insertHistory(db, getResources().getString(R.string.array_changed) + " " + name + " = {" + values + "}", name + " = {" + values + "}");
                }
            }
        })
                .create()
                .show();
    }

    String deleteSpaces(String s) {
        StringBuilder SB = new StringBuilder();
        SB.append(s);
        while(SB.indexOf(" ") != -1) {
            SB.deleteCharAt(SB.indexOf(" "));
        }
        return SB.toString();
    }

    String listToString(ArrayList<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String aList : list) {
            sb.append(aList);
            sb.append(", ");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
}
