package com.example.proiect_licenta_client.Adaptere;


import android.content.Context;
import android.graphics.Color;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.proiect_licenta_client.Containere.ConversatiiContainer;

import com.example.proiect_licenta_client.Others.CalculeazaData;
import com.example.proiect_licenta_client.Others.IncarcaPoze;
import com.example.proiect_licenta_client.PaginaPrincipalaViewPagersFragments.PaginaPrincipalaConversatiiFragment;
import com.example.proiect_licenta_client.R;


import java.util.ArrayList;

public class ConversatiiAdapter extends BaseAdapter {
    private ArrayList<ConversatiiContainer> conversatii;
    private Context context;
    private PaginaPrincipalaConversatiiFragment paginaPrincipalaConversatii;
    private ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat = null;
    private String cont;

    public void setConversatii(ArrayList<ConversatiiContainer> conversatii){
        this.conversatii=conversatii;
    }
    public ConversatiiAdapter(Context context, ArrayList<ConversatiiContainer> conversatii, PaginaPrincipalaConversatiiFragment paginaPrincipalaConversatii,String cont, ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat) {

        this.context = context;
        this.conversatii = conversatii;
        this.paginaPrincipalaConversatii = paginaPrincipalaConversatii;
        this.pozeDeIncarcat = pozeDeIncarcat;
        this.cont=cont;
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return conversatii.size();
    }

    @Override
    public ConversatiiContainer getItem(int arg0) {
        // TODO Auto-generated method stub
        return conversatii.get(arg0);
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
            v = inflater.inflate(R.layout.conversatii_fragment, arg2, false);
        }
        final ConversatiiContainer cc=conversatii.get(poz);
        if(cc.getMesajNou().equals("DA")){
            v.setBackgroundColor(Color.rgb(238,244,255));

        }
        else{
            v.setBackgroundColor(Color.WHITE);
        }
        v.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (paginaPrincipalaConversatii!= null)
                    paginaPrincipalaConversatii.startPaginaMesaje(cc.getNume());

            }

        });

       TextView tv1 = ((TextView) v.findViewById(R.id.conversatiiFragmentNume));
        tv1.setText(cc.getNume());
        tv1 = ((TextView) v.findViewById(R.id.conversatiiFragmentTextul));

        if(cont.equals(cc.getcineATrimisUltimulMesaj()))
            tv1.setText("Tu: "+cc.getTextul());
        else  tv1.setText(cc.getTextul());




        tv1 = ((TextView) v.findViewById(R.id.conversatiiFragmentData));
        tv1.setText(CalculeazaData.getData(cc.getDataUltimuluiMesaj()));


        ImageView imagineProfil = (ImageView) v.findViewById(R.id.conversatiiFragmentPozaProfil);
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








        new IncarcaPoze.LoadImage(imagineProfil,context,"PozaProfil" + cc.getIdPozaProfil(), new_width, new_height,"getPozaById",pozeDeIncarcat).execute();


        return v;
    }
}
