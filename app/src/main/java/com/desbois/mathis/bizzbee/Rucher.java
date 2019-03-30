package com.desbois.mathis.bizzbee;

import java.util.ArrayList;

public class Rucher extends Composant {
    private ArrayList<Composant> mRuches;

    public Rucher(int i) {
        this(i, "");
    }

    public Rucher(int i, String n) {
        this(i, n, Composant.NULL_PARENT, new ArrayList<>());
    }

    public Rucher(int i, String n, int p) {
        this(i, n, p, new ArrayList<>());
    }

    public Rucher(int i, String n, ArrayList<Composant> m) {
        this(i, n, NULL_PARENT, m);
    }

    public Rucher(int i, String n, int p, ArrayList<Composant> r) {
        super(i, n);

        mRuches = r;
    }

    public ArrayList<Composant> getRuches() {
        return mRuches;
    }
}
