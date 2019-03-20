package com.desbois.mathis.bizzbee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ListRucherFragment extends ListComponentFragment {
    private static String listRucherUrl = "/apiculteurInfos";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setActionBarTitle("Ruchers");

        return inflater.inflate(R.layout.fragment_list_rucher, container, false);
    }
}
