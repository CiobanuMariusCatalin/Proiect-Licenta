package com.example.proiect_licenta_client.Adaptere;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.TextView;


import com.example.proiect_licenta_client.Containere.IntereseContainer;

import com.example.proiect_licenta_client.Pagini.PaginaInterese;
import com.example.proiect_licenta_client.R;

import java.util.ArrayList;

public class IntereseAdapter extends BaseAdapter {
    private ArrayList<IntereseContainer> listaInterese;
    private Context context;
    private PaginaInterese paginaInterese;
    private String cont;
    private String profil;
   public IntereseAdapter(Context context,ArrayList<IntereseContainer> listaInterese,PaginaInterese paginaInterese,String cont,String profil){
        this.listaInterese=listaInterese;
        this.context=context;
        this.paginaInterese=paginaInterese;
       this.cont=cont;
       this.profil=profil;
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return listaInterese.size();
    }

    @Override
    public IntereseContainer getItem(int arg0) {
        // TODO Auto-generated method stub
        return listaInterese.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }
    @Override
    public View getView(int poz, View v, ViewGroup arg2) {
          //TREBUIE SA CREEZ MEREU NOI ELEMENTE ALE LISTVIEWEULUI ALTFEL O SA AM PROBLEME PENTRU CA
        //SE SCHIMBA ELEMENTELE DIN RATING BAR SI O SA FACA UPDATE CAND NU TRBUIE
        //if (v == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.interese_fragment, arg2, false);
       // }
        final IntereseContainer li = listaInterese.get(poz);
         final CheckBox cb1=((CheckBox) v.findViewById(R.id.IntereseFragmentCheckbox));
        final RatingBar rb1=(RatingBar) v.findViewById(R.id.IntereseFragmentCheckboxRatingBar);
        cb1.setText(li.getNume());
       if(li.getBifat()=="DA") {
           cb1.setChecked(true);
           rb1.setVisibility(View.VISIBLE);
           if(rb1.getRating()!=li.getRating())rb1.setRating(li.getRating());
       }
       if(li.getBifat()=="NU") {
           cb1.setChecked(false);
           rb1.setVisibility(View.GONE);
       }
       rb1.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                if(rating==0) ratingBar.setRating(1);
                else {
                    li.setRating((int) rating);
                    paginaInterese.schimbaRatingInteres(li.getNume(), li.getRating());
                }
            }
        });

        //daca cineva se uita pe interesele altei persoane nu le poate modifica
        if(!cont.equals(profil)){
            cb1.setEnabled(false);
            rb1.setEnabled(false);
        }else{
            cb1.setEnabled(true);
            rb1.setEnabled(true);
        }
        cb1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              final  CheckBox cb2=((CheckBox) v.findViewById(R.id.IntereseFragmentCheckbox));


                if (paginaInterese != null)
                    if(cb2.isChecked()) {
                        paginaInterese.addInteres(li.getNume());
                        //modific si containerul pentru ca in functie de acesta este creata mereu lista si cand dai scroll
                        li.setBifat("DA");
                        rb1.setRating(1);
                        rb1.setVisibility(View.VISIBLE);
                    }
                else{
                        paginaInterese.stergeInteres(li.getNume());
                        li.setBifat("NU");
                        rb1.setVisibility(View.GONE);
                    }

            }

        });
    return v;
    }
}
