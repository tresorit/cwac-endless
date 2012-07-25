/***
  Copyright (c) 2012 CommonsWare, LLC
  
  Licensed under the Apache License, Version 2.0 (the "License"); you may
  not use this file except in compliance with the License. You may obtain
  a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.commonsware.cwac.endless.demo;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class EndlessAdapterFragmentDemo extends Activity implements
    ActionBar.TabListener {
  private static final String TAG_SIMPLE="s";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ActionBar bar=getActionBar();

    bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

    bar.addTab(bar.newTab().setText(R.string.simple)
                  .setTabListener(this).setTag(Tabs.TAB_SIMPLE));
  }

  @Override
  public void onTabReselected(Tab tab, FragmentTransaction ft) {
    // no-op
  }

  @Override
  public void onTabSelected(Tab tab, FragmentTransaction ft) {
    switch ((Tabs)tab.getTag()) {
      case TAB_SIMPLE:
        showSimple();
        break;
    }
  }

  @Override
  public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    // no-op
  }

  private void showSimple() {
    Fragment f=getFragmentManager().findFragmentByTag(TAG_SIMPLE);

    if (f == null) {
      f=new EndlessAdapterFragment();
    }

    getFragmentManager().beginTransaction()
                        .replace(android.R.id.content, f, TAG_SIMPLE)
                        .commit();
  }

  private static enum Tabs {
    TAB_SIMPLE;
  }
}
