package com.tinmegali.springrestoauthandroidclient.mocks;

import com.tinmegali.springrestoauthandroidclient.api.ServerDetails;

import okhttp3.mockwebserver.MockResponse;

/**
 * com.tinmegali.springrestoauthandroidclient.mocks | SpringRestOauthAndroidClient
 * __________________________________
 * Created by tinmegali
 * 08/11/16
 *
 * @see <a href="http://www.tinmegali.com">tinmegali.com</a>
 * @see <a href="http://github.com/tinmegali">github</a>
 * ___________________________________
 */

public class MockClient  {

    public static final String token = "token";
    public static final String refresh_token = "refresh_token";

    public static class URL {
        public static final String TOKEN =
                ServerDetails.URL.TOKEN + "grant_type=password&" +
                "username=bill&" + "password=abc123";
        public static final String TOKEN_REFRESH =
                ServerDetails.URL.TOKEN +  "grant_type=refresh_token&" +
                        "refresh_token=" + refresh_token;
        public static final String USER_LIST =
                ServerDetails.URL.USER + "/?access_token=" + token;
    }


    public static class HTTP_BODY {
        public static final String TOKEN =
                "{\"access_token\":\""+token+"\"," +
                        "\"token_type\":\"bearer\"," +
                        "\"refresh_token\":\""+refresh_token+"\"," +
                        "\"expires_in\":2," +
                        "\"scope\":\"write read trust\"}";
        public static final String USER_LIST =
                "[{\"id\":1,\"name\":\"Sam\",\"age\":30,\"salary\":70000.0}," +
                        "{\"id\":2,\"name\":\"Tom\",\"age\":40,\"salary\":50000.0}," +
                        "{\"id\":3,\"name\":\"Jerome\",\"age\":45,\"salary\":30000.0}," +
                        "{\"id\":4,\"name\":\"Silvia\",\"age\":50,\"salary\":40000.0}]";
    }

    public static class HTTP_RESPONSES {
        public static final MockResponse TOKEN_NEW =
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(HTTP_BODY.TOKEN);

        public static final MockResponse USER_LIST =
                new MockResponse().setResponseCode(200).setBody( HTTP_BODY.USER_LIST );
    }

}
