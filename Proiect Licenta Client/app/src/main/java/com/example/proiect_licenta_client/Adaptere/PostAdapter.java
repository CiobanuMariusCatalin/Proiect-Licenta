package com.example.proiect_licenta_client.Adaptere;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.proiect_licenta_client.Containere.PostContainer;
import com.example.proiect_licenta_client.Others.CalculeazaData;
import com.example.proiect_licenta_client.Others.IncarcaPoze;
import com.example.proiect_licenta_client.PaginaPrincipalaViewPagersFragments.PaginaPrincipalaNewsFragment;
import com.example.proiect_licenta_client.Pagini.PaginaProfil;

import com.example.proiect_licenta_client.R;

import java.util.ArrayList;

public class PostAdapter extends BaseAdapter {
    private ArrayList<PostContainer> posturi = new ArrayList<>();
    private Context context;
    private PaginaPrincipalaNewsFragment news;
    private PaginaProfil paginaProfil;
    private int punctePrietenTemporar=-1;
    private int locInClasament=-1;
    private String cont;
    private String profil;
    private int idPozaProfil=-1;
    private ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat = null;

    public void setPosturi(ArrayList<PostContainer> posturi ){
        this.posturi=posturi;
    }
    public void setLocInClasament(int locInClasament){
        this.locInClasament=locInClasament;
    }
    public void setPunctePrietenTemporar(int punctePrietenTemporar){
        this.punctePrietenTemporar=punctePrietenTemporar;
    }
    public void setIdPozaProfil(int idPozaProfil){
        this.idPozaProfil=idPozaProfil;
    }
    public PostAdapter(Context context, ArrayList<PostContainer> posturi, PaginaPrincipalaNewsFragment news,String cont,String profil,ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat ) {
        this.context = context;
        this.posturi = posturi;
        this.news = news;
        this.cont=cont;
        this.profil=profil;
        this.pozeDeIncarcat= pozeDeIncarcat;
    }

    public PostAdapter(Context context, ArrayList<PostContainer> posturi, PaginaProfil paginaProfil,String cont,String profil,ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat) {
        this.context = context;
        this.posturi = posturi;
        this.paginaProfil = paginaProfil;
        this.cont=cont;
        this.profil=profil;
        this.pozeDeIncarcat= pozeDeIncarcat;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        //daca sunt pe pagina de profil inseamna ca am un element in plus anume fragmentul cu butoane.
        if (paginaProfil != null )
            return posturi.size()+1;
        else  return posturi.size();
    }

    @Override
    public PostContainer getItem(int arg0) {
        // TODO Auto-generated method stub
        return posturi.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public View getView(int poz, View v, ViewGroup arg2) {

        if (paginaProfil != null && poz == 0) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.pagina_profil_head_fragment, arg2, false);


            if (!profil.equals(cont)) {
                paginaProfil.aflaStatusPrietenie();

            } else {
                Button bt = (Button) v.findViewById(R.id.PaginaProfilShimbaPozaProfil);
                bt.setVisibility(View.VISIBLE);
                //NU IL LAS SA ISI TRIMITA SINGUR MESAJE
                Button bt2 = (Button) v.findViewById(R.id.PaginaProfilPaginaMesaje);
                bt2.setVisibility(View.GONE);
            }
            //prima data se apeleaza fara sa aibe datele deci am date incorecte asa ca apelez cand am datele corect
            //pentru ca e imposibil sa fie -1
            if(punctePrietenTemporar!=-1) {
                TextView tv1 = (TextView) v.findViewById(R.id.paginaProfilPuncte);
                tv1.setText("Puncte: " + punctePrietenTemporar);
            }
            if(locInClasament!=-1) {
                TextView tv1 = (TextView) v.findViewById(R.id.paginaProfilLocul);
                tv1.setText("Locul: " + locInClasament);
            }



            //prima data se apeleaza fara sa aibe datele deci am date incorecte asa ca apelez cand am datele corect
            //pentru ca e imposibil sa fie -1
            if(idPozaProfil!=-1) {
                ImageView imagineProfil = (ImageView) v.findViewById(R.id.PaginaProfilPozaDeProfil);

                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();

                int screenWidth;
                if (display.getWidth() < display.getHeight())
                    screenWidth = display.getWidth();
                else
                    screenWidth = display.getHeight();

                int new_width = screenWidth / 4;
                int new_height = new_width;


                imagineProfil.getLayoutParams().height = new_height;
                imagineProfil.getLayoutParams().width = new_width;

                IncarcaPoze.LoadImage loadImage = new IncarcaPoze.LoadImage(imagineProfil, context, "PaginaProfil" + idPozaProfil, new_width, new_height, "getPozaById", pozeDeIncarcat);
                // new IncarcaPoze.LoadImage(imagineProfil, pc.getPozaProfil(), "PozaProfil" + pc.getIdPozaProfil(), new_width, new_height, context, v).execute();
                loadImage.execute();

                imagineProfil.setVisibility(View.VISIBLE);
                //trebuie sa fie final ca sa il pot apcesa din onClicListener



                TextView tv1;
                tv1 = (TextView) v.findViewById(R.id.paginaProfilNumeleOwnerului);
                tv1.setText(profil);
            }
            return v;
        }
        //if (v == null) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.post_fragment, arg2, false);

        //}


