package cn.smiles.autoreader.ktool;

/**
 * 屏幕坐标转换
 */
public class KPhone {
    //基准屏幕宽高
    private static final double WIDTH = 720.0;
    private static final double HEIGHT = 1280.0;

    public static int cWidth;
    public static int cHeight;

    /**
     * 转换 x 坐标
     *
     * @param x
     * @return
     */
    public static double getX(double x) {
        return x / WIDTH * cWidth;
    }

    /**
     * 转换 y 坐标
     *
     * @param y
     * @return
     */
    public static double getY(double y) {
        return y / HEIGHT * cHeight;
    }

    /**
     * 滑动屏幕
     *
     * @param x1 起始坐标x
     * @param y1 起始坐标y
     * @param x2 终止坐标x
     * @param y2 终止坐标y
     * @return
     */
    public static boolean swipe(double x1, double y1, double x2, double y2) {
        double x11 = getX(x1);
        double y11 = getY(y1);
        double x22 = getX(x2);
        double y22 = getY(y2);
        return KTools.executeCommand(KTools.sformat("input touchscreen swipe %f %f %f %f", x11, y11, x22, y22));
    }

    /**
     * 启动指定包名APP
     *
     * @param launchIntent app启动 activity
     * @return
     */
    public static boolean startApp(String launchIntent) {
        return KTools.executeCommand(KTools.sformat("am start -n %s", launchIntent));
    }

    /**
     * 点击屏幕指定位置
     *
     * @param x 点击x
     * @param y 点击y
     * @return
     */
    public static boolean click(double x, double y) {
        double x2 = KPhone.getX(x);
        double y2 = KPhone.getY(y);
        return KTools.executeCommand(KTools.sformat("input tap %f %f", x2, y2));
    }

    /**
     * 按 HOME 键
     *
     * @return
     */
    public static boolean pressHomeButton() {
        return KTools.executeCommand("input keyevent KEYCODE_HOME");
    }

    /**
     * 按 BACK 键
     *
     * @return
     */
    public static boolean pressBackButton() {
        return KTools.executeCommand("input keyevent KEYCODE_BACK");
    }


    /**
     * Injects Unlock-Event for unlocking the device's screen
     *
     * @return If execution of shell-command was successful or not
     */
    public static boolean unlockDevice() {
        return KTools.executeCommand("input keyevent 82");
    }

    /**
     * Injects Powerbutton-Event for locking or activate the device's screen
     *
     * @return If execution of shell-command was successful or not
     */
    public static boolean pressPowerButton() {
        return KTools.executeCommand("input keyevent 26");
    }

    /**
     * Injects Swipe-Event (up to down) for opening the Notificationcenter
     *
     * @return If execution of shell-command was successful or not
     */
    public static boolean showNotificationCenter() {
        return KTools.executeCommand("input swipe 10 10 10 1000");
    }

    /**
     * 屏幕截图
     *
     * @return
     */
    public static boolean screenCapture() {
        return KTools.executeCommand("input keyevent 120");
    }
}
