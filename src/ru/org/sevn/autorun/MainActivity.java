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

import java.util.ArrayList;
import java.util.List;

import ru.org.sevn.autorun.app.dao.AppDAO;
import ru.org.sevn.alib.data.app.AppDetail;
import ru.org.sevn.alib.data.app.AppDetailComparator;
import ru.org.sevn.alib.gui.DrawableArrayAdapter;
import ru.org.sevn.autorun.sys.Apps;
import ru.org.sevn.alib.util.CollectionsUtil;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends Activity {
    
    public static final String APP_PACKAGE_NAME = "package_name";
    public static final String TASKS_DELAY = "tasks_delay";
    
    private Button btDn;
    private Button btAdd;
    private Button btRemove;
    private Button btUp;
    private View.OnClickListener buttonListener;
    private ListView listView;
    private DrawableArrayAdapter mListViewAdapter;
    
    private List<AppDetail> appDetails;

    private final AppDetailComparator mAppDetailComparator = new AppDetailComparator();
    private AppDAO.AppEntryReaderDbHelper mDbHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        btDn = (Button) findViewById(R.id.b_dn);
        btAdd = (Button) findViewById(R.id.b_add);
        btRemove = (Button) findViewById(R.id.b_del);
        btUp = (Button) findViewById(R.id.b_up);
        
        listView = (ListView) findViewById(R.id.listView_apps);
        
        buttonListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                case R.id.b_dn:
                    moveListItem(1);
                    break;
                case R.id.b_add:
                    addApp();
                    break;
                case R.id.b_del:
                    delApp();
                    break;
                case R.id.b_up:
                    moveListItem(-1);
                    break;
                }
                showInfo();
            }
        };
        
        btDn.setOnClickListener(buttonListener);
        btAdd.setOnClickListener(buttonListener);
        btRemove.setOnClickListener(buttonListener);
        btUp.setOnClickListener(buttonListener);
        
        initData();
        
        final List datalist = appDetails;
        mListViewAdapter = new DrawableArrayAdapter(this, R.layout.list_item, android.R.id.text1, datalist);
        listView.setAdapter(mListViewAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        
        //ButtonGridAdapter
    }
    
    protected void moveListItem(int i) {
        int pos = listView.getCheckedItemPosition();
        int newPos = pos+i;
        if (pos >= 0 && newPos >=0 && newPos < appDetails.size()) {
            CollectionsUtil.moveListItem(appDetails, pos, newPos);
            changeListView(null, null);
            listView.setItemChecked(newPos, true);
        }
    }

    private void changeListView(AppDetail ad, AppDetail ad2del) {
        if (ad != null) {
            appDetails.add(ad);
            ad.setSortOrder(appDetails.size());
            AppDAO.insertAppDetail(getDbHelper(), ad);
        } else {
            if (ad2del != null) {
                appDetails.remove(ad2del);
                AppDAO.removeAppDetail(getDbHelper(), ad2del);
                //TODO optimize it - update changed only
            }
            for (int i = 0; i < appDetails.size(); i++) {
                AppDetail adi = (AppDetail)appDetails.get(i);
                if (adi.getSortOrder() != i) {
                    adi.setSortOrder(i);
                    AppDAO.updateAppDetail(getDbHelper(), adi);
                }
            }
        }
        mListViewAdapter.notifyDataSetChanged();
    }
    
    protected void clearCache() {
        Apps.clearCache();
    }

    protected void showInfo() {
        // TODO Auto-generated method stub
        
    }

    protected void delApp() {
        int pos = listView.getCheckedItemPosition();
        AppDetail ad = (AppDetail)listView.getItemAtPosition(pos);
        changeListView(null,ad);
    }

    protected void addApp() {
        Intent intent = new Intent(this, SelectAppActivity.class);
        startActivityForResult(intent, 1);
    }

    protected void runApps() {
        runApps(this, appDetails);
    }
    public static void runApps(final Context context, final List<AppDetail> lst) {
        for (AppDetail ad : lst) {
            MainActivity.runApp(context, ad.getPackageName());
        }
        MainActivity.runApp(context, (String)null); //home activity
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        ArrayList<String> names = data.getStringArrayListExtra(SelectAppActivity.RET_PARAM);
        for (String name : names) {
            AppDetail ad = Apps.findApp(name, this, mAppDetailComparator);
            if (ad != null) {
                if (mListViewAdapter.containsKey(name)) {} else {
                    changeListView(ad.copy(), null);
                }
            }
        }
    }
    
    public static void runApp(Context ctx, AppDetail appDetail) {
        runApp(ctx, appDetail.getPackageName());
    }
    public static void runApp(final Context ctx, final String packageName) {
        //if (packageName == null) return;
        Intent i = new Intent(ctx, AppRunService.class);
        i.putExtra(APP_PACKAGE_NAME, packageName);
        ctx.startService(i);
    }
    
    private void initData() {
        appDetails = initData(this, getDbHelper(), mAppDetailComparator);
    }
    
    public static List<AppDetail> initData(final Context ctx, final SQLiteOpenHelper mDbHelper, final AppDetailComparator cmpr) {
        ArrayList<AppDetail> appDetails = new ArrayList<>();
        List<AppDetail> lst = AppDAO.selectAppDetails(mDbHelper, null, null, 0); 
        for(AppDetail ad : lst) {
            AppDetail adsys = Apps.findApp(ad.getPackageName(), ctx, cmpr);
            if (adsys != null) {
                ad.getExtra().setLabel(adsys.getExtra().getLabel());
                ad.getExtra().setIcon(adsys.getExtra().getIcon());
                appDetails.add(ad);
            }
        }
        return appDetails;
    }
    
    private AppDAO.AppEntryReaderDbHelper getDbHelper() {
        if (mDbHelper == null) {
            mDbHelper = new AppDAO.AppEntryReaderDbHelper(this);
        }
        return mDbHelper;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_run:
                runApps();
                return true;
            case R.id.action_clear_cache:
                clearCache();;
                return true;
            case R.id.action_preferences:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            case R.id.action_menu:
                new Handler().postDelayed(new Runnable() { 
                    public void run() { 
                      openOptionsMenu(); 
                    } 
                 }, 500);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }    
}
