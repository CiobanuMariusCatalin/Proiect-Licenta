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

import com.example.proiect_licenta_client.Containere.SearchRezultContainer;
import com.example.proiect_licenta_client.Pagini.PaginaSearch;
import com.example.proiect_licenta_client.Others.IncarcaPoze;
import com.example.proiect_licenta_client.R;

import java.util.ArrayList;

public class SearchRezultsAdapter extends BaseAdapter {
    private ArrayList<SearchRezultContainer> searchRezults;
    private Context context;
    private PaginaSearch paginaSearch;
    private ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat = null;

  public  SearchRezultsAdapter(Context context, ArrayList<SearchRezultContainer> searchRezults, PaginaSearch paginaSearch,ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat ) {

        this.context = context;
        this.searchRezults = searchRezults;
        this.paginaSearch = paginaSearch;
      this.pozeDeIncarcat= pozeDeIncarcat;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return searchRezults.size();
    }

    @Override
    public SearchRezultContainer getItem(int arg0) {
        // TODO Auto-generated method stub
        return searchRezults.get(arg0);
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
            v = inflater.inflate(R.layout.search_rezult_fragment, arg2, false);
        }

        TextView tv1;
        final SearchRezultContainer src = searchRezults.get(poz);
        v.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (paginaSearch != null)
                    paginaSearch.selecteazaProfil(src.getNume());

            }

        });


        tv1 = ((TextView) v.findViewById(R.id.searchRezultFragmentNume));
        tv1.setText(src.getNume());
        ImageView imagineProfil = (ImageView) v.findViewById(R.id.searchRezultFragmentPozaProfil);
        // imagineProfil.setImageBitmap((BitmapFactory.decodeByteArray(pozaProfil, 0, pozaProfil.length)));

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int screenWidth;
        if(display.getWidth()<display.getHeight())
            screenWidth = display.getWidth();
        else
            screenWidth = display.getHeight();
      //poza de profil este in functie de latimea ecranului in mod portret am impartiti la 9 pentru ca am vazut pe telefonu meu
        //ca dimensiunea e acceptabila asa
        int new_width = screenWidth/9;

        int new_height = new_width;

        imagineProfil.getLayoutParams().height = new_height;
        imagineProfil.getLayoutParams().width = new_width;






        new IncarcaPoze.LoadImage(imagineProfil,context,"PozaProfil" + src.getIdPozaProfil(), new_width, new_height,"getPozaById",pozeDeIncarcat).execute();
        return v;
    }
}
