package com.desbois.mathis.bizzbee;

public class Composant {
    public static int NULL_PARENT = -1;

    private String nom;
    private int id;
    private int idParent;

    public Composant(int i, String n) {
        this(i, n, NULL_PARENT);
    }

    public Composant(int i, String n, int p) {
        nom = n;
        id = i;
        idParent = p;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdParent() {
        return idParent;
    }

    public String toString() {
        return nom;
    }
}
