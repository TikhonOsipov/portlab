package com.tixon.portlab.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;

public class ActivityGreetings extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_greetings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.welcome_to_portlab);
        toolbar.setLogo(R.drawable.ic_launcher);

        Button button = (Button) findViewById(R.id.btn_startUse);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity();
            }
        });

        showUpdatesDialog();

        ScrollView scrollView = (ScrollView) findViewById(R.id.greetings_scroll_view);
        if(Build.VERSION.SDK_INT < 21) {
            scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        } else {
            scrollView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);

            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.primary700));
        }


    }

    void startActivity() {
        Intent intentStartUse = new Intent(this, ActivityMain.class);
        startActivity(intentStartUse);
    }

    void showUpdatesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.dialog_updates_title))
                .setMessage(getResources().getString(R.string.dialog_updates_message))
                .setIcon(R.drawable.ic_launcher)
                .setPositiveButton(getResources().getString(R.string.dialog_button_OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }
}
