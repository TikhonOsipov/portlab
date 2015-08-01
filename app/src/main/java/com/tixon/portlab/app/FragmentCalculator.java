package com.tixon.portlab.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.melnykov.fab.FloatingActionButton;
import com.purplebrain.adbuddiz.sdk.AdBuddiz;

import java.util.ArrayList;

public class FragmentCalculator extends Fragment implements View.OnClickListener {
    private static final String LOG_TAG = "myLogs";
    private static final String key_historyList = "history_list";
    private static final String key_expressionsList = "expressions_list";
    private static final String savedText_frag1 = "saved_editText_calculator";
    private static final String cursorPosition_frag1 = "cursorPosition_frag1";
    private static final String KEY_BAR_SHOWN = "key_barShown";
    private static final String KEY_FUNCTIONS_BAR_SHOWN = "key_functionsBarShown";

    private AdView adView;
    Parser p;
    MyDataHelper helper;
    SQLiteDatabase db;
    InputMethodManager imm;
    TaskRead tRead;
    SharedPreferences sPref, settings;
    ActionMode actionMode;
    RecyclerView mRecyclerView;

    ArrayList<String> history, expressions;
    ArrayList<HistoryItem> historyList;
    boolean smoothScrollToTop, hideKeyboard, barShown, functionsBarShown;
    String showDataFromHistory;
    int round, longClickPosition;
    ArrayList<Integer> longClickPositions;

    Button b_add, b_subtract, b_multiply, b_divide, b_power, b_LP, b_RP, b_functions, b_array, b_assign;
    Button b_sin, b_cos, b_tg, b_ctg, b_rad, b_deg, b_lg, b_ln, b_log, b_sqrt, b_pi, b_e;
    FloatingActionButton fab;
    EditText editText;
    LinearLayout operationsBar, functionsBar;
    FrameLayout myFrameLayout;

    //ArrayAdapter<String> history_adapter;
    HistoryAdapter history_adapter;

    public FragmentCalculator() {}

    public static Fragment newInstance() {
        FragmentCalculator fragmentCalculator = new FragmentCalculator();
        Bundle b = new Bundle();
        fragmentCalculator.setArguments(b);
        return fragmentCalculator;
    }

