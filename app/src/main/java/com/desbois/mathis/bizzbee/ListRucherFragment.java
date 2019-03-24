package com.desbois.mathis.bizzbee;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

public class ListRucherFragment extends ListComponentFragment {
    private static String listRucherUrl = "/apiculteurInfos";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setActionBarTitle("Ruchers");

        return inflater.inflate(R.layout.fragment_list_rucher, container, false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), "Item: " + position, Toast.LENGTH_SHORT).show();

        Intent babout = new Intent(getContext(), RucherActivity.class);
        babout.putExtra("idRuche", position);

        startActivity(babout);
    }
}
