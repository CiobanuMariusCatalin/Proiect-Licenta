package com.example.proiect_licenta_client.Pagini;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proiect_licenta_client.Adaptere.SearchRezultsAdapter;
import com.example.proiect_licenta_client.Others.ConexiuneLaInternet;
import com.example.proiect_licenta_client.Others.Constante;
import com.example.proiect_licenta_client.Containere.SearchRezultContainer;
import com.example.proiect_licenta_client.Others.IncarcaPoze;
import com.example.proiect_licenta_client.R;

public class PaginaSearch extends Activity {
    private SendTask task;
    private ArrayList<SearchRezultContainer> searchRezults;
    private SearchRezultsAdapter adaptor;
    private ArrayList<SendTask> asyncTaskuriActive = null;
    private int idLastSendTask = 0;
    private boolean suntAsyncTaskActive=false;
    private ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat = null;



    public void selecteazaProfil(String nume) {
        Intent intent = new Intent(this, PaginaProfil.class);
        intent.putExtra("profil", nume);
        startActivity(intent);
    }
    @Override
    protected void onPause() {

        super.onPause();
        //daca sunt asynctaskuri active pun valoarea booleana ca true sa stiu la onresume sa reincarc pagina.
        //si dau cancel la asynctaskuri
        if(asyncTaskuriActive.size()>0||pozeDeIncarcat.size()>0){
            // Toast.makeText(this, pozeDeIncarcat.size()+" "+asyncTaskuriActive.size(), Toast.LENGTH_SHORT)
            //          .show();
            suntAsyncTaskActive=true;
        }else{
            suntAsyncTaskActive=false;
        }
        for (int i = 0; i < asyncTaskuriActive.size(); i++) {
            asyncTaskuriActive.get(i).cancel(true);
            asyncTaskuriActive.remove(i);
            i--;
        }
        for (int i = 0; i < pozeDeIncarcat.size(); i++) {
            pozeDeIncarcat.get(i).cancel(true);
            pozeDeIncarcat.remove(i);
            i--;
        }

    }
    @Override
    public void onResume() {
        super.onResume();
        //daca au ramas asynctaskuri active cand am intrat in pauza atunci reincarc pagina  sa fiu
        //sigur ca am ultimele modificari
        if (suntAsyncTaskActive == true) {
            handleIntent(getIntent());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean("suntAsyncTaskActive",suntAsyncTaskActive);
        savedInstanceState.putParcelableArrayList("searchRezults", searchRezults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
          setContentView(R.layout.pagina_search);
        //creez un nou arraylist de taskuri active
        asyncTaskuriActive = new ArrayList<>();
        pozeDeIncarcat=new ArrayList<>();
        setTitle("Rezultatul cautarii");
        if (savedInstanceState == null) {
            handleIntent(getIntent());
            searchRezults = new ArrayList<>();

        } else {
            suntAsyncTaskActive=savedInstanceState.getBoolean("suntAsyncTaskActive");
            searchRezults = savedInstanceState.getParcelableArrayList("searchRezults");
            //In caz ca utilizatorul schimba orientarea telefonului se pierde textul din tv1
            //asa ca pun iar.
            Intent intent=getIntent();
            String query = intent.getStringExtra(SearchManager.QUERY);
            TextView tv1 = (TextView) findViewById(R.id.PaginaSearchQuery);
            tv1.setText("Rezultatele pentru \""+query+"\"");
        }

     /*   Toast.makeText(getApplicationContext(), "1",
                Toast.LENGTH_SHORT).show();*/
        ListView listV = (ListView) findViewById(R.id.PaginaSearchListView);
        adaptor = new SearchRezultsAdapter(this, searchRezults, this,pozeDeIncarcat);
        listV.setAdapter(adaptor);

    }

    //la aceasta activitate am android:launchMode="singleTop" inseamna ca daca activitatea mea este prima atunci este refolosita
    //aceasta ci nu este creata alta
    @Override
    protected void onNewIntent(Intent intent) {

        ListView listV = (ListView) findViewById(R.id.PaginaSearchListView);
        adaptor = new SearchRezultsAdapter(this, searchRezults, this,pozeDeIncarcat);
        listV.setAdapter(adaptor);

        setIntent(intent);
        handleIntent(intent);
    }

    ;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    public void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if(query.contains("'")) {
                int duration = Toast.LENGTH_SHORT;
                String text = "Caracterul ' nu este permis";
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }else {
                TextView tv1 = (TextView) findViewById(R.id.PaginaSearchQuery);
                tv1.setText("Rezultatele pentru \""+query+"\"");
                task = new SendTask("searchRezults", query);
                task.execute();
            }
        }
    }

