package com.tinmegali.springrestoauthandroidclient;

import com.google.gson.Gson;
import com.tinmegali.springrestoauthandroidclient.api.ServerDetails;
import com.tinmegali.springrestoauthandroidclient.api.TokenAuthenticator;
import com.tinmegali.springrestoauthandroidclient.models.ErrorUnauthorized;
import com.tinmegali.springrestoauthandroidclient.models.TokenResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;

/**
 * com.tinmegali.springrestoauthandroidclient | SpringRestOauthAndroidClient
 * __________________________________
 * Created by tinmegali
 * 08/11/16
 *
 * @see <a href="http://www.tinmegali.com">tinmegali.com</a>
 * @see <a href="http://github.com/tinmegali">github</a>
 * ___________________________________
 */
@RunWith(JUnit4.class)
public class OauthRequestsTest {

    OkHttpClient client;
    Gson gson;
    Request requestToken;
    RequestBody reqPostEmpty;

    @Before
    public void setup() {
        client =new OkHttpClient.Builder()
                .authenticator( new TokenAuthenticator() )
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

        gson = new Gson();

        reqPostEmpty = RequestBody.create(null, new byte[0]);
        requestToken = new Request.Builder()
                .url(ServerDetails.URL.TOKEN + "grant_type=password&" +
                        "username=bill&" +  // TODO get username
                        "password=abc123")  // TODO get user password
                .method("POST", reqPostEmpty)
                .build();
    }

    @Test
    public void getToken() throws Exception {

        // Getting token
        Response response = client.newCall(requestToken).execute();

        assertEquals( response.isSuccessful(), true );
        TokenResponse token = gson.fromJson(
                response.body().charStream(),
                TokenResponse.class);

        assertNotNull( token );
        assertNotNull( token.getAccessToken() );
        assertNotNull( token.getExpiresIn() );
        assertNotNull( token.getRefreshToken() );
        assertNotNull( token.getScope() );
        assertNotNull( token.getTokenType() );


    }

    @Test
    public void getTokenInvalidCredentials() throws Exception {
        // unauthorized request client
        client =new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

        Response response = client.newCall( requestToken ).execute();
        response.close();

        assertEquals( response.isSuccessful(), false );
        assertEquals( "Code:" + response.code(), response.code(), 401 );
    }

    @Test
    public void getTokenInvalidUser() throws Exception {
        // wrong req credentials
        Request reqWrongCred = new Request.Builder()
                .url(ServerDetails.URL.TOKEN + "grant_type=password&" +
                        "username=wrong&" +
                        "password=abc123")
                .method("POST", reqPostEmpty)
                .build();

        Response response = client.newCall( reqWrongCred ).execute();
        response.close();

        assertEquals( response.isSuccessful(), false );
        assertEquals( "Code:" + response.code(), response.code(), 400 );
    }

    @Test
    public void getTokenRefresh() throws  Exception {

        Response response = client.newCall( requestToken ).execute();
        assertEquals( response.isSuccessful(), true );

        TokenResponse token = gson.fromJson( response.body().charStream(), TokenResponse.class );
        response.close();
        assertNotNull( token );

        System.out.println("print token");
        printToken(token);

        // getting refresh
        Request reqRefresh = new Request.Builder()
                .url(ServerDetails.URL.TOKEN +
                        "grant_type=refresh_token&" +
                        "refresh_token=" + token.getRefreshToken())
                .method("POST", reqPostEmpty)
                .build();

        Response responseRefresh = client.newCall( reqRefresh ).execute();
        assertEquals( responseRefresh.isSuccessful(), true );
        TokenResponse tokenRefresh = gson.fromJson( responseRefresh.body().charStream(), TokenResponse.class );
        assertNotNull( tokenRefresh );
        assertNotNull( tokenRefresh.getAccessToken() );
        assertNotNull( tokenRefresh.getExpiresIn() );
        assertNotNull( tokenRefresh.getRefreshToken() );
        assertNotNull( tokenRefresh.getScope() );
        assertNotNull( tokenRefresh.getTokenType() );

        responseRefresh.close();
        System.out.println( "print tokenRefresh ");
        printToken( tokenRefresh );

        // wait until tokenRefresh became invalid
        long sleep = (4 + 1) * 1000; // Check on server the refresh token valid time
        System.out.println( "Sleeping for " + (sleep/1000) + " seconds" );
        Thread.sleep( sleep );

        // try to get a new token using invalid credential
        Request reqRefreshInvalid = new Request.Builder()
                .url(ServerDetails.URL.TOKEN +
                        "grant_type=refresh_token&" +
                        "refresh_token=" + tokenRefresh.getRefreshToken())
                .method("POST", reqPostEmpty)
                .build();
        Response invalidResponse = client.newCall( reqRefreshInvalid ).execute();

        System.out.println( "Resp code:" + invalidResponse.code() );
        assertFalse( invalidResponse.isSuccessful() );
        assertEquals( invalidResponse.code(), 400 );

        ErrorUnauthorized errorUnauthorized = gson.fromJson( invalidResponse.body().charStream(), ErrorUnauthorized.class );
        assertEquals(errorUnauthorized.getError(), "invalid_grant");
        invalidResponse.close();

    }

    private void printToken( TokenResponse token ) {
        System.out.println();
        System.out.println("---- START printing token ----");
        System.out.println();
        System.out.println("Token:" + token.getAccessToken() );
        System.out.println("RefreshToken:" + token.getRefreshToken() );
        System.out.println("ExpiredIn:" + token.getExpiresIn() );
        System.out.println("Scope:" + token.getScope() );
        System.out.println("Type:" + token.getTokenType() );
        System.out.println();
        System.out.println("---- END printing token ----");
        System.out.println();
    }

}
