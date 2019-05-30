package cn.smiles.autoreader.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import cn.smiles.autoreader.KApplication;
import cn.smiles.autoreader.activity.SettingActivity;
import cn.smiles.autoreader.ktool.KTools;
import cn.smiles.autoreader.rapp.ClearTools;
import cn.smiles.autoreader.rapp.RApp;

public class MyIntentService extends IntentService {
    private static final String ACTION_FOO = "cn.smiles.autoreader.service.action.FOO";
    private ArrayList<RApp> rApps;
    public static boolean isRun;

    public MyIntentService() {
        super("MyIntentService");
    }

    public static void startActionService(Context context, ArrayList<RApp> rApps) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.putExtra("rApps", rApps);
        intent.setAction(ACTION_FOO);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(final Intent intent) {
        if (intent != null) {
            //需要阅读的APP集合
            ArrayList<RApp> rApps = (ArrayList<RApp>) intent.getSerializableExtra("rApps");
            runAotuReader(rApps);
        }
    }

    private void runAotuReader(ArrayList<RApp> temp) {
        if (temp == null) return;
        rApps = new ArrayList<>(temp);
        new Thread(() -> {
            for (; ; ) {
                if (rApps.isEmpty()) {
                    KTools.showToast("待阅读 rApps==0");
                    return;
                }
                KTools.showToast("阅读服务启动中…");
                if (KTools.getBooleanPreference(SettingActivity.RANDOM_READING, false))
                    Collections.shuffle(rApps);//乱序阅读
                ClearTools.freeMemory();
                for (int i = 0; i < rApps.size(); i++) {
                    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    final int STARTHOUR = 0;//休眠开始时间，0
                    final int ENDHOUR = 5;//休眠结束时间，5 TODO
                    if (isRun && hour > STARTHOUR) {
                        if (hour < ENDHOUR) {
                            KTools.runAPPByPackageName(KApplication.context.getPackageName());
                            while (true) {
                                hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                                if (hour < ENDHOUR) {
                                    KTools.showToast("夜间休眠中……");
                                    KTools.sleep(2 * 60);
                                    continue;
                                }
                                break;
                            }
                        }
                    }
                    if (isRun) {
                        RApp rApp = rApps.get(i);
                        KTools.showToast("===" + (i + 1) + "/" + rApps.size() + "===");
                        rApp.runApp();
                    } else {
                        return;
                    }
                }
                ClearTools.clearAPK();
            }
        }).start();
    }

}
