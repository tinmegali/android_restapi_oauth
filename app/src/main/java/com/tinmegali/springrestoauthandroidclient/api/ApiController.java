package com.tinmegali.springrestoauthandroidclient.api;

import android.util.Log;

import com.tinmegali.springrestoauthandroidclient.api.errors.RestHttpException;
import com.tinmegali.springrestoauthandroidclient.api.errors.RestUnauthorizedException;
import com.tinmegali.springrestoauthandroidclient.models.User;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rest API Controller
 */
public class ApiController {

    private final String TAG = ApiController.class.getSimpleName();
    private static final String BASE_URL =  ServerDetails.URL.DOMAIN;

    private final OAuthManager oAuthManager;

    private final Retrofit retrofit;
    private final ApiEndpointInterface api;
    private OkHttpClient client;

    @Inject
    public ApiController( OAuthManager oAuthManager ) {

        this.oAuthManager = oAuthManager;
        this.client = new OkHttpClient.Builder()
                .authenticator( getBasicAuth(ServerDetails.CLIENT, ServerDetails.SECRET) )
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl( BASE_URL )
                .addConverterFactory(GsonConverterFactory.create())
                .client( client )
                .build();
        api = retrofit.create( ApiEndpointInterface.class );
    }

    private Authenticator getBasicAuth(final String username, final String password) {
        return new Authenticator() {
            private int mCounter = 0;

            @Override
            public Request authenticate(Route route, Response response) throws IOException {
                Log.d("OkHttp", "authenticate(Route route, Response response) | mCounter = " + mCounter);
                if (mCounter++ > 0) {
                    Log.d("OkHttp", "authenticate(Route route, Response response) | I'll return null");
                    return null;
                } else {
                    Log.d("OkHttp", "authenticate(Route route, Response response) | This is first time, I'll try to authenticate");
                    String credential = Credentials.basic(username, password);
                    return response.request().newBuilder().header("Authorization", credential).build();
                }
            }
        };
    }

    public List<User> getUsers() {
        Log.d(TAG, "getUsers");
        // get valid token
        try {
            String token = oAuthManager.getValidToken();
            Map<String, String> map= new HashMap<>();
            map.put("access_token", token);
            Call<List<User>> users = api.getUsers( map );
            return users.execute().body();

        } catch (RestUnauthorizedException e) {
            Log.d(TAG, "getUsers : RestUnauthorizedException");
            e.printStackTrace();
        } catch (RestHttpException e) {
            Log.d(TAG, "getUsers : RestHttpException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "getUsers : IOException");
            e.printStackTrace();
        }
        return null;
    }

}
