package cn.org.cctc.ssltestconnect;

import android.app.Application;

import cn.com.syan.libcurl.CurlHttpClient;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //只在程序开始的时候调用一次
        CurlHttpClient.init(this);
    }

}
