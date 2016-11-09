package com.tinmegali.springrestoauthandroidclient;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tinmegali.springrestoauthandroidclient.api.ApiController;
import com.tinmegali.springrestoauthandroidclient.api.OAuthManager;
import com.tinmegali.springrestoauthandroidclient.api.ServerDetails;
import com.tinmegali.springrestoauthandroidclient.api.exceptions.RestHttpException;
import com.tinmegali.springrestoauthandroidclient.models.TokenResponse;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Answers;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Calendar;

import javax.inject.Inject;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(JUnit4.class)
//@Config(constants = BuildConfig.class, sdk = 21)
public class OauthTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    SharedPreferences preferences;

    @Mock
    SharedPreferences.Editor editor;

    OkHttpClient client;

    Gson gson;

    OAuthManager oAuthManager;

    MockWebServer server;

    @Mock Context context;

    @Before
    public void setup() throws Exception {

        gson = new Gson();

        Mockito.when( editor.commit() ).thenReturn(true);
        Mockito.when( editor.putString( Matchers.any(String.class ), Matchers.any(String.class ))).thenReturn( editor );
        Mockito.when( editor.putLong( Matchers.any(String.class ), Matchers.any(Long.class ))).thenReturn( editor );
        Mockito.when( editor.putInt( Matchers.any(String.class ), Matchers.any(Integer.class ))).thenReturn( editor );
        Mockito.when( preferences.edit() ).thenReturn( editor );

    }


    private OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .proxy(
                        new Proxy(
                                Proxy.Type.HTTP,
                                new InetSocketAddress(server.getHostName(), server.getPort())
                        )
                ).build();
    }

    @Test
    public void refreshToken() throws Exception {

        server = new MockWebServer();

        // call 1 valid token using the refresh
        String token1 = "4e98803b-aa46-4f8a-a6a5-d41fc8ef9668";
        String tokenRefresh1 = "e84001d7-b126-49c4-bef0-48d010235c81";
        server.enqueue(
                new MockResponse().setResponseCode(200)
                        .setBody("{\"access_token\":\""+ token1 + "\"," +
                                "\"token_type\":\"bearer\"," +
                                "\"refresh_token\":" + "\"" + tokenRefresh1 +"\"," +
                                "\"expires_in\":119," +
                                "\"scope\":\"write read trust\"}")
        );
        // Call 2, invalid refresh token
        server.enqueue(
                new MockResponse().setResponseCode(400));

        // call 3, returning a new token with a different refresh
        String token2 = "4e98803b-aa46-4f8a-a6a5-XXXXXXX";
        String tokenRefresh2 = "e84001d7-b126-49c4-bef0-48d010235c81";
        server.enqueue(
                new MockResponse().setResponseCode(200)
                        .setBody("{\"access_token\":\""+ token2 + "\"," +
                                "\"token_type\":\"bearer\"," +
                                "\"refresh_token\":" + "\""+ tokenRefresh2+ "\"," +
                                "\"expires_in\":119," +
                                "\"scope\":\"write read trust\"}")
        );

        oAuthManager = new OAuthManager( preferences , getClient(), gson );

        // making the first call
        // retrieving a token using the refresh token
        String token = oAuthManager.getRefreshToken( "e84001d7-b126-49c4-bef0-48d010235c81" );
        assertNotNull( token );
        assertEquals( token, "4e98803b-aa46-4f8a-a6a5-d41fc8ef9668");

        // making the second and third calls
        // invalid token, calling for a new token
        String newToken = oAuthManager.getRefreshToken( tokenRefresh1 );
        assertNotNull(newToken);
        assertEquals( newToken, token2 );

        // making the second call using, invalid refresh token

    }

    @Test( expected = OAuthManager.TokenInvalidException.class)
    public void getSavedToken() throws Exception{

        server = new MockWebServer();
        server.enqueue(
                new MockResponse().setResponseCode(401)
                        .setBody("{\"timestamp\":1478627607365," +
                                "\"status\":401," +
                                "\"error\":\"Unauthorized\"," +
                                "\"message\":\"Full authentication is required to access this resource\"," +
                                "\"path\":\"/oauth/token\"}")
        );
        oAuthManager = new OAuthManager( preferences , getClient(), gson );

        Mockito.when( preferences.getString( OAuthManager.KEY_TOKEN, null))
                .thenReturn( "token" );
        Calendar cal = Calendar.getInstance();
        long time = cal.getTime().getTime();
        System.out.println("Current Time: " + time);

        // valid token saved
        long validTime = time + ((long)10000);
        Mockito.when( preferences.getLong( OAuthManager.KEY_VALID_UNTIL, 0 ))
                .thenReturn(validTime);
        String token = oAuthManager.getSavedToken();
        assertNotNull(token);
        assertEquals( token, "token");

        // invalid token
        long invalidTime = time - ((long) 10000 );
        Mockito.when( preferences.getLong( OAuthManager.KEY_VALID_UNTIL, 0))
                .thenReturn(invalidTime);
        String tokenNull = oAuthManager.getSavedToken();
        assertNull("token:" + tokenNull, tokenNull );

    }
}