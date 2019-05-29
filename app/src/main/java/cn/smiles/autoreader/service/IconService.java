package cn.smiles.autoreader.service;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Objects;

import cn.smiles.autoreader.KApplication;
import cn.smiles.autoreader.MainActivity;
import cn.smiles.autoreader.R;
import cn.smiles.autoreader.activity.SettingActivity;
import cn.smiles.autoreader.aidl.IMyAidlInterface;
import cn.smiles.autoreader.ktool.KPhone;
import cn.smiles.autoreader.ktool.KTools;
import cn.smiles.autoreader.rapp.ClearTools;
import cn.smiles.autoreader.rapp.RApp;
import cn.smiles.autoreader.view.MyLinearLayout;


public class IconService extends Service {

    private WindowManager windowManager;
    private Button wmContentView;
    private SharedPreferences asp;
    private int wmParamsX;
    private int wmParamsY;
    private int swh;
    private int screenHeight;
    private int screenWidth;
    private MyIAIDL myAidl;
    private MyServiceConnection myConn;
    private Handler mhandler;
    private ArrayList<RApp> rApps;
    public static boolean isRun;

    public IconService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        swh = KTools.dp2px(KApplication.context, 42);
        asp = KApplication.context.getSharedPreferences("IconService", Context.MODE_PRIVATE);
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        getScreenHeight();
        mhandler = new Handler();
        myAidl = new MyIAIDL();
        if (myConn == null)
            myConn = new MyServiceConnection();
    }

    /**
     * 获取屏幕宽高度、状态栏高度
     */
    private void getScreenHeight() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        bindService(new Intent(getApplicationContext(), RemoteService.class), myConn, Context.BIND_IMPORTANT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;
        boolean iShow = intent.getBooleanExtra("iShow", true);
        if (iShow) {
            //需要阅读的APP集合
            ArrayList<RApp> temp = (ArrayList<RApp>) intent.getSerializableExtra("rApps");
            if (wmContentView != null) {
                int rid = R.drawable.ic_play_circle_outline_pink_600_24dp;
                if (isRun) {
                    rid = R.drawable.ic_pause_circle_outline_pink_600_24dp;
                }
                wmContentView.setBackgroundResource(rid);
                runAotuReader(temp);
                return START_STICKY;
            }
            openAndroidWindow();
            runAotuReader(temp);
        } else {
            closeWindowView();
        }
        return START_STICKY;
    }

    private void runAotuReader(ArrayList<RApp> temp) {
        if (temp == null) return;
        rApps = new ArrayList<>(temp);
        new Thread(() -> {
            for (; ; ) {
                if (rApps == null) {
                    KTools.showToast("待阅读 rApps==null");
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

    /**
     * 开启窗口
     */
    @SuppressLint("ClickableViewAccessibility")
    private void openAndroidWindow() {
        wmParamsX = asp.getInt("wmParamsX", 0);
        wmParamsY = asp.getInt("wmParamsY", 0);
        wmContentView = new Button(getApplicationContext());
        int rid = R.drawable.ic_play_circle_outline_pink_600_24dp;
        if (isRun) {
            rid = R.drawable.ic_pause_circle_outline_pink_600_24dp;
        }
        wmContentView.setBackgroundResource(rid);
        final WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams(-2, -2);
        wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.width = swh;
        wmParams.height = swh;
        wmParams.x = wmParamsX;
        wmParams.y = wmParamsY;
        wmParams.gravity = Gravity.CENTER;
        wmContentView.setOnTouchListener(new View.OnTouchListener() {
            int downX, downY;
            int paramX, paramY;

            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        Log.i(TAG, "ACTION_DOWN");
                        downX = (int) event.getRawX();
                        downY = (int) event.getRawY();
                        paramX = wmParams.x;
                        paramY = wmParams.y;
                        int rid = R.drawable.ic_play_circle_outline_pink_100_24dp;
                        if (isRun) {
                            rid = R.drawable.ic_pause_circle_outline_pink_100_24dp;
                        }
                        v.setBackgroundResource(rid);
                        break;
                    case MotionEvent.ACTION_MOVE:
//                        Log.i(TAG, "ACTION_MOVE");
                        int dx = (int) event.getRawX() - downX;
                        int dy = (int) event.getRawY() - downY;
                        if (Math.abs(dx) > 6 || Math.abs(dy) > 6) {
                            wmParams.x = paramX + dx;
                            wmParams.y = paramY + dy;
                            windowManager.updateViewLayout(v, wmParams);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
//                        Log.i(TAG, "ACTION_UP");
                        int rid2 = R.drawable.ic_play_circle_outline_pink_600_24dp;
                        if (isRun) {
                            rid2 = R.drawable.ic_pause_circle_outline_pink_600_24dp;
                        }
                        v.setBackgroundResource(rid2);
                        int ux = (int) event.getRawX();
                        int uy = (int) event.getRawY();
                        if (Math.abs(ux - downX) < 6 && Math.abs(uy - downY) < 6) {
                            v.performClick();
                        } else {
                            final int x = wmParams.x;
                            ValueAnimator animation;
                            if (x < 0) {//向左移动
                                animation = ValueAnimator.ofInt(x, -screenWidth / 2);
                            } else {//向右移动
                                animation = ValueAnimator.ofInt(x, screenWidth / 2);
                            }
                            animation.setDuration(360);
                            animation.addUpdateListener(animation1 -> {
                                wmParams.x = (int) animation1.getAnimatedValue();
                                windowManager.updateViewLayout(v, wmParams);
                            });
                            animation.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    asp.edit().putInt("wmParamsX", wmParams.x).apply();
                                    asp.edit().putInt("wmParamsY", wmParams.y).apply();
                                }
                            });
                            animation.start();
                        }
                        break;
                }
                return true;
            }
        });
        wmContentView.setOnClickListener(v -> {
            openContentWindow();
            closeWindowView();
        });
        windowManager.addView(wmContentView, wmParams);
    }

    /**
     * 开启操作窗口
     */
    private void openContentWindow() {
        final View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tools_view_layout, null);
        final WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams(-1, -1);
        wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        wmParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.windowAnimations = android.R.style.Animation_InputMethod;

        view.findViewById(R.id.go_home).setOnLongClickListener(longClickListener);
        view.findViewById(R.id.go_home).setOnClickListener(v -> {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            closeContentWindow(view);
        });

        view.findViewById(R.id.recent_app).setOnLongClickListener(longClickListener);
        view.findViewById(R.id.recent_app).setOnClickListener(v -> {
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
            closeContentWindow(view);
        });

        view.findViewById(R.id.open_notification).setOnLongClickListener(longClickListener);
        view.findViewById(R.id.open_notification).setOnClickListener(v -> {
            KPhone.showNotificationCenter();
            closeContentWindow(view);
        });

        view.findViewById(R.id.open_app_setting).setOnLongClickListener(longClickListener);
        view.findViewById(R.id.open_app_setting).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
            closeContentWindow(view);
        });

        view.findViewById(R.id.sys_settings).setOnLongClickListener(longClickListener);
        view.findViewById(R.id.sys_settings).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
            closeContentWindow(view);
        });

        view.findViewById(R.id.screen_capture).setOnLongClickListener(longClickListener);
        view.findViewById(R.id.screen_capture).setOnClickListener(v -> {
            closeContentWindow(view, true);
            KApplication.handler.postDelayed(KPhone::screenCapture, 1000);
        });

        view.setOnClickListener(v -> closeContentWindow(view));
        ((MyLinearLayout) view).setKeyEventListener(() -> closeContentWindow(view, true));
        windowManager.addView(view, wmParams);
    }

    private View.OnLongClickListener longClickListener = v -> {
        CharSequence description = v.getContentDescription();
        if (!TextUtils.isEmpty(description))
            Toast.makeText(IconService.this, description, Toast.LENGTH_SHORT).show();
        return true;
    };

    /**
     * 关闭操作窗口
     *
     * @param view
     */
    private void closeContentWindow(View view, boolean... isDelayed) {
        windowManager.removeView(view);
        if (isDelayed.length > 0) {
            mhandler.postDelayed(this::openAndroidWindow, 1200);
        } else {
            openAndroidWindow();
        }
    }

    /**
     * 关闭
     */
    private void closeWindowView() {
        if (wmContentView != null) {
            windowManager.removeView(wmContentView);
            wmContentView = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myAidl;
    }

    private class MyIAIDL extends IMyAidlInterface.Stub {
        @Override
        public String getSername(int anInt) {
            return "IconService===" + anInt;
        }
    }

    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 另一个服务连接成功时会调用
            System.out.println("IconService 连接成功");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // 和另一个服务连接被异常中断时会调用,启动并绑定
            System.out.println("RemoteService 被干掉了");
            startService(new Intent(getApplicationContext(), RemoteService.class));
            bindService(new Intent(getApplicationContext(), RemoteService.class), myConn, Context.BIND_IMPORTANT);
        }
    }
}