    void createHistoryList(ArrayList<String> history, ArrayList<String> expressions, ArrayList<HistoryItem> historyList) {
        for(int i = 0; i < history.size(); i++) {
            historyList.add(new HistoryItem(expressions.get(i), history.get(i)));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //AdBuddiz.setPublisherKey("8d15e4c0-70c5-40ed-a2c4-df4567e62a48");
        //AdBuddiz.cacheAds(getActivity()); // this = current Activity
        //AdBuddiz.setTestModeActive();
        Log.d(LOG_TAG, "fragment_calc: onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calculator_layout, container, false);
        //Log.d(LOG_TAG, "fragment_calc: onCreateView");
        b_add = (Button) rootView.findViewById(R.id.button_add);
        b_subtract = (Button) rootView.findViewById(R.id.button_subtract);
        b_multiply = (Button) rootView.findViewById(R.id.button_multiply);
        b_divide = (Button) rootView.findViewById(R.id.button_divide);
        b_power = (Button) rootView.findViewById(R.id.button_power);
        b_LP = (Button) rootView.findViewById(R.id.button_LP);
        b_RP = (Button) rootView.findViewById(R.id.button_RP);
        b_functions = (Button) rootView.findViewById(R.id.button_show_functions);
        b_assign = (Button) rootView.findViewById(R.id.button_assign);
        b_array = (Button) rootView.findViewById(R.id.button_array);
        b_sin = (Button) rootView.findViewById(R.id.button_sin);
        b_cos = (Button) rootView.findViewById(R.id.button_cos);
        b_tg = (Button) rootView.findViewById(R.id.button_tg);
        b_ctg = (Button) rootView.findViewById(R.id.button_ctg);
        b_deg = (Button) rootView.findViewById(R.id.button_deg);
        b_rad = (Button) rootView.findViewById(R.id.button_rad);
        b_lg = (Button) rootView.findViewById(R.id.button_lg);
        b_ln = (Button) rootView.findViewById(R.id.button_ln);
        b_log = (Button) rootView.findViewById(R.id.button_log);
        b_sqrt = (Button) rootView.findViewById(R.id.button_sqrt);

        b_pi = (Button) rootView.findViewById(R.id.button_pi);
        b_e = (Button) rootView.findViewById(R.id.button_e);

        myFrameLayout = (FrameLayout) rootView.findViewById(R.id.frame_layout_calculator);
        fab = (FloatingActionButton) rootView.findViewById(R.id.fab_calculator);

        adView = (AdView) rootView.findViewById(R.id.adViewFirst);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        editText = (EditText) rootView.findViewById(R.id.editTextCalculator); //Connecting EditText
        if((getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
            editText.setTextSize(20);
        }
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.historyRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        /*if(Build.VERSION.SDK_INT < 21) {
            listViewHistory.setOverScrollMode(View.OVER_SCROLL_NEVER);
        } else {
            listViewHistory.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        }*/
        operationsBar = (LinearLayout) rootView.findViewById(R.id.layout_buttons_calculator);
        functionsBar = (LinearLayout) rootView.findViewById(R.id.layout_functions_calculator);

        b_add.setOnClickListener(this);
        b_subtract.setOnClickListener(this);
        b_multiply.setOnClickListener(this);
        b_divide.setOnClickListener(this);
        b_power.setOnClickListener(this);
        b_LP.setOnClickListener(this);
        b_RP.setOnClickListener(this);
        b_functions.setOnClickListener(this);
        b_assign.setOnClickListener(this);
        b_array.setOnClickListener(this);
        b_sin.setOnClickListener(this);
        b_cos.setOnClickListener(this);
        b_tg.setOnClickListener(this);
        b_ctg.setOnClickListener(this);
        b_deg.setOnClickListener(this);
        b_rad.setOnClickListener(this);
        b_lg.setOnClickListener(this);
        b_ln.setOnClickListener(this);
        b_log.setOnClickListener(this);
        b_sqrt.setOnClickListener(this);
        b_pi.setOnClickListener(this);
        b_e.setOnClickListener(this);
        editText.setOnClickListener(this);
        fab.setOnClickListener(this);

        p = new Parser();
        helper = new MyDataHelper(getActivity().getApplicationContext());
        db = helper.getWritableDatabase();
        history = new ArrayList<>();
        expressions = new ArrayList<>();
        historyList = new ArrayList<>();
        longClickPositions = new ArrayList<>();

        if(savedInstanceState != null) { //Restoring state
            Log.d(LOG_TAG, "CalculatorFragment: restoring SavedInstanceState");
            barShown = savedInstanceState.getBoolean(KEY_BAR_SHOWN, false); if(barShown) showOperationsBar();
            functionsBarShown = savedInstanceState.getBoolean(KEY_FUNCTIONS_BAR_SHOWN, false); if(functionsBarShown) showFunctionsBar();
            history = savedInstanceState.getStringArrayList(key_historyList);
            expressions = savedInstanceState.getStringArrayList(key_expressionsList);
        } else {
            //read history and expressions from database
            Log.d(LOG_TAG, "CalculatorFragment: fail to restore, try read history from database");
            helper.readHistory(db, history, expressions);
        }

        tRead = new TaskRead();
        tRead.execute(); //Reading from database
        loadPreferences();
        loadText();

        createHistoryList(history, expressions, historyList);
        //history_adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_activated_1, history);
        history_adapter = new HistoryAdapter(historyList, R.layout.history_item_view, getActivity());
        mRecyclerView.setAdapter(history_adapter);
        //listViewHistory.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        //ToDo
        /*listViewHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //if(howOftenContains(history.get(position), "=") > 1) {
                if(history.get(position).contains(":=")) {
                    insertInEditText(editText, p.deleteSpaces(history.get(position)));
                } else {
                    insertInEditText(editText, p.deleteSpaces(history.get(position).substring(history.get(position).lastIndexOf("=") + 1)));
                }
                //} else {
                //    insertInEditText(editText, p.deleteSpaces(history.get(position)));
                //}

                //StringBuilder result = new StringBuilder();
                //result.append(p.deleteSpaces(history.get(longClickPosition).substring(history.get(longClickPosition).indexOf("=")+1)));
                //insertInEditText(editText, result.toString());
            }
        });*/
        /*listViewHistory.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                //Log.d("myLogs", "onItemCheckedStateChanged: position = " + position + "; checked = " + checked);
                longClickPosition = position;
                if(checked) {
                    longClickPositions.add(position);
                } else {
                    longClickPositions.remove(longClickPositions.indexOf(position));
                }
                //Change contents of the menu
                if(longClickPositions.size() > 1) { //Checked more than 1 elements os history list
                    mode.getMenu().clear(); //Clear menu contents
                    mode.getMenuInflater().inflate(R.menu.context_menu_2, mode.getMenu());
                    mode.setTitle(String.valueOf(longClickPositions.size()));
                } else { //Checked only 1 element of history list
                    mode.getMenu().clear(); //Clear menu contents
                    mode.getMenuInflater().inflate(R.menu.context_menu, mode.getMenu());
                    mode.setTitle(history.get(longClickPosition));
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.context_menu, menu);
                mode.setTitle(history.get(longClickPosition));
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                //Hide ActionBar
                ((ActionBarActivity)getActivity()).getSupportActionBar().hide();
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.context_replace:
                        break;
                    case R.id.context_expression:
                        Log.d(LOG_TAG, "longClickPosition = " + longClickPosition);
                        insertInEditText(editText, expressions.get(longClickPosition));
                        mode.finish();
                        break;
                    case R.id.context_delete:
                        for(int i = 0; i < longClickPositions.size(); i++) { //This is for reverse history list (new is on top, list.add(0, item))
                            helper.deleteHistoryItem(db, helper.findHistoryByName(db, history.get(longClickPositions.get(i))));
                        }
                        history.clear();
                        expressions.clear();
                        helper.readHistory(db, history, expressions);
                        history_adapter.notifyDataSetChanged();
                        mode.finish();
                        break;
                    case R.id.context_array:
                        StringBuilder arrayContents = new StringBuilder();
                        for(int i = 0; i < longClickPositions.size(); i++) { //Create stringBuilder with items, split by ';'
                            if(howOftenContains(history.get(longClickPositions.get(i)), "=") == 1) {
                                if(!history.get(longClickPositions.get(i)).substring(history.get(longClickPositions.get(i)).indexOf("=") + 1).contains("{") && !history.get(longClickPositions.get(i)).substring(history.get(longClickPositions.get(i)).indexOf("=") + 1).contains("}")) {
                                    arrayContents.append(p.deleteSpaces(history.get(longClickPositions.get(i)).substring(history.get(longClickPositions.get(i)).indexOf("=") + 1)) + "; ");
                                }
                            } else {
                                if(!expressions.get(longClickPositions.get(i)).contains("{") && !expressions.get(longClickPositions.get(i)).contains("}") && !history.get(longClickPositions.get(i)).substring(history.get(longClickPositions.get(i)).indexOf("=") + 1).contains("{") && !history.get(longClickPositions.get(i)).substring(history.get(longClickPositions.get(i)).indexOf("=") + 1).contains("}")) {
                                    arrayContents.append(p.deleteSpaces(expressions.get(longClickPositions.get(i))) + "; ");
                                }
                            }
                        }
                        int tempCursorPosition = editText.getSelectionStart();
                        if(arrayContents.length() != 0) {
                            insertInEditText(editText, " {" + arrayContents.substring(0, arrayContents.length()-2) + "} ");
                            editText.setSelection(tempCursorPosition);
                            imm.showSoftInput(editText, 0);
                        }
                        showOperationsBar();
                        mode.finish();
                        break;
                    default: break;
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                longClickPositions.clear();
                //Show ActionBar
                ((ActionBarActivity)getActivity()).getSupportActionBar().show();
                actionMode = null;
            }
        });*/

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    insertInEditText(editText, "");
                    return true;
                }
                /*if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_DEL)) {
                    if(editText.length() > 0)
                    deleteFromEditText(editText);
                    return true;
                }*/
                return false;
            }
        });
        return rootView;
    }

    void startEquation() {
        String s = editText.getText().toString();
        Log.d("myLogs", "START EQUATION: ENTERED STRING: " + s);
        String storage;
        s = p.deleteSpaces(s);
        //s = replace(s, ";", ",");//Todo: commented from 03-03-2015 02:30
        if(!s.isEmpty()) {
            String tempExpression = s;
            //Modified 08.09.2014 21:11
            try {
                if(p.ContainsAssignment(s)) {
                    if(p.checkBrackets(s)) { //Modified 08-09-2014 22:05
                        p.assignOperation(s);
                        history.add(0, s);
                        expressions.add(0, s);
                        Log.d(LOG_TAG, "S WITH ASSIGNMENT: " + s);
                        history_adapter.notifyDataSetChanged();
                        editText.setText("");
                        p.lexemes.clear();
                        writeToDB(db, s, tempExpression);
                        //Todo if(smoothScrollToTop) listViewHistory.smoothScrollToPosition(0);
                        if(hideKeyboard) imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    } else {
                        //if it's error in brackets still
                        makeToast(getResources().getString(R.string.calc_brackets));
                        p.errorList = new ArrayList<>();
                        p.lexemes.clear();
                    }
                }
                else {
                    p.MakeListOfLexemes(s, p.lexemes);
                    p.DefineVariables(p.lexemes, p.vars);
                    p.DefineArrays(p.lexemes, p.arrays);
                    if(p.CheckBrackets(p.lexemes)) {
                        p.FindErrors(p.lexemes);
                        if(p.errorList.isEmpty()) {
                            //p.ParsingSet(p.lexemes);// // //
                            p.Array(p.lexemes, round);//Todo: updated 03-03-2015 02:17
                            if(!p.arrayHistory.isEmpty()) {
                                storage = s;
                                Log.d(LOG_TAG, "storage = " + storage);
                                String sWithArrayInserted = s + ": arrays inserted";
                                if(!p.arraysDefined.isEmpty()) {
                                    sWithArrayInserted += ": ";
                                    for(int i = 0; i < p.arraysDefined.size() - 1; i++) {
                                        sWithArrayInserted += p.arraysDefined.get(i).getName() + " = " + p.arraysDefined.get(i).getValuesString() + ", ";
                                    }
                                    sWithArrayInserted += p.arraysDefined.get(p.arraysDefined.size() - 1).getName() + " = " + p.arraysDefined.get(p.arraysDefined.size() - 1).getValuesString();
                                    p.arraysDefined.clear();
                                }
                                Log.d("myLogs", "ATTENTION: sWithArrayInserted = " + sWithArrayInserted);
                                history.add(0, sWithArrayInserted); //Todo: resources
                                expressions.add(0, s);
                                mRecyclerView.setAdapter(new HistoryAdapter(historyList, R.layout.history_item_view, getActivity()));
                                //history_adapter.notifyDataSetChanged();
                                writeToDB(db, sWithArrayInserted, tempExpression);

                                for(int i = 0; i < p.arrayHistory.size(); i++) {
                                    //s = s + " = " + p.Round(p.arrayHistory.get(i), round); //debug (stop point)
                                    s = storage + ": " + p.arrayHistory.get(i);
                                    history.add(0, s);
                                    expressions.add(0, storage);
                                    mRecyclerView.setAdapter(new HistoryAdapter(historyList, R.layout.history_item_view, getActivity()));
                                    //history_adapter.notifyDataSetChanged();
                                    //editText.setText("");
                                    //p.lexemes.clear();
                                    writeToDB(db, s, tempExpression);
                                }
                                p.lexemes.clear();
                                p.arrayHistory.clear();
                                p.arrayHistoryRaw.clear();
                            } else {
                                expressions.add(0, s);
                                try {
                                    s = s + " = " + p.Round(p.lexemes.get(0), round); //debug (stop point)
                                } catch (Exception e) {
                                    s = s + " = " + p.lexemes.get(0);
                                }

                                history.add(0, s);
                                //expressions.add(0, s);
                                mRecyclerView.setAdapter(new HistoryAdapter(historyList, R.layout.history_item_view, getActivity()));
                                //history_adapter.notifyDataSetChanged();
                                //editText.setText("");
                                p.lexemes.clear();
                                writeToDB(db, s, tempExpression);
                            }
                            //Todo if(smoothScrollToTop) listViewHistory.smoothScrollToPosition(0);
                            if(hideKeyboard) imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                            editText.setText("");
                        } else {
                            //Modified 11-03-2015 00:20 (last modify 08.09.2014 21:11)
                            for(int i = 0; i < p.errorList.size(); i++) {
                                if(p.errorList.get(i).description.equals("var")) {
                                    StringBuilder variablesList = new StringBuilder();
                                    for(int k = 0; k < p.errorList.get(i).what.size(); k++) {
                                        variablesList.append(p.errorList.get(i).what.get(k));
                                        variablesList.append("; ");
                                    }
                                    variablesList.deleteCharAt(variablesList.length()-1);
                                    variablesList.deleteCharAt(variablesList.length()-1);
                                    makeToast(getResources().getString(R.string.calc_undefined_variable) + ": " + variablesList.toString());
                                }
                                if(p.errorList.get(i).description.equals("bracket")) {
                                    makeToast(getResources().getString(R.string.calc_brackets));
                                }
                                if(p.errorList.get(i).description.equals("const")) {
                                    makeToast(getResources().getString(R.string.calc_constants_override));
                                }
                            }
                            p.errorList = new ArrayList<>();
                            p.lexemes.clear();
                        }
                    } else {
                        //if it's error in brackets still
                        makeToast(getResources().getString(R.string.calc_brackets));
                        p.errorList = new ArrayList<>();
                        p.lexemes.clear();
                    }
                }
            } catch (Exception e) {
                Log.d(LOG_TAG, "EXCEPTION: " + e.toString());
                p.errorList = new ArrayList<>();
                p.lexemes.clear();
                Toast.makeText(getActivity(), getResources().getString(R.string.calc_default_error) + ": " + e.toString(), Toast.LENGTH_LONG).show();
            }
        }
        p.errorList.clear();
        p.arraysDefined.clear();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //operationsBar
            case R.id.button_add:
                insertInEditText(editText, " + ");
                imm.showSoftInput(editText, 0); break;
            case R.id.button_subtract:
                insertInEditText(editText, " - ");
                imm.showSoftInput(editText, 0); break;
            case R.id.button_multiply:
                insertInEditText(editText, " * ");
                imm.showSoftInput(editText, 0); break;
            case R.id.button_divide:
                insertInEditText(editText, " / ");
                imm.showSoftInput(editText, 0); break;
            case R.id.button_power:
                insertInEditText(editText, " ^ ");
                imm.showSoftInput(editText, 0); break;
            case R.id.button_LP:
                insertInEditText(editText, " ( ");
                imm.showSoftInput(editText, 0); break;
            case R.id.button_RP:
                insertInEditText(editText, " ) ");
                imm.showSoftInput(editText, 0); break;
            case R.id.button_show_functions:
                //myFrameLayout.removeView(fab);
                if(!functionsBarShown) showFunctionsBar();
                else hideFunctionsBar();
                //myFrameLayout.addView(fab);
                break;
            case R.id.button_assign:
                insertInEditText(editText, " := ");
                imm.showSoftInput(editText, 0);
                break;
            case R.id.button_array:
                insertInEditText(editText, " {} ", 2);
                imm.showSoftInput(editText, 0);
                break;
            //functionsBar
            case R.id.button_pi:
                insertInEditText(editText, "pi");
                imm.showSoftInput(editText, 0); break;
            case R.id.button_e:
                insertInEditText(editText, "e");
                imm.showSoftInput(editText, 0); break;
            case R.id.button_sin:
                insertInEditText(editText, "sin()", 1);
                imm.showSoftInput(editText, 0); break;
            case R.id.button_cos:
                insertInEditText(editText, "cos()", 1);
                imm.showSoftInput(editText, 0); break;
            case R.id.button_tg:
                insertInEditText(editText, "tg()", 1);
                imm.showSoftInput(editText, 0); break;
            case R.id.button_ctg:
                insertInEditText(editText, "ctg()", 1);
                imm.showSoftInput(editText, 0); break;
            case R.id.button_deg:
                insertInEditText(editText, "deg()", 1);
                imm.showSoftInput(editText, 0); break;
            case R.id.button_rad:
                insertInEditText(editText, "rad()", 1);
                imm.showSoftInput(editText, 0); break;
            case R.id.button_lg:
                insertInEditText(editText, "log()", 1);
                imm.showSoftInput(editText, 0); break;
            case R.id.button_ln:
                insertInEditText(editText, "ln()", 1);
                imm.showSoftInput(editText, 0); break;
            case R.id.button_log:
                insertInEditText(editText, "log(, )", 3);
                imm.showSoftInput(editText, 0); break;
            case R.id.button_sqrt:
                insertInEditText(editText, "sqrt()", 1);
                imm.showSoftInput(editText, 0); break;
            case R.id.fab_calculator:
                startEquation(); break;
            default: break;
        }
    }

    public void slideIn(View view)
    {
        TranslateAnimation animate = new TranslateAnimation(0,0,-view.getHeight(),0);
        animate.setDuration(200);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.VISIBLE);
    }
    public void slideOut(View view)
    {
        TranslateAnimation animate = new TranslateAnimation(0,0,0,-view.getHeight());
        animate.setDuration(200);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu_calculator, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_options_bar:
                if(!barShown) showOperationsBar();
                else hideOperationsBar();
                if(functionsBarShown) hideFunctionsBar(); break;
            case R.id.action_delete:
                showDeleteDialog();
                break;
            case R.id.action_variables_and_functions:
                //AdBuddiz.showAd(getActivity());
                startActivity(new Intent(getActivity(), ActivityVariables.class));
                break;
            default: break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        adView.resume();
        tRead = new TaskRead();
        tRead.execute();
        loadPreferences();
    }

    @Override
    public void onPause() {
        super.onPause();
        adView.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adView.destroy();
    }

    @Override
    public void onDetach() {
        saveText();
        super.onDetach();
    }

    void writeToDB(SQLiteDatabase db, String s, String tempExpression) {
        helper.insertHistory(db, s, tempExpression);
        helper.deleteVariables(db);
        helper.insertVariables(db, p.vars);
        helper.deleteFunctions(db);
        helper.insertFunctions(db, p.functions);
        helper.deleteArrays(db);
        helper.insertArrays(db, p.arrays);
    }

    public static void insertInEditText(EditText ET, String what) {
        int position = ET.getSelectionStart();
        String text = ET.getText().toString();
        StringBuilder s_left, s_right;
        s_left = new StringBuilder();
        s_right = new StringBuilder();
        s_left.append(text.substring(0, position));
        s_right.append(text.substring(position));
        s_left.append(what);
        ET.setText(s_left.toString() + s_right.toString());
        ET.setSelection(position + what.length());
    }

    public static void insertInEditText(EditText ET, String what, int offset) {
        int position = ET.getSelectionStart();
        String text = ET.getText().toString();
        StringBuilder s_left, s_right;
        s_left = new StringBuilder();
        s_right = new StringBuilder();
        s_left.append(text.substring(0, position));
        s_right.append(text.substring(position));
        s_left.append(what);
        ET.setText(s_left.toString() + s_right.toString());
        ET.setSelection(position + what.length() - offset);
    }

    //Show delete dialog

    void showDeleteDialog() {
        final String[] delete_titles = getResources().getStringArray(R.array.settings_delete);
        final String dialog_cancel = getResources().getString(R.string.dialog_button_cancel);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_select_delete_title)
                .setIcon(R.drawable.ic_launcher)
                .setItems(delete_titles, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                showDeleteHistoryDialog();
                                break;
                            case 1:
                                showDeleteVariablesDialog();
                                break;
                            case 2:
                                showDeleteFunctionsDialog();
                                break;
                            case 3:
                                showDeleteArraysDialog();
                                break;
                            case 4:
                                showDeleteAllDialog();
                                break;
                            default: break;
                        }
                    }
                })
                .setNegativeButton(dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    //Dialogs for delete

    public void showDeleteHistoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.dialog_delete_title))
                .setMessage(getResources().getString(R.string.dialog_delete_history_message))
                .setIcon(R.drawable.ic_launcher)
                .setNegativeButton(getResources().getString(R.string.dialog_button_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(getResources().getString(R.string.dialog_button_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final SQLiteDatabase db = helper.getWritableDatabase();
                        helper.deleteHistoryAndExpressions(db);
                        history.clear();
                        expressions.clear();
                        history_adapter.notifyDataSetChanged();
                        makeToast(getResources().getString(R.string.history_delete_successful));
                    }
                })
                .show();
    }

    public void showDeleteVariablesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.dialog_delete_title))
                .setMessage(getResources().getString(R.string.dialog_delete_variables_message))
                .setIcon(R.drawable.ic_launcher)
                .setNegativeButton(getResources().getString(R.string.dialog_button_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(getResources().getString(R.string.dialog_button_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final SQLiteDatabase db = helper.getWritableDatabase();
                        helper.deleteVariables(db);
                        p.vars.clear();
                        makeToast(getResources().getString(R.string.variables_delete_successful));
                    }
                })
                .show();
    }

    public void showDeleteFunctionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.dialog_delete_title))
                .setMessage(getResources().getString(R.string.dialog_delete_functions_message))
                .setIcon(R.drawable.ic_launcher)
                .setNegativeButton(getResources().getString(R.string.dialog_button_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(getResources().getString(R.string.dialog_button_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final SQLiteDatabase db = helper.getWritableDatabase();
                        helper.deleteFunctions(db);
                        p.functions.clear();
                        makeToast(getResources().getString(R.string.functions_delete_successful));
                    }
                })
                .show();
    }

    public void showDeleteArraysDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.dialog_delete_title))
                .setMessage(getResources().getString(R.string.dialog_delete_arrays_message))
                .setIcon(R.drawable.ic_launcher)
                .setNegativeButton(getResources().getString(R.string.dialog_button_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(getResources().getString(R.string.dialog_button_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final SQLiteDatabase db = helper.getWritableDatabase();
                        helper.deleteArrays(db);
                        p.arrays.clear();
                        makeToast(getResources().getString(R.string.arrays_delete_successful));
                    }
                })
                .show();
    }

    public void showDeleteAllDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.dialog_delete_title))
                .setMessage(getResources().getString(R.string.dialog_delete_all_message))
                .setIcon(R.drawable.ic_launcher)
                .setNegativeButton(getResources().getString(R.string.dialog_button_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(getResources().getString(R.string.dialog_button_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final SQLiteDatabase db = helper.getWritableDatabase();
                        helper.deleteHistoryAndExpressions(db);
                        helper.deleteVariables(db);
                        helper.deleteFunctions(db);
                        helper.deleteArrays(db);
                        history.clear();
                        expressions.clear();
                        history_adapter.notifyDataSetChanged();
                        p.vars.clear();
                        p.functions.clear();
                        p.arrays.clear();
                        p.arrayHistory.clear();
                        makeToast(getResources().getString(R.string.all_data_delete_successful));
                    }
                })
                .show();
    }

    void makeToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    String replace(String string, String what, String to) {
        StringBuilder sb = new StringBuilder();
        sb.append(string);
        while(sb.indexOf(what) != -1) {
            int index = sb.indexOf(what);
            sb.replace(index, index + what.length(), to);
        }
        return sb.toString();
    }

    void showOperationsBar() {
        //slideIn(operationsBar);
        operationsBar.setVisibility(View.VISIBLE);
        barShown = true;
    }

    void hideOperationsBar() {
        //slideOut(operationsBar);
        operationsBar.setVisibility(View.GONE);
        barShown = false;
    }

    void showFunctionsBar() {
        //slideIn(functionsBar);
        functionsBar.setVisibility(View.VISIBLE);
        functionsBarShown = true;
    }

    void hideFunctionsBar() {
        //slideOut(functionsBar);
        functionsBar.setVisibility(View.GONE);
        functionsBarShown = false;
    }

    void loadPreferences() {
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        round = Integer.parseInt(settings.getString("list_round_calculator", "5"));
        smoothScrollToTop = settings.getBoolean("checkBox_smoothScroll", true);
        hideKeyboard = settings.getBoolean("checkBox_hideKB_calc", true);
        showDataFromHistory = settings.getString("selector_showDataFromHistory", getResources().getString(R.string.selector_showDataFromHistory_default));
    }

    void loadText() {
        sPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String savedText = sPref.getString(savedText_frag1, "");
        int position = sPref.getInt(cursorPosition_frag1, 0);
        editText.setText(savedText);
        editText.setSelection(position);
    }

    void saveText() {
        sPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(savedText_frag1, editText.getText().toString());
        if(editText.hasSelection()) ed.putInt(cursorPosition_frag1, editText.getSelectionEnd());
        ed.commit();
    }

    int howOftenContains(String where, String what) {
        int result = 0;
        StringBuilder sb = new StringBuilder();
        sb.append(where);
        for(int i = 0; i < sb.length(); i++) {
            if(String.valueOf(sb.charAt(i)).equals(what)) {
                result += 1;
            }
        }
        return result;
    }

    void deleteFromEditText(EditText editText) {
        int cursorPosition = editText.getSelectionStart();
        StringBuilder sb = new StringBuilder();
        sb.append(editText.getText().toString());
        for(int i = cursorPosition; i > 0; i--) {
            if(String.valueOf(sb.charAt(i-1)).equals(" ")) {
                sb.deleteCharAt(i-1);
                cursorPosition = i;
            } else {
                break;
            }
        }
        cursorPosition -= 1;
        sb.deleteCharAt(cursorPosition-1);
        cursorPosition -= 1;
        for(int i = cursorPosition; i > 0; i--) {
            if(String.valueOf(sb.charAt(i-1)).equals(" ")) {
                sb.deleteCharAt(i-1);
                cursorPosition = i;
            } else {
                break;
            }
        }
        editText.setText(sb.toString());
        editText.setSelection(cursorPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_BAR_SHOWN, barShown);
        outState.putBoolean(KEY_FUNCTIONS_BAR_SHOWN, functionsBarShown);
        outState.putStringArrayList(key_historyList, history);
        outState.putStringArrayList(key_expressionsList, expressions);
    }

    private class TaskRead extends AsyncTask<Void, Void, Void> {
        SQLiteDatabase db;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            db = helper.getWritableDatabase();
        }

        @Override
        protected Void doInBackground(Void... params) {
            p.vars.clear();
            p.functions.clear();
            p.arrays.clear();
            p.initial();
            helper.readVariables(db, p.vars);
            helper.readFunctions(db, p.functions);
            helper.readArrays(db, p.arrays);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