 /*           if(paginaProfil!=null) {
                if (poz == 0) {
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = inflater.inflate(R.layout.pagina_profil_header_fragment, arg2, false);
                } else {
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = inflater.inflate(R.layout.post_fragment, arg2, false);
                }
            }
            else{
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.post_fragment, arg2, false);
            }*/



        /*if (paginaProfil!=null && poz == 0) return v;*/

        TextView tv1;
        TextView tv2;
        TextView tv3;

        //daca sunt pe pagina de profil pe prima pozitie pun fragmentul cu butoane deci
        //pozitia in listview se duce in fata cu 1 , astfel trebuie sa mergem in spate cu 1 fata de pozitia
        //din lsitview
        int i = 0;
        if (paginaProfil != null) {
            i = 1;
        }

        //trebuie sa fie final ca sa il pot apcesa din onClicListener
        final PostContainer pc = posturi.get(poz - i);

        v.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (paginaProfil != null) paginaProfil.selecteazaComent(pc.getIdPost());
                //trimit si profilul de pe care se afla se il pot baga in sharedpreferences
                if (news != null) news.selecteazaComent(pc.getIdPost(),pc.getNewsFragmentProfil());

            }

        });


        tv1 = ((TextView) v.findViewById(R.id.postFragmentAutor));
        tv1.setText(pc.getAutor());
        tv1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (paginaProfil != null) paginaProfil.selecteazaProfil(pc.getAutor());
                if (news != null) news.selecteazaProfil(pc.getAutor());
            }
        });
        tv2 = ((TextView) v.findViewById(R.id.postFragmentMesaj));
        tv2.setText(pc.getText());

        //o folosesc cand sunt pe news feed sa am un delimitator dintre autorul postului si ownerul profilului comentului
        ImageView sageata = (ImageView) v.findViewById(R.id.postFragmentSageata);
        sageata.setVisibility(View.GONE);

        tv3 = ((TextView) v.findViewById(R.id.postFragmentProfil));
        tv3.setVisibility(View.GONE);
        if (pc.getNewsFragmentProfil() != null) {
            tv3.setText(pc.getNewsFragmentProfil());
            tv3.setVisibility(View.VISIBLE);
            sageata.setVisibility(View.VISIBLE);
            tv3.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (news != null) news.selecteazaProfil(pc.getNewsFragmentProfil());
                }
            });
        }

        ImageButton ib = (ImageButton) v
                .findViewById(R.id.postFragmentRemoveComment);
        //pentru ca viewe-urile sunt recilate posibil sa ramana visible de la un view anterior
        ib.setVisibility(View.GONE);
        /*Daca sunt pe news fragment inseamna ca pc.newsFragmentProfil!=null si ca userul curent sa fie ownerul profilului
        pe care se regaseste comentul curent si acel profil il gasim in pc.newsFragmentProfil daca este null profilul il regasim in
        variabila profil luata din SharedPreferences

         */
        if ((pc.getNewsFragmentProfil() == null && (cont.equals(profil) || cont.equals(pc.getAutor()))) ||
                (pc.getNewsFragmentProfil() != null &&(cont.equals(pc.getNewsFragmentProfil()) || cont.equals(pc.getAutor())))) {

            ib.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (paginaProfil != null) paginaProfil.stergeComent(pc.getIdPost());
                    if (news != null) news.stergeComent(pc.getIdPost());
                }

            });
            ib.setVisibility(View.VISIBLE);

        }
        ImageView imgView = (ImageView) v.findViewById(R.id.postFragmentImg);
        imgView.setVisibility(View.GONE);
        if (pc.getIdPoza() != -1) {

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            int bitmapWidth = pc.getPozaWidth();
            int bitmapHeight = pc.getPozaHeight();
            int screenWidth;

            screenWidth = display.getWidth();





            int new_width = screenWidth;
            int new_height = (int) Math.floor((double) bitmapHeight * ((double) new_width / (double) bitmapWidth));

            imgView.getLayoutParams().height = new_height;
            imgView.getLayoutParams().width = new_width;

            IncarcaPoze.LoadImage loadImage= new IncarcaPoze.LoadImage(imgView,context, pc.getIdPoza() + "", new_width, new_height,"getPozaById",pozeDeIncarcat);
            loadImage.execute();



            imgView.setVisibility(View.VISIBLE);





        }



        ImageView imagineProfil = (ImageView) v.findViewById(R.id.postFragmentPozaProfil);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int screenWidth;
       if(display.getWidth()<display.getHeight())
            screenWidth = display.getWidth();
        else
            screenWidth = display.getHeight();




        int new_width = screenWidth/8;
        int new_height = new_width;


        imagineProfil.getLayoutParams().height = new_height;
        imagineProfil.getLayoutParams().width = new_width;



        IncarcaPoze.LoadImage loadImage= new IncarcaPoze.LoadImage(imagineProfil,context,"PozaProfil" + pc.getIdPozaProfil(), new_width, new_height,"getPozaById",pozeDeIncarcat);
       // new IncarcaPoze.LoadImage(imagineProfil, pc.getPozaProfil(), "PozaProfil" + pc.getIdPozaProfil(), new_width, new_height, context, v).execute();
        loadImage.execute();

        imagineProfil.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (paginaProfil != null) paginaProfil.selecteazaProfil(pc.getAutor());
                if (news != null) news.selecteazaProfil(pc.getAutor());
            }
        });

        tv1=(TextView)v.findViewById(R.id.postFragmentDataPost);
        tv1.setText(CalculeazaData.getData(pc.getData_postarii()));

        return v;
    }


}
