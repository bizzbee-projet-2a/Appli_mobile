package com.desbois.mathis.bizzbee;

import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;

import java.util.ArrayList;

import androidx.fragment.app.ListFragment;

// Instances of this class are fragments representing a single
// object in our collection.
abstract class ListComponentFragment extends ListFragment
        implements AdapterView.OnItemClickListener, Connectable {
    protected ArrayList<Composant> mListe = new ArrayList<>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.i("TTTTTT", "Activity Created");
        makeRequest();
    }

    public void setmListe(ArrayList<Composant> liste) {
        mListe = liste;
    }

}