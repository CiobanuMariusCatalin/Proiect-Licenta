package com.example.proiect_licenta_client.PaginaPrincipalaViewPagersFragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.*;
import android.view.ViewGroup;
import android.widget.*;

import com.example.proiect_licenta_client.Adaptere.FriendRequestAdapter;
import com.example.proiect_licenta_client.Containere.FriendRequestContainer;
import com.example.proiect_licenta_client.Others.ConexiuneLaInternet;
import com.example.proiect_licenta_client.Others.Constante;
import com.example.proiect_licenta_client.Others.IncarcaPoze;
import com.example.proiect_licenta_client.Pagini.PaginaLogin;
import com.example.proiect_licenta_client.Pagini.PaginaPrincipala;
import com.example.proiect_licenta_client.Pagini.PaginaProfil;
import com.example.proiect_licenta_client.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;


public class PaginaPrincipalaFriendRequestsFragment extends ListFragment implements AbsListView.OnScrollListener {
    private ArrayList<FriendRequestContainer> friendRequests;

    private FriendRequestAdapter adaptor;
    private Thread thread;
    private SendTask task;
    private String cont;
    private View rootView;
    private ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat = null;
    private ArrayList<SendTask> asyncTaskuriActive = null;
    private int idLastSendTask = 0;



 public ArrayList<FriendRequestContainer> copieazaLista(ArrayList<FriendRequestContainer> friendRequests){
     ArrayList<FriendRequestContainer> friendRequestsPtAdapter=new ArrayList<>();
     for(int i=0;i<friendRequests.size();i++) {
         friendRequestsPtAdapter.add(new FriendRequestContainer());
         friendRequestsPtAdapter.get(i).setIdPozaProfil(friendRequests.get(i).getIdPozaProfil());
         friendRequestsPtAdapter.get(i).setPozaProfilWidth(friendRequests.get(i).getPozaProfilWidth());
        friendRequestsPtAdapter.get(i).setPozaProfilHeight(friendRequests.get(i).getPozaProfilHeight());
        friendRequestsPtAdapter.get(i).setNume(friendRequests.get(i).getNume());
     }
     return  friendRequestsPtAdapter;
 }

