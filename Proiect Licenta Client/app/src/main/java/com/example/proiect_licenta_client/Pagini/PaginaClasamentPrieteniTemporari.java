package com.example.proiect_licenta_client.Pagini;


import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;


import com.example.proiect_licenta_client.Adaptere.ClasamentPrieteniTemporariAdapter;
import com.example.proiect_licenta_client.Containere.ClasamentPrieteniTemporariContainer;
import com.example.proiect_licenta_client.Others.ConexiuneLaInternet;
import com.example.proiect_licenta_client.Others.Constante;
import com.example.proiect_licenta_client.Others.IncarcaPoze;
import com.example.proiect_licenta_client.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

public class PaginaClasamentPrieteniTemporari extends Activity {
    private SendTask task;

    private ArrayList<ClasamentPrieteniTemporariContainer> prieteniTemporari;
    private ClasamentPrieteniTemporariAdapter adaptor;
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
        //daca au ramas asynctaskuri active cand am intrat in pauza atunci reincarc pagina de profil sa fiu
        //sigur ca am ultimele modificari
        if (suntAsyncTaskActive == true) {
            task = new SendTask("startPaginaClasamentPrieteniTemporari");
            task.execute();

        }
    }


    private   class SendTask extends AsyncTask<Void, Void, Void> {
        private DataInputStream dis;
        private DataOutputStream dos;
        private  String comanda = "";
        private  ClasamentPrieteniTemporariContainer[] vectorRezultat;
        private  int n;
        private  String mesaj;
        private  boolean eroare = false;
        private  String textEroare = "";
        private  Context context = getApplicationContext();
        private  CharSequence text;
        private  int duration = Toast.LENGTH_LONG;
        private Toast toast;
        private int idPentruArrayListDeAyncTaskuriactive;
        SendTask(String comanda) {
            this.comanda = comanda;
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
            String adresa2 = "10.0.2.2";
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
                if(comanda.equals("startPaginaClasamentPrieteniTemporari")){
                    dos.writeUTF("startPaginaClasamentPrieteniTemporari");

                    n=dis.readInt();
                    if (n > 0) {
                        vectorRezultat = new ClasamentPrieteniTemporariContainer[n];

                        for (int i = 0; i < n; i++) {
                            vectorRezultat[i] = new ClasamentPrieteniTemporariContainer();

                            vectorRezultat[i].setNume(dis.readUTF());
                            //poza de profil
                            vectorRezultat[i].setIdPozaProfil(dis.readInt());
                            vectorRezultat[i].setPozaProfilWidth(dis.readInt());
                            vectorRezultat[i].setPozaProfilHeight(dis.readInt());
                            vectorRezultat[i].setPuncte(dis.readInt());
                        }
                    }
                }
              /*  try { Thread.sleep(3000); } catch (InterruptedException e) {

                    e.printStackTrace(); }*/
                dis.close();
                dos.close();
                cs.close();
            } catch (ConnectException ce) {
                eroare = true;
                textEroare = "Nu se poate accesa serverul";
                return null;
            } catch (Exception e) {
                eroare = true;
                textEroare ="A aparut o eroare, va rugam mai incercati odata";
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
            //pt  e doar o comanda in asynctaskul acesta pot sa trimit mereu aceasi lista
            if (comanda.equals("startPaginaClasamentPrieteniTemporari")) {


                prieteniTemporari.clear();
                if (n == 0) {
                    text = "Nu exista persoane in athiva de prieteni temporari";
                    toast = Toast.makeText(context, text, duration);

                    toast.show();
                } else
                    for (int i = 0; i < n; i++) {
                        prieteniTemporari.add(new ClasamentPrieteniTemporariContainer(vectorRezultat[i].getNume(), vectorRezultat[i].getIdPozaProfil(),vectorRezultat[i].getPozaProfilWidth(),vectorRezultat[i].getPozaProfilHeight(),vectorRezultat[i].getPuncte()));

                    }
                adaptor.notifyDataSetChanged();
            }

            super.onPostExecute(result);
        }
    }









    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean("suntAsyncTaskActive",suntAsyncTaskActive);

        savedInstanceState.putParcelableArrayList("prieteniTemporari", prieteniTemporari);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.pagina_clasament_prieteni_temporari);
        //creez un nou arraylist de taskuri active
        asyncTaskuriActive = new ArrayList<>();
        pozeDeIncarcat=new ArrayList<>();

        setTitle("Clasament Prieteni Temporari");
        if (savedInstanceState == null) {

            prieteniTemporari = new ArrayList<>();


            task=new SendTask("startPaginaClasamentPrieteniTemporari");
            task.execute();
        } else {
            suntAsyncTaskActive=savedInstanceState.getBoolean("suntAsyncTaskActive");
            prieteniTemporari = savedInstanceState.getParcelableArrayList("prieteniTemporari");

        }
        ListView listV = (ListView) findViewById(R.id.PaginaClasamentPrieteniTemporariListView);
        adaptor = new ClasamentPrieteniTemporariAdapter(this, prieteniTemporari, this,pozeDeIncarcat);
        listV.setAdapter(adaptor);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
/*        if (id == R.id.action_settings) {
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }
}
