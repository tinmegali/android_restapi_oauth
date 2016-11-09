package com.tinmegali.springrestoauthandroidclient.api;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.tinmegali.springrestoauthandroidclient.api.exceptions.RestHttpException;
import com.tinmegali.springrestoauthandroidclient.api.exceptions.RestUnauthorizedException;
import com.tinmegali.springrestoauthandroidclient.models.ErrorHttp;
import com.tinmegali.springrestoauthandroidclient.models.ErrorUnauthorized;
import com.tinmegali.springrestoauthandroidclient.models.TokenResponse;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import javax.inject.Inject;

import java.io.IOException;
import java.util.Calendar;

/**
 * Manages access_token and refresh_token.
 * <p>
 * Important:
 * 1 -When on production, consider changing the 'access_token'
 * saving //Login. For the sake of simplicity we're saving on
 * SharedPreferences here.
 * 2 - The username/password //Logic adopted here is completely bogus.
 * Out focus is on the rest/oauth //Logic. The password should at
 * least be passed to server using cryptografy.
 */
public class OAuthManager {

    private final String TAG = OAuthManager.class.getSimpleName();

    private SharedPreferences preferences;

    public static final String KEY_TOKEN = "token";
    public static final String KEY_REFRESH = "refresh_token";
    public static final String KEY_REFRESH_UNTIL = "refresh_valid_until";
    public static final String KEY_TOKEN_TYPE = "token_type";
    public static final String KEY_TOKEN_SCOPE = "token_scope";
    public static final String KEY_VALID_UNTIL = "valid_until";

    private OkHttpClient client;
    private Gson gson;

    @Inject
    public OAuthManager(SharedPreferences preferences, OkHttpClient client, Gson gson) {
        this.preferences = preferences;
        this.client = client;
        this.gson = gson;
    }

    /**
     * Save a {@link TokenResponse} object on sharedPreferences.
     */
    private void saveToken(TokenResponse token) {
        //Log.d(TAG, "saveToken");

        Calendar cal = Calendar.getInstance();
        long expiresIn = (token.getExpiresIn() * 1000);
        long validUntil = cal.getTimeInMillis() + expiresIn;

        preferences.edit()
                .putString(KEY_TOKEN, token.getAccessToken())
                .putString(KEY_REFRESH, token.getRefreshToken())
                .putLong(KEY_VALID_UNTIL, validUntil)
                .putString(KEY_TOKEN_SCOPE, token.getScope())
                .putString(KEY_TOKEN_TYPE, token.getTokenType())
                .apply();
    }

    /**
     * Retrieves a 'access_token' saved on sharedPreferences.
     *
     * @return A valid 'access_token'
     * @throws TokenInvalidException The token is invalid and it should be refreshed.
     * @throws TokenNotSaved         The token wasn't saved.
     */
    public String getSavedToken() throws TokenInvalidException, TokenNotSaved {
        //Log.d(TAG, "getSavedToken");
        String token = preferences.getString(KEY_TOKEN, null);
        if (token != null) {
            long validUntil = preferences.getLong(KEY_VALID_UNTIL, 0);
            Calendar cal = Calendar.getInstance();
            if (cal.getTimeInMillis() < validUntil) {
                // still valid
                return token;
            } else {
                //Log.d(TAG, "getSavedToken : invalid");
                clearTokenOnPrefs();
                throw new TokenInvalidException(preferences.getString(KEY_REFRESH, null));
            }
        } else throw new TokenNotSaved();
    }

    /**
     * Clean {@link TokenResponse} data saved on sharedPreferences.
     */
    private void clearTokenOnPrefs() {
        //Log.d(TAG, "clearTokenOnPrefs");
        preferences.edit()
                .putString(KEY_TOKEN, null)
                .putLong(KEY_VALID_UNTIL, 0)
                .putString(KEY_TOKEN_SCOPE, null)
                .putString(KEY_TOKEN_TYPE, null)
                .apply();
    }

