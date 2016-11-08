package com.tinmegali.springrestoauthandroidclient.dagger;

import com.tinmegali.springrestoauthandroidclient.LoginActivity;
import com.tinmegali.springrestoauthandroidclient.StarterActivity;
import com.tinmegali.springrestoauthandroidclient.WelcomeActivity;
import dagger.Component;

import javax.inject.Singleton;

/**
 * Created by tinmegali on 06/11/16.
 */
@Singleton
@Component( modules = { AppModule.class, ActivitiesModule.class })
public interface ActivitiesComponent {

    StarterActivity inject ( StarterActivity activity );
//    WelcomeActivity inject( WelcomeActivity activity );
//    LoginActivity inject( LoginActivity activity );

}
