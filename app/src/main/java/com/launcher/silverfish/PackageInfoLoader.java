package com.launcher.silverfish;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Stanislav Pintjuk on 8/8/16.
 * E-mail: stanislav.pintjuk@gmail.com
 */

/**
 * This class is for storing information about installed apps during the life time of the launcher.
 */
public class PackageInfoLoader {
    private static PackageInfoLoader ourInstance = new PackageInfoLoader();

    public static PackageInfoLoader getInstance() {
        return ourInstance;
    }

    HashMap<String, Bitmap> appIcons;
    HashMap<String, String> appLabels;
    List<String> appPkgNames;

    private PackageInfoLoader() {
        appIcons = new HashMap<String, Bitmap>();
        appLabels = new HashMap<String, String>();
        appPkgNames = new ArrayList<String>();
    }

    public Drawable getAppIcon(Resources res, String pkgName){
        Bitmap icon_bitmap = appIcons.get(pkgName);
        Drawable icon_drawable = new BitmapDrawable(res,icon_bitmap);
        return icon_drawable;
    }

    public String getAppLabel(String pkgName){
        String app_label = appLabels.get(pkgName);
        return app_label;
    }

    /**
     * Loads icons and labels of all installed applications.
     * Assumes that all hashmaps and the package name list is empty, so run this function only
     * during the life time of the launcher.
     * @param packageManager
     */
    public void loadApps(PackageManager packageManager){
        // load a list of all installed apps
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        List<ResolveInfo> availableActivities = packageManager.queryIntentActivities(intent, 0);

        for (ResolveInfo ri : availableActivities){
            // get the package name and label of activity.
            String pkg_name = ri.resolvePackageName;
            String label = (String) ri.loadLabel(packageManager);

            // load the app icon.
            Drawable icon_drawable = ri.loadIcon(packageManager);

            // store it as a bitmap to not risk any memory leaks
            Bitmap icon_bitmap = ((BitmapDrawable)icon_drawable).getBitmap();

            // add the values to the hashmaps
            appIcons.put(pkg_name, icon_bitmap);
            appLabels.put(pkg_name, label);
            appPkgNames.add(pkg_name);
        }

    }

    /**
     * Checks for any new installed apps.
     * Call this function when an app has potentially been installed, like in onPause()
     * @param packageManager
     */
    public void updateApps(PackageManager packageManager){
        // load a list of all installed apps
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        List<ResolveInfo> availableActivities = packageManager.queryIntentActivities(intent, 0);

        for (ResolveInfo ri : availableActivities){
            // get the package name and check if it already is in the list.
            // if it is then don't load the icon and label again.
            String pkg_name = ri.resolvePackageName;
            if (appPkgNames.contains(pkg_name)){
                continue;
            }

            String label = (String) ri.loadLabel(packageManager);

            // load the app icon.
            Drawable icon_drawable = ri.loadIcon(packageManager);

            // store it as a bitmap to not risk any memory leaks
            Bitmap icon_bitmap = ((BitmapDrawable)icon_drawable).getBitmap();

            // add the values to the hashmaps
            appIcons.put(pkg_name, icon_bitmap);
            appLabels.put(pkg_name, label);
            appPkgNames.add(pkg_name);
        }
    }

    /**
     * Removes the specified app's icon and label from the lists.
     * @param pkg_name
     */
    public void removeApp(String pkg_name){
        appIcons.remove(pkg_name);
        appLabels.remove(pkg_name);
        appPkgNames.remove(appPkgNames.indexOf(pkg_name));
    }

}
