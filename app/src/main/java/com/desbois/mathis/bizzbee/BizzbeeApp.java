package com.desbois.mathis.bizzbee;

import android.app.Application;
import android.content.res.Resources;
import android.util.Base64;

public class BizzbeeApp extends Application {
    private String login = "toto";
    private String password = "toto";

    private boolean connected = true;

    private static Resources resources;

    public BizzbeeApp() {
        super.onCreate();

        login = "";
        password = "";

        connected = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        resources = getResources();
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setCredentials() throws CredentialsException {
        if(connected && !login.equals("") && !password.equals("")) {
            login = "";
            password = "";

            connected = false;
        } else {
            throw new CredentialsException();
        }
    }

    public void setCredentials(String l, String p) throws CredentialsException {
        if(!connected && login.equals("") && password.equals("")) {
            login = l;
            password = p;

            connected = true;
        } else {
            throw new CredentialsException();
        }
    }

    public String getAuthorization() throws CredentialsException {
        if(connected) {
            return Base64.encodeToString((login + ":" + password).getBytes(), Base64.DEFAULT);
        } else {
            throw new CredentialsException();
        }
    }

    public static Resources getAppResources() {
        return resources;
    }
}
