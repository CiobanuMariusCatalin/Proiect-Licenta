package com.example.proiect_licenta_client.Adaptere;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proiect_licenta_client.Containere.MesajeContainer;
import com.example.proiect_licenta_client.Others.CalculeazaData;
import com.example.proiect_licenta_client.Pagini.PaginaMesaje;
import com.example.proiect_licenta_client.R;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MesajeAdapter extends BaseAdapter {
    private ArrayList<MesajeContainer> mesaje;
   private Context context;
   private PaginaMesaje paginaMesaje;
   private String cont;
    public void setMesaje( ArrayList<MesajeContainer> mesaje){
        this.mesaje=mesaje;
    }
   public MesajeAdapter(Context context, ArrayList<MesajeContainer> mesaje, PaginaMesaje paginaMesaje, String cont) {

        this.context = context;
        this.mesaje = mesaje;
        this.paginaMesaje = paginaMesaje;
        this.cont = cont;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mesaje.size();
    }

    @Override
    public MesajeContainer getItem(int arg0) {
        // TODO Auto-generated method stub
        return mesaje.get(arg0);
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
            v = inflater.inflate(R.layout.mesaje_fragment, arg2, false);
        }
        TextView tv1;
        TextView tv2;

        final MesajeContainer mc = mesaje.get(poz);

        tv2 = ((TextView) v.findViewById(R.id.mesajeFragmentMesaj));
        tv2.setText(mc.getText());





/*data postarii*/
        tv1 =((TextView) v.findViewById(R.id.mesajeFragmentData));
      ///  tv1.setText(mc.getData_postarii());
        tv1.setText(CalculeazaData.getData(mc.getData_postarii()));


        LinearLayout ll = (LinearLayout) v.findViewById(R.id.mesajFragmentContainer);
        LinearLayout ll2 = (LinearLayout) v.findViewById(R.id.mesajFragmentContainer2);
        if (cont.equals(mc.getNume())) {
            ll.setGravity(Gravity.RIGHT);
            ll2.setBackgroundResource(R.drawable.bubble2_green);
           /*rlp1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            rlp2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            rlp2.addRule(RelativeLayout.RIGHT_OF,R.id.mesajeFragmentMesaj);*/

        } else {
            ll.setGravity(Gravity.LEFT);
            ll2.setBackgroundResource(R.drawable.bubble2_yellow);


 /*         rlp1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            rlp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            rlp2.addRule(RelativeLayout.LEFT_OF,R.id.mesajeFragmentMesaj);*/
        }


        return v;
    }
}


