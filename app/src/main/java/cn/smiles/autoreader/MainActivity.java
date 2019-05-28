package cn.smiles.autoreader;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.smiles.autoreader.activity.SettingActivity;
import cn.smiles.autoreader.ktool.KPhone;
import cn.smiles.autoreader.ktool.KTools;
import cn.smiles.autoreader.rapp.BaiWanKanDian;
import cn.smiles.autoreader.rapp.DaZhongTouTiao;
import cn.smiles.autoreader.rapp.ErTouTiao;
import cn.smiles.autoreader.rapp.JuKanDian;
import cn.smiles.autoreader.rapp.MaYiTouTiao;
import cn.smiles.autoreader.rapp.QuKanDian;
import cn.smiles.autoreader.rapp.QuTouTiao;
import cn.smiles.autoreader.rapp.RApp;
import cn.smiles.autoreader.rapp.ShanDianHeZi;
import cn.smiles.autoreader.rapp.SouHuZiXun;
import cn.smiles.autoreader.rapp.TianTianQuWen;
import cn.smiles.autoreader.rapp.TouTiaoDuoDuo;
import cn.smiles.autoreader.rapp.WeiLiKanKan;
import cn.smiles.autoreader.rapp.XinTouTiao;
import cn.smiles.autoreader.rapp.YinLiZiXun;
import cn.smiles.autoreader.rapp.YouKanTou_ReDianZiXun;
import cn.smiles.autoreader.rapp.YueKanYueZuan;
import cn.smiles.autoreader.service.IconService;
import cn.smiles.autoreader.service.RemoteService;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.view_overlap)
    View viewOverlap;
    private ArrayList<RApp> rApps;
    private ArrayList<RApp> myDataset;
    private MyAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView.setHasFixedSize(true);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        recyclerView.setLayoutManager(layoutManager);

        rApps = new ArrayList<>();
        myDataset = addMyDatas();
        mAdapter = new MyAdapter(myDataset);
        recyclerView.setAdapter(mAdapter);

        KPhone.cWidth = getResources().getDisplayMetrics().widthPixels;
        KPhone.cHeight = getResources().getDisplayMetrics().heightPixels;

        KTools.getRootPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (IconService.isRun) {
            viewOverlap.setVisibility(View.VISIBLE);
        } else {
            viewOverlap.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    private ArrayList<RApp> addMyDatas() {
        ArrayList<RApp> myDataset = new ArrayList<>();
        myDataset.add(new WeiLiKanKan());
        myDataset.add(new BaiWanKanDian());
        myDataset.add(new TianTianQuWen());
        myDataset.add(new DaZhongTouTiao());
        myDataset.add(new ErTouTiao());
        myDataset.add(new JuKanDian());
        myDataset.add(new MaYiTouTiao());
        myDataset.add(new QuKanDian());
        myDataset.add(new QuTouTiao());
        myDataset.add(new YinLiZiXun());
        myDataset.add(new ShanDianHeZi());
        myDataset.add(new SouHuZiXun());
        myDataset.add(new TouTiaoDuoDuo());
        myDataset.add(new XinTouTiao());
        myDataset.add(new YouKanTou_ReDianZiXun());
        myDataset.add(new YueKanYueZuan());

        for (int i = 0; i < myDataset.size(); i++) {
            RApp rApp = myDataset.get(i);
            rApp.sIndex = i + 1;
            if (rApp.isEnable) {
                rApp.isCheck = KTools.getBooleanPreference(rApp.packageName, false);
                if (rApp.isCheck)
                    rApps.add(rApp);
            }
        }
        return myDataset;
    }

    private void runStart() {
        if (IconService.isRun)
            return;
        if (rApps.isEmpty()) {
            KTools.showToast("必须选择要阅读的APP！！！");
        } else {
            viewOverlap.setVisibility(View.VISIBLE);
            KTools.showToast("即将开始阅读：" + rApps.size() + " 个APP");
            moveTaskToBack(true);

            IconService.isRun = true;
            Collections.sort(rApps, (t, t1) -> Integer.compare(t.sIndex, t1.sIndex));

            Intent intent = new Intent(this.getApplicationContext(), IconService.class);
            intent.putExtra("rApps", rApps);
            intent.putExtra("iShow", true);
            startService(intent);
            startService(new Intent(this.getApplicationContext(), RemoteService.class));
        }
    }

    private void runStop() {
        if (!IconService.isRun)
            return;
        viewOverlap.setVisibility(View.INVISIBLE);
        IconService.isRun = false;
        Intent intent = new Intent(this.getApplicationContext(), IconService.class);
        intent.putExtra("iShow", false);
        startService(intent);
        KTools.showToastorLong(this, "停止中……");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_stop_reader) {
            runStop();
        }
        if (IconService.isRun) {
            KTools.showToast("阅读进行中\n设置必须先停止阅读");
            return super.onOptionsItemSelected(item);
        }
        int id = item.getItemId();
        switch (id) {
            case R.id.action_start_reader:
                runStart();
                break;
//            case R.id.action_stop_reader:
//                runStop();
//                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                break;
            case R.id.action_app_sys_setting:
                showInstalledAppDetails(getPackageName());
                break;
            case R.id.all_checked:
                for (RApp rApp : myDataset) {
                    rApp.isCheck = true;
                    if (!rApps.contains(rApp))
                        rApps.add(rApp);
                    KTools.setBooleanPreference(rApp.packageName, rApp.isCheck);
                }
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.not_all_checked:
                for (RApp rApp : myDataset) {
                    rApp.isCheck = false;
                    KTools.setBooleanPreference(rApp.packageName, rApp.isCheck);
                }
                rApps.clear();
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.open_display_page:
                startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS));
