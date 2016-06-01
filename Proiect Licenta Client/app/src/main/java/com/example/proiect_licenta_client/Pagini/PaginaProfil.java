package com.example.proiect_licenta_client.Pagini;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
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
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proiect_licenta_client.Adaptere.PostAdapter;
import com.example.proiect_licenta_client.Containere.PostContainer;
import com.example.proiect_licenta_client.Others.ConexiuneLaInternet;
import com.example.proiect_licenta_client.Others.Constante;
import com.example.proiect_licenta_client.Others.IncarcaPoze;
import com.example.proiect_licenta_client.R;

public class PaginaProfil extends Activity implements SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener {

    private String cont;
    private ListView listView;
    private String profil;
    private SendTask task;
    private String imgDecodableString;
    private ArrayList<PostContainer> listaPosturi;
    private PostAdapter adaptor;
    private int RESULT_LOAD_IMG = 1;
    private int Incarca_poza_profil = 2;
    private int SCHIMBA_VIZIBLITATE_POST = 3;
    private ArrayList<SendTask> asyncTaskuriActive = null;
    private int idLastSendTask = 0;
    private boolean suntAsyncTaskActive = false;
    private int punctePrietenTemporar = -1;
    private int locInClasament=-1;
    private int idPozaProfil=-1;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    //acest camp de mai jos le folosesc sa imi dau seama cum scroleaza userul
    //in jos sau in sus
    private int primulElementVizibilAnterior = 0;

    private ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat = null;



