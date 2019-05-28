package cn.smiles.autoreader.rapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.IBinder;

import java.lang.reflect.Method;
import java.util.Objects;

import cn.smiles.autoreader.KApplication;
import cn.smiles.autoreader.ktool.KPhone;
import cn.smiles.autoreader.ktool.KTools;

public class ClearTools {

    /**
     * 清理下载的apk文件
     */
    public static void clearAPK() {
        KTools.showToast("清理下载的垃圾APK文件。。。");
        KTools.sleep(2);
        boolean b2 = KTools.runAPPByPackageName("com.meizu.safe");
        if (!b2) {
            KPhone.startApp("com.meizu.safe/.SecurityMainActivity");
        }
        KTools.sleep(6);
        KPhone.click(204, 832);
        KTools.sleep(16);
        KPhone.click(448, 1220);
        KTools.sleep(6);
        KPhone.click(638, 405);
        KTools.sleep(3);
        KPhone.click(649, 563);
        KTools.sleep(3);
        KPhone.click(665, 739);
        KTools.sleep(3);
        KPhone.click(651, 909);
        KTools.sleep(3);
        KPhone.click(378, 1230);
        KTools.sleep(3);
        KPhone.click(380, 1088);
        KTools.sleep(9);
        KPhone.pressBackButton();
        KTools.sleep(3);
        KPhone.pressBackButton();
        KTools.sleep(3);
        KPhone.pressBackButton();
        KTools.sleep(3);
        KPhone.pressHomeButton();
    }

    /**
     * 释放已占用
     */
    public static void freeMemory() {
        KTools.showToast("释放系统内存中。。。");
        goHOME();
        KTools.sleep(2);
        KTools.runAPPByPackageName(KApplication.context.getPackageName());
        KTools.sleep(2);
        try {
            @SuppressLint("PrivateApi") Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
            Method getService = serviceManagerClass.getMethod("getService", String.class);
            IBinder retbinder = (IBinder) getService.invoke(serviceManagerClass, "statusbar");
            Class<?> statusBarClass = Class.forName(Objects.requireNonNull(retbinder.getInterfaceDescriptor()));
            Object statusBarObject = statusBarClass.getClasses()[0].getMethod("asInterface", IBinder.class).invoke(null, retbinder);
            Method clearAll = statusBarClass.getMethod("toggleRecentApps");
            clearAll.setAccessible(true);
            clearAll.invoke(statusBarObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        KTools.sleep(3);
        KPhone.click(366, 1120);
    }

    private static void goHOME() {
        KTools.sleep(3);
//            KPhone.pressHomeButton();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        KApplication.context.startActivity(startMain);
    }
}
