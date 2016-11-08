package com.tinmegali.springrestoauthandroidclient.api;

/**
 * Holds details about the server
 */
public class ServerDetails {

    public final static String CLIENT   = "my_trusted_client";
    public final static String SECRET   = "secret";

    public static final String ROLE_CLIENT          = "ROLE_CLIENT";
    public static final String ROLE_TRUSTED_CLIENT  = "ROLE_TRUSTED_CLIENT";

    public static final String SCOPE_TRUST = "trust";
    public static final String SCOPE_WRITE = "write";
    public static final String SCOPE_READ  = "read";

    public static final String GRAND_TYPE_PASSWORD      = "password";
    public static final String GRAND_TYPE_AUTH_CODE     = "authorization_code";
    public static final String GRAND_TYPE_REFRESH_TOKEN = "refresh_token";
    public static final String GRAND_TYPE_IMPLICIT      = "implicit";

    public static class URL {
        public static final String DOMAIN   = "http://192.168.25.3:8080";
        public static final String TOKEN    = DOMAIN + "/oauth/token?";
        public static final String USER     = DOMAIN + "/user/?";
                //http://192.168.25.3:8080/oauth/token?grant_type=password&username=bill&password=abc123
    }

    public static String getTokenUrl( String username, String password ) {
        return URL.TOKEN + "grant_type=password&username="
                + username + "&password=" + password;
    }

    public static String getTokenRefreshUrl( String tokenRefresh ) {
        return URL.TOKEN + "grant_type=refresh_token&refresh_token=" + tokenRefresh;
    }

    public static String getUsersUrl( String token ) {
        return URL.USER + token;
    }

}