    @Override
    public void onScroll(AbsListView listView, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (listView != null) {

            //compar primul element vizibil anterior cu cel curent si in functie de diferenta intre cei 2
            //imi dau seama daca userul a scrollat in sus sau in jos
            final int currentFirstVisibleItem = listView.getFirstVisiblePosition();
            //scroll in jos
            if (currentFirstVisibleItem > primulElementVizibilAnterior) {
                LinearLayout ll = (LinearLayout) findViewById(R.id.PaginaProfilMeniuDeJos);
                ll.setVisibility(View.GONE);

                EditText et=(EditText) findViewById(R.id.PaginaProfilAddMessager);
                //codul de jos ascunde tastatura virtuala
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et.getWindowToken(), 0);

                 /*   Toast.makeText(getApplicationContext(), "jos", Toast.LENGTH_SHORT)
                            .show();*/
                //scroll in sus
            } else if (currentFirstVisibleItem < primulElementVizibilAnterior) {
                LinearLayout ll = (LinearLayout) findViewById(R.id.PaginaProfilMeniuDeJos);
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
                //nu este listaPosturi.size()-1 pentru ca pe pagina de profil e un element in plus in listview
            if (listView.getLastVisiblePosition() == listaPosturi.size()) {
      /*          Toast.makeText(getActivity(), listView.getLastVisiblePosition()+" ,"+(listaPosturi.size()-1),
                        Toast.LENGTH_SHORT).show();*/
                //listviewul de pe pagina de profil are un element in plus deci potate sa dea scroll in jos
                //si pentru ca lista e goala mi-ar da eroare
                //asa nu il las sa dea scroll decat daca sunt elemente in lista
                if(listaPosturi.size()>0) {
                    task = new SendTask("paginaProfilGetMorePosts");
                    task.execute();
                }

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

        }
        return listaPosturiPtAdapter;
    }
    @Override
    public void onRefresh() {
        task = new SendTask("updatePaginaProfil");
        task.execute();
    }


    public void Interese(View view) {
        Intent intent = new Intent(this, PaginaInterese.class);
        intent.putExtra("profil", profil);
        startActivity(intent);
    }


    public void blocheazaToateButoanele() {
        ImageButton butonul1 = (ImageButton) findViewById(R.id.PaginaProfilAddPoza);
        Button butonul2 = (Button) findViewById(R.id.PaginaProfilShimbaPozaProfil);
        ImageButton butonul3 = (ImageButton) findViewById(R.id.PaginaProfilButtonAddMessage);
        Button butonul4 = (Button) findViewById(R.id.PaginaProfilFriendList);
        Button butonul5 = (Button) findViewById(R.id.PaginaProfilSchimaStatusPrietenie);
        Button butonul6 = (Button) findViewById(R.id.PaginaProfilDeclineFriendRequest);
        if (butonul1 != null) butonul1.setClickable(false);
        if (butonul2 != null) butonul2.setClickable(false);
        if (butonul3 != null) butonul3.setClickable(false);
        if (butonul4 != null) butonul4.setClickable(false);
        if (butonul5 != null) butonul5.setClickable(false);
        if (butonul6 != null) butonul6.setClickable(false);

    }

    public void deblocheazaToateButoanele() {
        ImageButton butonul1 = (ImageButton) findViewById(R.id.PaginaProfilAddPoza);
        Button butonul2 = (Button) findViewById(R.id.PaginaProfilShimbaPozaProfil);
        ImageButton butonul3 = (ImageButton) findViewById(R.id.PaginaProfilButtonAddMessage);
        Button butonul4 = (Button) findViewById(R.id.PaginaProfilFriendList);
        Button butonul5 = (Button) findViewById(R.id.PaginaProfilSchimaStatusPrietenie);
        Button butonul6 = (Button) findViewById(R.id.PaginaProfilDeclineFriendRequest);
        if (butonul1 != null) butonul1.setClickable(true);
        if (butonul2 != null) butonul2.setClickable(true);
        if (butonul3 != null) butonul3.setClickable(true);
        if (butonul4 != null) butonul4.setClickable(true);
        if (butonul5 != null) butonul5.setClickable(true);
        if (butonul6 != null) butonul6.setClickable(true);

    }


    public void goPaginaMesaje(View view) {
        blocheazaToateButoanele();
        Intent intent = new Intent(this, PaginaMesaje.class);
        intent.putExtra("cont", cont);
        intent.putExtra("partenerConversatie", profil);
        startActivity(intent);
        deblocheazaToateButoanele();
    }


    public void selecteazaProfil(String profil) {
        blocheazaToateButoanele();
        Intent intent = new Intent(this, PaginaProfil.class);
        intent.putExtra("profil", profil);
        startActivity(intent);
        deblocheazaToateButoanele();
    }

    public void selecteazaComent(int idComent) {
        blocheazaToateButoanele();
        Intent intent = new Intent(this, PaginaComent.class);
        intent.putExtra("idComent", idComent);
        startActivity(intent);
        deblocheazaToateButoanele();
    }

    public void stergeComent(int idComent) {

        blocheazaToateButoanele();
        task = new SendTask("stergeComent", idComent);
        task.execute();

    }

    public void addMessage(View view) {
        blocheazaToateButoanele();

        String mesaj;
        EditText et = (EditText) findViewById(R.id.PaginaProfilAddMessager);
        mesaj = et.getText().toString();
        if (mesaj.equals("")) {
            int duration = Toast.LENGTH_LONG;
            String text = "Mesajul trebuie sa contina cel putin un caracter";
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, text, duration);

            toast.show();
            deblocheazaToateButoanele();
        } else {
            if (mesaj.contains("'")) {
                int duration = Toast.LENGTH_SHORT;
                String text = "Caracterul ' nu este permis";
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            } else {
                Button bt = (Button) findViewById(R.id.PaginaProfilAlegeVizibilitatea);
                String vizibilitate = bt.getText().toString();
                task = new SendTask("addMessageProfil", mesaj, vizibilitate);
                et.setText("");
                task.execute();
            }
        }
    }


    public void schimbaStatusPrietenie(View view) {
        blocheazaToateButoanele();
        Button bt = (Button) findViewById(R.id.PaginaProfilSchimaStatusPrietenie);
        task = new SendTask("schimbaStatusPrietenie", bt.getText().toString());
        task.execute();
    }

    // TOATE OPTIUNILE DE PE PROFIL IN LEGATURA CU PRIETENI PUTEA FII FACUTA CU
    // UN SINGUR BUTON
    // IN AFARA DE ACCEPTARE SAU RESPINGEREA UNEI PRIETENII ASA CA INAFARA DE
    // RESPINGEREA
    // UNEI PRIETENI RESTUL DE OPTIUNI LE FAC CU UN SINGUR BUTON ANUME CEL CU
    // IDUL PaginaProfilSchimaStatusPrietenie
    public void RefuzaCererePrietenie(View view) {
        blocheazaToateButoanele();
        task = new SendTask("schimbaStatusPrietenie", "Respinge cererea de prietenie");
        task.execute();
    }

    public void friendList(View view) {
        blocheazaToateButoanele();
        Intent intent = new Intent(this, PaginaProfilFriendList.class);
        intent.putExtra("profil", profil);
        startActivity(intent);
        deblocheazaToateButoanele();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);