//                KPhone.startApp("com.android.settings/.SubSettings");
                break;
            case R.id.exit_app:
                KTools.showDialog(this, "确定退出阅读程序吗？", (dialogInterface, i) -> Process.killProcess(Process.myPid()));
                break;
            case R.id.test_01:
                KTools.showToast("。。。");
//                new Thread(ClearTools::clearAPK).start();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        moveTaskToBack(true);
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private ArrayList<RApp> mDataset;

        class MyViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkBox;

            MyViewHolder(View v) {
                super(v);
                checkBox = v.findViewById(R.id.checkBox);
            }
        }

        MyAdapter(ArrayList<RApp> myDataset) {
            mDataset = myDataset;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_text_view, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            RApp myData = mDataset.get(position);
            holder.checkBox.setText(myData.appName);
            holder.checkBox.setChecked(myData.isCheck);
            holder.checkBox.setEnabled(myData.isInstalledApp());
            holder.checkBox.setEnabled(myData.isEnable);
            holder.checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
                RApp rApp = (RApp) compoundButton.getTag();
                KTools.setBooleanPreference(rApp.packageName, b);
                rApp.isCheck = b;
                if (b) {
                    if (!rApps.contains(rApp))
                        rApps.add(rApp);
                } else {
                    rApps.remove(rApp);
                }
            });
            if (myData.isEnable) {
                holder.checkBox.setOnClickListener(null);
            } else {
                holder.checkBox.setOnClickListener(view -> {
                    KTools.showToast("没有安装 " + myData.appName + " APP~");
                });
            }
            holder.checkBox.setTag(myData);
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void showInstalledAppDetails(String packageName) {
        final int apiLevel = Build.VERSION.SDK_INT;
        Intent intent = new Intent();

        if (apiLevel >= 9) {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + packageName));
        } else {
            final String appPkgName = (apiLevel == 8 ? "pkg" : "com.android.settings.ApplicationPkgName");

            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra(appPkgName, packageName);
        }
        // Start Activity
        startActivity(intent);
    }
}
