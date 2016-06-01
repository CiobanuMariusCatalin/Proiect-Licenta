package com.example.proiect_licenta_client.Adaptere;

import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.proiect_licenta_client.Containere.ArhivaPrieteniTemporariContainer;
import com.example.proiect_licenta_client.Others.CalculeazaData;
import com.example.proiect_licenta_client.Pagini.PaginaArhivaPrieteniTemporari;
import com.example.proiect_licenta_client.Others.IncarcaPoze;
import com.example.proiect_licenta_client.R;

import java.util.ArrayList;

public class ArhivaPrieteniTemporariAdapter extends BaseAdapter {
    private ArrayList<ArhivaPrieteniTemporariContainer> prieteniTemporari;
    private Context context;
    private PaginaArhivaPrieteniTemporari paginaArhivaPrieteniTemporari;
    private ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat = null;
    public void setPrieteniTemporari(ArrayList<ArhivaPrieteniTemporariContainer> prieteniTemporari){
        this.prieteniTemporari=prieteniTemporari;
    }
  public  ArhivaPrieteniTemporariAdapter(Context context, ArrayList<ArhivaPrieteniTemporariContainer> prieteniTemporari, PaginaArhivaPrieteniTemporari paginaArhivaPrieteniTemporari,ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat ) {

        this.context = context;
        this.prieteniTemporari =  prieteniTemporari;
        this.paginaArhivaPrieteniTemporari = paginaArhivaPrieteniTemporari;
      this.pozeDeIncarcat= pozeDeIncarcat;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return prieteniTemporari.size();
    }

    @Override
    public ArhivaPrieteniTemporariContainer getItem(int arg0) {
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
            v = inflater.inflate(R.layout.arhiva_prieteni_temporari_fragment, arg2, false);
        }

        TextView tv1;
        final ArhivaPrieteniTemporariContainer apt = prieteniTemporari.get(poz);
        v.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (paginaArhivaPrieteniTemporari != null)
                    paginaArhivaPrieteniTemporari.selecteazaProfil(apt.getNume());

            }

        });


        tv1 = ((TextView) v.findViewById(R.id.arhivaPrieteniTemporariFragmentNume));
        tv1.setText(apt.getNume());
        ImageView imagineProfil = (ImageView) v.findViewById(R.id.arhivaPrieteniTemporariFragmentPozaProfil);
        // imagineProfil.setImageBitmap((BitmapFactory.decodeByteArray(pozaProfil, 0, pozaProfil.length)));
        tv1 = ((TextView) v.findViewById(R.id.arhivaPrieteniTemporariFragmentInterval));
        //scriu intervalul in care au fost prieteni temporari
        tv1.setText(CalculeazaData.getData(apt.getData_adaugarii())+"-"+CalculeazaData.getData(apt.getData_terminarii()));


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



      new IncarcaPoze.LoadImage(imagineProfil,context,"PozaProfil" + apt.getIdPozaProfil(), new_width, new_height,"getPozaById",pozeDeIncarcat).execute();
        return v;
    }
}
