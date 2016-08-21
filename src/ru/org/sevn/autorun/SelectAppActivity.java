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

import ru.org.sevn.alib.data.app.AppDetail;
import ru.org.sevn.alib.data.app.AppDetailComparator;
import ru.org.sevn.alib.gui.DrawableArrayAdapter;
import ru.org.sevn.autorun.sys.Apps;
import ru.org.sevn.alib.util.CollectionsUtil;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class SelectAppActivity extends Activity {

    private Button mButton;
    private EditText mEditText;
    private ListView mListView;
    private ArrayAdapter mListViewAdapter;
    private final List<AppDetail> appDetailsAll = new ArrayList<>();
    private final AppDetailComparator mAppDetailComparator = new AppDetailComparator();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_app);
        
        mEditText = (EditText)findViewById(R.id.editText_search_string);
        mListView = (ListView)findViewById(R.id.listView_apps);
        mButton = (Button)findViewById(R.id.button1);
        
        CollectionsUtil.copy(appDetailsAll, Apps.getAppsInfo(this, mAppDetailComparator));

        final List datalist = appDetailsAll;
        mListViewAdapter = new DrawableArrayAdapter(this, R.layout.list_item_multiple, android.R.id.text1, datalist);
        //mListViewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, datalist);
        mListView.setAdapter(mListViewAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        mEditText.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                mListViewAdapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }

            @Override
            public void afterTextChanged(Editable arg0) {}
        });
        
        mButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ArrayList<String> ret = new ArrayList<>();
                Intent intent = new Intent();
                SparseBooleanArray sbArray = mListView.getCheckedItemPositions();
                for (int i = 0; i < sbArray.size(); i++) {
                    int k = sbArray.keyAt(i);
                    if (sbArray.get(k)) {
                        AppDetail lAppDetail = (AppDetail)mListView.getItemAtPosition(k);
                        ret.add(lAppDetail.getPackageName());
                    }
                }
                intent.putExtra(RET_PARAM, ret);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        
    }
    
    public static final String RET_PARAM = "package_name";
}
