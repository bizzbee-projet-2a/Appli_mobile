package com.desbois.mathis.bizzbee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class ComposantAdapter extends ArrayAdapter<Composant> implements View.OnClickListener {
    private ArrayList<Composant> composants;
    private Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView firstLine;
        TextView secondLine;
    }

    public ComposantAdapter(ArrayList<Composant> data, Context context) {
        super(context, R.layout.adapter_row_composant, data);
        this.composants = data;
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {
        int position = (int) v.getTag();
        Composant composant = getItem(position);

        switch (v.getId()) {
            case R.id.firstLine:
                Snackbar.make(v, "Release date " +composant, Snackbar.LENGTH_LONG)
                        .setAction("No action", null).show();
                break;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Composant composant = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.adapter_row_composant, parent, false);
            viewHolder.firstLine = convertView.findViewById(R.id.firstLine);
            viewHolder.secondLine = convertView.findViewById(R.id.secondLine);;

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        viewHolder.firstLine.setText(composant.getNom());
        viewHolder.secondLine.setText("ID : " + composant.getId());
        // Return the completed view to render on screen
        return result;
    }
}
