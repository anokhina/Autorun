/*
 * Copyright 2016 Veronica Anokhina.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.org.sevn.autorun;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ru.org.sevn.alib.util.AppsUtil;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

public class AppRunService extends Service {

    private ExecutorService es;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    public void onCreate() {
        super.onCreate();
        es = Executors.newFixedThreadPool(1);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String name = intent.getStringExtra(MainActivity.APP_PACKAGE_NAME);
        startTask(name, startId);
        return super.onStartCommand(intent, flags, startId);
    }

    void startTask(final String name, final int startId) {
        //const_pref_run_app_delay
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        int delay = 2;
        try {
            delay = sharedPreferences.getInt(getContext().getString(R.string.const_pref_run_app_delay), 5);
        } catch (Exception e) {
            try {
                delay = Integer.parseInt(sharedPreferences.getString(getContext().getString(R.string.const_pref_run_app_delay), "2"));
            } catch (Exception ex) {}
        }
        if (delay < 2) {
            delay = 2;
        }
        MyRun mr = new MyRun(name, startId, delay);
        es.execute(mr);        
    }
    
    public Context getContext() {
        return this;
    }
    
    class MyRun implements Runnable {
        final String name;
        final int startId;
        final int delay;
        
        public MyRun(final String n, int startId, final int delay) {
            this.name = n;
            this.startId = startId;
            this.delay = delay;
        }
        
        @Override
        public void run() {
            if (name != null) {
                Intent i = getContext().getPackageManager()
                        .getLaunchIntentForPackage(name);
    
                if (i != null) {
                    //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try2sleep(delay);
                    getContext().startActivity(i);
                }
            } else {
                try2sleep(delay);
                AppsUtil.showHomeAny(getContext());
            }

            stopSelfResult(startId);
        }
    }
    private void try2sleep(final int delay) {
        try {
            TimeUnit.SECONDS.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }             
    }
    
}

