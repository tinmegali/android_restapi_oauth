package com.tinmegali.springrestoauthandroidclient.dagger;

import com.tinmegali.springrestoauthandroidclient.DetailActivity;
import com.tinmegali.springrestoauthandroidclient.NewUserActivity;
import com.tinmegali.springrestoauthandroidclient.StarterActivity;

import dagger.Component;

import javax.inject.Singleton;

/**
 * Created by tinmegali on 06/11/16.
 */
@Singleton
@Component( modules = { AppModule.class, ActivitiesModule.class })
public interface ActivitiesComponent {

    StarterActivity inject ( StarterActivity activity );
    DetailActivity inject(DetailActivity activity);
    NewUserActivity inject( NewUserActivity activity );

}
