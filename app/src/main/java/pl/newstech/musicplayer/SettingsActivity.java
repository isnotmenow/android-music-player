package pl.newstech.musicplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent activityThatCalled = getIntent();
        int localSortType = activityThatCalled.getExtras().getInt("settingSort");

        if(localSortType == 0) {
            RadioButton rB = (RadioButton) findViewById(R.id.radioButton);
            rB.setChecked(true);
        }
        else {
            RadioButton rB = (RadioButton) findViewById(R.id.radioButton2);
            rB.setChecked(true);
        }

    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        Intent goingBack = new Intent();
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioButton:
                if (checked)
                    goingBack.putExtra("settingSort", 0);
                    break;
            case R.id.radioButton2:
                if (checked)
                    goingBack.putExtra("settingSort", 1);
                    break;
        }
        setResult(RESULT_OK, goingBack);
        finish();
    }
}
