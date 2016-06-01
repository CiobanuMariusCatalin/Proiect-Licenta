package com.example.proiect_licenta_client.Pagini;


import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.proiect_licenta_client.Adaptere.MesajeAdapter;
import com.example.proiect_licenta_client.Containere.MesajeContainer;
import com.example.proiect_licenta_client.Others.ConexiuneLaInternet;
import com.example.proiect_licenta_client.Others.Constante;
import com.example.proiect_licenta_client.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

public class PaginaMesaje extends Activity implements AbsListView.OnScrollListener {
    private String cont;
    private String partenerConversatie;
    private SendTask task;
    private ArrayList<MesajeContainer> listaMesaje;
    private MesajeAdapter adaptor;
    private Thread thread;
    private ArrayList<SendTask> asyncTaskuriActive = null;
    private int idLastSendTask = 0;
    private ListView listV;
    private int primulElementVizibilAnterior = 100;
    @Override
    public void onScroll(AbsListView listView, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (listaMesaje != null) {


            //compar primul element vizibil anterior cu cel curent si in functie de diferenta intre cei 2
            //imi dau seama daca userul a scrollat in sus sau in jos
            final int currentFirstVisibleItem = listView.getFirstVisiblePosition();
            //scroll in jos
            if (currentFirstVisibleItem > primulElementVizibilAnterior) {
                LinearLayout ll = (LinearLayout) findViewById(R.id.PaginaMesajeMeniuDeJos);
                ll.setVisibility(View.GONE);
                EditText et=(EditText) findViewById(R.id.PaginaMesajeAddMessager);

                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et.getWindowToken(), 0);



      /*              Toast.makeText(getApplicationContext(), "jos", Toast.LENGTH_SHORT)
                            .show();*/
                //scroll in sus
            } else if (currentFirstVisibleItem < primulElementVizibilAnterior) {
                LinearLayout ll = (LinearLayout) findViewById(R.id.PaginaMesajeMeniuDeJos);
                ll.setVisibility(View.VISIBLE);
              /*      Toast.makeText(getApplicationContext(), "sus", Toast.LENGTH_SHORT)
                            .show();*/
            }

            primulElementVizibilAnterior = currentFirstVisibleItem;

        }
    }

    @Override
    public void onScrollStateChanged(AbsListView listView, int scrollState) {

        if (scrollState == SCROLL_STATE_IDLE) {
            if (listaMesaje != null) {

                if (listView.getFirstVisiblePosition() == 0) {

      /*          Toast.makeText(getActivity(), listView.getLastVisiblePosition()+" ,"+(listaPosturi.size()-1),
                        Toast.LENGTH_SHORT).show();*/
                    task = new SendTask("getMoreMessages");
                    task.execute();
                }


            }
        }
    }

    public ArrayList<MesajeContainer> copieazaLista(ArrayList<MesajeContainer> listaMesaje) {
        ArrayList<MesajeContainer> listaMesajePtAdapter = new ArrayList<>();
        for (int i = 0; i < listaMesaje.size(); i++) {
            listaMesajePtAdapter.add(new MesajeContainer());
            listaMesajePtAdapter.get(i).setData_postarii(listaMesaje.get(i).getData_postarii());
            listaMesajePtAdapter.get(i).setNume(listaMesaje.get(i).getNume());
            listaMesajePtAdapter.get(i).setText(listaMesaje.get(i).getText());
            listaMesajePtAdapter.get(i).setIdMesaj(listaMesaje.get(i).getIdMesaj());
        }
        return listaMesajePtAdapter;

    }

    public void addMessage(View view) {
        EditText et = (EditText) findViewById(R.id.PaginaMesajeAddMessager);
        String mesaj = et.getText().toString();
        if (mesaj.equals("")) {
            int duration = Toast.LENGTH_LONG;
            String text = "Mesajul trebuie sa contina cel putin un caracter";
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else {
            if (mesaj.contains("'")) {
                int duration = Toast.LENGTH_SHORT;
                String text = "Caracterul ' nu este permis";
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            } else {
                task = new SendTask("addMessagePaginaMesaje", mesaj);
                et.setText("");
                task.execute();
            }
        }
    }

    public void selecteazaProfil(String nume) {
        Intent intent = new Intent(this, PaginaProfil.class);
        intent.putExtra("profil", nume);
        startActivity(intent);
    }


    @Override
    public void onPause() {
        super.onPause();
 /*Toast.makeText(getApplicationContext(),asyncTaskuriActive.size()+"",
                        Toast.LENGTH_SHORT).show();*/

        for (int i = 0; i < asyncTaskuriActive.size(); i++) {
            asyncTaskuriActive.get(i).cancel(true);
            asyncTaskuriActive.remove(i);
        }
        thread.interrupt();
    }

    @Override
    public void onResume() {
        super.onResume();


        //folosesc threadul sa dau refresh la pagina odata la un timp ales de mine
        //onResume este apelat si cand este creata o noua activitate si cand isi revine dupa pauza/stop si dupa
        //ce este recreata activitatea dupa ce a fost distrusa de android asa ca stiu sigur ca se
        //apeleaza mereu
        thread = new Thread() {
            public void run() {
                while (true) {

                    try {

                            //Aici trebuie un task ce da refresh la pagina
                            //pentru moment scoate toate elementele si le baga iar
                            //Task-ul este inaintea de sleep dintr-un anumit motiv si anume daca activitatea intra in stop sau pauza
                            //vreau sa se dea refresh cat mai repede la pagina cand revin sa nu am date neadevarate.
                            task = new SendTask("updatePaginaMesaje");
                            task.execute();


                        //doarme 4s
                        Thread.sleep(4000);

                    } catch (Exception e) {

                        return;
                    }
                }
            }
        };
        thread.start();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString("cont", cont);
        savedInstanceState.putString("partenerConversatie", partenerConversatie);
        savedInstanceState.putParcelableArrayList("listaMesaje", listaMesaje);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.pagina_mesaje);
        //creez un nou arraylist de taskuri active
        asyncTaskuriActive = new ArrayList<>();
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            cont = intent.getStringExtra("cont");
            partenerConversatie = intent.getStringExtra("partenerConversatie");
            listaMesaje = new ArrayList<>();
            //   task = new SendTask("startPaginaMesaje");
            //  task.execute();
        } else {

            listaMesaje = savedInstanceState.getParcelableArrayList("listaMesaje");
            cont = savedInstanceState.getString("cont");
            partenerConversatie = savedInstanceState.getString("partenerConversatie");

        }
        setTitle(partenerConversatie);


        listV = (ListView) findViewById(R.id.PaginaMesajeListView);
        adaptor = new MesajeAdapter(this, copieazaLista(listaMesaje), this, cont);

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

    private class SendTask extends AsyncTask<Void, Void, Void> {
        private DataInputStream dis;
        private DataOutputStream dos;
        private String comanda = "";
        private MesajeContainer[] vectorRezultat;
        private ArrayList<MesajeContainer> listaMesajeCopie;
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
                if (comanda.equals("addMessagePaginaMesaje")) {
                    dos.writeUTF("addMessagePaginaMesaje");
                    dos.writeUTF(cont);
                    dos.writeUTF(partenerConversatie);
                    dos.writeUTF(mesaj);
                }
                if (comanda.equals("startPaginaMesaje")) {
                    dos.writeUTF("startPaginaMesaje");
                    dos.writeUTF(cont);
                    dos.writeUTF(partenerConversatie);
                    n = dis.readInt();
                    if (n > 0) {
                        vectorRezultat = new MesajeContainer[n];

                        for (int i = 0; i < n; i++) {
                            vectorRezultat[i] = new MesajeContainer();
                            vectorRezultat[i].setIdMesaj(dis.readInt());
                            vectorRezultat[i].setNume(dis.readUTF());


                            vectorRezultat[i].setText(dis.readUTF());
                            vectorRezultat[i].setData_postarii(dis.readUTF());
                            vectorRezultat[i].setData_postarii(dis.readUTF());
                        }
                    }
                        for (int i = 0; i < n; i++) {
                            listaMesaje.add(0, new MesajeContainer(vectorRezultat[i].getNume(), vectorRezultat[i].getText(), vectorRezultat[i].getIdMesaj(), vectorRezultat[i].getData_postarii()));

                        }
                    listaMesajeCopie=copieazaLista(listaMesaje);


                }
                if (comanda.equals("updatePaginaMesaje")) {
                    dos.writeUTF("updatePaginaMesaje");
                    dos.writeUTF(cont);
                    dos.writeUTF(partenerConversatie);
                    if (listaMesaje.isEmpty()) dos.writeBoolean(false);
                    else {
                        dos.writeBoolean(true);
                        dos.writeInt(listaMesaje.get(listaMesaje.size() - 1).getIdMesaj());
                    }
                    n = dis.readInt();
                    if (n > 0) {
                        vectorRezultat = new MesajeContainer[n];

                        for (int i = 0; i < n; i++) {
                            vectorRezultat[i] = new MesajeContainer();
                            vectorRezultat[i].setIdMesaj(dis.readInt());
                            vectorRezultat[i].setNume(dis.readUTF());

                            vectorRezultat[i].setText(dis.readUTF());
                            vectorRezultat[i].setData_postarii(dis.readUTF());
                        }
                    }

                        //update mesaje
                        //salvez pozitia unde trebuie adaugate posturile noi
                        //de exemplu trebuie pe pozitia 3 sa adaug 2 posturi noi ele fiind descrescatoare primul este mai recent ca al doilea
                        //deci adag primul post pe pozitia 3 dupa al 2 post tot pe pozitia 3 deci primul post vine pe pozitia 4
                        int ultimMesaj = listaMesaje.size();
                        for (int i = 0; i < n; i++) {
                            listaMesaje.add(ultimMesaj, new MesajeContainer(vectorRezultat[i].getNume(), vectorRezultat[i].getText(), vectorRezultat[i].getIdMesaj(), vectorRezultat[i].getData_postarii()));
                        }
                        listaMesajeCopie=copieazaLista(listaMesaje);
                }
                if (comanda.equals("getMoreMessages")) {
                    dos.writeUTF("getMoreMessages");
                    dos.writeUTF(cont);
                    dos.writeUTF(partenerConversatie);
                    dos.writeInt(listaMesaje.get(0).getIdMesaj());
                    n = dis.readInt();
                    if (n > 0) {
                        vectorRezultat = new MesajeContainer[n];

                        for (int i = 0; i < n; i++) {
                            vectorRezultat[i] = new MesajeContainer();
                            vectorRezultat[i].setIdMesaj(dis.readInt());
                            vectorRezultat[i].setNume(dis.readUTF());

                            vectorRezultat[i].setText(dis.readUTF());
                            vectorRezultat[i].setData_postarii(dis.readUTF());
                        }
                    }
                        //mesaje noi
                        for (int i = 0; i < n; i++) {
                            listaMesaje.add(0, new MesajeContainer(vectorRezultat[i].getNume(), vectorRezultat[i].getText(), vectorRezultat[i].getIdMesaj(), vectorRezultat[i].getData_postarii()));

                        }
                        listaMesajeCopie=copieazaLista(listaMesaje);

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

            if (comanda.equals("startPaginaMesaje")) {

                listaMesaje.clear();
                if (n == 0) {
                    text = "Nu exista mesaje intre voi doi";
                    toast = Toast.makeText(context, text, duration);

                    toast.show();
                } else {

                    adaptor.setMesaje(listaMesajeCopie);
                    adaptor.notifyDataSetChanged();
                    //duc utilizatorul in josul paginii unde este cel mai noi coment
                    listV.setSelection(listaMesaje.size() - 1);


                }
            }
            if (comanda.equals("addMessagePaginaMesaje")) {
                //duc utilizatorul in josul paginii unde este cel mai noi coment
                // if (n > 0) listV.setSelection(listaMesaje.size() - 1);


                    task = new SendTask("updatePaginaMesaje");
                    task.execute();


            }
            if (comanda.equals("updatePaginaMesaje")) {



                adaptor.setMesaje(listaMesajeCopie);
                adaptor.notifyDataSetChanged();

                //am observat ca daca pun codu aceasta inainte de notifyDataSEtChanged nu merge
                //adica nu ma duce in josul paginii.
                if (n > 0) {
                    //duc utilizatorul in josul paginii unde este cel nou mesaj
                   // listV.setSelection(listaMesajeCopie.size() - 1);
                    listV.smoothScrollToPosition(listaMesajeCopie.size() - 1);

                   EditText et=(EditText) findViewById(R.id.PaginaMesajeAddMessager);

                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(et.getWindowToken(), 0);



                   // LinearLayout et=(LinearLayout) findViewById(R.id.PaginaMesajeMeniuDeJos);
                   // et.requestFocus();
                }
            }
            if (comanda.equals("getMoreMessages")) {

                adaptor.setMesaje(listaMesajeCopie);
                adaptor.notifyDataSetChanged();
                if (n > 0) {
                    //fiindca am toate mesajele noi sunt la inceputul listei stiu sigur ca vechiul varf al listei este
                    //varful curent adica 0 + numarul de mesaje noi adica n , deci daca ma duc la pozitia
                    //n utilizatorul nici macar nu observa ca el defapt a ajuns la varfut listei si s-au incarcat noi mesaje

                    listV.smoothScrollToPosition(n);
                    //listV.setSelection(n);


                    EditText et=(EditText) findViewById(R.id.PaginaMesajeAddMessager);
                    //codul de jos ascunde tastatura virtuala
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(et.getWindowToken(), 0);

                }
            }
            super.onPostExecute(result);
        }
    }

}
