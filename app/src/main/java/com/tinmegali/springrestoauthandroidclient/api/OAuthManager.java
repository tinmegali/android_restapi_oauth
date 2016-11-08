package com.tinmegali.springrestoauthandroidclient.api;

import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;
import com.tinmegali.springrestoauthandroidclient.api.errors.RestHttpException;
import com.tinmegali.springrestoauthandroidclient.api.errors.RestUnauthorizedException;
import com.tinmegali.springrestoauthandroidclient.models.ErrorHttp;
import com.tinmegali.springrestoauthandroidclient.models.ErrorUnauthorized;
import com.tinmegali.springrestoauthandroidclient.models.TokenResponse;
import com.tinmegali.springrestoauthandroidclient.models.User;
import com.tinmegali.springrestoauthandroidclient.security.TokenAuthenticator;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Manages access_token and refresh_token.
 *
 * Important:
 * 1 -When on production, consider changing the 'access_token'
 * saving login. For the sake of simplicity we're saving on
 * SharedPreferences here.
 * 2 - The username/password logic adopted here is completely bogus.
 * Out focus is on the rest/oauth logic. The password should at
 * least be passed to server using cryptografy.
 *
 */
public class OAuthManager {

    private final String TAG = OAuthManager.class.getSimpleName();

    private SharedPreferences preferences;

    private final String KEY_TOKEN          = "token";
    private final String KEY_REFRESH        = "refresh_token";
    private final String KEY_REFRESH_UNTIL = "refresh_valid_until";
    private final String KEY_TOKEN_TYPE     = "token_type";
    private final String KEY_TOKEN_SCOPE    = "token_scope";
    private final String KEY_VALID_UNTIL    = "valid_until";

    private ApiEndpointInterface api;

    private OkHttpClient client;

    @Inject
    public OAuthManager( SharedPreferences preferences ) {
        this.preferences = preferences;
        client = new OkHttpClient.Builder()
                .authenticator( new TokenAuthenticator() )
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl( ServerDetails.URL.DOMAIN )
                .addConverterFactory(GsonConverterFactory.create())
                .client( client )
                .build();
        api = retrofit.create( ApiEndpointInterface.class );
    }

    /**
     * Save a {@link TokenResponse} object on sharedPreferences.
     */
    private void saveToken( TokenResponse token ) {
        Log.d(TAG, "saveToken");

        Calendar cal = Calendar.getInstance();
        long expiresIn = (token.getExpiresIn() * 1000);
        long validUntil =  cal.getTime().getTime() + expiresIn;

        preferences.edit()
                .putString(KEY_TOKEN, token.getAccessToken() )
                .putString(KEY_REFRESH, token.getRefreshToken() )
                .putLong(KEY_VALID_UNTIL, validUntil )
                .putString(KEY_TOKEN_SCOPE, token.getScope())
                .putString(KEY_TOKEN_TYPE, token.getTokenType() )
                .apply();
    }

    /**
     * Saves
     * @param token
     */
    private void saveRefreshToken( TokenResponse token ) {
        Log.d(TAG, "saveRefreshToken");

        Calendar cal = Calendar.getInstance();
        long expiresIn = (token.getExpiresIn() * 1000);
        long validUntil =  cal.getTime().getTime() + expiresIn;

        preferences.edit()
                .putString(KEY_TOKEN, token.getAccessToken() )
                .putString(KEY_REFRESH, token.getRefreshToken() )
                .putLong(KEY_REFRESH_UNTIL, validUntil )
                .putLong(KEY_VALID_UNTIL, 60000 )
                .putString(KEY_TOKEN_SCOPE, token.getScope())
                .putString(KEY_TOKEN_TYPE, token.getTokenType() )
                .apply();
    }

    /**
     * Retrieves a 'access_token' saved on sharedPreferences.
     * @return  A valid 'access_token'
     * @throws TokenInvalidException    The token is invalid and it should be refreshed.
     * @throws TokenNotSaved            The token wasn't saved.
     */
    private String getSavedToken() throws TokenInvalidException, TokenNotSaved {
        Log.d(TAG, "getSavedToken");
        String token = preferences.getString(KEY_TOKEN, null);
        if ( token != null ) {
            long validUntil = preferences.getLong( KEY_VALID_UNTIL,0 );
            Calendar cal = Calendar.getInstance();
            if ( cal.getTime().getTime() < validUntil ) {
                // still valid
                return token;
            } else {
                Log.d(TAG, "getSavedToken : invalid");
                clearTokenOnPrefs();
                throw new TokenInvalidException( preferences.getString(KEY_REFRESH, null) );
            }
        } else throw new TokenNotSaved();
    }

