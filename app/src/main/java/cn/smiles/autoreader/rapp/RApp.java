package cn.smiles.autoreader.rapp;

import java.io.Serializable;

import cn.smiles.autoreader.ktool.KTools;

public abstract class RApp implements Serializable {

    //CheckBox 属性
    public boolean isCheck;
    public boolean isEnable;
    public boolean isRed;//红色表示已阅读
    public int sIndex;//界面显示下标

    //APP 特有属性
    public String appName;
    public String packageName;
    String launchIntent;

    RApp() {
    }

    /**
     * run main
     */
    public abstract void runApp();

    /**
     * 阅读新闻
     *
     * @param si 已阅读文章篇数
     */
    protected abstract void reading(int si);

    /**
     * 刷新新闻列表
     */
    protected abstract void refreshList();

    /**
     * 领取时段奖励
     */
    protected abstract void timeReward();

    /**
     * 滑动翻页
     */
    protected abstract void checkInReward();

    /**
     * 检查是否安装此APP
     *
     * @return 返回是否
     */
    public boolean isInstalledApp() {
        return KTools.isPackageInstalled(packageName);
    }
}
