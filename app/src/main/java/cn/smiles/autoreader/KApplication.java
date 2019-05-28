package cn.smiles.autoreader;

import android.app.Application;
import android.content.Context;
import android.os.Handler;


public class KApplication extends Application {
    public static Context context;
    public static Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this.getApplicationContext();
        handler = new Handler();
    }

}
