package com.desbois.mathis.bizzbee;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import androidx.fragment.app.ListFragment;

// Instances of this class are fragments representing a single
// object in our collection.
abstract class ListComponentFragment extends ListFragment implements AdapterView.OnItemClickListener {
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Resources res = getResources();
        final String[] noms = res.getStringArray(R.array.Planets);

        ArrayList<String> mListe = new ArrayList<>(Arrays.asList(noms));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()),
                R.layout.adapter_row_ruche, R.id.firstLine, mListe);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }
}