/*
 * Copyright 2016 Stanislav Pintjuk
 * E-mail: stanislav.pintjuk@gmail.com
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.launcher.silverfish;

import android.content.ClipDescription;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.View;

import com.launcher.silverfish.dbmodel.TabTable;
import com.launcher.silverfish.sqlite.LauncherSQLiteHelper;

/**
 * This is the main activity of the launcher
 */
public class LauncherActivity extends FragmentActivity {

    //region Fields

    LauncherPagerAdapter mCollectionPagerAdapter;
    ViewPager mViewPager;

    // Used for telling home screen when a shortcut is added.
    private ShortcutAddListener shortcutAddListener;

    //endregion

    //region Android lifecycle

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);

        // Check if the app is started for the first time. If it is then we have to
        // populate the database with some default values.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean previouslyStarted = prefs.getBoolean(getString(R.string.pref_previously_started), false);
        if(!previouslyStarted) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(getString(R.string.pref_previously_started), Boolean.TRUE);
            edit.apply();
            createDefaultTabs();
        }

        // Create the pager
        mCollectionPagerAdapter =
                new LauncherPagerAdapter(
                        getSupportFragmentManager(), this);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mCollectionPagerAdapter);
        mViewPager.setCurrentItem(1);

        setDragListener();
    }

    //endregion

    //region First time setup

    private void createDefaultTabs() {
        LauncherSQLiteHelper sql = new LauncherSQLiteHelper(this.getBaseContext());

        // Load default names for the tab
        String[] defaultTabNames = new String[] {
            getString(R.string.tab_other),
            getString(R.string.tab_phone),
            getString(R.string.tab_games),
            getString(R.string.tab_internet),
            getString(R.string.tab_media),
            getString(R.string.tab_accessories),
            getString(R.string.tab_settings),
        };

        // Create and add the tables to the SQL database
        for (int i = 0; i < 7; i++) {
            TabTable tab = new TabTable();
            tab.label = defaultTabNames[i];
            sql.addTab(tab);
        }
    }

    //endregion

    //region Listeners

    private void setDragListener() {
        mViewPager.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        // Only care about the DRAG_APP_MOVE description.
                        ClipDescription cd = dragEvent.getClipDescription();
                        if (!cd.getLabel().toString().equals(Constants.DRAG_APP_MOVE))
                            return false;
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        // Don't do anything
                        break;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        changePage(dragEvent);
                        break;
                    case DragEvent.ACTION_DROP:
                        dropItem(dragEvent);
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        // Don't do anything
                        break;

                }
                return true;
            }
        });
    }

    public ShortcutAddListener getFragShortcutAddListenerRefreshListener() {
        return shortcutAddListener;
    }

    public void setFragShortcutAddListenerRefreshListener(ShortcutAddListener shortcutAddListener) {
        this.shortcutAddListener = shortcutAddListener;
    }

    //endregion

    //region Events

    private void dropItem(DragEvent dragEvent) {
        if (mViewPager.getCurrentItem() == 1) {
            String appName = dragEvent.getClipData().getItemAt(0).getText().toString();

            if(getFragShortcutAddListenerRefreshListener() != null) {
                getFragShortcutAddListenerRefreshListener().OnShortcutAdd(appName);
            }
        }
    }

    private void changePage(DragEvent dragEvent) {
        // Change page mid drag if drag is within threshold
        int threshold = Constants.SCREEN_CORNER_THRESHOLD;

        // Get display size
        int width = Utils.getScreenDimensions(this).x;

        // Change page
        if (mViewPager.getCurrentItem() == 0 && dragEvent.getX() >= width - threshold) {
            mViewPager.setCurrentItem(1);
        } else if(mViewPager.getCurrentItem() == 1 && dragEvent.getX() <= threshold) {
            mViewPager.setCurrentItem(0);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Override the functionality of back and home key
        if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME)
                && event.getRepeatCount() == 0) {
            event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Override the functionality of back and home key
        if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME)&& event.isTracking()
                && !event.isCanceled()) {
            mViewPager.setCurrentItem(1);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    //endregion
}

