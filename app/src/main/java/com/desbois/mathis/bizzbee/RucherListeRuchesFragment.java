package com.desbois.mathis.bizzbee;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

// Instances of this class are fragments representing a single
// object in our collection.
public class RucherListeRuchesFragment extends ListComponentFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_ruche, container, false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), "Item: " + position, Toast.LENGTH_SHORT).show();

        Intent babout = new Intent(getContext(), RucheActivity.class);
        babout.putExtra("idRuche", position);

        startActivity(babout);
    }
}