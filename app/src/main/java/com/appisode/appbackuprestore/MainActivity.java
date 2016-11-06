package com.appisode.appbackuprestore;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.appisode.appbackuprestore.adapter.FragmentAdapter;
import com.appisode.appbackuprestore.data.AppConfig;
import com.appisode.appbackuprestore.data.PermissionUtil;
import com.appisode.appbackuprestore.fragment.BackupFragment;
import com.appisode.appbackuprestore.fragment.RestoreFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

public class MainActivity extends AppCompatActivity {
    //ads
    private AdView mAdView;
    //for ads
    private InterstitialAd mInterstitialAd;
    //public static Toolbar toolbar;
    public static BackupFragment frag_backup;
    private RestoreFragment frag_restore;

    private ViewPager viewPager;
    private ActionBar actionBar;
    private Toolbar toolbar;

    private SearchView search;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void onCreateProcess() {
        actionBar   = getSupportActionBar();
        viewPager   = (ViewPager) findViewById(R.id.viewpager);
        fab         = (FloatingActionButton) findViewById(R.id.fab);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        initToolbar();
        prepareAds();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                frag_backup.refresh(true);
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showInterstitial();
                viewPager.setCurrentItem(tab.getPosition());
                // close contextual action mode
                if(frag_backup.getActionMode() !=  null){
                    frag_backup.getActionMode().finish();
                }
                if(frag_restore.getActionMode() !=  null){
                    frag_restore.getActionMode().finish();
                }

                if (tab.getPosition() == 0) {
                    fab.show();
                } else {
                    frag_restore.refreshList();
                    fab.hide();
                }
                search.onActionViewCollapsed();
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }

        });

        Window window = this.getWindow();

        if (getAPIVerison() >= 5.0) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));
        }

    }

    private void initToolbar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(false);
    }

    private void setupViewPager(ViewPager viewPager) {
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());

        if (frag_backup == null) {
            frag_backup = new BackupFragment();
        }
        if (frag_restore == null) {
            frag_restore = new RestoreFragment();
        }
        adapter.addFragment(frag_backup, getString(R.string.tab_title_backup));
        adapter.addFragment(frag_restore, getString(R.string.tab_title_restore));
        viewPager.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        search = (SearchView) menu.findItem(R.id.action_search).getActionView();
        search.setIconified(false);
        if (viewPager.getCurrentItem() == 0) {
            search.setQueryHint(getString(R.string.hint_backup_search));
        } else {
            search.setQueryHint(getString(R.string.hint_restore_search));
        }
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                try {
                    if (viewPager.getCurrentItem() == 0) {
                        frag_backup.bAdapter.getFilter().filter(s);
                    } else {
                        frag_restore.rAdapter.getFilter().filter(s);
                    }
                } catch (Exception e) {

                }
                return true;
            }
        });
        search.onActionViewCollapsed();
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search: {
                // this do magic
                supportInvalidateOptionsMenu();
                return true;
            }
            case R.id.action_rate: {
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                }
                return true;
            }
            case R.id.action_about: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("About");
                builder.setMessage(getString(R.string.about_text));
                builder.setNeutralButton("OK", null);
                builder.show();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static float getAPIVerison() {

        Float f = null;
        try {
            StringBuilder strBuild = new StringBuilder();
            strBuild.append(android.os.Build.VERSION.RELEASE.substring(0, 2));
            f = new Float(strBuild.toString());
        } catch (NumberFormatException e) {

        }
        return f.floatValue();
    }

    private void prepareAds(){
        mAdView = (AdView) findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);

        // Create the InterstitialAd and set the adUnitId.
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        AdRequest adRequest2 = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest2);
    }

    /** show ads */
    public void showInterstitial() {
        // Show the ad if it's ready
        if (AppConfig.ENABLE_ADSENSE && mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    @Override
    protected void onResume() {
        if(!PermissionUtil.isAllPermissionGranted(this)){
            showDialogPermission();
        }else{
            onCreateProcess();
        }
        super.onResume();
    }

    private void showDialogPermission(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_title_permission));
        builder.setMessage(getString(R.string.dialog_content_permission));
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                PermissionUtil.goToPermissionSettingScreen(MainActivity.this);
            }
        });
        builder.setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.show();
    }

}
