package com.tinmegali.springrestoauthandroidclient.api;

import okhttp3.*;

import java.io.IOException;

/**
 * Authenticator used at {@link OkHttpClient} on 'access_token' and 'refresh_token' requests
 */
public class TokenAuthenticator implements Authenticator {

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        String credentials =
                Credentials.basic( ServerDetails.CLIENT, ServerDetails.SECRET );
        return response.request( ).newBuilder()
                .addHeader("Authorization", credentials)
                .build();
    }
}
