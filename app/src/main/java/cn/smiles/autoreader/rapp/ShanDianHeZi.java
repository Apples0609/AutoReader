package cn.smiles.autoreader.rapp;

import cn.smiles.autoreader.ktool.KPhone;
import cn.smiles.autoreader.ktool.KTools;
import cn.smiles.autoreader.service.IconService;

public class ShanDianHeZi extends RApp {

    private final int READNUMBER = 16;//阅读文章数量

    public ShanDianHeZi() {
        appName = "闪电盒子";
        packageName = "c.l.a";
        launchIntent = "c.l.a/.views.AppBoxWelcomeActivity";
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
                if (!IconService.isRun) return;
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
            KTools.sleep(0.3);
            KPhone.pressBackButton();
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
            if (!IconService.isRun) return;
            KTools.showToast(KTools.sformat("%s 阅读第 %d 篇，滑动 %d/%d", appName, si, i, f));
            KPhone.swipe(383, 1060, 437, 283);
            KTools.sleep(KTools.getRandomNumberInRange(3, 6));
        }
        KPhone.pressBackButton();
        KTools.sleep(3);
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
        // 拆红包
        KTools.sleep(6);
        KPhone.click(65, 203);

        KTools.sleep(3);//1红包
        KPhone.click(82, 542);
        KTools.sleep(1);
        KPhone.pressBackButton();
        KTools.sleep(3);//2红包
        KPhone.click(228, 542);
        KTools.sleep(1);
        KPhone.pressBackButton();
        KTools.sleep(3);//3红包
        KPhone.click(362, 542);
        KTools.sleep(1);
        KPhone.pressBackButton();
        KTools.sleep(3);//4红包
        KPhone.click(513, 542);
        KTools.sleep(1);
        KPhone.pressBackButton();
        KTools.sleep(3);//5红包
        KPhone.click(650, 542);
        KTools.sleep(1);
        KPhone.pressBackButton();
        KTools.sleep(3);//6红包
        KPhone.click(82, 686);
        KTools.sleep(1);
        KPhone.pressBackButton();
        KTools.sleep(3);//7红包
        KPhone.click(228, 686);
        KTools.sleep(1);
        KPhone.pressBackButton();
        KTools.sleep(3);//8红包
        KPhone.click(362, 686);
        KTools.sleep(1);
        KPhone.pressBackButton();
        KTools.sleep(3);//9红包
        KPhone.click(513, 686);
        KTools.sleep(1);
        KPhone.pressBackButton();

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
        KPhone.click(394, 919);
        KTools.sleep(3);
        KPhone.pressBackButton();
        KTools.sleep(3);
        KPhone.click(75, 1224);
        refreshList();
    }

    /**
     * 领取时段奖励
     */
    @Override
    protected void timeReward() {
        KTools.showToast(KTools.sformat("领取 %s 时段奖励…", appName));
        KTools.sleep(3);
        KPhone.click(676, 939);
        KTools.sleep(3);
        KPhone.click(676, 939);
        KTools.sleep(3);
        KPhone.click(345, 1054);
    }

}
