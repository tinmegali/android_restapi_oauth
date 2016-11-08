package com.tinmegali.springrestoauthandroidclient;

import android.app.Application;
import com.tinmegali.springrestoauthandroidclient.dagger.*;

/**
 * Created by tinmegali on 06/11/16.
 */
public class MyApplication extends Application {

    ActivitiesComponent activitiesComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        AppComponent appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

        appComponent.inject( this );

        activitiesComponent = DaggerActivitiesComponent.builder()
                .appModule(new AppModule(this))
                .activitiesModule(new ActivitiesModule())
                .build();
    }

    public ActivitiesComponent getActivitiesComponent() {
        return activitiesComponent;
    }
}
