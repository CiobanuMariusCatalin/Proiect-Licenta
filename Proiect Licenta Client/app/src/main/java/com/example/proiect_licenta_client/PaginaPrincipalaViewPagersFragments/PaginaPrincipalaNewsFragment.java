package com.example.proiect_licenta_client.PaginaPrincipalaViewPagersFragments;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.*;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.support.v4.app.ListFragment;

import com.example.proiect_licenta_client.Adaptere.PostAdapter;
import com.example.proiect_licenta_client.Containere.PostContainer;
import com.example.proiect_licenta_client.Others.ConexiuneLaInternet;
import com.example.proiect_licenta_client.Others.Constante;
import com.example.proiect_licenta_client.Others.IncarcaPoze;
import com.example.proiect_licenta_client.Pagini.PaginaComent;
import com.example.proiect_licenta_client.Pagini.PaginaLogin;
import com.example.proiect_licenta_client.Pagini.PaginaProfil;
import com.example.proiect_licenta_client.Pagini.PaginaVizibilitatePost;
import com.example.proiect_licenta_client.R;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;


public class PaginaPrincipalaNewsFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener {

    private static int RESULT_LOAD_IMG = 1;
    private int Incarca_poza_profil = 2;
    private int SCHIMBA_VIZIBLITATE_POST=3;
    private String imgDecodableString;
    private String cont;
    private ArrayList<PostContainer> listaPosturi;
    private PostAdapter adaptor;
    private ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat = null;
    private SendTask task;
    private View viewRoot;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ArrayList<SendTask> asyncTaskuriActive = null;
    private int idLastSendTask = 0;
    private boolean suntAsyncTaskActive = false;
    private Context context;
    //acest camp de mai jos le folosesc sa imi dau seama cum scroleaza userul
    //in jos sau in sus
    private int primulElementVizibilAnterior=0;

