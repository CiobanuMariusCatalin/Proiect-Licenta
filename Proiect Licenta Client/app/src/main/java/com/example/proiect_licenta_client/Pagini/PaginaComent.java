package com.example.proiect_licenta_client.Pagini;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

import com.example.proiect_licenta_client.Adaptere.ComentsAdapter;
import com.example.proiect_licenta_client.Others.ConexiuneLaInternet;
import com.example.proiect_licenta_client.Others.Constante;
import com.example.proiect_licenta_client.Containere.ComentContainer;
import com.example.proiect_licenta_client.Others.IncarcaPoze;
import com.example.proiect_licenta_client.R;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

public class PaginaComent extends Activity implements SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener {
    private static int RESULT_LOAD_IMG = 1;
    private String cont;
    private String profil;
    private String imgDecodableString;
    private SendTask task;
    // aici afisam replyurile a unui coment
    // String autor;
    private int idComent;
    private ListView listView;
    private Thread thread;
    private ArrayList<ComentContainer> listaComenturi;

    private ComentsAdapter adaptor;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ArrayList<SendTask> asyncTaskuriActive = null;
    private int idLastSendTask = 0;
    private boolean suntAsyncTaskActive=false;
    //acest camp de mai jos le folosesc sa imi dau seama cum scroleaza userul
    //in jos sau in sus
    private int primulElementVizibilAnterior=0;
    private ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat = null;
    @Override
    public void onScroll(AbsListView listView, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (listView!= null) {


            //compar primul element vizibil anterior cu cel curent si in functie de diferenta intre cei 2
            //imi dau seama daca userul a scrollat in sus sau in jos
            final int currentFirstVisibleItem = listView.getFirstVisiblePosition();
            //scroll in jos
            if (currentFirstVisibleItem > primulElementVizibilAnterior) {
                LinearLayout ll = (LinearLayout) findViewById(R.id.PaginaComentMeniuDeJos);
                ll.setVisibility(View.GONE);


                EditText et=(EditText) findViewById(R.id.PaginaComentAddMessager);
                //codul de jos ascunde tastatura virtuala
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et.getWindowToken(), 0);


      /*              Toast.makeText(getApplicationContext(), "jos", Toast.LENGTH_SHORT)
                            .show();*/
                //scroll in sus
            } else if (currentFirstVisibleItem < primulElementVizibilAnterior) {
                LinearLayout ll = (LinearLayout) findViewById(R.id.PaginaComentMeniuDeJos);
                ll.setVisibility(View.VISIBLE);
              /*      Toast.makeText(getApplicationContext(), "sus", Toast.LENGTH_SHORT)
                            .show();*/
            }

            primulElementVizibilAnterior = currentFirstVisibleItem;

        }
    }
    public ArrayList<ComentContainer> copieazaLista(ArrayList<ComentContainer> coments){
        ArrayList<ComentContainer> listaComenturiAdapter=new ArrayList<>();
        for(int i=0;i<coments.size();i++){
            listaComenturiAdapter.add(new ComentContainer());
            listaComenturiAdapter.get(i).setAutor(coments.get(i).getAutor());
            listaComenturiAdapter.get(i).setData_postarii(coments.get(i).getData_postarii());
            listaComenturiAdapter.get(i).setIdComent(coments.get(i).getIdComent());
            if(coments.get(i).getIdPoza()!=-1) {
                listaComenturiAdapter.get(i).setIdPoza(coments.get(i).getIdPoza());
                listaComenturiAdapter.get(i).setPozaWidth(coments.get(i).getPozaWidth());
                listaComenturiAdapter.get(i).setPozaHeight(coments.get(i).getPozaHeight());
            }
            listaComenturiAdapter.get(i).setIdPozaProfil(coments.get(i).getIdPozaProfil());
            listaComenturiAdapter.get(i).setPozaProfilWidth(coments.get(i).getPozaProfilWidth());
            listaComenturiAdapter.get(i).setPozaProfilHeight(coments.get(i).getPozaProfilHeight());
            listaComenturiAdapter.get(i).setText(coments.get(i).getText());
        }
        return listaComenturiAdapter;
    }
    @Override
    public void onScrollStateChanged(AbsListView listView, int scrollState) {

        if (scrollState == SCROLL_STATE_IDLE) {
            if (listaComenturi != null && listView!=null) {

                if (listView.getLastVisiblePosition() == listaComenturi.size() - 1) {
      /*          Toast.makeText(getActivity(), listView.getLastVisiblePosition()+" ,"+(listaPosturi.size()-1),
                        Toast.LENGTH_SHORT).show();*/
                    task = new SendTask("paginaComentGetMoreComents");
                    task.execute();
                }


        }
        }
    }

    @Override
    public void onRefresh() {
        task = new SendTask("startPaginaComent");
        task.execute();
    }



    public void selecteazaProfil(String profil) {
        Intent intent = new Intent(this, PaginaProfil.class);
        intent.putExtra("profil", profil);
        startActivity(intent);
    }

    public void selecteazaComent(int id_coment) {
        Intent intent = new Intent(this, PaginaComent.class);
        intent.putExtra("idComent", id_coment);
        startActivity(intent);
    }

    public void stergeComent(int id_coment) {
        task = new SendTask("stergeComent", id_coment);

        task.execute();
    }

    public void addMessage(View view) {

        String mesaj;
        EditText et = (EditText) findViewById(R.id.PaginaComentAddMessager);
        mesaj = et.getText().toString();
        if (mesaj.equals("")) {
            int duration = Toast.LENGTH_LONG;
            String text = "Mesajul trebuie sa contina cel putin un caracter";
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, text, duration);

            toast.show();


        } else {
            if(mesaj.contains("'")) {
                int duration = Toast.LENGTH_SHORT;
                String text = "Caracterul ' nu este permis";
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }else {
                task = new SendTask("addMessageComent", mesaj);
                et.setText("");
                task.execute();
            }
        }
    }

    public void loadImagefromGallery(View view) {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {

            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
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
                EditText et = (EditText) findViewById(R.id.PaginaComentAddMessager);
                String caption = "";
                if (et.getText().toString().equals("")) caption = " ";
                else caption = et.getText().toString();
                if(caption.contains("'")) {
                    int duration = Toast.LENGTH_SHORT;
                    String text = "Caracterul ' nu este permis";
                    Context context = getApplicationContext();
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }else {
                    et.setText("");
                    task = new SendTask("uploadPozaComent", file, caption);
                    task.execute();
                }
                /*
                 * ImageView imgView = (ImageView) findViewById(R.id.imgView);
				 *
				 * imgView.setImageBitmap(BitmapFactory
				 * .decodeFile(imgDecodableString))
				 */
                ;

            } else {
                Toast.makeText(this, "Nu ai ales poza",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, e + "", Toast.LENGTH_LONG)
                    .show();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("suntAsyncTaskActive",suntAsyncTaskActive);
        savedInstanceState.putString("cont",cont);
        savedInstanceState.putString("profil",profil);
        savedInstanceState.putParcelableArrayList("listaComenturi", listaComenturi);
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
            task = new SendTask("startPaginaComent");
            task.execute();

        }
        //folosesc threadul sa dau refresh la pagina odata la un timp ales de mine
        //onResume este apelat si cand este creata o noua activitate si cand isi revine dupa pauza/stop si dupa
        //ce este recreata activitatea dupa ce a fost distrusa de android asa ca stiu sigur ca se
        //apeleaza mereu
       /* thread = new Thread() {
            public void run() {
                while (true) {

                    try {
                        //Aici trebuie un task ce da refresh la pagina
                        //pentru moment scoate toate elementele si le baga iar
                        //Task-ul este inaintea de sleep dintr-un anumit motiv si anume daca activitatea intra in stop sau pauza
                        //vreau sa se dea refresh cat mai repede la pagina cand revin sa nu am date neadevarate.
                        task = new SendTask("startPaginaComent");
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
        setContentView(R.layout.pagina_coment);
        asyncTaskuriActive = new ArrayList<>();
        pozeDeIncarcat=new ArrayList<>();


        Intent intent = getIntent();

        idComent = intent.getIntExtra("idComent", -1);
        //idComent = -1;
        setTitle("Comentarii");
        if (savedInstanceState == null) {
            SharedPreferences settings = getSharedPreferences("login", MODE_PRIVATE);

            cont = settings.getString("cont", "");
            profil=settings.getString("profil", "");


            //codul de mai jos ma asigura ca daca nu sa putut gasi numele contului sau al profilului in sharedperefence, pentru ca
            //un user nu poate crea nume fara nici un caracter,ca voi goli sharedprefence si ma voi intoarce pe pagina de autentificare
            //deci nu se poate intampla nimic neprevazut
            if(cont.equals("") ||profil.equals("")){
                SharedPreferences.Editor editor = settings.edit();
                if(settings.contains("cont"))editor.remove("cont");
                if(settings.contains("profil")) editor.remove("profil");
                if(settings.contains("parola")) editor.remove("parola");
                editor.commit();
                Toast.makeText(this,"A aparut o problema cu regasirea numele contului in fisiere aplicatiei",
                        Toast.LENGTH_SHORT).show();
                Intent intent2 = new Intent(this, PaginaLogin.class);
                //aceste 2 flaguri ma ajuta sa elimin tot din varful stivei astfel incat daca userul da back
                //pe pagina de login dupa ce a fost redirectionat de acest buton va iesi din aplicatie
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent2);
            }





            listaComenturi = new ArrayList<>();
            //acest task trebuia sa porneasca pagina si threadul trebuia doar sa ii dea refresh
            //dar pentru ca pe moment refreshul incarca iar pagina nu folosesc taskul de mai jos
            //ar trebuii sa il pun iar cand taskul din thread doar updateaza arraylistul ci nu il creaza iar
            task = new SendTask("startPaginaComent");
            task.execute();
        }
        if (savedInstanceState != null) {


            suntAsyncTaskActive=savedInstanceState.getBoolean("suntAsyncTaskActive");
            listaComenturi = savedInstanceState.getParcelableArrayList("listaComenturi");
            cont=savedInstanceState.getString("cont");
            profil=savedInstanceState.getString("profil");
            //daca nu este nici un post afisez un mesaj
            TextView tv1 = (TextView) findViewById(R.id.paginaComentNuSuntPosturi);
            tv1.setVisibility(View.GONE);
            if(listaComenturi.size()==0)
                tv1.setVisibility(View.VISIBLE);

        }



        listView = (ListView) findViewById(R.id.PaginaComentListView);
        adaptor = new ComentsAdapter(this, copieazaLista(listaComenturi), this,cont,profil,pozeDeIncarcat);
        listView.setAdapter(adaptor);
        //pentru swiperefreshlayout sa pun refresh listener aceasta activitate
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        //listener pentru listview sa fie aceasta activitate
        listView.setOnScrollListener(this);
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

        return super.onOptionsItemSelected(item);
    }

    private class SendTask extends AsyncTask<Void, Void, Void> {
        private File fisier1;

        private DataInputStream dis;
        private DataOutputStream dos;
        private String comanda = "";
        private int n;
        private int tempInt1;
        private ComentContainer[] vectorRezultat;
        private ArrayList<ComentContainer> listaComenturiCopie;
        private String mesaj;

        private boolean eroare = false;
        private String textEroare = "";
        private boolean comentExistent = true;
        private Context context = getApplicationContext();
        private CharSequence text;
        private int duration = Toast.LENGTH_LONG;
        private Toast toast;
        private ProgressDialog dialog;
        private int idPentruArrayListDeAyncTaskuriactive;
        SendTask(String comanda) {
            this.comanda = comanda;
        }

        SendTask(String comanda, String mesaj) {
            this.comanda = comanda;
            this.mesaj = mesaj;
        }

        SendTask(String comanda, File fisier1, String mesaj) {
            this.comanda = comanda;
            this.fisier1 = fisier1;
            this.mesaj = mesaj;
        }

        SendTask(String comanda, int tempInt1) {
            this.comanda = comanda;
            this.tempInt1 = tempInt1;
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
            if (comanda.equals("startPaginaComent") || comanda.equals("paginaComentGetMoreComents")) {
                dialog = new ProgressDialog(PaginaComent.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

                dialog.setIndeterminate(true);
                dialog.setCanceledOnTouchOutside(true);


                if (comanda.equals("startPaginaComent")) {
                    dialog.setMessage("Se incarca pagina cu comenturi...");
                }
                if (comanda.equals("paginaComentGetMoreComents")) {
                    dialog.setMessage("Se mai cauta comenturi de afisat...");
                }
                dialog.show();
            }


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
                if (comanda.equals("startPaginaComent")) {
                    dos.writeUTF("startPaginaComent");
                    dos.writeInt(idComent);
                    comentExistent = dis.readBoolean();
                    if (comentExistent == true) {
                        n = dis.readInt();
                        vectorRezultat = new ComentContainer[n];

                        for (int i = 0; i < n; i++) {
                            vectorRezultat[i] = new ComentContainer();
                            vectorRezultat[i].setIdComent(dis.readInt());
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
                        //introduc modificarile
                        listaComenturi.clear();
                        for (int i = 0; i < n; i++) {

                            if (vectorRezultat[i].getIdPoza() != -1)
                                listaComenturi.add(new ComentContainer(vectorRezultat[i].getIdComent(),
                                        vectorRezultat[i].getAutor(), vectorRezultat[i].getText(), vectorRezultat[i].getIdPozaProfil(),vectorRezultat[i].getPozaProfilWidth(),vectorRezultat[i].getPozaProfilHeight(), vectorRezultat[i].getIdPoza(),vectorRezultat[i].getPozaWidth(),vectorRezultat[i].getPozaHeight(),vectorRezultat[i].getData_postarii()));
                            else listaComenturi.add(new ComentContainer(vectorRezultat[i].getIdComent(),
                                    vectorRezultat[i].getAutor(), vectorRezultat[i].getText(),vectorRezultat[i].getIdPozaProfil(),vectorRezultat[i].getPozaProfilWidth(),vectorRezultat[i].getPozaProfilHeight(),vectorRezultat[i].getData_postarii()));
                        }
                        listaComenturiCopie=copieazaLista(listaComenturi);
                    }
                }
                if (comanda.equals("addMessageComent")) {
                    dos.writeUTF("addMessageComent");
                    dos.writeInt(idComent);
                    comentExistent = dis.readBoolean();
                    if (comentExistent == true) {
                        dos.writeUTF(cont);
                        dos.writeUTF(mesaj);
                    }
                }
                if (comanda.equals("stergeComent")) {
                    dos.writeUTF("stergeComent");



                        dos.writeInt(tempInt1);


                }
                if (comanda.equals("uploadPozaComent")) {
                    dos.writeUTF("uploadPozaComent");

                    dos.writeInt(idComent);
                    comentExistent = dis.readBoolean();
                    if (comentExistent == true) {


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
                        dos.writeUTF(cont);
                        dos.writeInt(idComent);
                        dos.writeUTF(mesaj);

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
                }
                if (comanda.equals("paginaComentGetMoreComents")) {
                    dos.writeUTF("paginaComentGetMoreComents");
                    dos.writeInt(idComent);
                    dos.writeInt(listaComenturi.get(listaComenturi.size() - 1).getIdComent());
                    comentExistent = dis.readBoolean();
                    if (comentExistent == true) {
                        n = dis.readInt();
                        vectorRezultat = new ComentContainer[n];

                        for (int i = 0; i < n; i++) {
                            vectorRezultat[i] = new ComentContainer();
                            vectorRezultat[i].setIdComent(dis.readInt());
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
                        for (int i = 0; i < n; i++) {

                            if (vectorRezultat[i].getIdPoza() != -1)
                                listaComenturi.add(new ComentContainer(vectorRezultat[i].getIdComent(),
                                        vectorRezultat[i].getAutor(), vectorRezultat[i].getText(), vectorRezultat[i].getIdPozaProfil(),vectorRezultat[i].getPozaProfilWidth(),vectorRezultat[i].getPozaProfilHeight(),  vectorRezultat[i].getIdPoza(),vectorRezultat[i].getPozaWidth(),vectorRezultat[i].getPozaHeight(),vectorRezultat[i].getData_postarii()));
                            else listaComenturi.add(new ComentContainer(vectorRezultat[i].getIdComent(),
                                    vectorRezultat[i].getAutor(), vectorRezultat[i].getText(), vectorRezultat[i].getIdPozaProfil(),vectorRezultat[i].getPozaProfilWidth(),vectorRezultat[i].getPozaProfilHeight(),vectorRezultat[i].getData_postarii()));
                        }
                        listaComenturiCopie=copieazaLista(listaComenturi);
                    }
                }
          /*      try { Thread.sleep(3000); } catch (InterruptedException e) {

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
                if (dialog != null) dialog.dismiss();
                toast.show();
                if(mSwipeRefreshLayout!=null)mSwipeRefreshLayout.setRefreshing(false);
                if (dialog != null) dialog.dismiss();
                //daca nu este nici un post afisez un mesaj
                TextView tv1 = (TextView) findViewById(R.id.paginaComentNuSuntPosturi);
                tv1.setVisibility(View.GONE);
                if(listaComenturi.size()==0)
                    tv1.setVisibility(View.VISIBLE);

                return;
            }

            // afisez butoanele daca exista comentul cu idul dat prin intent
            if (!comentExistent) {
                if (dialog != null) dialog.dismiss();
                text = "Comentul nu exista";
                toast = Toast.makeText(context, text, duration);
                toast.show();
                finish();
                return;
            }


            if (comanda.equals("startPaginaComent")) {

                adaptor.setComents(listaComenturiCopie);
                adaptor.notifyDataSetChanged();


            }
            if (comanda.equals("addMessageComent")) {

                task = new SendTask("startPaginaComent");
                task.execute();
                //ma duc la varful paginii cand dau un coment
                listView.setSelection(0);
            }
            if (comanda.equals("stergeComent")) {
                task = new SendTask("startPaginaComent");
                task.execute();
            }
            if (comanda.equals("uploadPozaComent")) {
                task = new SendTask("startPaginaComent");
                task.execute();
                //ma duc la varful paginii cand dau un coment
                listView.setSelection(0);
            }
            if (comanda.equals("paginaComentGetMoreComents")) {

                adaptor.setComents(listaComenturiCopie);
                adaptor.notifyDataSetChanged();
            }

            //daca nu este nici un post afisez un mesaj
            TextView tv1 = (TextView) findViewById(R.id.paginaComentNuSuntPosturi);
            tv1.setVisibility(View.GONE);
            if(listaComenturi.size()==0)
                tv1.setVisibility(View.VISIBLE);


            if(mSwipeRefreshLayout!=null)mSwipeRefreshLayout.setRefreshing(false);
            if (dialog != null) dialog.dismiss();
            super.onPostExecute(result);

        }
    }

}
