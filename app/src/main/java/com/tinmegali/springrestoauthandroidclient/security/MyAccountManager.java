package com.tinmegali.springrestoauthandroidclient.security;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.tinmegali.springrestoauthandroidclient.LoginActivity;
import com.tinmegali.springrestoauthandroidclient.models.TokenResponse;
import com.tinmegali.springrestoauthandroidclient.models.User;
import okhttp3.*;
import okio.BufferedSink;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.http.POST;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by tinmegali on 06/11/16.
 */
public class MyAccountManager {

    private static final String TAG = MyAccountManager.class.getSimpleName();

    public MyAccountManager() {
    }

    private final String ACCOUNT_TYPE = "com.tinmegali.oauth_rest_account";

    private static final String urlToken = "urlToken";
    private static final String CLIENT  = "my_trusted_client";
    private static final String SECRET  = "secret";

    //http://localhost:8080/oauth/token?grant_type=password&username=bill&password=abc123

    private static final String ROLE_CLIENT = "ROLE_CLIENT";
    private static final String ROLE_TRUSTED_CLIENT = "ROLE_TRUSTED_CLIENT";

    private static final String SCOPE_TRUST = "trust";
    private static final String SCOPE_WRITE = "write";
    private static final String SCOPE_READ  = "read";

    private static final String GRAND_TYPE_PASSWORD = "password";
    private static final String GRAND_TYPE_AUTH_CODE = "authorization_code";
    private static final String GRAND_TYPE_REFRESH_TOKEN = "refresh_token";
    private static final String GRAND_TYPE_IMPLICIT = "implicit";


    public void getToken() {

        Log.d(TAG, "getValidToken()");
        final String url =
                "http://192.168.25.3:8080/oauth/token?grant_type=password&username=bill&password=abc123";

        RequestBody reqbody = RequestBody.create(null, new byte[0]);
        Request request = new Request.Builder()
                .url(url)
                .method("POST", reqbody)
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .authenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        String credentials = Credentials.basic( "my_trusted_client", "secret" );
                        return response.request( ).newBuilder()
                                .addHeader("Authorization", credentials)
                                .build();
                    }
                }).build();

        try {
            Response res = client.newCall( request ).execute();
            Gson gson = new Gson();

            if ( !res.isSuccessful() ) Log.e(TAG, "Error on request");

            TokenResponse tokenResponse = gson.fromJson( res.body().charStream(), TokenResponse.class );
            Log.d(TAG, "token: " + tokenResponse.getAccessToken() );

            // get users
            Request reqUsers = new Request.Builder()
                    .url("http://192.168.25.3:8080/user/?access_token="+tokenResponse.getAccessToken())
                    .build();
            Response resClient = client.newCall( reqUsers ).execute();
//            Log.d(TAG, "responseUser: " + resClient.body().string());
            User[] users = gson.fromJson( resClient.body().charStream(), User[].class );
            for ( User user : users )
                Log.d(TAG, "user: " + user.getName() );

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
    private Request addBasicAuthHeaders(Request request) {
        final String login = "my_trusted_client";
        final String password = "p@secret";
        String credential = Credentials.basic(login, password);
        return request.newBuilder().header("Authorization", credential).build();
    }

    public String getJSON(String url, int timeout) {
        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("POST");
            c.setRequestProperty("Content-length", "0");
            String baseAuthStr = CLIENT + ":" + SECRET;
            c.setDoOutput(true);
            c.setDoInput(true);
            c.addRequestProperty("Authorization", "Basic " + baseAuthStr);
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.connect();
            int status = c.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    return sb.toString();
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;
    }




}
