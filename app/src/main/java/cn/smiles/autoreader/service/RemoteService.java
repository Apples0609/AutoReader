package cn.smiles.autoreader.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;
import cn.smiles.autoreader.aidl.IMyAidlInterface;


public class RemoteService extends Service {
    private final String TAG = "===";
    private MyIAIDL myAidl;
    private MyServiceConnection myConn;

    @Override
    public void onCreate() {
        super.onCreate();
        myAidl = new MyIAIDL();
        if (myConn == null)
            myConn = new MyServiceConnection();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        bindService(new Intent(getApplicationContext(), IconService.class), myConn, Context.BIND_IMPORTANT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myAidl;
    }

    private class MyIAIDL extends IMyAidlInterface.Stub {
        @Override
        public String getSername(int anInt) throws RemoteException {
            return "RemoteService===" + anInt;
        }
    }

    private class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 另一个服务连接成功时会调用
            Log.i(TAG, "RemoteService 连接成功");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // 和另一个服务连接被异常中断时会调用,启动并绑定
            Log.i(TAG, "IconService 被干掉了");
            startService(new Intent(getApplicationContext(), IconService.class));
            bindService(new Intent(getApplicationContext(), IconService.class), myConn, Context.BIND_IMPORTANT);
        }
    }

}
