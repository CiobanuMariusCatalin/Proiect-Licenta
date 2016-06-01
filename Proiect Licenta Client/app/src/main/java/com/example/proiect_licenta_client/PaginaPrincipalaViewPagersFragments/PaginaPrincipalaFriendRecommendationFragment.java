package com.example.proiect_licenta_client.PaginaPrincipalaViewPagersFragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.*;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proiect_licenta_client.Adaptere.FriendRecommandationAdapter;
import com.example.proiect_licenta_client.Containere.FriendRecommendationContainer;
import com.example.proiect_licenta_client.Others.ConexiuneLaInternet;
import com.example.proiect_licenta_client.Others.Constante;
import com.example.proiect_licenta_client.Others.IncarcaPoze;
import com.example.proiect_licenta_client.Pagini.PaginaLogin;
import com.example.proiect_licenta_client.Pagini.PaginaProfil;
import com.example.proiect_licenta_client.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

public class PaginaPrincipalaFriendRecommendationFragment extends ListFragment {
    private ArrayList<FriendRecommendationContainer> friendRecommendation;

    private FriendRecommandationAdapter adaptor;
    private Thread thread;
    private SendTask task;
    private String cont;
    private View rootView;
    private ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat = null;
    private ArrayList<SendTask> asyncTaskuriActive = null;
    private int idLastSendTask = 0;




    public ArrayList<FriendRecommendationContainer> copieazaLista(ArrayList<FriendRecommendationContainer> friendRecommendation){
        ArrayList<FriendRecommendationContainer> friendRecommendationPtAdapter=new ArrayList<>();
        for(int i=0;i<friendRecommendation.size();i++) {
            friendRecommendationPtAdapter.add(new FriendRecommendationContainer());
            friendRecommendationPtAdapter.get(i).setIdPozaProfil(friendRecommendation.get(i).getIdPozaProfil());
            friendRecommendationPtAdapter.get(i).setPozaProfilWidth(friendRecommendation.get(i).getPozaProfilWidth());
            friendRecommendationPtAdapter.get(i).setPozaProfilHeight(friendRecommendation.get(i).getPozaProfilHeight());
            friendRecommendationPtAdapter.get(i).setNume(friendRecommendation.get(i).getNume());
            friendRecommendationPtAdapter.get(i).setPrieteniInComun(friendRecommendation.get(i).getPrieteniInComun());
        }
        return  friendRecommendationPtAdapter;
    }

    public void selecteazaProfil(String profil) {

        Intent intent = new Intent(getActivity(), PaginaProfil.class);
        intent.putExtra("profil", profil);
        startActivity(intent);

    }
    public void dismissRecommendation(String nume){
        task = new SendTask("PaginaFriendRecommendationDismiss", nume);
        task.execute();
    }
    public void addFriend(String profil) {
        task = new SendTask("addFriend", profil);
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
                            task = new SendTask("startFriendRecommendation");
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
        savedInstanceState.putParcelableArrayList("friendRecommendation", friendRecommendation);
        savedInstanceState.putString("cont", cont);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //creez un nou arraylist de taskuri active
        asyncTaskuriActive = new ArrayList<>();
        pozeDeIncarcat=new ArrayList<>();

        if (savedInstanceState == null) {
            friendRecommendation = new ArrayList<>();
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
            friendRecommendation = savedInstanceState.getParcelableArrayList("friendRecommendation");
            cont = savedInstanceState.getString("cont");
        }
        //acest task trebuia sa porneasca pagina si threadul trebuia doar sa ii dea refresh
        //dar pentru ca pe moment refreshul incarca iar pagina nu folosesc taskul de mai jos
        //ar trebuii sa il pun iar cand taskul din thread doar updateaza arraylistul ci nu il creaza iar
        //  task = new SendTask("startfriendRecommendation");
        //  task.execute();
        adaptor = new FriendRecommandationAdapter(getActivity(),copieazaLista(friendRecommendation), this,pozeDeIncarcat);
        setListAdapter(adaptor);

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(
                R.layout.pagina_principala_friend_recommandation_fragment, container, false);
        return rootView;

    }

    private class SendTask extends AsyncTask<Void, Void, Void> {
        private DataInputStream dis;
        private DataOutputStream dos;
        private String comanda = "";
        private FriendRecommendationContainer[] vectorRezultat;
        private int n;
        private String mesaj;
        private ArrayList<FriendRecommendationContainer> friendRecommendationCopie;
        private boolean eroare = false;
        private String textEroare = "";
        private Context context = getActivity();
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
            if (ConexiuneLaInternet.conexiuneLaInternet(getActivity())== false) {
                eroare = true;
                textEroare = "Nu exista conexiune la internet";
                return null;
            }
            try {
                cs = new Socket(adresa, port);
                dos = new DataOutputStream(cs.getOutputStream());
                dis = new DataInputStream(cs.getInputStream());
                if (comanda.equals("startFriendRecommendation")) {
                    dos.writeUTF("startFriendRecommendation");
                    dos.writeUTF(cont);
                    n = dis.readInt();
                    if (n > 0) {
                        vectorRezultat = new FriendRecommendationContainer[n];

                        for (int i = 0; i < n; i++) {
                            vectorRezultat[i] = new FriendRecommendationContainer();

                            vectorRezultat[i].setNume(dis.readUTF());
                            vectorRezultat[i].setPrieteniInComun(dis.readInt());
                            //poza de profil
                            vectorRezultat[i].setIdPozaProfil(dis.readInt());
                            vectorRezultat[i].setPozaProfilWidth(dis.readInt());
                            vectorRezultat[i].setPozaProfilHeight(dis.readInt());
                        }
                    }
                        friendRecommendation.clear();
                        for (int i = 0; i < n; i++) {

                            friendRecommendation.add(new FriendRecommendationContainer(vectorRezultat[i].getNume(), vectorRezultat[i].getIdPozaProfil(), vectorRezultat[i].getPozaProfilWidth(), vectorRezultat[i].getPozaProfilHeight(), vectorRezultat[i].getPrieteniInComun()));

                        }
                        friendRecommendationCopie= copieazaLista(friendRecommendation);


                }
                if (comanda.equals("addFriend")) {
                    dos.writeUTF("addFriend");
                    dos.writeUTF(cont);
                    dos.writeUTF(mesaj);
                }
                if (comanda.equals("PaginaFriendRecommendationDismiss")) {
                  dos.writeUTF("PaginaFriendRecommendationDismiss");
                    dos.writeUTF(cont);
                    dos.writeUTF(mesaj);

                }
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
            if (comanda.equals("startFriendRecommendation")) {

                TextView tv = (TextView) rootView.findViewById(R.id.paginaPricipalaPrietenFriendRecommendetionInceput);
                tv.setVisibility(View.GONE);
                if (n == 0) {
                    tv.setVisibility(View.VISIBLE);
                }

                adaptor.setFriendRecommendation( friendRecommendationCopie);
                adaptor.notifyDataSetChanged();




            }
            if (comanda.equals("addFriend")) {

                    task = new SendTask("startFriendRecommendation");
                    task.execute();



            }
            if (comanda.equals("PaginaFriendRecommendationDismiss")) {

                    task = new SendTask("startFriendRecommendation");
                    task.execute();


            }
            super.onPostExecute(result);
        }
    }

}