    @Override
    public void onScroll(AbsListView listView, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (listView != null) {

            //compar primul element vizibil anterior cu cel curent si in functie de diferenta intre cei 2
            //imi dau seama daca userul a scrollat in sus sau in jos
            final int currentFirstVisibleItem = listView.getFirstVisiblePosition();
            //scroll in jos
            if (currentFirstVisibleItem > primulElementVizibilAnterior) {
                LinearLayout ll = (LinearLayout) viewRoot.findViewById(R.id.PaginaNewsFeedMeniuDeJos);
                ll.setVisibility(View.GONE);


                //codul de jos ascunde tastatura virtuala
                EditText et=(EditText) viewRoot.findViewById(R.id.PaginaNewsFeedAddMessager);
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                 /*   Toast.makeText(getApplicationContext(), "jos", Toast.LENGTH_SHORT)
                            .show();*/
                //scroll in sus
            } else if (currentFirstVisibleItem < primulElementVizibilAnterior) {
                LinearLayout ll = (LinearLayout)  viewRoot.findViewById(R.id.PaginaNewsFeedMeniuDeJos);
                ll.setVisibility(View.VISIBLE);
                    /*Toast.makeText(getApplicationContext(), "sus", Toast.LENGTH_SHORT)
                            .show();*/
            }

            primulElementVizibilAnterior = currentFirstVisibleItem;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView listView, int scrollState) {

        if (scrollState == SCROLL_STATE_IDLE) {
            if (listaPosturi != null && listView!=null) {

                if (listView.getLastVisiblePosition() == listaPosturi.size() - 1) {
      /*          Toast.makeText(getActivity(), listView.getLastVisiblePosition()+" ,"+(listaPosturi.size()-1),
                        Toast.LENGTH_SHORT).show();*/
                    task = new SendTask("paginaNewsFeedGetMorePosts");
                    task.execute();
                }


            }

        }
    }

    public ArrayList<PostContainer> copieazaLista(ArrayList<PostContainer> listaPosturi) {
        ArrayList<PostContainer> listaPosturiPtAdapter = new ArrayList<>();
        for (int i = 0; i < listaPosturi.size(); i++) {
            listaPosturiPtAdapter.add(new PostContainer());
            if(listaPosturi.get(i).getIdPoza()!=-1){
                listaPosturiPtAdapter.get(i).setIdPoza(listaPosturi.get(i).getIdPoza());
                listaPosturiPtAdapter.get(i).setPozaWidth(listaPosturi.get(i).getPozaWidth());
                listaPosturiPtAdapter.get(i).setPozaHeight(listaPosturi.get(i).getPozaHeight());
            }
            listaPosturiPtAdapter.get(i).setIdPozaProfil(listaPosturi.get(i).getIdPozaProfil());
            listaPosturiPtAdapter.get(i).setPozaProfilWidth(listaPosturi.get(i).getPozaProfilWidth());
            listaPosturiPtAdapter.get(i).setPozaProfilHeight(listaPosturi.get(i).getPozaProfilHeight());
            listaPosturiPtAdapter.get(i).setAutor(listaPosturi.get(i).getAutor());
            listaPosturiPtAdapter.get(i).setData_postarii(listaPosturi.get(i).getData_postarii());
            listaPosturiPtAdapter.get(i).setIdPost(listaPosturi.get(i).getIdPost());
            listaPosturiPtAdapter.get(i).setText(listaPosturi.get(i).getText());
            listaPosturiPtAdapter.get(i).setNewsFragmentProfil(listaPosturi.get(i).getNewsFragmentProfil());
        }
        return listaPosturiPtAdapter;
    }
    
    @Override
    public void onRefresh() {
        task = new SendTask("updatePaginaNewsFeed");
        task.execute();
    }



    public void stergeComent(int idComent) {
        task = new SendTask("stergeComent", idComent);
        task.execute();
    }


    public void selecteazaComent(int idComent,String profil) {
        //pun profilul de unde se afla comentul
        SharedPreferences settings = getActivity().getSharedPreferences("login",
                getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("profil",profil);
        editor.commit();
        Intent intent = new Intent(getActivity(), PaginaComent.class);
        intent.putExtra("idComent", idComent);
        startActivity(intent);

    }


    public void selecteazaProfil(String profil) {

        Intent intent = new Intent(getActivity(), PaginaProfil.class);
        intent.putExtra("profil", profil);
        startActivity(intent);

    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
       context=activity;
    }


    @Override
    public void onPause() {
        super.onPause();
        if(mSwipeRefreshLayout!=null)mSwipeRefreshLayout.setRefreshing(false);
//daca sunt asynctaskuri active pun valoarea booleana ca true sa stiu la onresume sa reincarc pagina.
        //si dau cancel la asynctaskuri
       /* Toast.makeText(getActivity(),asyncTaskuriActive.size()+"",
                          Toast.LENGTH_SHORT).show();*/
        if (asyncTaskuriActive.size() > 0 || pozeDeIncarcat.size()>0) {
            //      Toast.makeText(getApplicationContext(),asyncTaskuriActive.get(0).comanda,
            //          Toast.LENGTH_SHORT).show();
//           Toast.makeText(context, pozeDeIncarcat.size()+" "+asyncTaskuriActive.size(), Toast.LENGTH_SHORT)
//                   .show();
            suntAsyncTaskActive = true;
        } else {
            suntAsyncTaskActive = false;
        }
        for (int i = 0; i < asyncTaskuriActive.size();i++) {
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
       // thread.interrupt();
    }

    @Override
    public void onResume() {
        super.onResume();
        //daca au ramas asynctaskuri active cand am intrat in pauza atunci reincarc pagina  sa fiu
        //sigur ca am ultimele modificari
        if (suntAsyncTaskActive == true) {
            task = new SendTask("updatePaginaNewsFeed");
            task.execute();
        }
        //folosesc threadul sa dau refresh la pagina odata la un timp ales de mine
        //onResume este apelat si cand este creata o noua activitate si cand isi revine dupa pauza/stop si dupa
        //ce este recreata activitatea dupa ce a fost distrusa de android asa ca stiu sigur ca se
        //apeleaza mereu
      /*  thread = new Thread() {
            public void run() {
                while (true) {

                    try {
                        //Aici trebuie un task ce da refresh la pagina
                        //pentru moment scoate toate elementele si le baga iar
                        //In fragmente cand intra in stop ele sunt recreate deci nu este necesar sa fie taskul inainte de sleep
                        //dar las asa sa fie la fel ca cele din pagina de profil si cea de comenturi
                        task = new SendTask("updatePaginaNewsFeed");
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
        */
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelableArrayList("listaPosturi", listaPosturi);
        savedInstanceState.putString("cont",cont);
        savedInstanceState.putBoolean("suntAsyncTaskActive",suntAsyncTaskActive);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        //creez un nou arraylist de taskuri active
        asyncTaskuriActive = new ArrayList<>();
        //
        pozeDeIncarcat=new ArrayList<>();

        if (savedInstanceState == null) {
            listaPosturi = new ArrayList<>();
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
            task = new SendTask("startNewsFeed");
            task.execute();

        } else {
            suntAsyncTaskActive=savedInstanceState.getBoolean("suntAsyncTaskActive");
            listaPosturi = savedInstanceState.getParcelableArrayList("listaPosturi");
            cont = savedInstanceState.getString("cont");
            //daca nu este nici un post afisez un mesaj
            TextView tv1 = (TextView) viewRoot.findViewById(R.id.paginaPricipalaNewsFeedNuSuntPosturi);
            tv1.setVisibility(View.GONE);
            if(listaPosturi.size()==0)
                tv1.setVisibility(View.VISIBLE);
        }
        //acest task trebuia sa porneasca pagina si threadul trebuia doar sa ii dea refresh
        //dar pentru ca pe moment refreshul incarca iar pagina nu folosesc taskul de mai jos
        //ar trebuii sa il pun iar cand taskul din thread doar updateaza arraylistul ci nu il creaza iar
        //task = new SendTask("startNewsFeed");
        //task.execute();
        adaptor = new PostAdapter(getActivity(), copieazaLista(listaPosturi), this,cont,cont,pozeDeIncarcat);
        setListAdapter(adaptor);
        getListView().setOnScrollListener(this);



        //setez ce fac butoanele din meniul de la fundul paginii
        //butonul ce imi alege vizibilitatea
        Button bt=(Button) viewRoot.findViewById(R.id.PaginaNewsFeedAlegeVizibilitatea);
        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PaginaVizibilitatePost.class);
                startActivityForResult(intent, SCHIMBA_VIZIBLITATE_POST);
            }

        });
        //butonul de adaugat poze
        ImageButton bt2=(ImageButton) viewRoot.findViewById(R.id.PaginaNewsFeedAddPoza);
        bt2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Create intent to Open Image applications like Gallery, Google Photos
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
            }

        });
        //butonul ce imi adauga un mesaj scris
        ImageButton bt3=(ImageButton) viewRoot.findViewById(R.id.PaginaNewsFeedButtonAddMessage);
        bt3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String mesaj;
                EditText et = (EditText) viewRoot.findViewById(R.id.PaginaNewsFeedAddMessager);
                mesaj = et.getText().toString();
                if (mesaj.equals("")) {
                    int duration = Toast.LENGTH_LONG;
                    String text = "Mesajul trebuie sa contina cel putin un caracter";
                    Context context = getActivity();
                    Toast toast = Toast.makeText(context, text, duration);

                    toast.show();

                } else {
                    if(mesaj.contains("'")) {
                        int duration = Toast.LENGTH_SHORT;
                        String text = "Caracterul ' nu este permis";
                        Context context = getActivity().getApplicationContext();
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }else {

                        Button bt = (Button) viewRoot.findViewById(R.id.PaginaNewsFeedAlegeVizibilitatea);
                        String vizibilitate = bt.getText().toString();
                        task = new SendTask("addMessageProfil", mesaj, vizibilitate);
                        et.setText("");
                        task.execute();
                    }
                }
            }

        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

      // if(adaptor!=null) adaptor.setPozeDeIncarcat(pozeDeIncarcat);
        viewRoot = inflater.inflate(R.layout.pagina_principala_news_fragment, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) viewRoot.findViewById(R.id.container);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        return viewRoot;


    }







    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {

            // When an Image is picked
            if ((requestCode == RESULT_LOAD_IMG  ||requestCode==SCHIMBA_VIZIBLITATE_POST) && resultCode == getActivity().RESULT_OK
                    && null != data) {
                if(requestCode==SCHIMBA_VIZIBLITATE_POST){
                    String rezultat=data.getStringExtra("vizibilitate");
                    Button bt=(Button) viewRoot.findViewById(R.id.PaginaNewsFeedAlegeVizibilitatea);
                    bt.setText(rezultat);

                }else {

                    // Get the Image from data

                    Uri selectedImage = data.getData();
                    // trebuie array de string pentru ca asa este parametru la
                    // apelul lui query de mai jos
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    // Get the cursor
                    Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imgDecodableString = cursor.getString(columnIndex);
                    cursor.close();
                    File file = new File(imgDecodableString);
                    if (requestCode == RESULT_LOAD_IMG) {
                        EditText et = (EditText) viewRoot.findViewById(R.id.PaginaNewsFeedAddMessager);
                        String caption = "";
                        //sa nu fie string gol
                        if (et.getText().toString().equals("")) caption = " ";
                        else caption = et.getText().toString();

                        if(caption.contains("'")) {
                            int duration = Toast.LENGTH_SHORT;
                            String text = "Caracterul ' nu este permis";
                            Context context = getActivity().getApplicationContext();
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }else {


                            Button bt = (Button) viewRoot.findViewById(R.id.PaginaNewsFeedAlegeVizibilitatea);
                            String vizibilitate = bt.getText().toString();
                            task = new SendTask("uploadPozaPost", file, caption, vizibilitate);
                            task.execute();
                            et.setText("");
                        }
                    }


                }
            } else {
                if(requestCode!=SCHIMBA_VIZIBLITATE_POST) {
                    Toast.makeText(getActivity(), "Nu ai ales poza",
                            Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), e + "", Toast.LENGTH_LONG)
                    .show();
        }

    }




    private class SendTask extends AsyncTask<Void, Void, Void> {
        private DataInputStream dis;
        private ArrayList<PostContainer> listaPosturiCopie;
        private DataOutputStream dos;
        private String comanda = "";
        private int n;
        private String mesaj;
        private String mesaj2;
        private File fisier1;
        private boolean eroare = false;
        private String textEroare = "";
        private int idCom;
        private ProgressDialog dialog;
        private Context context = getActivity().getApplicationContext();
        private CharSequence text;
        private int duration = Toast.LENGTH_LONG;
        private Toast toast;
        private PostContainer[] vectorRezultat;
        private boolean profilExistent;
        private int idPentruArrayListDeAyncTaskuriactive;
        private boolean  prietenTemporarAddReview;
        private String prietenTemporarNumelePersoaneiPentruReview;
        private int rating;

        SendTask(String comanda) {
            this.comanda = comanda;
        }
        SendTask(String comanda,String mesaj,int rating) {
            this.comanda = comanda;
            this.rating=rating;
            this.mesaj=mesaj;
        }
        SendTask(String comanda, int idCom) {
            this.comanda = comanda;
            this.idCom = idCom;
        }
        SendTask(String comanda, File fisier1, String mesaj,String mesaj2) {
            this.comanda = comanda;
            this.fisier1 = fisier1;
            this.mesaj = mesaj;
            this.mesaj2=mesaj2;
        }
        SendTask(String comanda, String mesaj,String mesaj2) {
            this.comanda = comanda;
            this.mesaj = mesaj;
            this.mesaj2=mesaj2;
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


            //cand incarc prima data pagina sau cand incarc mai multe rezultate afisez ca se incarca
            //ca sa stie userul ce se petrece
            if (comanda.equals("startNewsFeed") || comanda.equals("paginaNewsFeedGetMorePosts")) {
                dialog = new ProgressDialog(getActivity());
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

                dialog.setIndeterminate(true);
                dialog.setCanceledOnTouchOutside(true);

            }
            if (comanda.equals("startNewsFeed")) {
                dialog.setMessage("Se incarca pagina de News Feed...");
                dialog.show();
            }
            if (comanda.equals("paginaNewsFeedGetMorePosts")) {
                dialog.setMessage("Se mai cauta posturi de afisat...");
                dialog.show();
            }
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
                if (comanda.equals("startNewsFeed")) {
                    dos.writeUTF("startNewsFeed");
                    dos.writeUTF(cont);

                    n = dis.readInt();
                    vectorRezultat = new PostContainer[n];
                    for (int i = 0; i < n; i++) {
                        vectorRezultat[i] = new PostContainer();
                        vectorRezultat[i].setIdPost(dis.readInt());
                        vectorRezultat[i].setText(dis.readUTF());
                        vectorRezultat[i].setAutor(dis.readUTF());
                        vectorRezultat[i].setIdPozaProfil(dis.readInt());
                        vectorRezultat[i].setPozaProfilWidth(dis.readInt());
                        vectorRezultat[i].setPozaProfilHeight(dis.readInt());

                        String temp = dis.readUTF();
                        if (temp.equals("arePoza")) {
                            vectorRezultat[i].setIdPoza(dis.readInt());
                            vectorRezultat[i].setPozaWidth(dis.readInt());
                            vectorRezultat[i].setPozaHeight(dis.readInt());

                        }
                        vectorRezultat[i].setNewsFragmentProfil(dis.readUTF());
                        vectorRezultat[i].setData_postarii(dis.readUTF());
                    }


                    //bag noile elemente
                    listaPosturi.clear();
                    for (int i = 0; i < n; i++) {

                        if (vectorRezultat[i].getIdPoza() != -1)
                            listaPosturi.add(new PostContainer(vectorRezultat[i].getIdPost(),
                                    vectorRezultat[i].getAutor(), vectorRezultat[i].getText(), vectorRezultat[i].getIdPozaProfil(),vectorRezultat[i].getPozaProfilWidth(),vectorRezultat[i].getPozaProfilHeight(), vectorRezultat[i].getNewsFragmentProfil(), vectorRezultat[i].getIdPoza(),vectorRezultat[i].getPozaWidth(),vectorRezultat[i].getPozaHeight(),vectorRezultat[i].getData_postarii()));
                        else listaPosturi.add(new PostContainer(vectorRezultat[i].getIdPost(),
                                vectorRezultat[i].getAutor(), vectorRezultat[i].getText(),vectorRezultat[i].getIdPozaProfil(),vectorRezultat[i].getPozaProfilWidth(),vectorRezultat[i].getPozaProfilHeight(), vectorRezultat[i].getNewsFragmentProfil(),vectorRezultat[i].getData_postarii()));

                    }
                    listaPosturiCopie=copieazaLista(listaPosturi);
                }
                if (comanda.equals("stergeComent")) {
                    dos.writeUTF("stergeComent");

                    dos.writeInt(idCom);
                }
                if (comanda.equals("updatePaginaNewsFeed")) {
                    dos.writeUTF("updatePaginaNewsFeed");
                    dos.writeUTF(cont);
                    //Sterge din lista comenturile ce numai se regasesc in baza de date
                    dos.writeInt(listaPosturi.size());
                    for (int i = 0; i < listaPosturi.size(); i++) {
                        dos.writeInt(listaPosturi.get(i).getIdPost());
                        boolean comentExista = dis.readBoolean();
                        if (comentExista == false) {
                            listaPosturi.remove(i);
                            //pentru ca sters pozitia i inseamna ca elementul de pe pozitia i+1 o sa vina pe i deci
                            //dau i-- pentru ca atunci cand se va incrementa sa ramana tot pe pozitia i
                            i--;
                        }
                    }

                    //updatez restul de comenturi ramase
                    dos.writeInt(listaPosturi.size());
                    for (int k = 0; k < listaPosturi.size(); k++) {
                        dos.writeInt(listaPosturi.get(k).getIdPost());
                        boolean comentModificat = dis.readBoolean();
                        if (comentModificat == true) {
                            listaPosturi.get(k).setIdPozaProfil(dis.readInt());
                           listaPosturi.get(k).setPozaProfilWidth(dis.readInt());
                            listaPosturi.get(k).setPozaProfilHeight(dis.readInt());
                        }

                    }


                    //trimit idul comentului cel mai recent daca exista comenturi ca sa ii aflu data postarii
                    //si sa adaug in lista comenturile mai recente ca acesta
                    //Trimit false daca nu exista comenturi in lista si true daca exista comenturi in lista
                    if (listaPosturi.size() == 0) dos.writeBoolean(false);
                    else {
                        dos.writeBoolean(true);
                        //trimit idul primului coment pentru ca sunt sortate dupa data postarii descrescator
                        dos.writeInt(listaPosturi.get(0).getIdPost());
                    }
                    n = dis.readInt();
                    vectorRezultat = new PostContainer[n];
                    for (int i = 0; i < n; i++) {
                        vectorRezultat[i] = new PostContainer();
                        vectorRezultat[i].setIdPost(dis.readInt());
                        vectorRezultat[i].setText(dis.readUTF());
                        vectorRezultat[i].setAutor(dis.readUTF());
                        vectorRezultat[i].setIdPozaProfil(dis.readInt());
                       vectorRezultat[i].setPozaProfilWidth(dis.readInt());
                        vectorRezultat[i].setPozaProfilHeight(dis.readInt());

                        String temp = dis.readUTF();
                        if (temp.equals("arePoza")) {
                            vectorRezultat[i].setIdPoza(dis.readInt());
                            vectorRezultat[i].setPozaWidth(dis.readInt());
                            vectorRezultat[i].setPozaHeight(dis.readInt());
                        }
                        vectorRezultat[i].setNewsFragmentProfil(dis.readUTF());
                        vectorRezultat[i].setData_postarii(dis.readUTF());
                    }
                    //pun elementele noi
                    for (int i = 0; i < n; i++) {

                        if (vectorRezultat[i].getIdPoza() != -1)
                            listaPosturi.add(0, new PostContainer(vectorRezultat[i].getIdPost(),
                                    vectorRezultat[i].getAutor(), vectorRezultat[i].getText(), vectorRezultat[i].getIdPozaProfil(),vectorRezultat[i].getPozaProfilWidth(),vectorRezultat[i].getPozaProfilHeight(), vectorRezultat[i].getNewsFragmentProfil(), vectorRezultat[i].getIdPoza(),vectorRezultat[i].getPozaWidth(),vectorRezultat[i].getPozaHeight(),vectorRezultat[i].getData_postarii()));
                        else listaPosturi.add(0, new PostContainer(vectorRezultat[i].getIdPost(),
                                vectorRezultat[i].getAutor(), vectorRezultat[i].getText(), vectorRezultat[i].getIdPozaProfil(),vectorRezultat[i].getPozaProfilWidth(),vectorRezultat[i].getPozaProfilHeight(), vectorRezultat[i].getNewsFragmentProfil(),vectorRezultat[i].getData_postarii()));
                    }
                    listaPosturiCopie=copieazaLista(listaPosturi);

                }
                if (comanda.equals("paginaNewsFeedGetMorePosts")) {
                    dos.writeUTF("paginaNewsFeedGetMorePosts");
                    dos.writeUTF(cont);
                    dos.writeInt(listaPosturi.get(listaPosturi.size() - 1).getIdPost());
                    n = dis.readInt();
                    if (n > 0) {

                        vectorRezultat = new PostContainer[n];
                        for (int i = 0; i < n; i++) {
                            vectorRezultat[i] = new PostContainer();
                            vectorRezultat[i].setIdPost(dis.readInt());
                            vectorRezultat[i].setText(dis.readUTF());
                            vectorRezultat[i].setAutor(dis.readUTF());
                            vectorRezultat[i].setIdPozaProfil(dis.readInt());
                           vectorRezultat[i].setPozaProfilWidth(dis.readInt());
                            vectorRezultat[i].setPozaProfilHeight(dis.readInt());
                            String temp = dis.readUTF();
                            if (temp.equals("arePoza")) {
                                vectorRezultat[i].setIdPoza(dis.readInt());
                                vectorRezultat[i].setPozaWidth(dis.readInt());
                                vectorRezultat[i].setPozaHeight(dis.readInt());
                            }
                            vectorRezultat[i].setNewsFragmentProfil(dis.readUTF());
                            vectorRezultat[i].setData_postarii(dis.readUTF());
                        }
                    }

                    for (int i = 0; i < n; i++) {

                        if (vectorRezultat[i].getIdPoza() != -1)
                            listaPosturi.add(new PostContainer(vectorRezultat[i].getIdPost(),
                                    vectorRezultat[i].getAutor(), vectorRezultat[i].getText(), vectorRezultat[i].getIdPozaProfil(),vectorRezultat[i].getPozaProfilWidth(),vectorRezultat[i].getPozaProfilHeight(), vectorRezultat[i].getNewsFragmentProfil(),vectorRezultat[i].getIdPoza(),vectorRezultat[i].getPozaWidth(),vectorRezultat[i].getPozaHeight(),vectorRezultat[i].getData_postarii()));
                        else listaPosturi.add(new PostContainer(vectorRezultat[i].getIdPost(),
                                vectorRezultat[i].getAutor(), vectorRezultat[i].getText(),  vectorRezultat[i].getIdPozaProfil(),vectorRezultat[i].getPozaProfilWidth(),vectorRezultat[i].getPozaProfilHeight(), vectorRezultat[i].getNewsFragmentProfil(),vectorRezultat[i].getData_postarii()));
                    }
                    listaPosturiCopie=copieazaLista(listaPosturi);

                }
                if (comanda.equals("addMessageProfil")) {
                    //acelasi ca si cel de pe pagina de profil
                    dos.writeUTF("addMessageProfil");
                    dos.writeUTF(cont);

                    profilExistent = dis.readBoolean();
                    if (profilExistent) {
                        dos.writeUTF(cont);
                        dos.writeUTF(mesaj);
                        //trimit si cu ce vizibilitate
                        //daca ownerul profilului a trimis acest mesaj a avut si posibilitatea sa modifice vizbilitatea
                        //daca nu este implicit toti.
                        dos.writeUTF(mesaj2);
                    }
                }
                if (comanda.equals("uploadPozaPost")) {
                    //acelasi ca si cel de pe pagina de profil
                    dos.writeUTF("uploadPozaPost");

                    byte[] buffer = new byte[8192];
                    BufferedInputStream buf = new BufferedInputStream(
                            new FileInputStream(fisier1));
                    ByteArrayOutputStream rez = new ByteArrayOutputStream();
                    int lung;
                    while ((lung = buf.read(buffer)) != -1) {
                        rez.write(buffer);
                    }
                    buf.close();
                    byte[] bytes = rez.toByteArray();

                    //aflu rezolutia pozei
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    //daca inJustDecodeBounds e true imi intoarce doar options nu creeaza si bitmapul in sine
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray( bytes, 0,  bytes.length, options);
                    int bitmapWidth = options.outWidth;
                    int bitmapHeight = options.outHeight;



                    lung = bytes.length;
                    dos.writeUTF(cont);
                    dos.writeUTF(cont);
                    dos.writeUTF(mesaj);
                    //vizibilitatea
                    dos.writeUTF(mesaj2);
                    dos.writeInt(lung);
                    dos.write(bytes);
                    //trimit width
                    dos.writeInt(bitmapWidth);
                    //trimit height
                    dos.writeInt(bitmapHeight);

                }
                if(comanda.equals("addReviewPrietenTemporar")){
                    dos.writeUTF("addReviewPrietenTemporar");
                    dos.writeUTF(cont);
                    prietenTemporarAddReview=dis.readBoolean();
                    if(prietenTemporarAddReview==true){
                        prietenTemporarNumelePersoaneiPentruReview=dis.readUTF();
                    }
                }
                if(comanda.equals("addReviewPrietenTemporarDaNota")){
                    dos.writeUTF("addReviewPrietenTemporarDaNota");
                    dos.writeUTF(cont);
                    //trimit numele prietenului cui ii dau rating
                    dos.writeUTF(mesaj);
                    dos.writeInt(rating);
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
                if (dialog != null) dialog.hide();
                if(mSwipeRefreshLayout!=null) mSwipeRefreshLayout.setRefreshing(false);

                //daca nu este nici un post afisez un mesaj
                TextView tv1 = (TextView) viewRoot.findViewById(R.id.paginaPricipalaNewsFeedNuSuntPosturi);
                tv1.setVisibility(View.GONE);
                if(listaPosturi.size()==0)
                    tv1.setVisibility(View.VISIBLE);


                return;
            }
            if (comanda.equals("startNewsFeed")) {
                adaptor.setPosturi(listaPosturiCopie);
                adaptor.notifyDataSetChanged();
                task=new SendTask("addReviewPrietenTemporar");
                task.execute();

            }
            if (comanda.equals("stergeComent")) {
                task = new SendTask("updatePaginaNewsFeed");
                task.execute();
            }
            if (comanda.equals("updatePaginaNewsFeed")) {


                adaptor.setPosturi(listaPosturiCopie);
                adaptor.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
                //verific daca mai sunt prieteni temporari cu care am avut o conversatie si nu le-am dat rating
                task=new SendTask("addReviewPrietenTemporar");
                task.execute();
            }
            if (comanda.equals("paginaNewsFeedGetMorePosts")) {


                    adaptor.setPosturi(listaPosturiCopie);
                    adaptor.notifyDataSetChanged();


            }
            if (comanda.equals("addMessageProfil")) {
                task = new SendTask("updatePaginaNewsFeed");
                task.execute();
                //duc utilizatorul in varful listei
                getListView().setSelection(0);
            }
            if (comanda.equals("uploadPozaPost")) {
                task = new SendTask("updatePaginaNewsFeed");
                task.execute();
                //duc utilizatorul in varful listei
                getListView().setSelection(0);
            }
            if(comanda.equals("addReviewPrietenTemporar")){
            if( prietenTemporarAddReview==true){

                final Dialog rankDialog = new Dialog(getActivity());
                rankDialog.setContentView(R.layout.dialog_rating_layout);

                TextView tv1=(TextView) rankDialog.findViewById(R.id.dialogRatingLayoutNumePrieten);
                tv1.setText("Evalueaza prietenul temporar : "+prietenTemporarNumelePersoaneiPentruReview);

                RatingBar  ratingBar = (RatingBar)rankDialog.findViewById(R.id.ratingBar);
                ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    public void onRatingChanged(RatingBar ratingBar, float rating,
                                                boolean fromUser) {
                        task=new SendTask("addReviewPrietenTemporarDaNota",prietenTemporarNumelePersoaneiPentruReview,(int)rating);
                        task.execute();
                        /*Toast.makeText(getActivity(), rating+" ",
                                Toast.LENGTH_SHORT).show();*/
                    rankDialog.dismiss();

                    }
                });
                rankDialog.show();
            }
            }


            //daca nu este nici un post afisez un mesaj
            TextView tv1 = (TextView) viewRoot.findViewById(R.id.paginaPricipalaNewsFeedNuSuntPosturi);
            tv1.setVisibility(View.GONE);
            if(listaPosturi.size()==0)
                tv1.setVisibility(View.VISIBLE);

            //daca am progress dialog il ascund cand ajung aici;
            if (dialog != null) dialog.dismiss();
            super.onPostExecute(result);
        }
    }


}