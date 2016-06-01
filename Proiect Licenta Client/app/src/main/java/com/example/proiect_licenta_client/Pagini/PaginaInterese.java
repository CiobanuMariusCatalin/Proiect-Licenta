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
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.proiect_licenta_client.Adaptere.IntereseAdapter;
import com.example.proiect_licenta_client.Adaptere.SearchRezultsAdapter;
import com.example.proiect_licenta_client.Containere.IntereseContainer;

import com.example.proiect_licenta_client.Others.ConexiuneLaInternet;
import com.example.proiect_licenta_client.Others.Constante;
import com.example.proiect_licenta_client.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

public class PaginaInterese extends Activity {
    private String profil;
    private String cont;
    private SendTask task;
    private ArrayList<SendTask> asyncTaskuriActive = null;
    private int idLastSendTask = 0;
    private boolean suntAsyncTaskActive=false;
    private ArrayList<IntereseContainer> listaInterese;
    private IntereseAdapter adaptor;

/*    String[] listaCheckBox = {"Filme","Filme actiune","Filme horror","Filme comedie","Filme SF","Filme aventura","Filme documentar",
            "Filme drama","Filme thriller","Seriale","Talk Show-uri","Carti","Jocuri","Jocuri Pc","Jocuri PS4","Jocuri Xbox","Jocuri Wii",
            "Muzica","Masini","Gatit","Telefoane","Celebritati","Calatoritul","Carti","Tehnologia",
            "Stiri","Plimbatul","Motociclete","Progamatul","Voluntariat","Muzica clasica","Muzica rock","Muzica heavy metal",
            "Muzica pop","Muzica hip hop","Muzica dubstep","Caini","Pisici","Animale exotice","Animale marine",
    };*/
    String[]  listaCheckBox= {"Aerobicul","Animale de companie","Animale exotice","Animale marine","Baschetul","Blogging-ul","Caini","Calatoritul",
        "Cantatul","Celebritati","Cititul","Colectionarea de obiecte","Comedie stand-up","Constructii","Cusutul","Dansul","Desenatul","Filmele",
        "Filmele SF","Filmele de actiune","Filmele de aventura","Filmele de comedie","Filmele documentar","Filmele drama","Filmele horror","Filmele thriller",
        "Fitness-ul","Fotbalul","Fotografia","Gatitul","Gradinaritul","Ingrijirea copiilor","Jocuri","Jocuri PS4","Jocuri Pc","Jocuri Wii",
        "Jocuri Xbox","Jocuri de noroc","Jogging-ul","Jonglarea","Limbi straine","Magia","Masini","Mersul pe bicicleta","Motociclete","Muzee",
        "Muzica","Muzica clasica","Muzica dubstep","Muzica heavy metal","Muzica hip hop","Muzica pop","Muzica rock","Patinatul","Pictatul",
        "Pisici","Plimbatul","Progamatul","Puzzle-uri","Seriale","Sportul","Sporturi extreme","Stiri","Talk Show-uri","Tehnologia","Telefoane",
        "Televizorul","Tenis de camp","Tenis de masa","Voluntariat","Yoga"};
    public void addInteres(String interes){
        task = new SendTask("addInteres",interes);
        task.execute();
    }
    public void stergeInteres(String interes){
        task = new SendTask("stergeInteres",interes);
        task.execute();
    }
    public void schimbaRatingInteres(String interes,int rating){
        task = new SendTask("schimbaRatingInteres",interes,rating);
        task.execute();
    }
    @Override
    protected void onPause() {
        super.onPause();
        /*Toast.makeText(getApplicationContext(),asyncTaskuriActive.size()+"",
                        Toast.LENGTH_SHORT).show();*/
        //daca sunt asynctaskuri active pun valoarea booleana ca true sa stiu la onresume sa reincarc pagina.
        //si dau cancel la asynctaskuri
        if(asyncTaskuriActive.size()>0){
            //      Toast.makeText(getApplicationContext(),asyncTaskuriActive.get(0).comanda,
            //          Toast.LENGTH_SHORT).show();
            suntAsyncTaskActive=true;
        }else{
            suntAsyncTaskActive=false;
        }
        for (int i = 0; i < asyncTaskuriActive.size(); i++) {
            asyncTaskuriActive.get(i).cancel(true);
            asyncTaskuriActive.remove(i);
        }

    }
    @Override
    public void onResume() {
        super.onResume();
        //daca au ramas asynctaskuri active cand am intrat in pauza atunci reincarc pagina  sa fiu
        //sigur ca am ultimele modificari
        if (suntAsyncTaskActive == true) {
            task = new SendTask("listaInterese");
            task.execute();

        }
    }



    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("suntAsyncTaskActive",suntAsyncTaskActive);
        savedInstanceState.putString("profil", profil);
        savedInstanceState.putString("cont", cont);
        savedInstanceState.putParcelableArrayList("listaInterese",listaInterese);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.pagina_profil_interese);
        //creez un nou arraylist de taskuri active
        asyncTaskuriActive = new ArrayList<>();
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            SharedPreferences settings = getSharedPreferences("login", MODE_PRIVATE);
            cont = settings.getString("cont", "");

            profil = intent.getStringExtra("profil");




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









            //instantiez lista cu interese cu numele din vectorul de stringuri si pun ca nu sunt bifate
            //bifez doar dupa ce comunic cu serverul
            listaInterese=new ArrayList<>();
            for(int i=0;i<listaCheckBox.length;i++){
                listaInterese.add(new IntereseContainer(listaCheckBox[i]));
            }

            task = new SendTask("listaInterese");
            task.execute();
        } else {
            listaInterese = savedInstanceState.getParcelableArrayList("listaInterese");
            suntAsyncTaskActive=savedInstanceState.getBoolean("suntAsyncTaskActive");
            profil = savedInstanceState.getString("profil");
            cont = savedInstanceState.getString("cont");
        }




        setTitle("Interesele lui:" + profil);



        ListView listV = (ListView) findViewById(R.id.PaginaIntereseListView);
        adaptor = new IntereseAdapter(this,listaInterese, this,cont,profil);
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
        private IntereseContainer[] vectorRezultat;
        private int n;
        private String mesaj;

        private boolean eroare = false;
        private String textEroare = "";
        private Context context = getApplicationContext();
        private CharSequence text;
        private int duration = Toast.LENGTH_LONG;
        private Toast toast;
        private int idPentruArrayListDeAyncTaskuriactive;
        private int rating;
        SendTask(String comanda) {
            this.comanda = comanda;
        }

        SendTask(String comanda, String mesaj) {
            this.comanda = comanda;
            this.mesaj = mesaj;
        }
        SendTask(String comanda, String mesaj,int rating) {
            this.comanda = comanda;
            this.mesaj = mesaj;
            this.rating=rating;
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
                if (comanda.equals("listaInterese")) {
                    dos.writeUTF("listaInterese");
                    dos.writeUTF(profil);
                    n = dis.readInt();
                    vectorRezultat = new IntereseContainer[n];
                    for (int i = 0; i < n; i++) {
                        vectorRezultat[i] = new IntereseContainer();

                        vectorRezultat[i].setNume(dis.readUTF());
                        vectorRezultat[i].setRating(dis.readInt());
                    }
                }
                if (comanda.equals("addInteres")) {
                    dos.writeUTF("addInteres");
                    dos.writeUTF(profil);
                    dos.writeUTF(mesaj);
                }
                if (comanda.equals("stergeInteres")) {
                    dos.writeUTF("stergeInteres");
                    dos.writeUTF(profil);
                    dos.writeUTF(mesaj);
                }
                if(comanda.equals("schimbaRatingInteres")){
                    dos.writeUTF("schimbaRatingInteres");
                    dos.writeUTF(profil);
                    dos.writeUTF(mesaj);
                    dos.writeInt(rating);
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
            /*try { Thread.sleep(7000); } catch (InterruptedException e) {

                e.printStackTrace(); }*/
            if (eroare == true) {
                text = textEroare;
                toast = Toast.makeText(context, text, duration);

                toast.show();
                return;
            }
            if (comanda.equals("listaInterese")) {

                if (n == 0) {
                    text = "Nu a fost selectat nici un interes";
                    toast = Toast.makeText(context, text, duration);
                    toast.show();
                } else {
                    for (int i = 0; i < vectorRezultat.length; i++) {
                    for(int j=0;j<listaInterese.size();j++)
                       if(listaInterese.get(j).getNume().equals(vectorRezultat[i].getNume())) {
                           listaInterese.get(j).setBifat("DA");
                           //setez ratingul
                            listaInterese.get(j).setRating(vectorRezultat[i].getRating());
                       }
                    }
                adaptor.notifyDataSetChanged();
                }

            }
            if (comanda.equals("addInteres")) {
               // task = new SendTask("listaInterese");
                //task.execute();
            }
            if (comanda.equals("stergeInteres")) {

            }
            if(comanda.equals("schimbaRatingInteres")){

            }
            super.onPostExecute(result);
        }
    }

}
