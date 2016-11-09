package com.tinmegali.springrestoauthandroidclient.api;

import android.util.Log;

import com.tinmegali.springrestoauthandroidclient.api.exceptions.RestHttpException;
import com.tinmegali.springrestoauthandroidclient.api.exceptions.RestUnauthorizedException;
import com.tinmegali.springrestoauthandroidclient.models.User;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
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
    public ApiController( OAuthManager oAuthManager, OkHttpClient client ) {

        this.oAuthManager = oAuthManager;
        this.client = client;

        retrofit = new Retrofit.Builder()
                .baseUrl( BASE_URL )
                .addConverterFactory(GsonConverterFactory.create())
                .client( client )
                .build();
        api = retrofit.create( ApiEndpointInterface.class );
    }

    private Map<String, String> getQueryMap( String token ) {
        Map<String, String> map = new HashMap<>();
        map.put("access_token", token);
        return map;
    }

    public List<User> getUsers() throws RestUnauthorizedException, RestHttpException, IOException {
        //Log.d(TAG, "getUsers");
        // get valid token
        String token = oAuthManager.getValidToken();

        Call<List<User>> users = api.getUsers( getQueryMap( token ));
        return users.execute().body();
    }

    public void deleteUser( String userId ) throws RestUnauthorizedException, RestHttpException, IOException {
        //Log.d(TAG, "deleteUser:" + userId);
        String token = oAuthManager.getValidToken();
        if ( token != null ) {
            Call<ResponseBody> responseCall = api.deleteUser(userId,getQueryMap(token));
            responseCall.execute();
        }
    }

    public  User addUser( User user ) throws RestUnauthorizedException, RestHttpException, IOException {
        //Log.d(TAG, "addUser");
        String token = oAuthManager.getValidToken();
        if ( token != null ) {
            Call<User> newUserCall = api.createUser( getQueryMap(token), user );
            return newUserCall.execute().body();
        } return null;
    }

    public User getUser( String id ) throws RestUnauthorizedException, RestHttpException, IOException {
        //Log.d(TAG, "getUser:" + id);
        String token = oAuthManager.getValidToken();
        if ( token != null ) {
            Call<User> userCall = api.getUser(id, getQueryMap(token));
            return userCall.execute().body();
        } return null;
    }

}
