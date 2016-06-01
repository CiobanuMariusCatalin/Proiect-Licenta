package com.example.proiect_licenta_client.Pagini;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SearchView;

import com.example.proiect_licenta_client.R;



public class PaginaVizibilitatePost extends Activity {
    public void done(View view){
        RadioButton bt1=(RadioButton) findViewById(R.id.radio_eu);
        RadioButton bt2=(RadioButton) findViewById(R.id.radio_prieteni);
        RadioButton bt3=(RadioButton) findViewById(R.id.radio_toti);
        if(bt1.isChecked()){
            Intent resultIntent =getIntent();
            resultIntent.putExtra("vizibilitate","eu");
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
        if(bt2.isChecked()){
            Intent resultIntent =getIntent();
            resultIntent.putExtra("vizibilitate","prieteni");
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
        if(bt3.isChecked()){
            Intent resultIntent =getIntent();
            resultIntent.putExtra("vizibilitate","toti");
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.pagina_vizibilitate_post);
        setTitle("Vizbilitate");
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
/*        if (id == R.id.action_settings) {
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }
}
