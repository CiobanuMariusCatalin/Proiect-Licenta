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
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proiect_licenta_client.Adaptere.ArhivaPrieteniTemporariAdapter;
import com.example.proiect_licenta_client.Others.ConexiuneLaInternet;
import com.example.proiect_licenta_client.Others.Constante;
import com.example.proiect_licenta_client.Containere.ArhivaPrieteniTemporariContainer;
import com.example.proiect_licenta_client.Others.IncarcaPoze;
import com.example.proiect_licenta_client.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

public class PaginaArhivaPrieteniTemporari extends Activity implements AbsListView.OnScrollListener {
    private SendTask task;
    private String cont;
    private ArrayList<ArhivaPrieteniTemporariContainer> prieteniTemporari;
    private ArhivaPrieteniTemporariAdapter adaptor;
    private ArrayList<SendTask> asyncTaskuriActive = null;
    private int idLastSendTask = 0;
    private boolean suntAsyncTaskActive = false;
    private ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat = null;


    @Override
    public void onScroll(AbsListView listView, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {

    }

    @Override
    public void onScrollStateChanged(AbsListView listView, int scrollState) {

        if (scrollState == SCROLL_STATE_IDLE) {
            if (listView != null && prieteniTemporari != null) {

                if (listView.getLastVisiblePosition() == prieteniTemporari.size() - 1) {
      /*          Toast.makeText(getActivity(), listView.getLastVisiblePosition()+" ,"+(prieteniTemporari.size()-1),
                        Toast.LENGTH_SHORT).show();*/
                    task = new SendTask("arhivaPrieteniTemporariGetMore");
                    task.execute();
                }


            }
        }
    }

    public ArrayList<ArhivaPrieteniTemporariContainer> copieazaLista(ArrayList<ArhivaPrieteniTemporariContainer> prieteniTemporari) {
        ArrayList<ArhivaPrieteniTemporariContainer> prieteniTemporariPtAdapter = new ArrayList<>();
        for (int i = 0; i < prieteniTemporari.size(); i++) {
            prieteniTemporariPtAdapter.add(new ArhivaPrieteniTemporariContainer());
            prieteniTemporariPtAdapter.get(i).setIdPozaProfil(prieteniTemporari.get(i).getIdPozaProfil());
            prieteniTemporariPtAdapter.get(i).setPozaProfilWidth(prieteniTemporari.get(i).getPozaProfilWidth());
            prieteniTemporariPtAdapter.get(i).setPozaProfilHeight(prieteniTemporari.get(i).getPozaProfilHeight());
            prieteniTemporariPtAdapter.get(i).setNume(prieteniTemporari.get(i).getNume());
            prieteniTemporariPtAdapter.get(i).setData_adaugarii(prieteniTemporari.get(i).getData_adaugarii());
            prieteniTemporariPtAdapter.get(i).setData_terminarii(prieteniTemporari.get(i).getData_terminarii());
        }
        return prieteniTemporariPtAdapter;

    }

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
        if (asyncTaskuriActive.size() > 0 || pozeDeIncarcat.size() > 0) {
            // Toast.makeText(this, pozeDeIncarcat.size()+" "+asyncTaskuriActive.size(), Toast.LENGTH_SHORT)
            //          .show();
            suntAsyncTaskActive = true;
        } else {
            suntAsyncTaskActive = false;
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
            task = new SendTask("startPaginaArhivaPrieteniTemporari");
            task.execute();

        }
    }

    private class SendTask extends AsyncTask<Void, Void, Void> {
        private DataInputStream dis;
        private DataOutputStream dos;
        private String comanda = "";
        private ArhivaPrieteniTemporariContainer[] vectorRezultat;
        private ArrayList<ArhivaPrieteniTemporariContainer> prieteniTemporariCopie;
        private int n;
        private String mesaj;
        private boolean eroare = false;
        private String textEroare = "";
        private Context context = getApplicationContext();
        private CharSequence text;
        private int duration = Toast.LENGTH_LONG;
        private Toast toast;
        private int idPentruArrayListDeAyncTaskuriactive;

        SendTask(String comanda) {
            this.comanda = comanda;
        }

