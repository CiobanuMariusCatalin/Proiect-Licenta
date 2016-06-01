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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.proiect_licenta_client.Containere.ComentContainer;
import com.example.proiect_licenta_client.Others.CalculeazaData;
import com.example.proiect_licenta_client.Pagini.PaginaComent;
import com.example.proiect_licenta_client.Others.IncarcaPoze;
import com.example.proiect_licenta_client.R;

import java.util.ArrayList;

public class ComentsAdapter extends BaseAdapter {
    private ArrayList<ComentContainer> coments;
    private Context context;
    private PaginaComent paginaComent;
    private  String cont;
    private  String profil;
    private ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat = null;
    public ComentsAdapter(Context context, ArrayList<ComentContainer> coments, PaginaComent paginaComent,String cont,String profil,ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat ) {
        this.context = context;
        this.coments = coments;
        this.paginaComent = paginaComent;
        this.cont=cont;
        this.profil=profil;
        this.pozeDeIncarcat= pozeDeIncarcat;
    }
public void setComents(ArrayList<ComentContainer> coments){
    this.coments=coments;
}
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return coments.size();
    }

    @Override
    public ComentContainer getItem(int arg0) {
        // TODO Auto-generated method stub
        return coments.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public View getView(int poz, View v, ViewGroup arg2) {


//NU RECILEZ PENTRU CA AM POZE SI ARATA CIUDAT CAND APAR SI DISPAR POZE CE NU AR TREBUII SA FIE
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.comment_fragment, arg2, false);



        TextView tv1;
        TextView tv2;
        //trebuie sa fie final ca sa il pot accesa din onClicListener
        final ComentContainer cc = coments.get(poz);

        v.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (paginaComent != null) paginaComent.selecteazaComent(cc.getIdComent());
            }

        });


        tv1 = ((TextView) v.findViewById(R.id.commentFragmentAutor));
        tv1.setText(cc.getAutor());
        tv1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (paginaComent != null) paginaComent.selecteazaProfil(cc.getAutor());
            }
        });
        tv2 = ((TextView) v.findViewById(R.id.commentFragmentMesaj));
        tv2.setText(cc.getText());



        ImageButton ib = (ImageButton) v
                .findViewById(R.id.commentFragmentRemoveComment);
        //pentru ca viewe-urile sunt recilate posibil sa ramana visible de la un view anterior
        ib.setVisibility(View.GONE);
        if (cont.equals(profil) || cont.equals(cc.getAutor())) {

            ib.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    if (paginaComent != null) paginaComent.stergeComent(cc.getIdComent());
                }

            });
            ib.setVisibility(View.VISIBLE);

        }
        ImageView imgView = (ImageView) v.findViewById(R.id.commentFragmentImg);
        imgView.setVisibility(View.GONE);
        if (cc.getIdPoza() != -1) {


            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            int bitmapWidth = cc.getPozaWidth();
            int bitmapHeight = cc.getPozaHeight();
            int screenWidth;

            screenWidth = display.getWidth();




            //vreau sa incarc imaginea in memorie cu widthul si heightul deviceului in pozitie portret pentru
            //a ocupa mai putin spatiu cand va fii pe landscape imaginiile vor ocupa tot widthul deviceului

            //daca heightul este mai mare ca width inseamna ca suntem in orientare portret
            //altfel inseamna ca suntem landscape

            int new_width= screenWidth-100;
            int new_height = (int) Math.floor((double) bitmapHeight *( (double) new_width / (double) bitmapWidth));

            imgView .getLayoutParams().height=new_height;
            imgView .getLayoutParams().width=new_width;


            new IncarcaPoze.LoadImage(imgView,context,cc.getIdPoza() + "", new_width, new_height,"getPozaById",pozeDeIncarcat).execute();
            imgView.setVisibility(View.VISIBLE);



          /*  imgView.setVisibility(View.VISIBLE);
            //imgView.setImageBitmap(BitmapFactory.decodeByteArray(poza, 0, poza.length));
            imgView.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(cc.poza, 0, cc.poza.length), 400, 200, false));*/
        }
        ImageView imagineProfil = (ImageView) v.findViewById(R.id.commentFragmentPozaProfil);
        // imagineProfil.setImageBitmap((BitmapFactory.decodeByteArray(pozaProfil, 0, pozaProfil.length)));



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





        imagineProfil.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (paginaComent != null) paginaComent.selecteazaProfil(cc.getAutor());
            }
        });


        new IncarcaPoze.LoadImage(imagineProfil,context,"PozaProfil" +cc.getIdPozaProfil(), new_width, new_height,"getPozaById",pozeDeIncarcat).execute();

        //timestampul
        tv1=(TextView)v.findViewById(R.id.commentFragmentDataPost);
        tv1.setText(CalculeazaData.getData(cc.getData_postarii()));
        return v;
    }

}
