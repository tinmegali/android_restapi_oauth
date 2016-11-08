package com.tinmegali.springrestoauthandroidclient;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.tinmegali.springrestoauthandroidclient.api.ApiController;
import com.tinmegali.springrestoauthandroidclient.api.OAuthManager;
import com.tinmegali.springrestoauthandroidclient.api.ServerDetails;
import com.tinmegali.springrestoauthandroidclient.models.TokenResponse;
import com.tinmegali.springrestoauthandroidclient.models.User;
import com.tinmegali.springrestoauthandroidclient.security.MyAccountManager;

import javax.inject.Inject;

import java.io.IOException;
import java.util.List;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class StarterActivity extends AppCompatActivity {

    private static final String TAG = StarterActivity.class.getSimpleName();

    @Inject
    MyAccountManager accountManager;

    @Inject
    ApiController apiController;

    @Inject
    SharedPreferences preferences;

    @Inject
    OAuthManager oAuthManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ((MyApplication)getApplication()).getActivitiesComponent().inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);

        // check if has user
        hasUser();
    }

    private void hasUser() {
        Log.d(TAG, "hasUSer");

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                preferences.edit().clear().apply();

                OkHttpClient client = new OkHttpClient.Builder()
                        .build();
                String credentials = Credentials.basic( ServerDetails.CLIENT, ServerDetails.SECRET );
                RequestBody reqpost = RequestBody.create(null, new byte[0]);
                Request req = new Request.Builder()
                        .addHeader("Authorization", credentials)
                        .url(ServerDetails.URL.TOKEN+"grant_type=password&username=bill&password=abc123")
                        .method("POST", reqpost)
                        .build();

                Gson gson = new Gson();

                try {

                    TokenResponse token = gson.fromJson(
                            client.newCall( req ).execute().body().charStream(),
                            TokenResponse.class);

                    req = new Request.Builder()
                            .addHeader("Authorization", credentials)
                            .url( ServerDetails.URL.USER +"access_token=" + token.getAccessToken() )
                            .build();

                    User[] users = gson.fromJson(
                            client.newCall( req ).execute().body().charStream(),
                            User[].class);

                    for( User user:users) {
                        Log.d(TAG, "user:" + user.getName());
                    }

                    List<User> usersList = apiController.getUsers( );
                    if ( usersList!= null) {
                        for (User user : usersList) {
                            Log.d(TAG, "2 user: " + user.getName());
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "Trying to get token...");
                List<User> userList = apiController.getUsers();
                if ( userList != null ) {
                    for (User user : userList)
                        Log.d(TAG, "User.name:" + user.getName());
                } else
                Log.e(TAG, "null user list.");
                return null;
            }
        }.execute();

    }
}
