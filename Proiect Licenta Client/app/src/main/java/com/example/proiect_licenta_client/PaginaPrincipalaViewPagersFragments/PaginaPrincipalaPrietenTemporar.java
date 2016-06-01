package com.example.proiect_licenta_client.PaginaPrincipalaViewPagersFragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proiect_licenta_client.Others.ConexiuneLaInternet;
import com.example.proiect_licenta_client.Others.Constante;
import com.example.proiect_licenta_client.Others.IncarcaPoze;
import com.example.proiect_licenta_client.Pagini.PaginaArhivaPrieteniTemporari;
import com.example.proiect_licenta_client.Pagini.PaginaClasamentPrieteniTemporari;
import com.example.proiect_licenta_client.Pagini.PaginaLogin;
import com.example.proiect_licenta_client.Pagini.PaginaMesaje;
import com.example.proiect_licenta_client.Pagini.PaginaProfil;
import com.example.proiect_licenta_client.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

public class PaginaPrincipalaPrietenTemporar extends Fragment {
  private  SendTask task;
    private   String cont;
    private   View rootView;
    private  String prietenTemporar;
    private Context context;
    private Thread thread;
    private ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat = null;
    private ArrayList<SendTask> asyncTaskuriActive = null;
    private int idLastSendTask = 0;
    private boolean suntAsyncTaskActive=false;




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
                        task = new SendTask("startPaginaPrietenTemporar");
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
    private  class SendTask extends AsyncTask<Void, Void, Void> {
        private     DataInputStream dis;
        private     DataOutputStream dos;
        private     String comanda = "";
        private    boolean eroare = false;
        private   String textEroare = "";
        private   Context context = getActivity();
        private    CharSequence text;
        private    int duration = Toast.LENGTH_LONG;
        private    Toast toast;
        private   boolean prietenTemporarGasit;
        private    ArrayList<String> intereseInComun;
        private    int nrIntereseInComun;
        private int idPozaProfil=-1;
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

            if (ConexiuneLaInternet.conexiuneLaInternet(getActivity())== false) {
                eroare = true;
                textEroare = "Nu exista conexiune la internet";
                return null;
            }
            try {
                cs = new Socket(adresa, port);
                dos = new DataOutputStream(cs.getOutputStream());
                dis = new DataInputStream(cs.getInputStream());
                if(comanda.equals("removePrietenTemporar")){
                    dos.writeUTF("removePrietenTemporar");
                    dos.writeUTF(cont);
                }
                if (comanda.equals("startPaginaPrietenTemporar")) {
                    dos.writeUTF("startPaginaPrietenTemporar");
                    dos.writeUTF(cont);
                    prietenTemporarGasit = dis.readBoolean();
                    if (prietenTemporarGasit == true) {
                        nrIntereseInComun=dis.readInt();
                        intereseInComun=new ArrayList<>();
                        if(nrIntereseInComun>0){
                            for(int i=0;i<nrIntereseInComun;i++)
                            intereseInComun.add(dis.readUTF());
                        }
                        prietenTemporar = dis.readUTF();
                        idPozaProfil=dis.readInt();
                    }
                }
/*              try { Thread.sleep(4000); } catch (InterruptedException e) {

                    e.printStackTrace();
                }*/
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
            if(comanda.equals("removePrietenTemporar")){
                task = new SendTask("startPaginaPrietenTemporar");
                task.execute();
            }
            if (comanda.equals("startPaginaPrietenTemporar")) {
                LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.paginaPricipalaPrietenTemporarContainer);
                ll.setVisibility(View.GONE);
                TextView tv1 = (TextView) rootView.findViewById(R.id.paginaPricipalaPrietenTemporarMesajInceput);
                tv1.setVisibility(View.GONE);
                if (prietenTemporarGasit == true) {
                    tv1.setText("Prietenul tau temporar de astazi este:");
                    tv1.setVisibility(View.VISIBLE);

                    //afisez interesele in comun
                    TextView tv3 = (TextView) rootView.findViewById(R.id. paginaPricipalaPrietenTemporarInteresInComun);
                    if(nrIntereseInComun>0){
                        String listaIntereseComune="Aveti urmatoarele interese in comun:";
                        for(int i=0;i<intereseInComun.size();i++){
                          if(i==intereseInComun.size()-1)  listaIntereseComune+=intereseInComun.get(i)+".";
                            else listaIntereseComune+=intereseInComun.get(i)+", ";
                        }
                        tv3.setText(listaIntereseComune);
                    }else{
                        tv3.setText("Nu aveti nici un interes in comun");
                    }



                    TextView tv2 = (TextView) rootView.findViewById(R.id.paginaPricipalaPrietenTemporarNume);
                    tv2.setText(prietenTemporar);



                    ImageView imagineProfil = (ImageView) rootView.findViewById(R.id.paginaPricipalaPrietenTemporarPozaProfil);
                    // imagineProfil.setImageBitmap((BitmapFactory.decodeByteArray(pozaProfil, 0, pozaProfil.length)));
                  //  imagineProfil.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(pozProfilprietenTemporar, 0, pozProfilprietenTemporar.length), 70, 70, false));

                    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                    Display display = wm.getDefaultDisplay();

                    int screenWidth;
                    if(display.getWidth()<display.getHeight())
                        screenWidth = display.getWidth();
                    else
                        screenWidth = display.getHeight();
                    int new_width = screenWidth/9;
                    int new_height = new_width;



                    imagineProfil.getLayoutParams().height = new_height;
                    imagineProfil.getLayoutParams().width = new_width;

                    new IncarcaPoze.LoadImage(imagineProfil,context,"PozaProfil" + idPozaProfil, new_width, new_height,"getPozaById",pozeDeIncarcat).execute();
                    imagineProfil.setVisibility(View.VISIBLE);
                    ll.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                            selecteazaProfil(prietenTemporar);
                        }

                    });
                    ll.setVisibility(View.VISIBLE);
                } else {
                    tv1.setText("Nu am putut gasi un prieten temporar");
                    tv1.setVisibility(View.VISIBLE);
                }

            }

            super.onPostExecute(result);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("suntAsyncTaskActive", suntAsyncTaskActive);

        savedInstanceState.putString("cont", cont);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(
                R.layout.pagina_principala_prieten_temporar_fragment, container, false);

        asyncTaskuriActive=new ArrayList<>();
        pozeDeIncarcat=new ArrayList<>();
        if (savedInstanceState == null) {
            suntAsyncTaskActive=false;
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
            suntAsyncTaskActive=savedInstanceState.getBoolean("suntAsyncTaskActive");
            cont = savedInstanceState.getString("cont");
        }

        Button bt = (Button) rootView.findViewById(R.id.paginaPricipalaPrietenTemporarArhiva);
        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), PaginaArhivaPrieteniTemporari.class);
                startActivity(intent);
            }


        });
        Button bt2 = (Button) rootView.findViewById(R.id.paginaPricipalaPrietenTemporarMesaje);
        bt2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PaginaMesaje.class);
                intent.putExtra("cont", cont);
                intent.putExtra("partenerConversatie", prietenTemporar);
                startActivity(intent);
            }


        });
        Button bt3 = (Button) rootView.findViewById(R.id.paginaPricipalaPrietenTemporarRemovePrieten);
        bt3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                task = new SendTask("removePrietenTemporar");
                task.execute();
            }


        });
        Button bt4 = (Button) rootView.findViewById(R.id.paginaPricipalaPrietenTemporarClasament);
        bt4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PaginaClasamentPrieteniTemporari.class);
                startActivity(intent);
            }


        });


        return rootView;


    }
}
