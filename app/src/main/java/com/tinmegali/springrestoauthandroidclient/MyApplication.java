package com.tinmegali.springrestoauthandroidclient;

import android.app.Application;
import com.tinmegali.springrestoauthandroidclient.dagger.*;


public class MyApplication extends Application {

    ActivitiesComponent activitiesComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        AppComponent appComponent = DaggerAppComponent.builder()
                .build();

        appComponent.inject( this );

        activitiesComponent = DaggerActivitiesComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public ActivitiesComponent getActivitiesComponent() {
        return activitiesComponent;
    }
}
