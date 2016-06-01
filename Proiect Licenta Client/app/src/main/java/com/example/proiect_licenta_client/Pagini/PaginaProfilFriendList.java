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
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proiect_licenta_client.Adaptere.FriendListAdapter;
import com.example.proiect_licenta_client.Containere.FriendListContainer;
import com.example.proiect_licenta_client.Others.ConexiuneLaInternet;
import com.example.proiect_licenta_client.Others.Constante;
import com.example.proiect_licenta_client.Others.IncarcaPoze;
import com.example.proiect_licenta_client.R;

public class PaginaProfilFriendList extends Activity implements AbsListView.OnScrollListener {
    private String profil;
    private SendTask task;
    private ArrayList<FriendListContainer> listaPrieteni;
    private FriendListAdapter adaptor;
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
            if (listView != null) {

                if (listView.getLastVisiblePosition() == listaPrieteni.size() - 1) {
      /*          Toast.makeText(getActivity(), listView.getLastVisiblePosition()+" ,"+(listaPosturi.size()-1),
                        Toast.LENGTH_SHORT).show();*/
                    task = new SendTask("friendListGetMoreFriends");
                    task.execute();
                }


            }
        }
    }
    public ArrayList<FriendListContainer> copieazaLista(ArrayList<FriendListContainer> listaPrieteni){
        ArrayList<FriendListContainer> listaPrieteniPtAdapter = new ArrayList<>();
        for (int i = 0; i < listaPrieteni.size(); i++) {
            listaPrieteniPtAdapter.add(new FriendListContainer());
            listaPrieteniPtAdapter.get(i).setIdPozaProfil(listaPrieteni.get(i).getIdPozaProfil());
            listaPrieteniPtAdapter.get(i).setPozaProfilWidth(listaPrieteni.get(i).getPozaProfilWidth());
            listaPrieteniPtAdapter.get(i).setPozaProfilHeight(listaPrieteni.get(i).getPozaProfilHeight());
            listaPrieteniPtAdapter.get(i).setNume(listaPrieteni.get(i).getNume());

        }
        return  listaPrieteniPtAdapter;

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
        //daca au ramas asynctaskuri active cand am intrat in pauza atunci reincarc pagina sa fiu
        //sigur ca am ultimele modificari
        if (suntAsyncTaskActive == true) {
            task = new SendTask("friendList");
            task.execute();

        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("suntAsyncTaskActive", suntAsyncTaskActive);
        savedInstanceState.putString("profil", profil);
        savedInstanceState.putParcelableArrayList("listaPrieteni", listaPrieteni);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.pagina_profil_friend_list);
        //creez un nou arraylist de taskuri active
        asyncTaskuriActive = new ArrayList<>();
        pozeDeIncarcat = new ArrayList<>();
        if (savedInstanceState == null) {

            Intent intent = getIntent();
            profil = intent.getStringExtra("profil");
            listaPrieteni = new ArrayList<>();
            task = new SendTask("friendList");
            task.execute();
        } else {


            suntAsyncTaskActive = savedInstanceState.getBoolean("suntAsyncTaskActive");
            listaPrieteni = savedInstanceState.getParcelableArrayList("listaPrieteni");
            profil = savedInstanceState.getString("profil");
            //in caz ca nu exista nici un rezultat.
            TextView tv1 = (TextView) findViewById(R.id.paginaProfilFriendListNuSuntPrieteni);
            tv1.setVisibility(View.GONE);
            if (listaPrieteni.size() == 0)
                tv1.setVisibility(View.VISIBLE);
        }
        setTitle("Lista Prieteni ai lui:" + profil);


        ListView listV = (ListView) findViewById(R.id.PaginaProfilFriendListListView);
        adaptor = new FriendListAdapter(this, copieazaLista(listaPrieteni), this, pozeDeIncarcat);
        listV.setAdapter(adaptor);
        listV.setOnScrollListener(this);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    private class SendTask extends AsyncTask<Void, Void, Void> {
        private DataInputStream dis;
        private DataOutputStream dos;
        private String comanda = "";
        private FriendListContainer[] vectorRezultat;
        private ArrayList<FriendListContainer> listaPrieteniCopie;
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

        SendTask(String comanda, String mesaj) {
            this.comanda = comanda;
            this.mesaj = mesaj;
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
                if (comanda.equals("friendList")) {
                    dos.writeUTF("friendList");
                    dos.writeUTF(profil);

                    n = dis.readInt();
                    if (n > 0) {
                        vectorRezultat = new FriendListContainer[n];

                        for (int i = 0; i < n; i++) {
                            vectorRezultat[i] = new FriendListContainer();

                            vectorRezultat[i].setNume(dis.readUTF());
                            //poza de profil
                            vectorRezultat[i].setIdPozaProfil(dis.readInt());
                            vectorRezultat[i].setPozaProfilWidth(dis.readInt());
                            vectorRezultat[i].setPozaProfilHeight(dis.readInt());
                        }
                    }
                        //adaug
                        listaPrieteni.clear();
                            for (int i = 0; i < n; i++) {
                                listaPrieteni.add(new FriendListContainer(vectorRezultat[i].getNume(), vectorRezultat[i].getIdPozaProfil(), vectorRezultat[i].getPozaProfilWidth(), vectorRezultat[i].getPozaProfilHeight()));


                            }
                        listaPrieteniCopie=copieazaLista(listaPrieteni);



                }
                if (comanda.equals("friendListGetMoreFriends")) {
                    dos.writeUTF("friendListGetMoreFriends");
                    dos.writeUTF(profil);
                    dos.writeUTF(listaPrieteni.get(listaPrieteni.size() - 1).getNume());
                    n = dis.readInt();
                    if (n > 0) {
                        vectorRezultat = new FriendListContainer[n];

                        for (int i = 0; i < n; i++) {
                            vectorRezultat[i] = new FriendListContainer();

                            vectorRezultat[i].setNume(dis.readUTF());
                            //poza de profil
                            vectorRezultat[i].setIdPozaProfil(dis.readInt());
                            vectorRezultat[i].setPozaProfilWidth(dis.readInt());
                            vectorRezultat[i].setPozaProfilHeight(dis.readInt());
                        }
                    }
                        //adaug
                        for (int i = 0; i < n; i++) {
                            listaPrieteni.add(new FriendListContainer(vectorRezultat[i].getNume(), vectorRezultat[i].getIdPozaProfil(), vectorRezultat[i].getPozaProfilWidth(), vectorRezultat[i].getPozaProfilHeight()));


                        }
                        listaPrieteniCopie=copieazaLista(listaPrieteni);


                }
              /*  try { Thread.sleep(3000); } catch (InterruptedException e) {

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
            if (comanda.equals("friendList")) {



                adaptor.setFriends(listaPrieteniCopie);
                adaptor.notifyDataSetChanged();
            }
            if (comanda.equals("friendListGetMoreFriends")) {

                adaptor.setFriends(listaPrieteniCopie);
                adaptor.notifyDataSetChanged();
            }

            TextView tv1 = (TextView) findViewById(R.id.paginaProfilFriendListNuSuntPrieteni);
            tv1.setVisibility(View.GONE);
            if (listaPrieteni.size() == 0)
                tv1.setVisibility(View.VISIBLE);

            super.onPostExecute(result);
        }
    }

}