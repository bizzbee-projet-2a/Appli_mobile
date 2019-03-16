package com.desbois.mathis.bizzbee;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Objects;

public class WelcomeFragment extends Fragment implements View.OnClickListener {

    private Button mConnexionButton;
    private Button mAboutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ((MainActivity) getActivity()).setActionBarTitle("Accueil");

        View root = inflater.inflate(R.layout.fragment_welcome, container, false);

        mConnexionButton = root.findViewById(R.id.activity_main_connexion_button);
        mAboutButton = root.findViewById(R.id.activity_main_about_button);
        mAboutButton.setOnClickListener(this);
        mConnexionButton.setOnClickListener(this);

        return root;
    }

    @Override
    public void onClick(View v) {
        ((WelcomeListener) Objects.requireNonNull(getActivity())).onButtonClick(v);
    }

    public interface WelcomeListener {
        void onButtonClick(View v);
    }
}