    /**
     * Refresh the 'access_token' using a 'refresh_token'.
     *
     * @return a valid 'access_token'
     * @throws TokenRefreshInvalid The 'refresh_token' isn't valid anymore.
     */
    public String refreshToken(String refreshToken)
            throws TokenRefreshInvalid, RestHttpException, RestUnauthorizedException {
        //Log.d(TAG, "refreshToken");
        try {
            return getRefreshToken(refreshToken);
        } catch (IOException e) {
            e.printStackTrace();
            throw new TokenRefreshInvalid();
        }
    }

    /**
     * Get a new 'access_token' from the server.
     */
    public String getNewToken() throws IOException, RestHttpException {
//        //Log.d(TAG, "getNewToken");

        RequestBody reqpost = RequestBody.create(null, new byte[0]);
        Request req = new Request.Builder()
                .url(ServerDetails.URL.TOKEN + "grant_type=password&" +
                        "username=bill&" +  // TODO get username
                        "password=abc123")  // TODO get user password
                .method("POST", reqpost)
                .build();

        Response response = client.newCall(req).execute();
        if (response.isSuccessful()) {
            TokenResponse token = gson.fromJson(
                    response.body().charStream(),
                    TokenResponse.class);
            response.close();
            saveToken(token);
            return token.getAccessToken();
        } else {
            ErrorHttp errorHttp = gson.fromJson(response.body().charStream(), ErrorHttp.class);
            response.close();
            throw new RestHttpException(errorHttp);
        }
    }

    /**
     * Refresh the 'access_token' using a valid 'refresh_token'
     */
    public String getRefreshToken(String refreshToken)
            throws IOException, RestHttpException, RestUnauthorizedException {
        //Log.d(TAG, "getRefreshToken");

        RequestBody reqpost = RequestBody.create(null, new byte[0]);
        Request req = new Request.Builder()
                .url(ServerDetails.URL.TOKEN +
                        "grant_type=refresh_token&" +
                        "refresh_token=" + refreshToken)
                .method("POST", reqpost)
                .build();

        Response response = client.newCall(req).execute();
        if (response.isSuccessful()) {
            TokenResponse token = gson.fromJson(
                    response.body().charStream(),
                    TokenResponse.class);

            //Log.d(TAG, "getRefreshToken : token:" + token.getAccessToken() );
            saveToken(token);
            response.close();
            return token.getAccessToken();
        } else {
            if ( response.code() == 400 ) {
                // refresh token invalid. get a new one
                response.close();
                return getNewToken();
            } else if ( response.code() >= 401 && response.code() <= 499 ) {
                ErrorUnauthorized errorHttp = gson.fromJson(response.body().charStream(), ErrorUnauthorized.class);
                response.close();
                throw new RestUnauthorizedException( errorHttp );
            }
            else {
                ErrorHttp errorHttp = gson.fromJson( response.body().charStream(), ErrorHttp.class );
                response.close();
                throw new RestHttpException( errorHttp );
            }
        }
    }

    /**
     * Gets a valid 'access_token'. It will try to get it from the sharedPreferences, refresh it
     * using a 'refresh_token' and at last, it will fetch a new 'access_token' from the server.
     *
     * @return A valid 'access_token'
     * @throws RestUnauthorizedException Unauthorized
     * @throws RestHttpException         Some http exception
     */
    public String getValidToken() throws RestUnauthorizedException, RestHttpException, IOException {
        //Log.d(TAG, "getValidToken");

        // check if has token saved
        try {
            return getSavedToken();
        } catch (TokenInvalidException e) {
            // Token not valid anymore
            //Log.d(TAG, "getValidToken : token not valid. try to refresh.");
            String refreshToken = preferences.getString(KEY_REFRESH, null);
            if (refreshToken != null)
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
            //Log.d(TAG, "getSavedToken : tokenNotSaved");
            // don't have any token saved.
            // getting a new one
            return getNewToken();
        }
    }

    /**
     * Invalid Token Exception
     */
    public static class TokenInvalidException extends Exception {
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
    public static class TokenNotSaved extends Exception {
        TokenNotSaved() {
            super();
        }
    }

    /**
     * The refresh token is invalid Exception
     */
    public static class TokenRefreshInvalid extends Exception {
        TokenRefreshInvalid() {
            super();
        }
    }

}
