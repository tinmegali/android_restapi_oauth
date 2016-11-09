package com.tinmegali.springrestoauthandroidclient.dagger;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.tinmegali.springrestoauthandroidclient.MyApplication;
import com.tinmegali.springrestoauthandroidclient.api.ApiController;
import com.tinmegali.springrestoauthandroidclient.api.OAuthManager;
import com.tinmegali.springrestoauthandroidclient.api.TokenAuthenticator;

import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

import javax.inject.Singleton;

/**
 * Created by tinmegali on 06/11/16.
 */

@Module
public class AppModule {

    MyApplication application;

    public AppModule(MyApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    MyApplication providesApplication() { return application; }

    @Provides
    @Singleton
    SharedPreferences providesPreferences() {
        return application.getSharedPreferences("user_prefs",0);
    }

    @Provides
    OkHttpClient providesOkHttpClient() {
        return new OkHttpClient.Builder()
                .authenticator( new TokenAuthenticator() )
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    Gson providesGson() {
        return new Gson();
    }

    @Provides
    OAuthManager providesOauthManager( SharedPreferences preferences, OkHttpClient client, Gson gson ) {
        return new OAuthManager( preferences, client, gson );
    }

    @Singleton
    @Provides
    ApiController providesApiController( OAuthManager oAuthManager, OkHttpClient client ) {
        return new ApiController( oAuthManager, client );
    }



}