    /**
     * Clean {@link TokenResponse} data saved on sharedPreferences.
     */
    private void clearTokenOnPrefs() {
        Log.d(TAG, "clearTokenOnPrefs");
        preferences.edit()
                .putString( KEY_TOKEN, null )
                .putLong(KEY_VALID_UNTIL, 0 )
                .putString(KEY_TOKEN_SCOPE, null )
                .putString(KEY_TOKEN_TYPE, null )
                .apply();
    }

    /**
     * Refresh the 'access_token' using a 'refresh_token'.
     * @return  a valid 'access_token'
     * @throws TokenRefreshInvalid  The 'refresh_token' isn't valid anymore.
     */
    private String refreshToken( String refreshToken ) throws TokenRefreshInvalid {
        Log.d(TAG, "refreshToken");
        try {
            return getRefreshToken( refreshToken );
        } catch (IOException e) {
            e.printStackTrace();
            throw new TokenRefreshInvalid();
        }
    }

    /**
     * Get a new 'access_token' from the server.
     */
    private String getNewToken() throws IOException {
        Log.d(TAG, "getNewToken");

        OkHttpClient client = new OkHttpClient.Builder()
                .build();
        String credentials = Credentials.basic(ServerDetails.CLIENT, ServerDetails.SECRET);
        RequestBody reqpost = RequestBody.create(null, new byte[0]);
        Request req = new Request.Builder()
                .addHeader("Authorization", credentials)
                .url(ServerDetails.URL.TOKEN + "grant_type=password&" +
                        "username=bill&" +  // TODO get username
                        "password=abc123")  // TODO get user password
                .method("POST", reqpost)
                .build();

        Gson gson = new Gson();

        TokenResponse token = gson.fromJson(
                client.newCall(req).execute().body().charStream(),
                TokenResponse.class);

        saveToken( token );
        Log.d(TAG, "getNewToken : token:" + token.getAccessToken() );
        return token.getAccessToken();
    }

    /**
     * Refresh the 'access_token' using a valid 'refresh_token'
     */
    private String getRefreshToken(String refreshToken) throws IOException {
        Log.d(TAG, "getRefreshToken");

        OkHttpClient client = new OkHttpClient.Builder()
                .build();
        String credentials = Credentials.basic(ServerDetails.CLIENT, ServerDetails.SECRET);
        RequestBody reqpost = RequestBody.create(null, new byte[0]);
        Request req = new Request.Builder()
                .addHeader("Authorization", credentials)
                .url(ServerDetails.URL.TOKEN +
                        "grant_type=refresh_token&" +
                        "refresh_token=" + refreshToken)
                .method("POST", reqpost)
                .build();

        Gson gson = new Gson();

        TokenResponse token = gson.fromJson(
                client.newCall(req).execute().body().charStream(),
                TokenResponse.class);

        saveToken( token );
        Log.d(TAG, "getRefreshToken : token:" + token.getAccessToken() );
        return token.getAccessToken();
    }

    /**
     * Gets a valid 'access_token'. It will try to get it from the sharedPreferences, refresh it
     * using a 'refresh_token' and at last, it will fetch a new 'access_token' from the server.
     *
     * @return  A valid 'access_token'
     * @throws RestUnauthorizedException    Unauthorized
     * @throws RestHttpException            Some http exception
     */
    public String getValidToken() throws RestUnauthorizedException, RestHttpException,  IOException {
        Log.d(TAG, "getValidToken");

        // check if has token saved
        try {
            return getSavedToken();
        } catch (TokenInvalidException e) {
            // Token not valid anymore
            Log.d(TAG, "getValidToken : token not valid. try to refresh.");
            String refreshToken = preferences.getString(KEY_REFRESH, null);
            if ( refreshToken != null )
                try {
                    return refreshToken(refreshToken);
                } catch (TokenRefreshInvalid tokenRefreshInvalid) {
                    // Refresh invalid or error catching.
                    // get a new token
                    return getNewToken();
                }
            else {
                // don't have a refresh. Get a new token
                return getNewToken();
            }

        } catch (TokenNotSaved tokenNotSaved) {
            Log.d(TAG, "getSavedToken : tokenNotSaved");
            // don't have any token saved.
            // getting a new one
            return getNewToken();
        }
    }

    /**
     * Invalid Token Exception
     */
    private static class TokenInvalidException extends Exception {
        String refreshToken;
        TokenInvalidException(String refreshToken) {
            super();
            this.refreshToken = refreshToken;
        }
        public String getRefreshToken() {
            return refreshToken;
        }
    }

    /**
     * No token saved on memory Exception
     */
    private static class TokenNotSaved extends Exception {
        TokenNotSaved() {
            super();
        }
    }

    /**
     * The refresh token is invalid Exception
     */
    private static class TokenRefreshInvalid extends Exception {
        TokenRefreshInvalid() {
            super();
        }
    }

}
