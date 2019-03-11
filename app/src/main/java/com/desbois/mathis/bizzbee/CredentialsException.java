package com.desbois.mathis.bizzbee;

import android.util.Log;

public class CredentialsException extends Exception {
    public CredentialsException() {
        super();
        Log.e("Bizzbee", "CredentialsException : Problème avec les identifiants.");
    }

    @Override
    public String toString() {
        return "CredentialsException : Problème avec les identifiants.";
    }
}
