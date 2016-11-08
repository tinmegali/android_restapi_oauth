package com.tinmegali.springrestoauthandroidclient.dagger;

import android.accounts.AccountManager;
import android.content.SharedPreferences;
import com.tinmegali.springrestoauthandroidclient.MyApplication;
import com.tinmegali.springrestoauthandroidclient.api.ApiController;
import com.tinmegali.springrestoauthandroidclient.api.OAuthManager;
import com.tinmegali.springrestoauthandroidclient.security.MyAccountManager;
import com.tinmegali.springrestoauthandroidclient.security.TokenAuthenticator;

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
    MyAccountManager providesAccountManager() {
        return new MyAccountManager();
    }

    @Provides
    SharedPreferences providesPreferences() {
        return application.getSharedPreferences("user_prefs",0);
    }

    @Provides
    OAuthManager providesOauthManager( SharedPreferences preferences) {
        return new OAuthManager(preferences);
    }

    @Singleton
    @Provides
    ApiController providesApiController( OAuthManager oAuthManager ) {
        return new ApiController( oAuthManager );
    }



}
