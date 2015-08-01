package com.tixon.portlab.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

public class ActivitySettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_activity_content);

        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        ListPreference calculatorRoundListPreference = (ListPreference) findPreference("list_round_calculator");
        ListPreference matrixRoundListPreference = (ListPreference) findPreference("list_round_matrix");

        String stringRoundCalculator = sp.getString("list_round_calculator", "5");
        String[] roundEntriesCalculator = getResources().getStringArray(R.array.entries_round);
        String[] roundEntryValuesCalculator = getResources().getStringArray(R.array.entry_values_round);
        int roundCalculatorIndex = 0;
        for(int i = 0; i < roundEntryValuesCalculator.length; i++) {
            if(roundEntryValuesCalculator[i].equals(stringRoundCalculator)) {
                roundCalculatorIndex = i; break;
            }
        }
        calculatorRoundListPreference.setSummary(roundEntriesCalculator[roundCalculatorIndex]);

        String stringRoundMatrix = sp.getString("list_round_matrix", "3");
        String[] roundEntriesMatrix = getResources().getStringArray(R.array.entries_round);
        String[] roundEntryValuesMatrix = getResources().getStringArray(R.array.entry_values_round);
        int roundMatrixIndex = 0;
        for(int i = 0; i < roundEntryValuesMatrix.length; i++) {
            if(roundEntryValuesMatrix[i].equals(stringRoundMatrix)) {
                roundMatrixIndex = i; break;
            }
        }
        matrixRoundListPreference.setSummary(roundEntriesMatrix[roundMatrixIndex]);
    }

    @Override
    public boolean onIsMultiPane() {
        return super.onIsMultiPane();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransition(R.anim.right_slow, R.anim.right_fast);
    }

    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if(pref instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) pref;
            String keyListPreference = listPreference.getKey();

            if(keyListPreference.equals("list_round_calculator")) {
                String stringRound = sharedPreferences.getString("list_round_calculator", "5");
                String[] roundEntries = getResources().getStringArray(R.array.entries_round);
                String[] roundEntryValues = getResources().getStringArray(R.array.entry_values_round);
                int roundIndex = 0;
                for(int i = 0; i < roundEntryValues.length; i++) {
                    if(roundEntryValues[i].equals(stringRound)) {
                        roundIndex = i; break;
                    }
                }
                pref.setSummary(roundEntries[roundIndex]);
            }

            if(keyListPreference.equals("list_round_matrix")) {
                String stringRound = sharedPreferences.getString("list_round_matrix", "3");
                String[] roundEntries = getResources().getStringArray(R.array.entries_round);
                String[] roundEntryValues = getResources().getStringArray(R.array.entry_values_round);
                int roundIndex = 0;
                for(int i = 0; i < roundEntryValues.length; i++) {
                    if(roundEntryValues[i].equals(stringRound)) {
                        roundIndex = i; break;
                    }
                }
                pref.setSummary(roundEntries[roundIndex]);
            }
        }
    }
}
