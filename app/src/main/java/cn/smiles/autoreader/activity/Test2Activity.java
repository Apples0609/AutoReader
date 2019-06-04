package cn.smiles.autoreader.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.smiles.autoreader.R;

public class Test2Activity extends AppCompatActivity {

    @BindView(R.id.button)
    Button button;
    @BindView(R.id.button2)
    Button button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);
        ButterKnife.bind(this);


    }

    @OnClick({R.id.button, R.id.button2})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button:
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.ruijie.wifim");
                if (launchIntent != null) {
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(launchIntent);
                }
                break;
            case R.id.button2:
                //1.
//                Intent intent = new Intent();
//                intent.setComponent(new ComponentName("com.ruijie.wifim", "cn.com.ruijie.wifibox.activity.PingActivity"));
//                startActivity(intent);

                //2.
//                Intent intent2 = new Intent(Intent.ACTION_MAIN);
//                intent2.setClassName("com.ruijie.wifim", "cn.com.ruijie.wifibox.activity.PingActivity");
//                startActivity(intent2);
//                finish();

                //3.
                Intent launchIntent3 = getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.ruijie.wifim");
                if (launchIntent3 != null) {
                    launchIntent3.putExtra("AppID", "MY-CHILD-APP1");
                    launchIntent3.putExtra("UserID", "MY-APP");
                    launchIntent3.putExtra("Password", "MY-PASSWORD");
                    startActivity(launchIntent3);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), " launch Intent not available", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
