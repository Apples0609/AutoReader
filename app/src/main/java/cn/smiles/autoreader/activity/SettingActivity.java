package cn.smiles.autoreader.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CheckBox;

import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.smiles.autoreader.R;
import cn.smiles.autoreader.ktool.KTools;

public class SettingActivity extends AppCompatActivity {

    public static final String RANDOM_READING = "random_reading";

    @BindView(R.id.cb_req_random_reading)
    CheckBox cbReqRandomReading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        boolean b1 = KTools.getBooleanPreference(RANDOM_READING, false);
        cbReqRandomReading.setChecked(b1);
        cbReqRandomReading.setOnCheckedChangeListener((compoundButton, b) -> {
            KTools.setBooleanPreference(RANDOM_READING, b);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return true;
    }
}
