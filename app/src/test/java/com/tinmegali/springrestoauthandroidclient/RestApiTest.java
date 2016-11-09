package com.tinmegali.springrestoauthandroidclient;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.tinmegali.springrestoauthandroidclient.api.ApiController;
import com.tinmegali.springrestoauthandroidclient.api.OAuthManager;
import com.tinmegali.springrestoauthandroidclient.mocks.MockClient;
import com.tinmegali.springrestoauthandroidclient.models.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
public class RestApiTest {

    private ApiController api;
    private OAuthManager oAuthManager;
    private MockWebServer server;

    SharedPreferences preferences;
    OkHttpClient client;

    SharedPreferences.Editor editor;
    Gson gson;

    @Before
    public void preSetup() throws Exception {

        preferences = Mockito.mock( SharedPreferences.class);
        editor = Mockito.mock( SharedPreferences.Editor.class );
        gson = new Gson();

        Mockito.when( editor.commit() ).thenReturn(true);
        Mockito.when( editor.putString( Matchers.any(String.class ), Matchers.any(String.class ))).thenReturn( editor );
        Mockito.when( editor.putLong( Matchers.any(String.class ), Matchers.any(Long.class ))).thenReturn( editor );
        Mockito.when( editor.putInt( Matchers.any(String.class ), Matchers.any(Integer.class ))).thenReturn( editor );
        Mockito.when( preferences.edit() ).thenReturn( editor );

        oAuthManager = Mockito.mock( OAuthManager.class );
        Mockito.when( oAuthManager.getNewToken() )
                .thenReturn( MockClient.token  );
        Mockito.when( oAuthManager.getRefreshToken( Matchers.anyString() ))
                .thenReturn( MockClient.refresh_token );
        Mockito.when( oAuthManager.getSavedToken() )
                .thenReturn( MockClient.token );
        Mockito.when( oAuthManager.getNewToken() )
                .thenReturn( MockClient.token );
        Mockito.when( oAuthManager.getValidToken() )
                .thenReturn( MockClient.token );
    }

    private OkHttpClient getClient() {
        return client = new OkHttpClient.Builder()
                .proxy(
                        new Proxy(
                                Proxy.Type.HTTP,
                                new InetSocketAddress(server.getHostName(), server.getPort())
                        )
                ).build();
    }

    private void setup() throws Exception {
        getClient();
//        oAuthManager = new OAuthManager( preferences, client, gson );

        api = new ApiController( oAuthManager, client );
    }


    @Test
    public void testSetup() throws Exception{
        server = new MockWebServer();
        server.start();

        setup();
        assertNotNull( api );
    }

    @Test
    public void getUsers() throws Exception {
        // get all users

        server = new MockWebServer();

        // first call user list
        server.enqueue(
                MockClient.HTTP_RESPONSES.USER_LIST
        );
        server.start();

        setup();

        List<User> userList = api.getUsers();
        assertNotNull( userList );
        assertEquals( 4, userList.size() );
    }

    @Test
    public void addUser() throws Exception {
        server = new MockWebServer();

        String responseUser = "{\"id\":1,\"name\":\"Sam\",\"age\":30,\"salary\":70000.0}";
        server.enqueue(
                new MockResponse().setBody(
                        responseUser
                )
                .setResponseCode(200)
        );
        server.start();
        setup();


        User user = new User();
        user.setSalary(30);
        user.setAge(20);
        user.setName("Name");

        User savedUser = api.addUser( user );
        assertNotNull( savedUser );
    }
}
