package com.tinmegali.springrestoauthandroidclient.service;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.tinmegali.springrestoauthandroidclient.api.ApiController;
import com.tinmegali.springrestoauthandroidclient.api.exceptions.RestHttpException;
import com.tinmegali.springrestoauthandroidclient.api.exceptions.RestUnauthorizedException;

import java.io.IOException;

import javax.inject.Inject;

/**
 * com.tinmegali.springrestoauthandroidclient.service | SpringRestOauthAndroidClient
 * __________________________________
 * Created by tinmegali
 * 08/11/16
 *
 * @see <a href="http://www.tinmegali.com">tinmegali.com</a>
 * @see <a href="http://github.com/tinmegali">github</a>
 * ___________________________________
 */

public class ApiService extends Service {

    @Inject
    ApiController apiController;

    private final IBinder binder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void getUsers() {
        try {
            apiController.getUsers();
        } catch (RestUnauthorizedException e) {
            // check credentials
            e.printStackTrace();
            // check server
        } catch (RestHttpException e) {
            e.printStackTrace();
            // some IO problem
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class WorkerThread extends HandlerThread {

        public WorkerThread() {
            super("ApiService_WThread");
        }
    }

    private class LocalBinder extends Binder {
        ApiService getService() {
            return ApiService.this;
        }
    }
}
