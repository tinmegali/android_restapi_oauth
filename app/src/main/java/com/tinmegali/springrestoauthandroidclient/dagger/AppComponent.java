package com.tinmegali.springrestoauthandroidclient.dagger;

import com.tinmegali.springrestoauthandroidclient.MyApplication;
import dagger.Component;

import javax.inject.Singleton;

/**
 * Created by tinmegali on 06/11/16.
 */

@Singleton
@Component( modules = AppModule.class )
public interface AppComponent {

    MyApplication inject( MyApplication app );

}
