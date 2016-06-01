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

import com.example.proiect_licenta_client.Containere.FriendListContainer;
import com.example.proiect_licenta_client.Pagini.PaginaProfilFriendList;
import com.example.proiect_licenta_client.Others.IncarcaPoze;
import com.example.proiect_licenta_client.R;

import java.util.ArrayList;

public class FriendListAdapter extends BaseAdapter {
   private ArrayList<FriendListContainer> friends;
   private Context context;
   private PaginaProfilFriendList paginaProfilFriendList;
    private ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat = null;

    public void setFriends(ArrayList<FriendListContainer> friends){
        this.friends=friends;
    }
   public FriendListAdapter(Context context, ArrayList<FriendListContainer> friends, PaginaProfilFriendList paginaProfilFriendList,ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat) {

        this.context = context;
        this.friends = friends;
        this.paginaProfilFriendList = paginaProfilFriendList;
       this.pozeDeIncarcat= pozeDeIncarcat;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return friends.size();
    }

    @Override
    public FriendListContainer getItem(int arg0) {
        // TODO Auto-generated method stub
        return friends.get(arg0);
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
            v = inflater.inflate(R.layout.pagina_profil_friend_list_fragment, arg2, false);
        }
        TextView tv1;
        final FriendListContainer flc = friends.get(poz);
        v.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (paginaProfilFriendList != null)
                    paginaProfilFriendList.selecteazaProfil(flc.getNume());

            }

        });
        tv1 = ((TextView) v.findViewById(R.id.friendListNume));
        tv1.setText(flc.getNume());


        ImageView imagineProfil = (ImageView) v.findViewById(R.id.friendListFragmentPozaProfil);
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








        new IncarcaPoze.LoadImage(imagineProfil,context,"PozaProfil" + flc.getIdPozaProfil(), new_width, new_height,"getPozaById",pozeDeIncarcat).execute();
        return v;
    }
}
