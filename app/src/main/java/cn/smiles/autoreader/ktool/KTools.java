package cn.smiles.autoreader.ktool;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.Nullable;
import cn.smiles.autoreader.KApplication;
import cn.smiles.autoreader.R;

public class KTools {

    /**
     * 获取区间随机整数
     *
     * @param min
     * @param max
     * @return
     */
    public static int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    /**
     * String 格式化
     *
     * @param str
     * @param obj
     * @return
     */
    public static String sformat(String str, Object... obj) {
        return String.format(Locale.getDefault(), str, obj);
    }

    /**
     * 线程休眠
     *
     * @param c 单位秒
     */
    public static void sleep(double c) {
        if (c > 0)
            SystemClock.sleep(Double.valueOf(c * 1000).intValue());
    }

    /**
     * 通过包名运行APP
     *
     * @param spackage
     * @return
     */
    public static boolean runAPPByPackageName(String spackage) {
        if (TextUtils.isEmpty(spackage))
            return false;
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(spackage);
        if (launchIntent != null) {
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            KApplication.context.startActivity(launchIntent);
            return true;
        }
        return false;
    }

    /**
     * 显示 Toast
     *
     * @param m
     */
    public static void showToast(String m) {
        System.out.println(m);
        if (!TextUtils.isEmpty(m)) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Toast.makeText(KApplication.context, m, Toast.LENGTH_SHORT).show();
            } else {
                KApplication.handler.post(() -> {
                    Toast.makeText(KApplication.context, m, Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    /**
     * 执行命令行
     *
     * @param command
     * @return
     */
    public static boolean executeCommand(String command) {
        try {
            Process suShell = Runtime.getRuntime().exec("su");
            DataOutputStream commandLine = new DataOutputStream(suShell.getOutputStream());
            commandLine.writeBytes(command + '\n');
            commandLine.flush();
            commandLine.writeBytes("exit\n");
            commandLine.flush();
            return suShell.waitFor() == 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 执行 su 命令获取 root 权限
     */
    public static void getRootPermission() {
        new Thread(() -> {
            try {
                Runtime.getRuntime().exec("su");
                KApplication.handler.post(() -> {
                    Toast.makeText(KApplication.context, "ROOT权限获取成功", Toast.LENGTH_LONG).show();
                });
            } catch (IOException e) {
                e.printStackTrace();
                KApplication.handler.post(() -> {
                    Toast.makeText(KApplication.context, "没有获取到ROOT权限！！！", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    /**
     * 获取 PackageManager 对象
     *
     * @return
     */
    public static PackageManager getPackageManager() {
        return KApplication.context.getPackageManager();
    }


    /**
     * 检查是否安装指定APP包
     *
     * @param packageName
     * @return
     */
    public static boolean isPackageInstalled(String packageName) {
        AtomicBoolean found = new AtomicBoolean(true);
        try {
            getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            found.set(false);
        }
        return found.get();
    }

    public static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(KApplication.context);
    }

    /**
     * Helper method to retrieve a String value from {@link SharedPreferences}.
     *
     * @return The value from shared preferences, or null if the value could not be read.
     */
    public static String getStringPreference(String key, String defaultValue) {
        String value = null;
        SharedPreferences preferences = getSharedPreferences();
        if (preferences != null) {
            value = preferences.getString(key, defaultValue);
        }
        return value;
    }

    /**
     * Helper method to write a String value to {@link SharedPreferences}.
     *
     * @param key
     * @param value
     * @return true if the new value was successfully written to persistent storage.
     */
    public static boolean setStringPreference(String key, String value) {
        SharedPreferences preferences = getSharedPreferences();
        if (preferences != null && !TextUtils.isEmpty(key)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, value);
            return editor.commit();
        }
        return false;
    }

    /**
     * Helper method to retrieve a float value from {@link SharedPreferences}.
     *
     * @param key
     * @param defaultValue A default to return if the value could not be read.
     * @return The value from shared preferences, or the provided default.
     */
    public static float getFloatPreference(String key, float defaultValue) {
        float value = defaultValue;
        SharedPreferences preferences = getSharedPreferences();
        if (preferences != null) {
            value = preferences.getFloat(key, defaultValue);
        }
        return value;
    }

    /**
     * Helper method to write a float value to {@link SharedPreferences}.
     *
     * @param key
     * @param value
     * @return true if the new value was successfully written to persistent storage.
     */
    public static boolean setFloatPreference(String key, float value) {
        SharedPreferences preferences = getSharedPreferences();
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putFloat(key, value);
            return editor.commit();
        }
        return false;
    }

    /**
     * Helper method to retrieve a long value from {@link SharedPreferences}.
     *
     * @param key
     * @param defaultValue A default to return if the value could not be read.
     * @return The value from shared preferences, or the provided default.
     */
    public static long getLongPreference(String key, long defaultValue) {
        long value = defaultValue;
        SharedPreferences preferences = getSharedPreferences();
        if (preferences != null) {
            value = preferences.getLong(key, defaultValue);
        }
        return value;
    }

    /**
     * Helper method to write a long value to {@link SharedPreferences}.
     *
     * @param key
     * @param value
     * @return true if the new value was successfully written to persistent storage.
     */
    public static boolean setLongPreference(String key, long value) {
        SharedPreferences preferences = getSharedPreferences();
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(key, value);
            return editor.commit();
        }
        return false;
    }

    /**
     * Helper method to retrieve an integer value from {@link SharedPreferences}.
     *
     * @param key
     * @param defaultValue A default to return if the value could not be read.
     * @return The value from shared preferences, or the provided default.
     */
    public static int getIntegerPreference(String key, int defaultValue) {
        int value = defaultValue;
        SharedPreferences preferences = getSharedPreferences();
        if (preferences != null) {
            value = preferences.getInt(key, defaultValue);
        }
        return value;
    }

    /**
     * Helper method to write an integer value to {@link SharedPreferences}.
     *
     * @param key
     * @param value
     * @return true if the new value was successfully written to persistent storage.
     */
    public static boolean setIntegerPreference(String key, int value) {
        SharedPreferences preferences = getSharedPreferences();
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(key, value);
            return editor.commit();
        }
        return false;
    }

    /**
     * Helper method to retrieve a boolean value from {@link SharedPreferences}.
     *
     * @param key
     * @param defaultValue A default to return if the value could not be read.
     * @return The value from shared preferences, or the provided default.
     */
    public static boolean getBooleanPreference(String key, boolean defaultValue) {
        boolean value = defaultValue;
        SharedPreferences preferences = getSharedPreferences();
        if (preferences != null) {
            value = preferences.getBoolean(key, defaultValue);
        }
        return value;
    }

    /**
     * Helper method to write a boolean value to {@link SharedPreferences}.
     *
     * @param key
     * @param value
     * @return true if the new value was successfully written to persistent storage.
     */
    public static boolean setBooleanPreference(String key, boolean value) {
        SharedPreferences preferences = getSharedPreferences();
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(key, value);
            return editor.commit();
        }
        return false;
    }


    /**
     * 改变app语言位置
     *
     * @param activity
     * @param locale
     */
    @SuppressWarnings("deprecation")
    public static void setLocale(Activity activity, Locale locale) {
        int ll = 1;//默认中文 1：中文 2：英文
        if (locale == Locale.ENGLISH) {
            ll = 2;
        }
        setIntegerPreference("localeLang", ll);
        Resources resources = activity.getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity.getApplicationContext().createConfigurationContext(configuration);
        } else {
            resources.updateConfiguration(configuration, displayMetrics);
        }
    }

    /**
     * 通过id获取string.xml中字符串
     *
     * @param context
     * @param resid
     * @return
     */
    public static String getStr8Res(Context context, int resid) {
        return context.getResources().getString(resid);
    }

    /**
     * 通过id获取string.xml根据参数格式化字符串
     *
     * @param context
     * @param resid
     * @param objs
     * @return
     */
    public static String getStr8Res2(Context context, int resid, Object... objs) {
        return context.getResources().getString(resid, objs);
    }

    public static String md5(String string) {
        byte[] encodeBytes = null;
        try {
            encodeBytes = MessageDigest.getInstance("MD5").digest(string.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException neverHappened) {
            throw new RuntimeException(neverHappened);
        }
        return toHexString(encodeBytes);
    }

    private static final char[] hexDigits =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String toHexString(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(hexDigits[(b >> 4) & 0x0F]);
            hex.append(hexDigits[b & 0x0F]);
        }
        return hex.toString();
    }

    /**
     * 加载本地apk文件的icon
     *
     * @param filePath
     * @param context
     * @return
     */
    public static String loadAPKIcon(String filePath, Context context) {
        try {
            File ipath = new File(KApplication.context.getExternalCacheDir(), "apkPicTHRUMB");
            if (!ipath.exists())
                ipath.mkdirs();
            File ifile = new File(ipath, md5(filePath));
            if (!ifile.exists()) {
                PackageManager packageManager = context.getPackageManager();
                PackageInfo packageInfo = packageManager.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
                if (packageInfo != null) {
                    ApplicationInfo appInfo = packageInfo.applicationInfo;
                    appInfo.sourceDir = filePath;
                    appInfo.publicSourceDir = filePath;
                    Drawable icon = appInfo.loadIcon(packageManager);
                    Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
                    KTools.createDirorFileParentDir(ipath);
                    FileOutputStream fos = new FileOutputStream(ifile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.close();
                }
            }
            return ifile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 生成指定区间随机数
     *
     * @param Min
     * @param Max
     * @return
     */
    public static int buildRandom(int Min, int Max) {
        return Min + (int) (Math.random() * (Max - Min + 1));
    }

    /**
     * 检测是否有效端口，端口未被占用
     *
     * @param port
     * @return
     */
    public static boolean isAvailableLocalPort(int port) {
        try {
            // ServerSocket try to open a LOCAL port
            new ServerSocket(port).close();
            // local port can be opened, it's available
            return true;
        } catch (IOException e) {
            // local port cannot be opened, it's in use
            return false;
        }
    }


    /**
     * 格式化文件大小
     *
     * @param activity
     * @param size
     * @return
     */
    public static String getFileFormatSize(Activity activity, long size) {
        return Formatter.formatFileSize(activity, size);
    }

    /**
     * 通过文件后缀获取文件夹类型
     *
     * @param fname
     * @return
     */
    public static String getMimeTypeFromExtension(String fname) {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(fname));
    }

    /**
     * URL 编码特殊字符
     *
     * @param url
     * @return
     */
    public static String urlEncode(String url) {
        //修正反斜杠为斜杠
        url = url.replace("\\", "/");
        //使用长文本代替要保留字符串
        url = url.replace(":", "_*colon*_")
                .replace("/", "_*slash*_")
                .replace("\\", "_*backslash*_")
                .replace(" ", "_*blank*_")
                .replace("?", "_*question*_")
                .replace("=", "_*equal*_")
                .replace(";", "_*semicolon*_");
        //进行编码
        try {
            url = URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        url = url.replace("_*colon*_", ":")
                .replace("_*slash*_", "/")
                .replace("_*backslash*_", "\\")
                .replace("_*blank*_", "%20")
                .replace("_*question*_", "?")
                .replace("_*equal*_", "=")
                .replace("_*semicolon*_", ";");
        return url;
    }

    /**
     * 是否有效的文件名称
     *
     * @param file
     * @return
     */
    public static boolean isFilenameValid(String file) {
        File f = new File(file);
        try {
            f.getCanonicalPath();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 检验文件md5
     *
     * @return
     */
    public static boolean checkMD5(String md5, File updateFile) {
        if (TextUtils.isEmpty(md5) || updateFile == null) {
            return false;
        }

        String calculatedDigest = MD5(updateFile);
        if (calculatedDigest == null) {
            return false;
        }
        return calculatedDigest.equalsIgnoreCase(md5);
    }


    /**
     * 通过文件路径获取文件md5
     *
     * @return
     */
    public static String MD5(String updateFile) {
        return MD5(new File(updateFile));
    }

    /**
     * 通过文件路径获取文件md5
     *
     * @return
     */
    public static String MD5(File updateFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 截取字节数据一部分
     *
     * @param original
     * @param from
     * @param to
     * @return
     */
    public static byte[] copyOfRange(byte[] original, int from, int to) {
        return Arrays.copyOfRange(original, from, to);
    }

    /**
     * 截取字节数据从0开始到指定位置
     *
     * @param original
     * @param to
     * @return
     */
    public static byte[] copyOfRange(byte[] original, int to) {
        return copyOfRange(original, 0, to);
    }

    /**
     * Append the given byte arrays to one big array
     *
     * @param arrays The arrays to append
     * @return The complete array containing the appended data
     */
    public static final byte[] append(byte[]... arrays) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (arrays != null) {
            for (final byte[] array : arrays) {
                if (array != null) {
                    out.write(array, 0, array.length);
                }
            }
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    private static long lastClickTime;
    private final static int SPACE_TIME = 500;

    /**
     * 500毫秒内，防止多次重复调用 点击
     *
     * @return
     */
    public static boolean preventDoubleClick() {
        return preventDoubleClick2(SPACE_TIME);
    }

    /**
     * 初始化最后点击时间，防止页面跳转时，生效
     */
    public static void initLastClickTime() {
        lastClickTime = 0;
    }

    /**
     * 指定毫秒内，防止多次重复调用 点击
     *
     * @return
     */
    public static boolean preventDoubleClick2(int space_time2) {
        long currentTime = SystemClock.uptimeMillis();
        boolean isClick = true;
        if (currentTime - lastClickTime > space_time2) {
            isClick = false;
        }
        lastClickTime = currentTime;
        return isClick;
    }

    /**
     * 运行内容在主线程
     *
     * @param runnable
     */
    public static void runMainThread(Runnable runnable) {
        if (runnable == null) return;
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            KApplication.handler.post(runnable);
        }
    }

    /**
     * 显示单个长toast
     *
     * @param context
     * @param msg
     */
    public static void showToastorLong(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * 显示单个短toast
     *
     * @param context
     * @param msg
     */
    public static void showToastorShort(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    private static ProgressDialog progressDialog;
    public final static String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    /**
     * 获取WiFi连接地址
     *
     * @param context
     * @return
     */
    public static String getWiFiAddress(Context context) {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                int ipAddress = wifiInfo.getIpAddress();
                return (ipAddress & 0xFF) + "." +
                        ((ipAddress >> 8) & 0xFF) + "." +
                        ((ipAddress >> 16) & 0xFF) + "." +
                        (ipAddress >> 24 & 0xFF);
            }
        }
        return "";
    }


    /**
     * 获取路由ip地址
     *
     * @param context
     * @return
     */
    public static String getRouterAddress(Context context) {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            final DhcpInfo dhcp = wifiManager.getDhcpInfo();
            if (dhcp != null) {
                return Formatter.formatIpAddress(dhcp.gateway);
            }
        }
        return null;
    }

    /**
     * 获取当前时间0点的毫秒值
     *
     * @return
     */
    public static String getNowDate0TimeMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return String.valueOf(calendar.getTimeInMillis());
    }

    /**
     * 获取当前时间日期的毫秒值
     *
     * @return
     */
    public static String getNowDateTimeMillis() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 转换字符串日期时间毫秒值为格式化字符串，eg： 2016-11-11 11:11:11
     *
     * @param dateMillis
     * @return
     */
    public static String getFormatDateTime(String dateMillis) {
        return new SimpleDateFormat(dateTimeFormat, Locale.getDefault()).format(new Date(Long.parseLong(dateMillis)));
    }

    /**
     * 转换日期时间毫秒值为格式化字符串，eg： 2016-11-11 11:11:11
     *
     * @param dateMillis
     * @return
     */
    public static String getFormatDateTime2(long dateMillis) {
        return new SimpleDateFormat(dateTimeFormat, Locale.getDefault()).format(new Date(dateMillis));
    }

    /**
     * 转换日期时间秒值为格式化字符串，eg： 2016-11-11 11:11:11
     *
     * @param dateMillis
     * @return
     */
    public static String getFormatDateTime(long dateMillis) {
        return getFormatDateTime2(dateMillis * 1000);
    }

    /**
     * 获取当前时间日期的毫秒值格式化字符串，eg：2016-11-11 11:11:11
     *
     * @return
     */
    public static String getNowFormatDateTime() {
        return new SimpleDateFormat(dateTimeFormat, Locale.getDefault()).format(new Date());
    }

    /**
     * 获取当前时间日期的毫秒值+指定毫秒后，的格式化字符串，eg：2016-11-11 11:11:11
     *
     * @return
     */
    public static String getNowFormatDateTime(long addMillisecond) {
        return new SimpleDateFormat(dateTimeFormat, Locale.getDefault()).format(new Date(new Date().getTime() + addMillisecond));
    }

    /**
     * 转换格式化字符串日期时间eg： 2016-11-11 11:11:11，为毫秒值字符串
     *
     * @param dateStr
     * @return
     */
    public static String getFormatStr2Millis(String dateStr) {
        try {
            return String.valueOf(new SimpleDateFormat(dateTimeFormat, Locale.getDefault()).parse(dateStr).getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 日期格式化，根据传进来样式
     *
     * @param date
     * @param pattern
     * @return
     */
    public static String dateFormat(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * 等待进度对话框
     *
     * @param activity
     * @param msg
     * @param iscancel
     * @return
     */
    public static ProgressDialog showProgressDialog(Activity activity, String msg, boolean iscancel) {
        if (progressDialog == null || progressDialog.getContext() != activity) {
            progressDialog = ProgressDialog.show(activity, null, msg, true, iscancel);
        }
        return progressDialog;
    }

    /**
     * 关闭progress进度框
     */
    public static void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    /**
     * 等待进度对话框 不能取消
     *
     * @param activity
     * @param msg
     */
    public static ProgressDialog showProgressDialog(Activity activity, String msg) {
        return showProgressDialog(activity, msg, false);
    }

    /**
     * 等待进度对话框，可取消，待取消按钮及回调
     *
     * @param activity
     * @param msg
     * @param listener
     * @return
     */
    public static void showProgressDialog2(Activity activity, String msg, DialogInterface.OnClickListener listener) {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, KTools.getStr8Res(activity, R.string.cancel_text), listener);
        progressDialog.show();
    }

    /**
     * 请稍候等待对话框 不能取消
     *
     * @param activity
     */
    public static ProgressDialog showProgressDialog(Activity activity) {
        return showProgressDialog(activity, KTools.getStr8Res(activity, R.string.please_wait_ing));
    }

    /**
     * 显示一个提示框
     *
     * @param activity
     * @param title
     * @param msg
     * @param pbtn
     * @param listener1
     * @param nbtn
     * @param listener2
     */
    public static void showDialog(Activity activity, String title, String msg,
                                  String pbtn, DialogInterface.OnClickListener listener1, String nbtn, DialogInterface.OnClickListener listener2, boolean isCancel) {
        if (activity != null && !activity.isFinishing())
            new AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(msg)
                    .setPositiveButton(pbtn, listener1)
                    .setNegativeButton(nbtn, listener2)
                    .setCancelable(isCancel)
                    .create().show();
    }


    /**
     * 带有下次不再提示 alertdialog
     *
     * @param activity
     * @param msg
     * @param cancelLis
     * @param okLis
     */
    public static void showNoTipDialog(Activity activity, String msg, boolean isCancel,
                                       String cancelText, View.OnClickListener cancelLis, String okText, View.OnClickListener okLis) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(isCancel);
        View view = LayoutInflater.from(activity).inflate(R.layout.alert_dialog_notip_layout, null);
        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        TextView message = (TextView) view.findViewById(R.id.message);
        message.setText(msg);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        Button cancelBtn = (Button) view.findViewById(R.id.btn_cancel);
        cancelBtn.setOnClickListener(cancelLis);
        cancelBtn.setText(cancelText);
        cancelBtn.setTag(alertDialog);
        cancelBtn.setTag(R.layout.alert_dialog_notip_layout, checkBox);
        Button okBtn = (Button) view.findViewById(R.id.btn_ok);
        okBtn.setText(okText);
        okBtn.setOnClickListener(okLis);
        okBtn.setTag(alertDialog);
        okBtn.setTag(R.layout.alert_dialog_notip_layout, checkBox);
        alertDialog.show();
    }

    /**
     * 显示提示框，带确定 取消回调，确定 取消文字自定义，按外部不可以取消
     *
     * @param activity
     * @param msg
     */
    public static void showDialog(Activity activity, String msg,
                                  String pbtn, DialogInterface.OnClickListener listener1, String nbtn, DialogInterface.OnClickListener listener2) {
        showDialog(activity, KTools.getStr8Res(activity, R.string.prompt_text), msg, pbtn, listener1, nbtn, listener2, false);
    }

    /**
     * 显示提示框，带确定 取消回调，确定 取消文字自定义，按外部可以取消
     *
     * @param activity
     * @param msg
     */
    public static void showDialog2(Activity activity, String msg,
                                   String pbtn, DialogInterface.OnClickListener listener1, String nbtn, DialogInterface.OnClickListener listener2) {
        showDialog(activity, KTools.getStr8Res(activity, R.string.prompt_text), msg, pbtn, listener1, nbtn, listener2, true);
    }

    /**
     * 显示提示框，无回调，带确定按钮，按外部不可以取消
     *
     * @param activity
     * @param msg
     */
    public static void showDialog(Activity activity, String msg) {
        showDialog(activity, KTools.getStr8Res(activity, R.string.prompt_text), msg, KTools.getStr8Res(activity, R.string.ok_text_text), null, null, null, false);
    }

    /**
     * 显示提示框，无回调，带确定按钮，按外部可以取消
     *
     * @param activity
     * @param msg
     */
    public static void showDialog2(Activity activity, String msg) {
        showDialog(activity, KTools.getStr8Res(activity, R.string.prompt_text), msg, KTools.getStr8Res(activity, R.string.ok_text_text), null, null, null, true);
    }

    /**
     * 显示提示框，带确定 取消按钮，带确定回调，按外部不可取消
     *
     * @param activity
     * @param msg
     * @param listener
     */
    public static void showDialog(Activity activity, String msg, DialogInterface.OnClickListener listener) {
        showDialog(activity, KTools.getStr8Res(activity, R.string.prompt_text), msg, KTools.getStr8Res(activity, R.string.ok_text_text), listener,
                KTools.getStr8Res(activity, R.string.cancel_text), null, false);
    }

    /**
     * 显示弹框，指定标题，带确定 取消按钮，带确定回调，按外部不可取消
     *
     * @param activity
     * @param msg
     * @param listener
     */
    public static void showDialog(Activity activity, String title, String msg, DialogInterface.OnClickListener listener) {
        showDialog(activity, title, msg, KTools.getStr8Res(activity, R.string.ok_text_text), listener, KTools.getStr8Res(activity, R.string.cancel_text), null, false);
    }

    /**
     * 显示提示框，带确定回调，带确定按钮，按外部不可以取消
     *
     * @param activity
     * @param msg
     * @param listener
     */
    public static void showDialog2(Activity activity, String msg, DialogInterface.OnClickListener listener) {
        showDialog(activity, KTools.getStr8Res(activity, R.string.prompt_text), msg, KTools.getStr8Res(activity, R.string.ok_text_text), listener, null, null, false);
    }

    /**
     * 显示提示框，带确定 取消按钮，带确定 取消回调，按外部不可取消
     *
     * @param activity
     * @param msg
     * @param listener
     */
    public static void showDialog(Activity activity, String msg, DialogInterface.OnClickListener listener, DialogInterface.OnClickListener listener2) {
        showDialog(activity, KTools.getStr8Res(activity, R.string.prompt_text), msg, KTools.getStr8Res(activity, R.string.ok_text_text), listener,
                KTools.getStr8Res(activity, R.string.cancel_text), listener2, false);
    }

    /**
     * dp转px
     *
     * @param context
     * @param dp
     * @return
     */
    public static int dp2px(Context context, int dp) {
        return Math.round(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
    }

    /**
     * 切换键盘显示与否状态
     *
     * @param activity 上下文
     */
    public static void toggleSoftInput(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    /**
     * 隐藏软键盘
     *
     * @param activity
     */
    public static void hideSoftMethod(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view == null)
            view = new View(activity);
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    /**
     * 系统软键盘不弹出 但显示光标
     *
     * @param ed
     */
    public static void hideSoftInputMethod(EditText ed) {
        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        try {
            int currentVersion = android.os.Build.VERSION.SDK_INT;
            String methodName = null;
            if (currentVersion >= 16) {
                // 4.2
                methodName = "setShowSoftInputOnFocus";
            } else if (currentVersion >= 14) {
                // 4.0
                methodName = "setSoftInputShownOnFocus";
            }
            if (methodName == null) {
                ed.setInputType(InputType.TYPE_NULL);
            } else {
                Class<EditText> cls = EditText.class;
                Method setShowSoftInputOnFocus;
                setShowSoftInputOnFocus = cls.getMethod(methodName, boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(ed, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据给定文件路径创建文件夹
     *
     * @param filePpath
     */
    public static boolean createDirorFileParentDir(File filePpath) {
        if (filePpath == null) return false;
        if (filePpath.exists()) return true;
        if (filePpath.isFile()) {
            File parentFile = filePpath.getParentFile();
            if (!parentFile.exists())
                return parentFile.mkdirs();
        } else {
            if (!filePpath.exists())
                return filePpath.mkdirs();
        }
        return true;
    }

    /**
     * 根据给定字符串路径创建文件夹
     *
     * @param path
     */
    public static boolean createDirorFileParentDir(String path) {
        return createDirorFileParentDir(new File(path));
    }

    public static boolean sLastVisiable = false;
    public static int screenHeight;
    public static int softKeyboardHeight;

    /**
     * 获取连接WiFi的ssid
     *
     * @param context
     * @return
     */
    public static String getConnWifiSSID(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                return wifiInfo.getSSID().replace("\"", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 软键盘隐藏 显示回调
     *
     * @author kaifang
     */
    public static interface OnSoftKeyBoardVisibleListener {
        /**
         * 软键盘显示 隐藏回调
         *
         * @param visible      软键盘状态
         * @param scrollHeight 输入框遮盖时Y需滚动高度
         */
        void onSoftKeyBoardVisible(boolean visible, int scrollHeight);
    }

    /**
     * 监听软键盘状态，滚动装饰层View显示键盘遮盖的输入框
     *
     * @param activity
     * @param listener
     */
    public static void addOnSoftKeyBoardVisibleListener(final Activity activity,
                                                        final OnSoftKeyBoardVisibleListener listener) {
        final View decorView = activity.getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (screenHeight == 0)
                    screenHeight = decorView.getHeight();
                View currentFocus = activity.getCurrentFocus();
                int edittextHeight = 0;
                int[] location = new int[2];
                if (currentFocus != null) {
                    edittextHeight = currentFocus.getHeight();
                    currentFocus.getLocationOnScreen(location);
                }
                Rect rect = new Rect();
                decorView.getWindowVisibleDisplayFrame(rect);
                boolean visible = (double) rect.bottom / screenHeight < 1.0;
                if (softKeyboardHeight == 0)
                    softKeyboardHeight = screenHeight - rect.bottom;
                if (visible != sLastVisiable) {
                    int scrollHeight = location[1] + edittextHeight - softKeyboardHeight;
                    scrollHeight = (scrollHeight < 0) ? 0 : scrollHeight;
                    if (listener != null) {
                        listener.onSoftKeyBoardVisible(visible, scrollHeight);
                    } else {
                        if (decorView.getScrollY() != scrollHeight)
                            decorView.scrollTo(0, scrollHeight);
                    }
                }
                sLastVisiable = visible;
            }
        });
    }

    /**
     * 键盘遮盖EditText，滚动指定ViewGroup，显示输入框
     *
     * @param activity
     * @param viewGroup
     * @param listener
     */
    public static void addOnSoftKeyBoardVisibleListener(final Activity activity, final ViewGroup viewGroup,
                                                        final OnSoftKeyBoardVisibleListener listener) {
        final View decorView = activity.getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (screenHeight == 0)
                    screenHeight = decorView.getHeight();
                View currentFocus = activity.getCurrentFocus();
                int edittextHeight = 0;
                int[] location = new int[2];
                if (currentFocus != null) {
                    edittextHeight = currentFocus.getHeight();
                    currentFocus.getLocationOnScreen(location);
                }
                Rect rect = new Rect();
                decorView.getWindowVisibleDisplayFrame(rect);
                int displayHight = rect.bottom - rect.top;
                boolean visible = (double) displayHight / screenHeight < 0.8;
                if (softKeyboardHeight == 0)
                    softKeyboardHeight = screenHeight - rect.bottom;
                if (visible != sLastVisiable) {
                    int scrollHeight = location[1] + edittextHeight - softKeyboardHeight;
                    scrollHeight = (scrollHeight < 0) ? 0 : scrollHeight;
                    if (listener != null) {
                        listener.onSoftKeyBoardVisible(visible, scrollHeight);
                    } else {
                        if (viewGroup.getScrollY() != scrollHeight)
                            viewGroup.scrollTo(0, scrollHeight);
                    }
                }
                sLastVisiable = visible;
            }
        });
    }

    /**
     * 获取APP版本名称
     *
     * @return
     */
    public static String getAPPVersionName(Context context) {
        try {
            String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            return versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "-0000";
    }

    /**
     * 获取版本号
     *
     * @return
     */
    public static int getAPPVersionCode(Context context) {
        try {
            int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            return versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * 写入字符串到文件
     *
     * @param file
     * @param content
     */
    public static void writeStr2File(File file, String content) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 写入字符串到文件
     *
     * @param filePath
     * @param content
     */
    public static void writeStr2File(String filePath, String content) {
        writeStr2File(new File(filePath), content);
    }

    /**
     * 写入字节数组到文件
     *
     * @param file
     * @param bys
     */
    public static void writebytes2File(File file, byte[] bys) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(bys);
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 写入字节数组到文件
     *
     * @param filePath
     * @param bys
     */
    public static void writebytes2File(String filePath, byte[] bys) {
        writebytes2File(new File(filePath), bys);
    }

    /**
     * 追加字符串到文件
     *
     * @param file
     * @param content
     */
    public static void writeStr2FileAppend(File file, String content) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 追加字符串到文件
     *
     * @param filePath
     * @param content
     */
    public static void writeStr2FileAppend(String filePath, String content) {
        writeStr2FileAppend(new File(filePath), content);
    }

    /**
     * 追加字节数组到文件
     *
     * @param file
     * @param bys
     */
    public static void writebytes2FileAppend(File file, byte[] bys) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, true));
            bos.write(bys);
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 追加字节数组到文件
     *
     * @param filePath
     * @param bys
     */
    public static void writebytes2FileAppend(String filePath, byte[] bys) {
        writebytes2FileAppend(new File(filePath), bys);
    }

    /**
     * 从字符流获取字符串内容，UTF-8内容格式
     *
     * @param is
     * @return
     */
    @Nullable
    public static String readfile2String(InputStream is) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String temp = null;
            StringBuilder builder = new StringBuilder();
            while ((temp = reader.readLine()) != null)
                builder.append(temp);
            reader.close();
            return builder.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取文本文件中内容
     *
     * @param file
     * @return 返回文本内容字符串，文件不存在返回null
     */
    public static String readfile2String(File file) {
        if (file == null || !file.exists())
            return null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String temp = null;
            StringBuilder builder = new StringBuilder();
            while ((temp = reader.readLine()) != null)
                builder.append(temp);
            reader.close();
            return builder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取文本文件中内容，传入字符串文件路径
     *
     * @param filePath
     * @return 返回文本内容字符串，文件不存在返回null
     */
    public static String readfile2String(String filePath) {
        return readfile2String(new File(filePath));
    }

    /**
     * 安装apk
     *
     * @param file
     */
    public static void installAPK(File file, Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + file.toString()), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 安装apk
     *
     * @param filePath
     */
    public static void installAPK(String filePath, Context context) {
        installAPK(new File(filePath), context);
    }

    /**
     * 获取xml字符串中指定节点的文本内容，有多个相同节点时返回第一个节点内容
     *
     * @param xml
     * @param key
     * @return
     */
    public static String getXmlNodeText(String xml, String key) {
        if (TextUtils.isEmpty(xml) || TextUtils.isEmpty(key))
            return null;
        try {
            XmlPullParser pullParser = Xml.newPullParser();
            pullParser.setInput(new ByteArrayInputStream(xml.getBytes()), "UTF-8");
            int eventType = pullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (key.equals(pullParser.getName())) {
                            eventType = pullParser.next();
                            return pullParser.getText();
                        }
                        break;
                }
                eventType = pullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取指定xml文件中指定节点的文本内容，有多个相同节点时返回第一个节点内容
     *
     * @param xmlFile
     * @param key
     * @return
     */
    public static String getXmlNodeText(File xmlFile, String key) {
        return getXmlNodeText(readfile2String(xmlFile), key);
    }

    /**
     * 删除文件或文件夹下所有文件
     *
     * @param file
     */
    public static void delFiles(File file) {
        if (file == null || !file.exists())
            return;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File file2 : files) {
                delFiles(file2);
            }
        } else {
            file.delete();
        }
    }

    /**
     * 删除文件或文件夹下所有文件，接收字符串路径
     *
     * @param filePath
     */
    public static void delFiles(String filePath) {
        delFiles(new File(filePath));
    }

    /**
     * 获取活动网络信息
     * <p>
     * 需添加权限 {@code <uses-permission android:name=
     * "android.permission.ACCESS_NETWORK_STATE"/>}
     * </p>
     *
     * @param context 上下文
     * @return NetworkInfo
     */
    private static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * 判断网络是否连接
     * <p>
     * 需添加权限 {@code <uses-permission android:name=
     * "android.permission.ACCESS_NETWORK_STATE"/>}
     * </p>
     *
     * @param context 上下文
     * @return {@code true}: 是<br>
     * {@code false}: 否
     */
    public static boolean isConnected(Context context) {
        NetworkInfo info = getActiveNetworkInfo(context);
        return info != null && info.isConnected();
    }

    /**
     * 记录日志
     *
     * @param log
     */
    public static void writeCashLog(Context context, String log) {
        if (!isLog)
            return;
        writePhoneInfo(context, "log.log");
        writeLogInfo("log.log", log);
    }

    /**
     * 收集手机基本信息
     *
     * @param context
     * @return
     */
    public static String buildBody(Context context) {
        StringBuilder sb = new StringBuilder();

        sb.append('\n').append("APPLICATION INFORMATION").append('\n');
        PackageManager pm = context.getPackageManager();
        ApplicationInfo ai = context.getApplicationInfo();
        sb.append("Application : ").append(pm.getApplicationLabel(ai)).append('\n');

        try {
            PackageInfo pi = pm.getPackageInfo(ai.packageName, 0);
            sb.append("Version Code: ").append(pi.versionCode).append('\n');
            sb.append("Version Name: ").append(pi.versionName).append('\n');
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        sb.append('\n').append("DEVICE INFORMATION").append('\n');
        sb.append("Board: ").append(Build.BOARD).append('\n');
        sb.append("BOOTLOADER: ").append(Build.BOOTLOADER).append('\n');
        sb.append("BRAND: ").append(Build.BRAND).append('\n');
        sb.append("CPU_ABI: ").append(Build.CPU_ABI).append('\n');
        sb.append("CPU_ABI2: ").append(Build.CPU_ABI2).append('\n');
        sb.append("DEVICE: ").append(Build.DEVICE).append('\n');
        sb.append("DISPLAY: ").append(Build.DISPLAY).append('\n');
        sb.append("FINGERPRINT: ").append(Build.FINGERPRINT).append('\n');
        sb.append("HARDWARE: ").append(Build.HARDWARE).append('\n');
        sb.append("HOST: ").append(Build.HOST).append('\n');
        sb.append("ID: ").append(Build.ID).append('\n');
        sb.append("MANUFACTURER: ").append(Build.MANUFACTURER).append('\n');
        sb.append("PRODUCT: ").append(Build.PRODUCT).append('\n');
        sb.append("TAGS: ").append(Build.TAGS).append('\n');
        sb.append("TYPE: ").append(Build.TYPE).append('\n');
        sb.append("USER: ").append(Build.USER).append('\n');

        return sb.toString();
    }

    private static boolean isLog = true;

    /**
     * 指定文件日志写入手机信息
     */
    public static void writePhoneInfo(Context context, String fname) {
        if (!isLog)
            return;
        try {
            FileWriter writer = new FileWriter(new File(Environment.getExternalStorageDirectory(), fname), true);
            writer.write("Record Time：" + getNowFormatDateTime());
            writer.write(buildBody(context));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 记录日志内容，自定义名称，配合上边写入手机信息调用
     *
     * @param log
     */
    public static void writeLogInfo(String fname, String log) {
        if (!isLog)
            return;
        try {
            FileWriter writer = new FileWriter(new File(Environment.getExternalStorageDirectory(), fname), true);
            writer.write(log);
            writer.write("\n");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
