package com.example.proiect_licenta_client.Adaptere;


import android.content.Context;
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

import com.example.proiect_licenta_client.Containere.FriendRecommendationContainer;
import com.example.proiect_licenta_client.Others.IncarcaPoze;
import com.example.proiect_licenta_client.PaginaPrincipalaViewPagersFragments.PaginaPrincipalaFriendRecommendationFragment;
import com.example.proiect_licenta_client.R;

import java.util.ArrayList;

public class FriendRecommandationAdapter extends BaseAdapter {
   private ArrayList<FriendRecommendationContainer> friendRecommendation;
   private Context context;
   private PaginaPrincipalaFriendRecommendationFragment paginaFriendRecommendation;
    private ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat = null;
    public void setFriendRecommendation( ArrayList<FriendRecommendationContainer> friendRecommendation){
        this.friendRecommendation=friendRecommendation;
    }
   public FriendRecommandationAdapter(Context context, ArrayList<FriendRecommendationContainer> friendRecommendation, PaginaPrincipalaFriendRecommendationFragment paginaFriendRecommendation,ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat) {

        this.context = context;
        this.friendRecommendation = friendRecommendation;
        this.paginaFriendRecommendation = paginaFriendRecommendation;
       this.pozeDeIncarcat= pozeDeIncarcat;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return friendRecommendation.size();
    }

    @Override
    public FriendRecommendationContainer getItem(int arg0) {
        // TODO Auto-generated method stub
        return friendRecommendation.get(arg0);
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
            v = inflater.inflate(R.layout.friend_reccomandation_fragment, arg2, false);
        }
        TextView tv1;
        final FriendRecommendationContainer frc = friendRecommendation.get(poz);
        v.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (paginaFriendRecommendation != null)
                    paginaFriendRecommendation.selecteazaProfil(frc.getNume());

            }

        });
        tv1 = ((TextView) v.findViewById(R.id.friendRecommandationFragmentAutor));
        tv1.setText(frc.getNume());

        //setez numarul de prieteni in comun
        tv1 = ((TextView) v.findViewById(R.id. friendRecommandationFragmentNrPrieteniInComun));
        tv1.setText("Aveti "+frc.getPrieteniInComun()+" prieteni in comun");

        ImageView imagineProfil = (ImageView) v.findViewById(R.id.friendRecommandationFragmentPozaProfil);

        ImageButton ib = (ImageButton) v.findViewById(R.id. friendRecommandationFragmentRemove);
        ib.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (paginaFriendRecommendation != null)
                    paginaFriendRecommendation.dismissRecommendation(frc.getNume());

            }
        });

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





       new IncarcaPoze.LoadImage(imagineProfil,context,"PozaProfil" + frc.getIdPozaProfil(), new_width, new_height,"getPozaById",pozeDeIncarcat).execute();

        Button buttonAccept = (Button) v.findViewById(R.id.friendRecommandationFragmentAddFriend);

        buttonAccept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (paginaFriendRecommendation != null)
                    paginaFriendRecommendation.addFriend(frc.getNume());
            }

        });


        return v;
    }
}