    private  class SendTask extends AsyncTask<Void, Void, Void> {
        private DataInputStream dis;
        private DataOutputStream dos;
        private String comanda = "";
        private SearchRezultContainer[] vectorRezultat;
        private int n;
        private String mesaj;
        private boolean eroare = false;
        private String textEroare = "";
        private Context context = getApplicationContext();
        private CharSequence text;
        private int duration = Toast.LENGTH_LONG;
        private Toast toast;
        private int idPentruArrayListDeAyncTaskuriactive;

        SendTask(String comanda, String mesaj) {
            this.comanda = comanda;
            this.mesaj = mesaj;
        }
        @Override
        protected void onPreExecute() {

            //In caz ca ajung la valoarea maxima a lui int resetez
            if(idLastSendTask==Integer.MAX_VALUE)
                idLastSendTask=0;
            //adaug sendtaskul curent in lista de asynctaskuri active si il scot la onPostExecute.
            //asynctaskurile sunt executate unul dupa altul asa ca nu e nevoie sa sincronizez nimic.
            idPentruArrayListDeAyncTaskuriactive = idLastSendTask++;
            asyncTaskuriActive.
                    add(this);

        }
        protected Void doInBackground(Void... arg0) {
            int port = 505;
            String adresa = Constante.adresa;
            Socket cs = null;
            if (ConexiuneLaInternet.conexiuneLaInternet(getApplicationContext()) == false) {
                eroare = true;
                textEroare = "Nu exista conexiune la internet";
                return null;
            }
            try {
                cs = new Socket(adresa, port);
                dos = new DataOutputStream(cs.getOutputStream());
                dis = new DataInputStream(cs.getInputStream());
                //pentru ca asynctaskul face doar un lucru pot sa ii trimit lista de rezultate direct adapterului
                //ca nu exista probleme sa se suprascrie rezultatele altor asynctaskuri
                if (comanda.equals("searchRezults")) {
                    dos.writeUTF("searchRezults");
                    dos.writeUTF(mesaj);
                    n = dis.readInt();
                    if (n > 0) {
                        vectorRezultat = new SearchRezultContainer[n];

                        for (int i = 0; i < n; i++) {
                            vectorRezultat[i] = new SearchRezultContainer();

                            vectorRezultat[i].setNume(dis.readUTF());

                            //poza de profil
                            vectorRezultat[i].setIdPozaProfil(dis.readInt());
                            vectorRezultat[i].setPozaProfilWidth(dis.readInt());
                            vectorRezultat[i].setPozaProfilHeight(dis.readInt());
                        }
                    }
                }
/*               try { Thread.sleep(3000); } catch (InterruptedException e) {

                    e.printStackTrace();}*/
                dis.close();
                dos.close();
                cs.close();
            } catch (ConnectException ce) {
                eroare = true;
                textEroare = "Nu se poate accesa serverul";
                return null;
            } catch (Exception e) {
                eroare = true;
                textEroare = "A aparut o eroare, va rugam mai incercati odata";
                return null;

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //dupa ce am terminat cu asynctaskul curent il scot din asynctaskuri deschise

            for (int i = 0; i < asyncTaskuriActive.size(); i++) {
                if (asyncTaskuriActive.get(i).idPentruArrayListDeAyncTaskuriactive == this.idPentruArrayListDeAyncTaskuriactive) {
                    asyncTaskuriActive.remove(i);
                }
            }
            if (eroare == true) {
                text = textEroare;
                toast = Toast.makeText(context, text, duration);

                toast.show();
                return;
            }
            if (comanda.equals("searchRezults")) {

                if (n == 0) {
                    searchRezults.clear();
                    adaptor.notifyDataSetChanged();
                    text = "Nu exista rezultate pentru cautarea efectuata";
                    toast = Toast.makeText(context, text, duration);
                    toast.show();
                } else {
                    searchRezults.clear();
                    for (int i = 0; i < n; i++) {
                        searchRezults.add(new SearchRezultContainer(vectorRezultat[i].getNume(),vectorRezultat[i].getIdPozaProfil(),vectorRezultat[i].getPozaProfilWidth(),vectorRezultat[i].getPozaProfilHeight()));


                    }
                    adaptor.notifyDataSetChanged();
                }
            }

            super.onPostExecute(result);
        }
    }
}
