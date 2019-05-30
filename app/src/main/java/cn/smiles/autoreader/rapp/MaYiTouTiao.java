package cn.smiles.autoreader.rapp;

import cn.smiles.autoreader.ktool.KPhone;
import cn.smiles.autoreader.ktool.KTools;
import cn.smiles.autoreader.service.MyIntentService;

public class MaYiTouTiao extends RApp {

    private final int READNUMBER = 16;//阅读文章数量

    public MaYiTouTiao() {
        appName = "蚂蚁头条";
        packageName = "com.ldzs.zhangxin";
        launchIntent = "com.ldzs.zhangxin/com.weishang.wxrd.activity.SplashActivity";
        isEnable = isInstalledApp();
    }

    /**
     * run main
     */
    @Override
    public void runApp() {
        KTools.sleep(2);
        if (isInstalledApp()) {
            KTools.showToast("正在打开 " + appName);
            boolean b2 = KTools.runAPPByPackageName(packageName);
            if (!b2) {
                KPhone.startApp(launchIntent);
            }
            KTools.sleep(20);
            for (int i = 1; i <= READNUMBER; i++) {
                if (!MyIntentService.isRun) return;
                KTools.showToast(KTools.sformat("%s 正在阅读 %d/%d 篇", appName, i, READNUMBER));
                reading(i);
                //时段奖励
                if (i % 3 == 0) {
                    timeReward();
                    refreshList();
                }
                //签到奖励
                if (i % 5 == 0) {
                    checkInReward();
                }
            }
            KTools.sleep(3);
            KPhone.pressBackButton();
            KTools.sleep(2);
            KPhone.click(355, 896);
            KTools.sleep(3);
            KPhone.pressHomeButton();
            ClearTools.freeMemory();
        } else {
            KTools.showToast(KTools.sformat("没有安装 %s", appName));
        }
    }

    /**
     * 阅读新闻
     *
     * @param si 已阅读文章篇数
     */
    @Override
    protected void reading(int si) {
        KTools.sleep(3);
        KPhone.swipe(383, 1060, 437, 283);
        KTools.sleep(3);
        KPhone.click(270, 472);
        KTools.sleep(9);
        int f = 6;
        for (int i = 1; i <= f; i++) {
            if (!MyIntentService.isRun) return;
            KTools.showToast(KTools.sformat("%s 阅读第 %d 篇，滑动 %d/%d", appName, si, i, f));
            KPhone.swipe(383, 1060, 437, 283);
            KTools.sleep(KTools.getRandomNumberInRange(3, 6));
        }
        KPhone.pressBackButton();
    }

    /**
     * 刷新新闻列表
     */
    @Override
    protected void refreshList() {
        KTools.showToast("刷新新闻列表…");
        KTools.sleep(3);
        KPhone.click(75, 1225);
    }

    /**
     * 领取签到奖励
     */
    @Override
    protected void checkInReward() {
        KTools.showToast(KTools.sformat("领取 %s 签到奖励中…", appName));
        KTools.sleep(3);
        KPhone.click(512, 1229);
        KTools.sleep(3);
        KPhone.click(566, 120);
        KTools.sleep(2);
        KPhone.click(395, 1022);
        refreshList();
    }

    /**
     * 领取时段奖励
     */
    @Override
    protected void timeReward() {
        KTools.showToast(KTools.sformat("领取 %s 时段奖励中…", appName));
        KTools.sleep(3);
        KPhone.click(88, 92);
        KTools.sleep(5);
        KPhone.pressBackButton();
    }

}