        savedInstanceState.putParcelableArrayList("listaPosturi", listaPosturi);
        savedInstanceState.putString("cont", cont);

        savedInstanceState.putString("profil", profil);

        savedInstanceState.putBoolean("suntAsyncTaskActive", suntAsyncTaskActive);

        savedInstanceState.putInt("idPozaProfil", idPozaProfil);
        savedInstanceState.putInt("punctePrietenTemporar", punctePrietenTemporar);
        savedInstanceState.putInt("locInClasament", locInClasament);

    }

    @Override
    public void onPause() {
        super.onPause();


        if(mSwipeRefreshLayout!=null)mSwipeRefreshLayout.setRefreshing(false);

        //daca sunt asynctaskuri active pun valoarea booleana ca true sa stiu la onresume sa reincarc pagina.
        //si dau cancel la asynctaskuri
        if (asyncTaskuriActive.size() > 0||pozeDeIncarcat.size()>0) {
          /*  Toast.makeText(this, pozeDeIncarcat.size()+" "+asyncTaskuriActive.size(), Toast.LENGTH_SHORT)
                    .show();*/
            suntAsyncTaskActive = true;

        } else {

            suntAsyncTaskActive = false;
        }
        for (int i = 0; i < asyncTaskuriActive.size(); i++) {
            asyncTaskuriActive.get(i).cancel(true);
            asyncTaskuriActive.remove(i);
        }

        //deblochez butoanele pentru ca numai intra in onpostexecute unde se deblocau deobicei butoanele
        deblocheazaToateButoanele();

    }

    @Override
    public void onResume() {
        super.onResume();

        //daca au ramas asynctaskuri active cand am intrat in pauza atunci reincarc pagina de profil sa fiu
        //sigur ca am ultimele modificari
        if (suntAsyncTaskActive == true) {
            task = new SendTask("updatePaginaProfil");
            task.execute();

        }
        /*
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
                        task = new SendTask("updatePaginaProfil");
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
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.pagina_profil);
        //creez un nou arraylist de taskuri active
        asyncTaskuriActive = new ArrayList<>();
        pozeDeIncarcat=new ArrayList<>();

        // in caz ca procesul este omorat trebuie sa luam datele inapoi
        if (savedInstanceState != null) {
            suntAsyncTaskActive = savedInstanceState.getBoolean("suntAsyncTaskActive");

            profil = savedInstanceState.getString("profil");

            cont = savedInstanceState.getString("cont");
            punctePrietenTemporar = savedInstanceState.getInt("punctePrietenTemporar");
            locInClasament=savedInstanceState.getInt("locInClasament");
            idPozaProfil=savedInstanceState.getInt("idPozaProfil");
            listaPosturi = savedInstanceState.getParcelableArrayList("listaPosturi");
            //daca nu este nici un post afisez un mesaj.Cazul acesta este pentru cand utilizatorul schimba
            //orientarea si este creata iar activitate
            TextView tv1 = (TextView) findViewById(R.id.paginaProfilNuSuntPosturi);
            tv1.setVisibility(View.GONE);
            if (listaPosturi.size() == 0)
                tv1.setVisibility(View.VISIBLE);

        } else {
            Intent intent = getIntent();
            profil = intent.getStringExtra("profil");
            SharedPreferences settings = getSharedPreferences("login", MODE_PRIVATE);
            cont = settings.getString("cont", "");


            //codul de mai jos ma asigura ca daca nu sa putut gasi numele contului sau al profilului in sharedperefence, pentru ca
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
                Intent intent2 = new Intent(this, PaginaLogin.class);
                //aceste 2 flaguri ma ajuta sa elimin tot din varful stivei astfel incat daca userul da back
                //pe pagina de login dupa ce a fost redirectionat de acest buton va iesi din aplicatie
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent2);
            }


            listaPosturi = new ArrayList<>();
            //acest task trebuia sa porneasca pagina si threadul trebuia doar sa ii dea refresh
            //dar pentru ca pe moment refreshul incarca iar pagina nu folosesc taskul de mai jos
            //ar trebuii sa il pun iar cand taskul din thread doar updateaza arraylistul ci nu il creaza iar
            task = new SendTask("startPaginaProfil");
            task.execute();

        }


        SharedPreferences settings = getSharedPreferences("login", MODE_PRIVATE);

        // pun profilul in sharedprefences pentru a putea lua numele profilului
        // in commentfragment ca sa ii dau posibilitatea ownerului profilului
        // sa stearga orice fel de comment/reply de pe pagina lui+alte activitati initial foloseam doar pt coment fragment

        SharedPreferences.Editor editor = settings.edit();
        editor.putString("profil", profil);
        editor.commit();

        setTitle("Pagina de profil");

        if (!profil.equals(cont)) {

            //  task = new SendTask("aflaStatusPrietenie");
            //  task.execute();


            // bt.setText("")
        } else {
/*            //daca profilul este al clientului logat atunci las optiunea sa isi schimbe poze de profil
            Button bt = (Button) findViewById(R.id.PaginaProfilShimbaPozaProfil);
            bt.setVisibility(View.VISIBLE);
            //NU IL LAS SA ISI TRIMITA SINGUR MESAJE
            Button bt2 = (Button) findViewById(R.id.PaginaProfilPaginaMesaje);
            bt2.setVisibility(View.GONE);
            */
            //pentru ownerul profilului ii dau optiuni sa aleaga cine vede ce scrie, are 3 optiuni : eu,toti,prieteni
            Button bt3 = (Button) findViewById(R.id.PaginaProfilAlegeVizibilitatea);
            bt3.setVisibility(View.VISIBLE);
        }
        listView = (ListView) findViewById(R.id.PaginaProfilListView);
        adaptor = new PostAdapter(this, copieazaLista(listaPosturi), this, cont, profil,pozeDeIncarcat);

        listView.setAdapter(adaptor);


        if(savedInstanceState!=null){
            //setez punctele de prieten temporar si poza de profil in caz ca sa recreat activitatea
            //sa le ia de aici.
            adaptor.setPunctePrietenTemporar(punctePrietenTemporar);
            adaptor.setLocInClasament(locInClasament);
            adaptor.setIdPozaProfil(idPozaProfil);
            //il folosesc sa reincarce poza de profil si numele utiliatorului cand schimba orientare
            adaptor.notifyDataSetChanged();
        }

        //listener pentru listview sa fie aceasta activitate
        listView.setOnScrollListener(this);


        //pentru swiperefreshlayout sa pun refresh listener aceasta activitate
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.container);
        mSwipeRefreshLayout.setOnRefreshListener(this);


    }

    public void loadImagefromGallery(View view) {
        blocheazaToateButoanele();
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    public void alegeVizibilitateaPostului(View view) {
        blocheazaToateButoanele();
        Intent intent = new Intent(this, PaginaVizibilitatePost.class);

        startActivityForResult(intent, SCHIMBA_VIZIBLITATE_POST);
        deblocheazaToateButoanele();
    }

    public void loadProfilePciturefromGallery(View view) {
        blocheazaToateButoanele();
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, Incarca_poza_profil);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {

            // When an Image is picked
            if ((requestCode == RESULT_LOAD_IMG || requestCode == Incarca_poza_profil || requestCode == SCHIMBA_VIZIBLITATE_POST) && resultCode == RESULT_OK
                    && null != data) {
                if (requestCode == SCHIMBA_VIZIBLITATE_POST) {
                    String rezultat = data.getStringExtra("vizibilitate");
                    Button bt = (Button) findViewById(R.id.PaginaProfilAlegeVizibilitatea);
                    bt.setText(rezultat);
                    deblocheazaToateButoanele();
                } else {

                    // Get the Image from data

                    Uri selectedImage = data.getData();
                    // trebuie array de string pentru ca asa este parametru la
                    // apelul lui query de mai jos
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    // Get the cursor
                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imgDecodableString = cursor.getString(columnIndex);
                    cursor.close();
                    File file = new File(imgDecodableString);
                    if (requestCode == RESULT_LOAD_IMG) {
                        EditText et = (EditText) findViewById(R.id.PaginaProfilAddMessager);
                        String caption = "";
                        //sa nu fie string gol
                        if (et.getText().toString().equals("")) caption = " ";
                        else caption = et.getText().toString();
                        if (caption.contains("'")) {
                            int duration = Toast.LENGTH_SHORT;
                            String text = "Caracterul ' nu este permis";
                            Context context = getApplicationContext();
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                            deblocheazaToateButoanele();
                        } else {
                            Button bt = (Button) findViewById(R.id.PaginaProfilAlegeVizibilitatea);
                            String vizibilitate = bt.getText().toString();
                            task = new SendTask("uploadPozaPost", file, caption, vizibilitate);
                            task.execute();
                            et.setText("");
                        }
                    }
                    if (requestCode == Incarca_poza_profil) {
                        task = new SendTask("schimbaPozaProfil", file);
                        task.execute();
                    }
                /*
                 * ImageView imgView = (ImageView) findViewById(R.id.imgView);
				 *
				 * imgView.setImageBitmap(BitmapFactory
				 * .decodeFile(imgDecodableString))
				 */

                }
            } else {
                //deblochez aici butoanele ca pe celalalt caz se deblocau in postExecute din async si
                //pentru ca nu intru in async trebuie sa o fac aici.
                if (requestCode != SCHIMBA_VIZIBLITATE_POST) {
                    deblocheazaToateButoanele();
                    Toast.makeText(this, "Nu ai ales poza",
                            Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, e + "", Toast.LENGTH_LONG)
                    .show();
        }

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

        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void aflaStatusPrietenie() {

        task = new SendTask("aflaStatusPrietenie");
        task.execute();
    }

    public class SendTask extends AsyncTask<Void, Void, Void> {
        private File fisier1;

        private OutputStream os;
        private InputStream is;
        private DataInputStream dis;

        private DataOutputStream dos;
        public String comanda = "";
        private int idComent;
        private PostContainer[] vectorRezultat = null;
        private ArrayList<PostContainer> listaPosturiCopie;
        private int n;
        private String mesaj;
        private String mesaj2;

        private String raspuns;
        private boolean eroare = false;
        private String textEroare = "";
        private boolean profilExistent = true;
        private Context context = getApplicationContext();
        private CharSequence text;
        private int duration = Toast.LENGTH_LONG;
        private Toast toast;
        private ProgressDialog dialog;
        private int idPentruArrayListDeAyncTaskuriactive;


        SendTask(String comanda, int idComent) {
            this.comanda = comanda;
            this.idComent = idComent;
        }

        SendTask(String comanda, File fisier1, String mesaj, String mesaj2) {
            this.comanda = comanda;
            this.fisier1 = fisier1;
            this.mesaj = mesaj;
            this.mesaj2 = mesaj2;
        }

        SendTask(String comanda, File fisier1) {
            this.comanda = comanda;
            this.fisier1 = fisier1;
        }

        SendTask(String comanda) {
            this.comanda = comanda;
        }

        SendTask(String comanda, String mesaj) {
            this.comanda = comanda;
            this.mesaj = mesaj;
        }

        SendTask(String comanda, String mesaj, String mesaj2) {
            this.comanda = comanda;
            this.mesaj = mesaj;
            this.mesaj2 = mesaj2;
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
            if (comanda.equals("startPaginaProfil") || comanda.equals("paginaProfilGetMorePosts")) {
                dialog = new ProgressDialog(PaginaProfil.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

                dialog.setIndeterminate(true);
                dialog.setCanceledOnTouchOutside(true);


                if (comanda.equals("startPaginaProfil")) {
                    dialog.setMessage("Se incarca pagina de profil...");
                }
                if (comanda.equals("paginaProfilGetMorePosts")) {
                    dialog.setMessage("Se mai cauta posturi de afisat...");
                }
                dialog.show();
            }


        }

        protected Void doInBackground(Void... arg0) {
            int port = 505;
            String adresa2 = "10.0.2.2";
            String adresa = Constante.adresa;
            Socket cs = null;

            try {

                if (ConexiuneLaInternet.conexiuneLaInternet(getApplicationContext()) == false) {
                    eroare = true;
                    textEroare = "Nu exista conexiune la internet";
                    return null;
                }
                cs = new Socket(adresa, port);

                os = cs.getOutputStream();
                is = cs.getInputStream();
                dos = new DataOutputStream(os);
                dis = new DataInputStream(is);
                if (comanda.equals("startPaginaProfil")) {
                    dos.writeUTF("startPaginaProfil");
                    dos.writeUTF(profil);
                    dos.writeUTF(cont);
                    profilExistent = dis.readBoolean();
                    if (profilExistent == true) {
                        //iau cate puncte are
                        punctePrietenTemporar = dis.readInt();
                        //iau locul in clasament
                        locInClasament=dis.readInt();
                        //iau poza de profil a profilului curent
                        idPozaProfil=dis.readInt();
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
                            vectorRezultat[i].setData_postarii(dis.readUTF());
                        }
                        //adaug datele noi
                        adaptor.setPunctePrietenTemporar(punctePrietenTemporar);
                        //pun locul in clasament
                        adaptor.setLocInClasament(locInClasament);
                        //poza de profil
                        adaptor.setIdPozaProfil(idPozaProfil);
                        listaPosturi.clear();
                        for (int i = 0; i < n; i++) {

                            if (vectorRezultat[i].getIdPoza() != -1)
                                listaPosturi.add(new PostContainer(vectorRezultat[i].getIdPost(),
                                        vectorRezultat[i].getAutor(), vectorRezultat[i].getText(), vectorRezultat[i].getIdPozaProfil(), vectorRezultat[i].getPozaProfilWidth(), vectorRezultat[i].getPozaProfilHeight(), vectorRezultat[i].getIdPoza(), vectorRezultat[i].getPozaWidth(), vectorRezultat[i].getPozaHeight(), vectorRezultat[i].getData_postarii()));
                            else listaPosturi.add(new PostContainer(vectorRezultat[i].getIdPost(),
                                    vectorRezultat[i].getAutor(), vectorRezultat[i].getText(), vectorRezultat[i].getIdPozaProfil(), vectorRezultat[i].getPozaProfilWidth(), vectorRezultat[i].getPozaProfilHeight(), vectorRezultat[i].getData_postarii()));
                        }
                        listaPosturiCopie=copieazaLista(listaPosturi);
                    }
                }
                if (comanda.equals("addMessageProfil")) {
                    dos.writeUTF("addMessageProfil");
                    dos.writeUTF(profil);

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
                if (comanda.equals("aflaStatusPrietenie")) {
                    dos.writeUTF("aflaStatusPrietenie");
                    dos.writeUTF(cont);
                    dos.writeUTF(profil);
                    profilExistent = dis.readBoolean();
                    if (profilExistent) {
                        raspuns = dis.readUTF();
                    }
                }
                if (comanda.equals("schimbaStatusPrietenie")) {
                    dos.writeUTF("schimbaStatusPrietenie");
                    dos.writeUTF(cont);
                    dos.writeUTF(profil);
                    profilExistent = dis.readBoolean();
                    if (profilExistent) {
                        dos.writeUTF(mesaj);
                    }

                }
                if (comanda.equals("stergeComent")) {

                    dos.writeUTF("stergeComent");
                    // aici numai verific daca profilul exista pentru ca
                    // este irelevant daca a aparut cometul pe ecran incerc ii
                    // folosesc idul sa
                    // il sterg

                    dos.writeInt(idComent);

                }
                if (comanda.equals("uploadPozaPost")) {
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
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                    int bitmapWidth = options.outWidth;
                    int bitmapHeight = options.outHeight;


                    lung = bytes.length;
                    dos.writeUTF(cont);
                    dos.writeUTF(profil);
                    dos.writeUTF(mesaj);
                    //vizibilitatea
                    dos.writeUTF(mesaj2);
                    dos.writeInt(lung);
                    dos.write(bytes);

                    //trimit width
                    dos.writeInt(bitmapWidth);
                    //trimit height
                    dos.writeInt(bitmapHeight);

				/*	dimPoza = dis.readInt();
                    poza=new byte[dimPoza];
					dis.readFully(poza);
                 */
                }
                if (comanda.equals("schimbaPozaProfil")) {
                    dos.writeUTF("schimbaPozaProfil");

                    byte[] buffer = new byte[8192];
                    BufferedInputStream buf = new BufferedInputStream(
                            new FileInputStream(fisier1));
                    ByteArrayOutputStream rez = new ByteArrayOutputStream();
                    int lung;
                    while ((lung = buf.read(buffer)) != -1) {
                        rez.write(buffer);
                    }
                    byte[] bytes = rez.toByteArray();


                    //aflu rezolutia pozei
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    //daca inJustDecodeBounds e true imi intoarce doar options nu creeaza si bitmapul in sine
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                    int bitmapWidth = options.outWidth;
                    int bitmapHeight = options.outHeight;


                    lung = bytes.length;
                    dos.writeUTF(profil);
                    dos.writeInt(lung);
                    dos.write(bytes);


                    //trimit width
                    dos.writeInt(bitmapWidth);
                    //trimit height
                    dos.writeInt(bitmapHeight);
                    buf.close();
                }
                if (comanda.equals("updatePaginaProfil")) {
                    dos.writeUTF("updatePaginaProfil");
                    dos.writeUTF(profil);
                    dos.writeUTF(cont);
                    //iau cate puncte are
                    punctePrietenTemporar = dis.readInt();
                    //pun locul in clasament
                    locInClasament=dis.readInt();
                    //iau poza de profil a profilului curent
                    idPozaProfil=dis.readInt();



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
                        vectorRezultat[i].setData_postarii(dis.readUTF());
                    }

                    //datele noi
                    adaptor.setPunctePrietenTemporar(punctePrietenTemporar);
                    adaptor.setLocInClasament(locInClasament);
                    adaptor.setIdPozaProfil(idPozaProfil);
                    for (int i = 0; i < n; i++) {

                        if (vectorRezultat[i].getIdPoza() != -1)
                            listaPosturi.add(0, new PostContainer(vectorRezultat[i].getIdPost(),
                                    vectorRezultat[i].getAutor(), vectorRezultat[i].getText(), vectorRezultat[i].getIdPozaProfil(), vectorRezultat[i].getPozaProfilWidth(), vectorRezultat[i].getPozaProfilHeight(), vectorRezultat[i].getIdPoza(), vectorRezultat[i].getPozaWidth(), vectorRezultat[i].getPozaHeight(), vectorRezultat[i].getData_postarii()));
                        else listaPosturi.add(0, new PostContainer(vectorRezultat[i].getIdPost(),
                                vectorRezultat[i].getAutor(), vectorRezultat[i].getText(), vectorRezultat[i].getIdPozaProfil(), vectorRezultat[i].getPozaProfilWidth(), vectorRezultat[i].getPozaProfilHeight(), vectorRezultat[i].getData_postarii()));
                    }
                    listaPosturiCopie=copieazaLista(listaPosturi);
                }

/*				 try { Thread.sleep(3000); } catch (InterruptedException e) {

				 e.printStackTrace(); }
*/

                if (comanda.equals("paginaProfilGetMorePosts")) {
                    dos.writeUTF("paginaProfilGetMorePosts");
                    dos.writeUTF(profil);
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
                            vectorRezultat[i].setData_postarii(dis.readUTF());
                        }
                    }
                    //date noi
                    for (int i = 0; i < n; i++) {

                        if (vectorRezultat[i].getIdPoza() != -1)
                            listaPosturi.add(new PostContainer(vectorRezultat[i].getIdPost(),
                                    vectorRezultat[i].getAutor(), vectorRezultat[i].getText(), vectorRezultat[i].getIdPozaProfil(), vectorRezultat[i].getPozaProfilWidth(), vectorRezultat[i].getPozaProfilHeight(), vectorRezultat[i].getIdPoza(), vectorRezultat[i].getPozaWidth(), vectorRezultat[i].getPozaHeight(), vectorRezultat[i].getData_postarii()));
                        else listaPosturi.add(new PostContainer(vectorRezultat[i].getIdPost(),
                                vectorRezultat[i].getAutor(), vectorRezultat[i].getText(), vectorRezultat[i].getIdPozaProfil(), vectorRezultat[i].getPozaProfilWidth(), vectorRezultat[i].getPozaProfilHeight(), vectorRezultat[i].getData_postarii()));
                    }
                    listaPosturiCopie=copieazaLista(listaPosturi);
                }
               /*try { Thread.sleep(1000); } catch (InterruptedException e) {

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
                if (dialog != null) dialog.dismiss();

                if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);
                //daca nu este nici un post afisez un mesaj
                TextView tv1 = (TextView) findViewById(R.id.paginaProfilNuSuntPosturi);
                tv1.setVisibility(View.GONE);
                if (listaPosturi.size() == 0)
                    tv1.setVisibility(View.VISIBLE);
                deblocheazaToateButoanele();
                return;
            }

            if (profilExistent == false) {
                findViewById(R.id.PaginaProfilParinte).setVisibility(View.GONE);
                text = "Profilul nu exista";
                toast = Toast.makeText(context, text, duration);
                toast.show();
                //daca nu exista activitatea ies
                deblocheazaToateButoanele();
                finish();
                return;
            }
            if (comanda.equals("startPaginaProfil")) {
                adaptor.setPosturi(listaPosturiCopie);
                adaptor.notifyDataSetChanged();
                // DUPA CE VREAU SA ADAUG UN MESAJ BUTONUL DE ADAUGARE DE MESAJE
                // VREAU SA NU MEARGA
                // PANA CAND TERMIN DE TRIMIS ACEST MESAJ SI EL TRIMITEREA
                // MESAJUL SE TERMINA DUPA
                // SE UPDATEAZA PAGINA CURENTA ADAUGAND ACEST MESAJ
                ImageButton bt = (ImageButton) findViewById(R.id.PaginaProfilButtonAddMessage);
                bt.setClickable(true);
                if (profil.equals(cont)) {
                    findViewById(R.id.PaginaProfilParinte).setVisibility(
                            View.VISIBLE);
                }
            }
            if (comanda.equals("addMessageProfil")) {

                task = new SendTask("updatePaginaProfil");
                task.execute();
                //ma duc la varful paginii cand postez ceva
                if(listaPosturi.size()>0)listView.setSelection(0);
                else listView.setSelection(1);
            }
            if (comanda.equals("aflaStatusPrietenie")) {
                // in caz ca numai se mai vad butoanele prind exceptia de null
                try {
                    Button bt = (Button) findViewById(R.id.PaginaProfilSchimaStatusPrietenie);
                    if (raspuns.equals("prieteni"))
                        bt.setText("Sterge prieten");
                    if (raspuns.equals("trebuieProfilOwnerSaDeaAccept"))
                        bt.setText("Sterge cererea de prietenie");
                    if (raspuns.equals("trebuieContOwnerSaDeaAccept")) {
                        bt.setText("Accepta cererea de prietenie");
                        Button bt2 = (Button) findViewById(R.id.PaginaProfilDeclineFriendRequest);
                        bt2.setVisibility(View.VISIBLE);
                    }
                    if (raspuns.equals("straini"))
                        bt.setText("Adauga prieten");
                    bt.setVisibility(View.VISIBLE);
                    findViewById(R.id.PaginaProfilParinte).setVisibility(
                            View.VISIBLE);
                } catch (Exception e) {
                   // Toast.makeText(context, e + "", duration).show();
                }
            }
            if (comanda.equals("schimbaStatusPrietenie")) {
                // in caz ca numai se mai vad butoanele prind exceptia de null
                try {
                    Button bt = (Button) findViewById(R.id.PaginaProfilSchimaStatusPrietenie);
                    Button bt2 = (Button) findViewById(R.id.PaginaProfilDeclineFriendRequest);
                    if (bt != null && bt2 != null) {
                        if (mesaj.equals("Adauga prieten"))
                            bt.setText("Sterge cererea de prietenie");
                        if (mesaj.equals("Sterge prieten"))
                            bt.setText("Adauga prieten");
                        if (mesaj.equals("Sterge cererea de prietenie"))
                            bt.setText("Adauga prieten");
                        if (mesaj.equals("Accepta cererea de prietenie")) {
                            bt.setText("Sterge prieten");

                            bt2.setVisibility(View.GONE);
                        }
                        if (mesaj.equals("Respinge cererea de prietenie")) {
                            bt.setText("Adauga prieten");

                            bt2.setVisibility(View.GONE);
                        }
                    }
                } catch (Exception e) {

                }

            }
            if (comanda.equals("stergeComent")) {
                task = new SendTask("updatePaginaProfil");
                task.execute();
            }
            if (comanda.equals("uploadPozaPost")) {

                task = new SendTask("updatePaginaProfil");
                task.execute();
                //ma duc la varful paginii cand postez ceva
                if(listaPosturi.size()>0)listView.setSelection(0);
                else listView.setSelection(1);
            }
            if (comanda.equals("schimbaPozaProfil")) {
                task = new SendTask("updatePaginaProfil");
                task.execute();
            }
            if (comanda.equals("updatePaginaProfil")) {
                adaptor.setPosturi(listaPosturiCopie);
                adaptor.notifyDataSetChanged();
                //ascund rotita de refresh

                mSwipeRefreshLayout.setRefreshing(false);
            }
            if (comanda.equals("paginaProfilGetMorePosts")) {

                adaptor.setPosturi(listaPosturiCopie);
                adaptor.notifyDataSetChanged();
            }

            //daca nu este nici un post afisez un mesaj
            TextView tv1 = (TextView) findViewById(R.id.paginaProfilNuSuntPosturi);
            tv1.setVisibility(View.GONE);
            if (listaPosturi.size() == 0)
                tv1.setVisibility(View.VISIBLE);

            if (dialog != null) dialog.dismiss();
            deblocheazaToateButoanele();


            super.onPostExecute(result);
        }
    }

}
