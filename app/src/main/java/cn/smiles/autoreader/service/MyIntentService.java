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
import cn.smiles.autoreader.rapp.BaiWanKanDian;
import cn.smiles.autoreader.rapp.ClearTools;
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

public class MyIntentService extends IntentService {
    private static final String ACTION_FOO = "cn.smiles.autoreader.service.action.FOO";
    public static boolean isRun;

    public MyIntentService() {
        super("MyIntentService");
    }

    public static void startActionService(Context context) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_FOO);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(final Intent intent) {
        if (intent != null) {
            runAotuReader();
        }
    }

    private void runAotuReader() {
        new Thread(() -> {
            for (; ; ) {
                ArrayList<RApp> rApps = new ArrayList<>(getAllReadsApp());
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
                rApps.clear();
                System.gc();
            }
        }).start();
    }

    /**
     * 获取需要阅读的APP实例集合
     *
     * @return
     */
    private ArrayList<RApp> getAllReadsApp() {
        ArrayList<RApp> rApps = new ArrayList<>();
        isReadRApp(rApps, new WeiLiKanKan());
        isReadRApp(rApps, new BaiWanKanDian());
        isReadRApp(rApps, new TianTianQuWen());
        isReadRApp(rApps, new DaZhongTouTiao());
        isReadRApp(rApps, new ErTouTiao());
        isReadRApp(rApps, new JuKanDian());
        isReadRApp(rApps, new MaYiTouTiao());
        isReadRApp(rApps, new QuKanDian());
        isReadRApp(rApps, new QuTouTiao());
        isReadRApp(rApps, new YinLiZiXun());
        isReadRApp(rApps, new ShanDianHeZi());
        isReadRApp(rApps, new SouHuZiXun());
        isReadRApp(rApps, new TouTiaoDuoDuo());
        isReadRApp(rApps, new XinTouTiao());
        isReadRApp(rApps, new YouKanTou_ReDianZiXun());
        isReadRApp(rApps, new YueKanYueZuan());
        return rApps;
    }

    /**
     * 判断是否需要
     *
     * @param rApps
     * @param rApp
     */
    private void isReadRApp(ArrayList<RApp> rApps, RApp rApp) {
        boolean b = KTools.getBooleanPreference(rApp.packageName, false);
        if (b)
            rApps.add(rApp);
    }

}
