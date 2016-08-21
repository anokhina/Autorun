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
package ru.org.sevn.autorun.sys;

import java.util.List;

import ru.org.sevn.alib.data.app.AppDetail;
import ru.org.sevn.alib.data.app.AppDetailComparator;
import ru.org.sevn.alib.data.app.AppsList;
import android.content.Context;

public class Apps {
    
    private static class SingletonHelper{
        private static final AppsList INSTANCE = new AppsList();
    }
    
    public static AppsList getInstance(){
        return SingletonHelper.INSTANCE;
    }
    
    public static List<AppDetail> getAppsInfo(final Context ctx){
        return getInstance().getAppsInfo(ctx);
    }
    
    public static List<AppDetail> getAppsInfo(final Context ctx, final AppDetailComparator cmpr){
        return getInstance().getAppsInfo(ctx, cmpr);
    }   
    
    public static void clearCache() {
        getInstance().clearCache();
    }
    
    public static AppDetail findApp(String p, final Context ctx, final AppDetailComparator cmpr) {
        return getInstance().findApp(p, ctx, cmpr);
    }
    
}
