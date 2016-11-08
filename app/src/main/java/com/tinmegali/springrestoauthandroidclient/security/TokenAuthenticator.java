package com.tinmegali.springrestoauthandroidclient.security;

import com.tinmegali.springrestoauthandroidclient.api.ServerDetails;
import okhttp3.*;

import java.io.IOException;

/**
 * Created by tinmegali on 07/11/16.
 */
public class TokenAuthenticator implements Authenticator {

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        String credentials = Credentials.basic( ServerDetails.CLIENT, ServerDetails.SECRET );
        return response.request( ).newBuilder()
                .addHeader("Authorization", credentials)
                .build();
    }
}