        @Override
        protected void onPreExecute() {

            //In caz ca ajung la valoarea maxima a lui int resetez
            if (idLastSendTask == Integer.MAX_VALUE)
                idLastSendTask = 0;
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
                if (comanda.equals("startPaginaArhivaPrieteniTemporari")) {
                    dos.writeUTF("startPaginaArhivaPrieteniTemporari");
                    dos.writeUTF(cont);
                    n = dis.readInt();
                    if (n > 0) {
                        vectorRezultat = new ArhivaPrieteniTemporariContainer[n];

                        for (int i = 0; i < n; i++) {
                            vectorRezultat[i] = new ArhivaPrieteniTemporariContainer();

                            vectorRezultat[i].setNume(dis.readUTF());
                            vectorRezultat[i].setData_adaugarii(dis.readUTF());
                            vectorRezultat[i].setData_terminarii(dis.readUTF());
                            //poza de profil
                            vectorRezultat[i].setIdPozaProfil(dis.readInt());
                            vectorRezultat[i].setPozaProfilWidth(dis.readInt());
                            vectorRezultat[i].setPozaProfilHeight(dis.readInt());

                        }
                    }
                    //date noi
                    prieteniTemporari.clear();


                    for (int i = 0; i < n; i++) {
                        prieteniTemporari.add(new ArhivaPrieteniTemporariContainer(vectorRezultat[i].getNume(), vectorRezultat[i].getIdPozaProfil(), vectorRezultat[i].getPozaProfilWidth(), vectorRezultat[i].getPozaProfilHeight(), vectorRezultat[i].getData_adaugarii(), vectorRezultat[i].getData_terminarii()));

                    }
                    prieteniTemporariCopie = copieazaLista(prieteniTemporari);


                }
                if (comanda.equals("arhivaPrieteniTemporariGetMore")) {
                    dos.writeUTF("arhivaPrieteniTemporariGetMore");
                    dos.writeUTF(cont);
                    dos.writeUTF(prieteniTemporari.get(prieteniTemporari.size() - 1).getData_adaugarii());
                    n = dis.readInt();
                    if (n > 0) {
                        vectorRezultat = new ArhivaPrieteniTemporariContainer[n];

                        for (int i = 0; i < n; i++) {
                            vectorRezultat[i] = new ArhivaPrieteniTemporariContainer();

                            vectorRezultat[i].setNume(dis.readUTF());
                            vectorRezultat[i].setData_adaugarii(dis.readUTF());
                            vectorRezultat[i].setData_terminarii(dis.readUTF());
                            //poza de profil
                            vectorRezultat[i].setIdPozaProfil(dis.readInt());
                            vectorRezultat[i].setPozaProfilWidth(dis.readInt());
                            vectorRezultat[i].setPozaProfilHeight(dis.readInt());

                        }
                    }
                    for (int i = 0; i < n; i++) {
                        prieteniTemporari.add(new ArhivaPrieteniTemporariContainer(vectorRezultat[i].getNume(), vectorRezultat[i].getIdPozaProfil(), vectorRezultat[i].getPozaProfilWidth(), vectorRezultat[i].getPozaProfilHeight(), vectorRezultat[i].getData_adaugarii(), vectorRezultat[i].getData_terminarii()));
                    }
                    prieteniTemporariCopie = copieazaLista(prieteniTemporari);

                }
              /* try { Thread.sleep(3000); } catch (InterruptedException e) {

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

            if (comanda.equals("startPaginaArhivaPrieteniTemporari")) {


                adaptor.setPrieteniTemporari(prieteniTemporariCopie);
                adaptor.notifyDataSetChanged();
            }
            if (comanda.equals("arhivaPrieteniTemporariGetMore")) {

                adaptor.setPrieteniTemporari(prieteniTemporariCopie);
                adaptor.notifyDataSetChanged();
            }
            TextView tv1 = (TextView) findViewById(R.id.paginaArhivaPrieteniTemporariNuSuntElemente);
            tv1.setVisibility(View.GONE);
            if (prieteniTemporari.size() == 0) {
                tv1.setVisibility(View.VISIBLE);
            }
            super.onPostExecute(result);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("suntAsyncTaskActive", suntAsyncTaskActive);

        savedInstanceState.putString("cont", cont);
        savedInstanceState.putParcelableArrayList("prieteniTemporari", prieteniTemporari);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.pagina_arhiva_prieteni_temporari);
        //creez un nou arraylist de taskuri active
        asyncTaskuriActive = new ArrayList<>();
        pozeDeIncarcat = new ArrayList<>();
        setTitle("Arhiva Prieteni Temporari");
        if (savedInstanceState == null) {

            prieteniTemporari = new ArrayList<>();
            SharedPreferences settings = getSharedPreferences("login", MODE_PRIVATE);
            cont = settings.getString("cont", "");


            //codul de mai jos ma asigura ca daca nu sa putut gasi numele contului in sharedperefence, pentru ca
            //un user nu poate crea nume fara nici un caracter,ca voi goli sharedprefence si ma voi intoarce pe pagina de autentificare
            //deci nu se poate intampla nimic neprevazut
            if (cont.equals("")) {
                SharedPreferences.Editor editor = settings.edit();
                if (settings.contains("cont")) editor.remove("cont");
                if (settings.contains("profil")) editor.remove("profil");
                if (settings.contains("parola")) editor.remove("parola");
                editor.commit();
                Toast.makeText(this, "A aparut o problema cu regasirea numele contului in fisiere aplicatiei",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, PaginaLogin.class);
                //aceste 2 flaguri ma ajuta sa elimin tot din varful stivei astfel incat daca userul da back
                //pe pagina de login dupa ce a fost redirectionat de acest buton va iesi din aplicatie
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }


            task = new SendTask("startPaginaArhivaPrieteniTemporari");
            task.execute();
        } else {
            suntAsyncTaskActive = savedInstanceState.getBoolean("suntAsyncTaskActive");
            prieteniTemporari = savedInstanceState.getParcelableArrayList("prieteniTemporari");
            cont = savedInstanceState.getString("cont");

            TextView tv1 = (TextView) findViewById(R.id.paginaArhivaPrieteniTemporariNuSuntElemente);
            tv1.setVisibility(View.GONE);
            if (prieteniTemporari.size() == 0) {
                tv1.setVisibility(View.VISIBLE);
            }
        }


        ListView listV = (ListView) findViewById(R.id.PaginaArhivaPriteniTemporariListView);
        adaptor = new ArhivaPrieteniTemporariAdapter(this, copieazaLista(prieteniTemporari), this, pozeDeIncarcat);
        listV.setAdapter(adaptor);
        listV.setOnScrollListener(this);

    }


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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
}
