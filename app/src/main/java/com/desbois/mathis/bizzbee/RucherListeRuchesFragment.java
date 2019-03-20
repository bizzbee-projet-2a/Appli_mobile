package com.desbois.mathis.bizzbee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

// Instances of this class are fragments representing a single
// object in our collection.
public class RucherListeRuchesFragment extends ListComponentFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_ruche, container, false);
    }
}
