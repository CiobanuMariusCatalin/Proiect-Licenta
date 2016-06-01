package com.example.proiect_licenta_client.Adaptere;


import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.proiect_licenta_client.Containere.FriendRequestContainer;
import com.example.proiect_licenta_client.Others.IncarcaPoze;
import com.example.proiect_licenta_client.PaginaPrincipalaViewPagersFragments.PaginaPrincipalaFriendRequestsFragment;
import com.example.proiect_licenta_client.R;

import java.util.ArrayList;

public class FriendRequestAdapter extends BaseAdapter {
    private ArrayList<FriendRequestContainer> friendRequests;
    private Context context;
    private PaginaPrincipalaFriendRequestsFragment paginaFriendRequest;
    private ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat = null;
    public void setFriendRequests(ArrayList<FriendRequestContainer> friendRequests){
        this.friendRequests=friendRequests;
    }
 public   FriendRequestAdapter(Context context, ArrayList<FriendRequestContainer> friendRequests, PaginaPrincipalaFriendRequestsFragment paginaFriendRequest,ArrayList<IncarcaPoze.LoadImage> pozeDeIncarcat) {

        this.context = context;
        this.friendRequests = friendRequests;
        this.paginaFriendRequest = paginaFriendRequest;
     this.pozeDeIncarcat= pozeDeIncarcat;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return friendRequests.size();
    }

    @Override
    public FriendRequestContainer getItem(int arg0) {
        // TODO Auto-generated method stub
        return friendRequests.get(arg0);
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
            v = inflater.inflate(R.layout.friend_requests_fragment, arg2, false);
        }
        TextView tv1;
        final FriendRequestContainer frc = friendRequests.get(poz);
        v.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (paginaFriendRequest != null)
                    paginaFriendRequest.selecteazaProfil(frc.getNume());

            }

        });
        tv1 = ((TextView) v.findViewById(R.id.friendRequestsFragmentAutor));
        tv1.setText(frc.getNume());
        ImageView imagineProfil = (ImageView) v.findViewById(R.id.friendRequestsFragmentPozaProfil);


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

        Button buttonAccept = (Button) v.findViewById(R.id.friendRequestsFragmentAcceptFriendRequest);

        buttonAccept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (paginaFriendRequest != null)
                    paginaFriendRequest.acceptFriendRequest(frc.getNume());

            }

        });

        Button buttonDecline = (Button) v.findViewById(R.id.friendRequestsFragmentDeclineFriendRequest);
        buttonDecline.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (paginaFriendRequest != null)
                    paginaFriendRequest.declineFriendRequest(frc.getNume());

            }

        });
        return v;
    }
}
