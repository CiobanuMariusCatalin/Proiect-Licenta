package com.example.proiect_licenta_client.Pagini;

import android.os.Bundle;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.example.proiect_licenta_client.PaginaPrincipalaViewPagersFragments.PaginaPrincipalaConversatiiFragment;
import com.example.proiect_licenta_client.PaginaPrincipalaViewPagersFragments.PaginaPrincipalaFriendRecommendationFragment;
import com.example.proiect_licenta_client.PaginaPrincipalaViewPagersFragments.PaginaPrincipalaFriendRequestsFragment;
import com.example.proiect_licenta_client.PaginaPrincipalaViewPagersFragments.PaginaPrincipalaNewsFragment;
import com.example.proiect_licenta_client.PaginaPrincipalaViewPagersFragments.PaginaPrincipalaPrietenTemporar;
import com.example.proiect_licenta_client.PaginaPrincipalaViewPagersFragments.PaginaPrincipalaSettingsFragment;
import com.example.proiect_licenta_client.R;

public class PaginaPrincipala extends FragmentActivity {
   private String cont;

    private  String parola;
    private  PaginaPrincipalaPageAdapter paginaPrincipalaPageAdapter;
    private   ViewPager mViewPager;
    private   ActionBar actionBar;
    // SendTask task;

    /*butonul ce are onClick aceasta functie este defapt in fragmetul de settings al paginii principale
        si cand este apasat se apeleaza functia din activitate
        */
    public void ProfilUserCurent(View view) {
        Intent intent = new Intent(this, PaginaProfil.class);
        intent.putExtra("profil", cont);
        startActivity(intent);
    }

    public void logout(View view) {

        SharedPreferences settings = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        if(settings.contains("cont"))editor.remove("cont");
        if(settings.contains("profil")) editor.remove("profil");
        if(settings.contains("parola")) editor.remove("parola");
        editor.commit();

        Intent intent = new Intent(this, PaginaLogin.class);
        //aceste 2 flaguri ma ajuta sa elimin tot din varful stivei astfel incat daca userul da back
        //pe pagina de login dupa ce a fost redirectionat de acest buton va iesi din aplicatie
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);


    }
    public void Interese(View view) {
        Intent intent = new Intent(this, PaginaInterese.class);
        intent.putExtra("profil", cont);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);


        savedInstanceState.putString("cont", cont);
        savedInstanceState.putString("parola", parola);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pagina_principala);
        SharedPreferences settings;
        if (savedInstanceState == null) {
            settings = getSharedPreferences("login", MODE_PRIVATE);
            cont = settings.getString("cont", "");
            parola = settings.getString("parola", "");
        } else {
            cont = savedInstanceState.getString("cont");
            parola = savedInstanceState.getString("parola");
        }

        setTitle("PaginaPrincipala");
        settings = getSharedPreferences("login", MODE_PRIVATE);

        paginaPrincipalaPageAdapter = new PaginaPrincipalaPageAdapter(
                getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager
                .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                    }
                });
        mViewPager.setAdapter(paginaPrincipalaPageAdapter);
        mViewPager.setOffscreenPageLimit(5);

        // Enable Tabs on Action Bar
        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {

            @Override
            public void onTabSelected(Tab tab, FragmentTransaction ft) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(Tab tab, FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(Tab tab, FragmentTransaction ft) {

            }

        };

        actionBar.addTab(actionBar.newTab().setText("Stiri")
                .setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Cereri de prietenie")
                .setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Recomandari de prieteni")
                .setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Conversatii")
                .setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Prieten Temporar")
                .setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Cont")
                .setTabListener(tabListener));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        return true;
    }

    ;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    public class PaginaPrincipalaPageAdapter extends FragmentStatePagerAdapter {


        public PaginaPrincipalaPageAdapter(FragmentManager fm) {
            super(fm);
        }

        //getItem e de la 0 la n
        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;
            switch (i) {
                case 0:
                    fragment = new PaginaPrincipalaNewsFragment();
                    break;
                case 1:
                    fragment = new PaginaPrincipalaFriendRequestsFragment();
                    break;
                case 2:
                    fragment = new PaginaPrincipalaFriendRecommendationFragment();
                    break;
                case 3:
                    fragment = new PaginaPrincipalaConversatiiFragment();
                    break;
                case 4:
                    fragment = new PaginaPrincipalaPrietenTemporar();
                    break;
                case 5:
                    fragment = new PaginaPrincipalaSettingsFragment();
                    break;
            }

            //Bundle args = new Bundle();

            return fragment;
        }

        @Override
        public int getCount() {
            return 6;
        }

	/*	asta il foloseam pt Title Strip e face acelasi lucru ca tab dar arata altfel
     * @Override
		public CharSequence getPageTitle(int position) {
			return "OBJECT " + (position + 1);
		}*/
    }
}
