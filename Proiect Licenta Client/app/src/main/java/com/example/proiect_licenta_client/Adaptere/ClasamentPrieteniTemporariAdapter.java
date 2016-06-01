package com.example.proiect_licenta_client.Adaptere;


import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.proiect_licenta_client.Containere.ClasamentPrieteniTemporariContainer;
import com.example.proiect_licenta_client.Others.IncarcaPoze;

import com.example.proiect_licenta_client.Pagini.PaginaClasamentPrieteniTemporari;
import com.example.proiect_licenta_client.R;

import java.util.ArrayList;

public class ClasamentPrieteniTemporariAdapter extends BaseAdapter {
    private ArrayList<ClasamentPrieteniTemporariContainer> prieteniTemporari;
    private Context context;
    private PaginaClasamentPrieteniTemporari paginaClasamentPrieteniTemporari;
    private ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat = null;
    public ClasamentPrieteniTemporariAdapter(Context context, ArrayList<ClasamentPrieteniTemporariContainer> prieteniTemporari, PaginaClasamentPrieteniTemporari paginaClasamentPrieteniTemporari,ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat) {

        this.context = context;
        this.prieteniTemporari = prieteniTemporari;
        this.paginaClasamentPrieteniTemporari = paginaClasamentPrieteniTemporari;
        this.pozeDeIncarcat= pozeDeIncarcat;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return prieteniTemporari.size();
    }

    @Override
    public ClasamentPrieteniTemporariContainer getItem(int arg0) {
        // TODO Auto-generated method stub
        return prieteniTemporari.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public View getView(int poz, View v, ViewGroup arg2) {
        if (v == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.clasament_prieteni_temporari_fragment, arg2, false);
        }
        TextView tv1;
        final ClasamentPrieteniTemporariContainer cptc = prieteniTemporari.get(poz);
        v.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (paginaClasamentPrieteniTemporari != null)
                    paginaClasamentPrieteniTemporari.selecteazaProfil(cptc.getNume());

            }

        });
        tv1 = ((TextView) v.findViewById(R.id.clasamentPrieteniTemporariFragmentNume));
        tv1.setText(cptc.getNume());

        tv1=((TextView) v.findViewById(R.id.clasamentPrieteniTemporariFragmentLocul));
        tv1.setText("Locul:"+(poz+1));






        ImageView imagineProfil = (ImageView) v.findViewById(R.id.clasamentPrieteniTemporariFragmentPozaProfil);

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






        new IncarcaPoze.LoadImage(imagineProfil,context,"PozaProfil" + cptc.getIdPozaProfil(), new_width, new_height,"getPozaById",pozeDeIncarcat).execute();
        TextView tv2;
        tv2 = ((TextView) v.findViewById(R.id.clasamentPrieteniTemporariFragmentPuncte));
        tv2.setText("Puncte: "+cptc.getPuncte());
        return v;
    }
}
