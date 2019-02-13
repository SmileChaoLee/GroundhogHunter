package com.smile.groundhoghunter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

public class TwoPlayerActivity extends AppCompatActivity {

    private static final String TAG = "TwoPlayerActivity";
    // private properties
    private float textFontSize;
    private float fontScale;
    private int mediaType;
    private EditText playerNameEditText;
    private String playerName;
    private ListView playerListView;
    private ArrayList<String> playerNameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, GroundhogHunterApp.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);
        fontScale = ScreenUtil.suitableFontScale(this, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);

        mediaType = GameView.BluetoothMediaType;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mediaType = extras.getInt("MediaType");
        }

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            // not Oreo
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_two_players);

        playerListView = findViewById(R.id.playerListView);
        playerNameList = new ArrayList<>();
        playerNameList.add("Chao Lee 1");
        playerNameList.add("Chao Lee 2");
        playerNameList.add("Chao Lee 3");
        playerNameList.add("Chao Lee 4");
        playerNameList.add("Chao Lee 555555555555555555555555555555555555");
        playerNameList.add("Chao Lee 6");
        playerNameList.add("Chao Lee 7");
        playerNameList.add("Chao Lee 8");
        playerNameList.add("Chao Lee 9");
        playerNameList.add("Chao Lee 10");

        playerListView.setAdapter(new twoPlayerListAdapter(this, R.layout.player_list_item_layout, R.id.playerNameTextView, playerNameList));

        TextView twoPlayerSettingTitle = findViewById(R.id.twoPlayerSettingTitle);
        ScreenUtil.resizeTextSize(twoPlayerSettingTitle, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        final RadioButton bluetoothRadioButton = findViewById(R.id.bluetoothRadioButton);
        ScreenUtil.resizeTextSize(bluetoothRadioButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        bluetoothRadioButton.setChecked(false);
        bluetoothRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaType = GameView.BluetoothMediaType;
            }
        });
        final RadioButton lanRadioButton = findViewById(R.id.lanRadioButton);
        ScreenUtil.resizeTextSize(lanRadioButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        lanRadioButton.setChecked(false);
        lanRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaType = GameView.LanMediaType;
            }
        });
        final RadioButton internetRadioButton = findViewById(R.id.internetRadioButton);
        ScreenUtil.resizeTextSize(internetRadioButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        internetRadioButton.setChecked(false);
        internetRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaType = GameView.InternetMediaType;
            }
        });

        switch (mediaType) {
            case GameView.BluetoothMediaType:
                bluetoothRadioButton.setChecked(true);
                break;
            case GameView.LanMediaType:
                lanRadioButton.setChecked(true);
                break;
            case GameView.InternetMediaType:
                internetRadioButton.setChecked(true);
                break;
        }

        playerNameEditText = findViewById(R.id.playerNameEditText);
        ScreenUtil.resizeTextSize(playerNameEditText, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        playerNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                playerName = editable.toString();
                Log.d(TAG, "playerName = " + playerName);
            }
        });

        final Button createGameButton = findViewById(R.id.createGameButton);
        ScreenUtil.resizeTextSize(createGameButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        final Button joinGameButton = findViewById(R.id.joinGameButton);
        ScreenUtil.resizeTextSize(joinGameButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        final Button confirmButton = findViewById(R.id.confirmTwoPlayerButton);
        ScreenUtil.resizeTextSize(confirmButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToPrevious(true);
            }
        });

        final Button cancelButton = findViewById(R.id.cancelTwoPlayerButton);
        ScreenUtil.resizeTextSize(cancelButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToPrevious(false);
            }
        });
    }

    @Override
    public void onBackPressed() {
        returnToPrevious(false);
    }

    private void returnToPrevious(boolean confirmed) {

        Intent returnIntent = new Intent();
        Bundle extras = new Bundle();
        extras.putInt("MediaType", mediaType);
        returnIntent.putExtras(extras);

        int resultYn = Activity.RESULT_OK;
        if (!confirmed) {
            // cancelled
            resultYn = Activity.RESULT_CANCELED;
        }

        setResult(resultYn, returnIntent);    // can bundle some data to previous activity

        finish();
    }

    private class twoPlayerListAdapter extends ArrayAdapter {

        @SuppressWarnings("unchecked")
        public twoPlayerListAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List objects) {
            super(context, resource, textViewResourceId, objects);
        }
    }
}
