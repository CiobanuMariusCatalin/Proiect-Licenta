package com.example.proiect_licenta_client.PaginaPrincipalaViewPagersFragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.*;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;

import com.example.proiect_licenta_client.R;

public class PaginaPrincipalaSettingsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(
                R.layout.pagina_principala_settings_fragment, container, false);


        return rootView;


    }
}
