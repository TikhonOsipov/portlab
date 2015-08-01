package com.tixon.portlab.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;

public class FragmentMatrix extends Fragment {

    public FragmentMatrix(){}
    private AdView adView;
    private Spinner spinner;
    private EditText ET1, ET2, ET_result;
    private TextView TV1, TV2;
    private FloatingActionButton fab;
    private RadioGroup radioGroup;
    private ScrollView scrollView;
    private int spinnerSelectedPosition;
    SharedPreferences sPref, settings;
    private int checkedRadio, round;
    private InputMethodManager imm;

    private StringBuilder log;

    boolean ET1_click, ET2_click = false;

    /*TaskLoad tLoad;
    TaskSave tSave;*/

    private static final String spinnerPosition = "spinner_selection";
    private static final String savedText_frag2_1 = "saved_editText_matrix_1";
    private static final String savedText_frag2_2 = "saved_editText_matrix_2";
    private static final String savedSpinnerPosition = "savedSpinnerPosition";
    private static final String savedRadioGroupPosition = "savedRadioGroupPosition";
    private static final String savedText_frag2_res = "saved_editText_result";

    public static Fragment newInstance() {
        FragmentMatrix fragmentMatrix = new FragmentMatrix();
        Bundle b = new Bundle();
        fragmentMatrix.setArguments(b);
        return fragmentMatrix;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_matrix_layout, container, false);
        //AdView
        adView = (AdView) rootView.findViewById(R.id.adViewSecond);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        //Reading preferences from ActivityPref_preferences
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        round = Integer.parseInt(settings.getString("list_round_matrix", "3"));
        spinner = (Spinner) rootView.findViewById(R.id.spinner_matrix);
        //Button detailedSolution = (Button) rootView.findViewById(R.id.buttonDetailedSolution);
        TV1 = (TextView) rootView.findViewById(R.id.TV_matrix1);
        TV2 = (TextView) rootView.findViewById(R.id.TV_matrix2);
        ET1 = (EditText) rootView.findViewById(R.id.matrix1);
        ET2 = (EditText) rootView.findViewById(R.id.matrix2);
        ET1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ET1_click = true;
                ET2_click = false;
                return false;
            }
        });
        ET2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ET1_click = false;
                ET2_click = true;
                return false;
            }
        });
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        scrollView = (ScrollView) rootView.findViewById(R.id.scroll_matrix);
        ET_result = (EditText) rootView.findViewById(R.id.ET_result);
        ET1.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "DroidSansMono.ttf"));
        ET2.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "DroidSansMono.ttf"));
        ET_result.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "DroidSansMono.ttf"));
        radioGroup = (RadioGroup) rootView.findViewById(R.id.radioGroup);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkedRadio = checkedId;
            }
        });

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab_matrix);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),
                R.layout.spinner_item, getResources().getStringArray(R.array.matrix_operations));
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setPrompt(getResources().getString(R.string.spinner_title));
        try {
            /*tLoad = new TaskLoad();
            tLoad.execute();*/
            loadText();
            switch (checkedRadio) {
                case R.id.radio_matrix1:
                    radioGroup.check(R.id.radio_matrix1);
                    break;
                case R.id.radio_matrix2:
                    radioGroup.check(R.id.radio_matrix2);
                    break;
                default: break;
            }
        } catch (Exception ex) {
            Log.d("myLogs", ex.toString());
        }

        ET1.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_SPACE)) {
                    // && (keyCode == KeyEvent.KEYCODE_SPACE)
                    //(event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_SPACE)
                    String text = ET1.getText().toString();
                    int position = ET1.getSelectionStart();
                    StringBuilder s_left, s_right;
                    s_left = new StringBuilder();
                    s_right = new StringBuilder();
                    s_left.append(text.substring(0, position));
                    s_right.append(text.substring(position));
                    s_left.append("; ");
                    ET1.setText(s_left.toString() + s_right.toString());
                    ET1.setSelection(position + 2);
                    return true;
                }
                return false;
            }
        });

        ET2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_SPACE)) {
                    String text = ET2.getText().toString();
                    int position = ET2.getSelectionStart();
                    StringBuilder s_left, s_right;
                    s_left = new StringBuilder();
                    s_right = new StringBuilder();
                    s_left.append(text.substring(0, position));
                    s_right.append(text.substring(position));
                    s_left.append("; ");
                    ET2.setText(s_left.toString() + s_right.toString());
                    ET2.setSelection(position + 2);
                    return true;
                }
                return false;
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerSelectedPosition = position;
                Log.d("myLogs", "ITEM_SELECTED | position = " + position + "; id = " + id);
                switch (position) {
                    case 0:case 1:case 2:case 3:
                        TV1.setText(getResources().getString(R.string.matrix1));
                        TV2.setText(getResources().getString(R.string.matrix2));
                        //Log.d("myLogs", TV1.getText().toString());
                        radioGroup.setVisibility(view.VISIBLE);
                        break;
                    case 4:
                        TV1.setText(getResources().getString(R.string.matrix));
                        TV2.setText(getResources().getString(R.string.exponent));
                        radioGroup.setVisibility(view.GONE);
                        break;
                    case 5:case 6:case 7:case 8:
                        TV1.setText(getResources().getString(R.string.matrix1));
                        TV2.setText(getResources().getString(R.string.matrix2));
                        radioGroup.setVisibility(view.GONE);
                        break;
                    default: break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("myLogs", "NOTHING SELECTED");
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Matrix m = new Matrix();
                String s = "";
                ArrayList<ArrayList<Double>> matrix, matrix1, matrix2, matrix_result;
                switch (checkedRadio) {
                    case R.id.radio_matrix1:
                        //DeleteSpaces
                        s = deleteSpaces(ET1.getText().toString());
                        if(!s.equals("")) {
                            switch (spinnerSelectedPosition) {
                                case 0:
                                    //Trace
                                    matrix = m.read(ET1);
                                    ET_result.setText(String.valueOf(m.Trace(matrix)));
                                    scrollToBottom(scrollView);
                                    hideKeyboard(ET1);
                                    break;
                                case 1:
                                    //Determinant
                                    log = new StringBuilder();
                                    log.append("Determinant:").append("\n");
                                    matrix = m.read(ET1);
                                    if(m.IsMatrixSquare(matrix)) {
                                        double D = m.det(matrix, log);
                                        ET_result.setText(String.valueOf(D));
                                        scrollToBottom(scrollView);
                                        hideKeyboard(ET1);
                                    } else makeToast(getResources().getString(R.string.matrix1_should_be_square));
                                    break;
                                case 2:
                                    //Transpose
                                    matrix = m.read(ET1);
                                    ET_result.setText(m.MatrixToString(m.Transpose(matrix), round));
                                    scrollToBottom(scrollView);
                                    hideKeyboard(ET1);
                                    break;
                                case 3:
                                    //Reverse
                                    matrix = m.read(ET1);
                                    if(m.IsMatrixSquare(matrix)) {
                                        if(!m.ZeroDeterminant(matrix)) {
                                            ArrayList<ArrayList<Double>> reversed;
                                            reversed = m.Reverse(matrix);
                                            ET_result.setText(m.MatrixToString(reversed, round));
                                            scrollToBottom(scrollView);
                                            hideKeyboard(ET1);
                                        } else makeToast(getResources().getString(R.string.determinant_is_0));
                                    } else makeToast(getResources().getString(R.string.matrix1_should_be_square));
                                    break;
                                default: break;
                            }
                        } else {
                            makeToast(getResources().getString(R.string.ET_empty));
                        }
                        break;
                    case R.id.radio_matrix2:
                        s = deleteSpaces(ET2.getText().toString());
                        if(!s.equals("")) {
                            switch (spinnerSelectedPosition) {
                                case 0:
                                    //Trace
                                    matrix = m.read(ET2);
                                    ET_result.setText(String.valueOf(m.Trace(matrix)));
                                    scrollToBottom(scrollView);
                                    hideKeyboard(ET2);
                                    break;
                                case 1:
                                    //Determinant
                                    log = new StringBuilder();
                                    log.append("Determinant:").append("\n");
                                    matrix = m.read(ET2);
                                    if(m.IsMatrixSquare(matrix)) {
                                        double D = m.det(matrix, log);
                                        ET_result.setText(String.valueOf(D));
                                        scrollToBottom(scrollView);
                                        hideKeyboard(ET2);
                                    } else makeToast(getResources().getString(R.string.matrix2_should_be_square));
                                    break;
                                case 2:
                                    //Transpose
                                    matrix = m.read(ET2);
                                    ET_result.setText(m.MatrixToString(m.Transpose(matrix), round));
                                    scrollToBottom(scrollView);
                                    hideKeyboard(ET2);
                                    break;
                                case 3:
                                    //Reverse
                                    matrix = m.read(ET2);
                                    if(m.IsMatrixSquare(matrix)) {
                                        if(!m.ZeroDeterminant(matrix)) {
                                            ArrayList<ArrayList<Double>> reversed;
                                            reversed = m.Reverse(matrix);
                                            ET_result.setText(m.MatrixToString(reversed, round));
                                            scrollToBottom(scrollView);
                                            hideKeyboard(ET2);
                                        } else makeToast(getResources().getString(R.string.determinant_is_0));
                                    } else makeToast(getResources().getString(R.string.matrix2_should_be_square));
                                    break;
                                default: break;
                            }
                        } else {
                            makeToast(getResources().getString(R.string.ET_empty));
                        }
                        break;
                    default: break;
                }

                if(radioGroup.getVisibility() == getView().GONE) {
                    String s1 = ""; String s2 = "";
                    s1 = deleteSpaces(ET1.getText().toString());
                    s2 = deleteSpaces(ET2.getText().toString());
                    if(!(s1.equals("") || s2.equals(""))) {
                        switch (spinnerSelectedPosition) {
                            case 4:
                                //Exponentiation
                                matrix = m.read(ET1);
                                try {
                                    int exp = Integer.parseInt(ET2.getText().toString());
                                    if(exp >= 1) {
                                        if(m.IsMatrixSquare(matrix)) {
                                            matrix_result = m.Exponentiation(matrix, exp);
                                            ET_result.setText(m.MatrixToString(matrix_result, round));
                                            scrollToBottom(scrollView);
                                            hideKeyboard(ET1);
                                            hideKeyboard(ET2);
                                        } else makeToast(getResources().getString(R.string.matrix_should_be_square));
                                    } else makeToast(getResources().getString(R.string.exponent_moreThan_1));
                                } catch (Exception e) {
                                    makeToast(getResources().getString(R.string.exponent_integer));
                                }
                                break;
                            case 5:
                                //Addition
                                matrix1 = m.read(ET1);
                                matrix2 = m.read(ET2);
                                if(m.SameSize(matrix1, matrix2)) {
                                    matrix_result = m.Addition(matrix1, matrix2);
                                    ET_result.setText(m.MatrixToString(matrix_result, round));
                                    scrollToBottom(scrollView);
                                    hideKeyboard(ET1);
                                    hideKeyboard(ET2);
                                } else makeToast(getResources().getString(R.string.matrices_should_be_same_size));
                                break;
                            case 6:
                                //Subtraction
                                matrix1 = m.read(ET1);
                                matrix2 = m.read(ET2);
                                if(m.SameSize(matrix1, matrix2)) {
                                    matrix_result = m.Subtraction(matrix1, matrix2);
                                    ET_result.setText(m.MatrixToString(matrix_result, round));
                                    scrollToBottom(scrollView);
                                    hideKeyboard(ET1);
                                    hideKeyboard(ET2);
                                } else makeToast(getResources().getString(R.string.matrices_should_be_same_size));
                                break;
                            case 7:
                                //Multiply
                                matrix1 = m.read(ET1);
                                matrix2 = m.read(ET2);
                                if (m.AreConsistent(matrix1, matrix2)) {
                                    matrix_result = m.Multiply(matrix1, matrix2);
                                    ET_result.setText(m.MatrixToString(matrix_result, round));
                                    scrollToBottom(scrollView);
                                    hideKeyboard(ET1);
                                    hideKeyboard(ET2);
                                } else makeToast(getResources().getString(R.string.matrices_should_be_consistent));
                                break;
                            case 8:
                                //Division
                                matrix1 = m.read(ET1);
                                matrix2 = m.read(ET2);
                                if(m.AreConsistent(matrix1, matrix2)) {
                                    if(m.IsMatrixSquare(matrix2)) {
                                        matrix_result = m.Division(matrix1, matrix2);
                                        ET_result.setText(m.MatrixToString(matrix_result, round));
                                        scrollToBottom(scrollView);
                                        hideKeyboard(ET1);
                                        hideKeyboard(ET2);
                                    } else makeToast(getResources().getString(R.string.matrix2_should_be_square));
                                } else makeToast(getResources().getString(R.string.matrices_should_be_consistent));
                                break;
                            default: break;
                        }
                    } else {
                        makeToast(getResources().getString(R.string.ET_empty));
                    }
                }
                //ET_result.setSelection(ET_result.length());
                //end of Button_OnClick
            }
        });

        /*detailedSolution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog with detailed solution (using SB log)
                showDetailedDialog(log);
            }
        });*/
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.context_menu_matrix, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.add_semicolon:
                if(ET1_click) {
                    FragmentCalculator.insertInEditText(ET1, "; ");
                }
                if(ET2_click) {
                    FragmentCalculator.insertInEditText(ET2, "; ");
                }
        }
        return super.onOptionsItemSelected(item);
    }

    //Reading preferences from ActivityPref_preferences
    @Override
    public void onResume() {
        adView.resume();//Todo
        super.onResume();
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        round = Integer.parseInt(settings.getString("list_round_matrix", "3"));
        ET1.setSelection(ET1.length());
        ET1_click = true;
        ET2_click = false;
    }

    @Override
    public void onPause() {
        adView.pause();//Todo
        super.onPause();
    }

    @Override
    public void onDestroy() {
        adView.destroy();//Todo
        super.onDestroy();
    }

    void hideKeyboard(EditText ET) {
        imm.hideSoftInputFromWindow(ET.getWindowToken(), 0);
    }

    void scrollToBottom(final ScrollView scroll) {
        /*scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(View.FOCUS_DOWN);
            }
        });*/
    }

    @Override
    public void onDetach() {
        saveText();
        /*tSave = new TaskSave();
        tSave.execute();*/
        super.onDetach();
    }

    void saveText() {
        sPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(savedText_frag2_1, ET1.getText().toString());
        ed.putString(savedText_frag2_2, ET2.getText().toString());
        ed.putInt(savedSpinnerPosition, spinner.getSelectedItemPosition()); //ToDo
        ed.putInt(savedRadioGroupPosition, checkedRadio);
        ed.putString(savedText_frag2_res, ET_result.getText().toString());
        //if(ET1.hasSelection()) ed.putInt(cursorPosition_frag1, ET1.getSelectionEnd());
        ed.commit();
    }

    void loadText() {
        sPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String savedText1 = sPref.getString(savedText_frag2_1, "");
        String savedText2 = sPref.getString(savedText_frag2_2, "");
        String savedResult = sPref.getString(savedText_frag2_res, "");
        int spinnerPosition = sPref.getInt(savedSpinnerPosition, 0);
        ET1.setText(savedText1);
        ET2.setText(savedText2);
        ET_result.setText(savedResult);
        spinner.setSelection(spinnerPosition);
        checkedRadio = sPref.getInt(savedRadioGroupPosition, 0);
    }

    String deleteSpaces(String s) {
        StringBuilder sb = new StringBuilder();
        sb.append(s);
        while (sb.indexOf(" ") != -1) {
            sb.deleteCharAt(sb.indexOf(" "));
        }
        return sb.toString();
    }

    void makeToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    void showDetailedDialog(StringBuilder log) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_launcher)
                .setTitle("Title")
                .setMessage(log.toString())
                .setNeutralButton(getResources().getString(R.string.dialog_button_OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }
}
