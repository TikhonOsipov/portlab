package com.tixon.portlab.app;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class FragmentPrefAppearance extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_appearance);
    }
}
