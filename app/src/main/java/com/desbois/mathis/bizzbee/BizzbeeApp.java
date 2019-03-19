package com.desbois.mathis.bizzbee;

import android.app.Application;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Base64;

public class BizzbeeApp extends Application {
    private String servUrl;
    private boolean isBizzbeeUrl;

    private String login;
    private String password;

    private boolean connected;

    private static Resources resources;

    private Intent intentService;

    public BizzbeeApp() {
        super.onCreate();

        login = "";
        password = "";
        servUrl = "";

        connected = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        resources = getResources();

        intentService = new Intent(this, BizzbeeService.class);
    }

    public void startBizzbeeService() {
        startService(intentService);
    }

    public void stopBizzbeeService() {
        stopService(intentService);
    }

    public boolean isBizzbeeUrl() {
        return isBizzbeeUrl;
    }

    public String getServUrl() {
        return servUrl;
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

    public boolean setServUrl(String s) {
        String beautified = Utils.beautifyUrl(s);
        isBizzbeeUrl = false;
        if(Utils.isBizzbeeUrl(beautified)) {
            servUrl = s;
            isBizzbeeUrl = true;
        }

        return isBizzbeeUrl;
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