    @Override
    public void onScroll(AbsListView listView, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {

    }

    @Override
    public void onScrollStateChanged(AbsListView listView, int scrollState) {

        if (scrollState == SCROLL_STATE_IDLE) {
            if (friendRequests!= null) {

                if (listView.getLastVisiblePosition() == friendRequests.size() - 1) {
      /*          Toast.makeText(getActivity(), listView.getLastVisiblePosition()+" ,"+(listaPosturi.size()-1),
                        Toast.LENGTH_SHORT).show();*/
                    task = new SendTask("paginaFriendRequestsGetMoreFriendRequests");
                    task.execute();
                }


            }
        }
    }




    public void selecteazaProfil(String profil) {

        Intent intent = new Intent(getActivity(), PaginaProfil.class);
        intent.putExtra("profil", profil);
        startActivity(intent);

    }

    public void acceptFriendRequest(String profil) {
        task = new SendTask("acceptFriendRequest", profil);
        task.execute();
    }

    public void declineFriendRequest(String profil) {
        task = new SendTask("declineFriendRequest", profil);
        task.execute();
    }



    @Override
    public void onPause() {
        super.onPause();
        //opresc asynctaskurile active
        for (int i = 0; i < asyncTaskuriActive.size(); i++) {
            asyncTaskuriActive.get(i).cancel(true);
            asyncTaskuriActive.remove(i);
            i--;
        }
        for (int i = 0; i <pozeDeIncarcat.size(); i++) {
            pozeDeIncarcat.get(i).cancel(true);
            pozeDeIncarcat.remove(i);
            i--;
        }
        //opresc threadul ce imi da refresh la pagina
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
                            //In fragmente cand intra in stop ele sunt recreate deci nu este necesar sa fie taskul inainte de sleep
                            //dar las asa sa fie la fel ca cele din pagina de profil si cea de comenturi
                            task = new SendTask("startFriendRequests");
                            task.execute();

                        //doarme 25s
                        Thread.sleep(25000);


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
        savedInstanceState.putParcelableArrayList("friendRequests", friendRequests);
        savedInstanceState.putString("cont", cont);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //creez un nou arraylist de taskuri active
        asyncTaskuriActive = new ArrayList<>();
        pozeDeIncarcat=new ArrayList<>();

        if (savedInstanceState == null) {
            friendRequests = new ArrayList<>();
            SharedPreferences settings = getActivity().getSharedPreferences("login", getActivity().MODE_PRIVATE);
            cont = settings.getString("cont", "");
            //codul de mai jos ma asigura ca daca nu sa putut gasi numele contului in sharedperefence, pentru ca
            //un user nu poate crea nume fara nici un caracter,ca voi goli sharedprefence si ma voi intoarce pe pagina de autentificare
            //deci nu se poate intampla nimic neprevazut
            if(cont.equals("")){
                SharedPreferences.Editor editor = settings.edit();
                if(settings.contains("cont"))editor.remove("cont");
                if(settings.contains("profil")) editor.remove("profil");
                if(settings.contains("parola")) editor.remove("parola");
                editor.commit();
                Toast.makeText(getActivity(),"A aparut o problema cu regasirea numele contului in fisiere aplicatiei",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), PaginaLogin.class);
                //aceste 2 flaguri ma ajuta sa elimin tot din varful stivei astfel incat daca userul da back
                //pe pagina de login dupa ce a fost redirectionat de acest buton va iesi din aplicatie
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        } else {
            friendRequests = savedInstanceState.getParcelableArrayList("friendRequests");
            cont = savedInstanceState.getString("cont");
        }
        //acest task trebuia sa porneasca pagina si threadul trebuia doar sa ii dea refresh
        //dar pentru ca pe moment refreshul incarca iar pagina nu folosesc taskul de mai jos
        //ar trebuii sa il pun iar cand taskul din thread doar updateaza arraylistul ci nu il creaza iar
        //  task = new SendTask("startFriendRequests");
        //  task.execute();
        adaptor = new FriendRequestAdapter(getActivity(), copieazaLista(friendRequests), this,pozeDeIncarcat);
        setListAdapter(adaptor);
        getListView().setOnScrollListener(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(
                R.layout.pagina_principala_friend_requests_fragment, container, false);


        return rootView;


    }

    private class SendTask extends AsyncTask<Void, Void, Void> {
        private DataInputStream dis;
        private DataOutputStream dos;
        private String comanda = "";
        private FriendRequestContainer[] vectorRezultat;
        private int n;
        private String mesaj;
        private boolean eroare = false;
        private String textEroare = "";
        private Context context = getActivity();
        private CharSequence text;
        private int duration = Toast.LENGTH_LONG;
        private Toast toast;
        private int idPentruArrayListDeAyncTaskuriactive;
        private ProgressDialog dialog;
        private ArrayList<FriendRequestContainer> friendRequestsCopie;
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
            if(idLastSendTask==Integer.MAX_VALUE)
                idLastSendTask=0;
            //adaug sendtaskul curent in lista de asynctaskuri active si il scot la onPostExecute.
            //asynctaskurile sunt executate unul dupa altul asa ca nu e nevoie sa sincronizez nimic.
            idPentruArrayListDeAyncTaskuriactive = idLastSendTask++;
            asyncTaskuriActive.
                    add(this);
            if (comanda.equals("paginaFriendRequestsGetMoreFriendRequests") ) {
                dialog = new ProgressDialog(getActivity());
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setIndeterminate(true);
                dialog.setCanceledOnTouchOutside(true);
                dialog.setMessage("Se mai incarca cereri de prietenie...");
                dialog.show();
            }
        }
        protected Void doInBackground(Void... arg0) {
            int port = 505;
            String adresa2 = "10.0.2.2";
            String adresa = Constante.adresa;
            Socket cs = null;
            if (ConexiuneLaInternet.conexiuneLaInternet(getActivity()) == false) {
                eroare = true;
                textEroare = "Nu exista conexiune la internet";
                return null;
            }
            try {
                cs = new Socket(adresa, port);
                dos = new DataOutputStream(cs.getOutputStream());
                dis = new DataInputStream(cs.getInputStream());

                if (comanda.equals("startFriendRequests")) {
                    dos.writeUTF("startFriendRequests");
                    dos.writeUTF(cont);
                    n = dis.readInt();
                    if (n > 0) {
                        vectorRezultat = new FriendRequestContainer[n];

                        for (int i = 0; i < n; i++) {
                            vectorRezultat[i] = new FriendRequestContainer();

                            vectorRezultat[i].setNume(dis.readUTF());
                            //poza de profil
                            vectorRezultat[i].setIdPozaProfil(dis.readInt());
                            vectorRezultat[i].setPozaProfilWidth(dis.readInt());
                            vectorRezultat[i].setPozaProfilHeight(dis.readInt());
                        }
                    }
                        //reincarc pagina
                        friendRequests.clear();
                        for (int i = 0; i < n; i++) {
                            friendRequests.add(new FriendRequestContainer(vectorRezultat[i].getNume(), vectorRezultat[i].getIdPozaProfil(), vectorRezultat[i].getPozaProfilWidth(), vectorRezultat[i].getPozaProfilHeight()));
                        }
                        friendRequestsCopie=copieazaLista(friendRequests);

                }
                if (comanda.equals("acceptFriendRequest")) {
                    dos.writeUTF("acceptFriendRequest");
                    dos.writeUTF(cont);
                    dos.writeUTF(mesaj);
                    boolean crereaExista = dis.readBoolean();
                    if (crereaExista == false) {
                        eroare = true;
                        textEroare = "Cererea de prietenie nu exista";
                    }
                }
                if (comanda.equals("declineFriendRequest")) {
                    dos.writeUTF("declineFriendRequest");
                    dos.writeUTF(cont);
                    dos.writeUTF(mesaj);
                    boolean crereaExista = dis.readBoolean();
                    if (crereaExista == false) {
                        eroare = true;
                        textEroare = "Cererea de prietenie nu exista";
                    }
                }
                //incarc cate 20 de posturi odata
                if(comanda.equals("paginaFriendRequestsGetMoreFriendRequests")) {

                        dos.writeUTF("paginaFriendRequestsGetMoreFriendRequests");
                        dos.writeUTF(cont);
                        dos.writeUTF(friendRequests.get(friendRequests.size() - 1).getNume());
                        n = dis.readInt();
                        if (n > 0) {
                            vectorRezultat = new FriendRequestContainer[n];

                            for (int i = 0; i < n; i++) {
                                vectorRezultat[i] = new FriendRequestContainer();

                                vectorRezultat[i].setNume(dis.readUTF());
                                //poza de profil
                                vectorRezultat[i].setIdPozaProfil(dis.readInt());
                                vectorRezultat[i].setPozaProfilWidth(dis.readInt());
                                vectorRezultat[i].setPozaProfilHeight(dis.readInt());
                            }

                        }
                            //elementen noi

                            for (int i = 0; i < n; i++) {
                                friendRequests.add(new FriendRequestContainer(vectorRezultat[i].getNume(), vectorRezultat[i].getIdPozaProfil(), vectorRezultat[i].getPozaProfilWidth(), vectorRezultat[i].getPozaProfilHeight()));
                            }
                            friendRequestsCopie=copieazaLista(friendRequests);

                    }

    /*         try { Thread.sleep(4000); } catch (InterruptedException e) {

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
                if (dialog != null) dialog.dismiss();

                return;
            }
            if (comanda.equals("startFriendRequests")) {

                // LinearLayout fragContainer = (LinearLayout)
                // findViewById(R.id.LLFragmentContainer);
                TextView tv = (TextView) rootView.findViewById(R.id.PaginaPrincipalaFriendReqNuSuntFriendRequests);
                //devine invizibil de fiecare data cand se incarca pagina pentru ca daca ar fii vizibila
                //si dupa cateva incercari ar aparea cereri de prietenie ar aparea si textul care imi spune
                //ca nu am cereri de prietenie
                tv.setVisibility(View.GONE);

                if (n == 0) {
                    tv.setVisibility(View.VISIBLE);
                }

                adaptor.setFriendRequests(friendRequestsCopie);
                adaptor.notifyDataSetChanged();

            }
            if (comanda.equals("acceptFriendRequest")) {

                    task = new SendTask("startFriendRequests");
                    task.execute();


            }
            if (comanda.equals("declineFriendRequest")) {

                    task = new SendTask("startFriendRequests");
                    task.execute();

            }
            if(comanda.equals("paginaFriendRequestsGetMoreFriendRequests")){

                adaptor.setFriendRequests(friendRequestsCopie);
                adaptor.notifyDataSetChanged();
                //pentru ca am terminat asynctaskul ce updateaza paginiile inseamna ca dau permisiune si threadului
                //principal si threadului din background sa apeleze acest asynctask

            }
            if (dialog != null) dialog.dismiss();
            super.onPostExecute(result);
        }
    }
}
