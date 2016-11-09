package com.tinmegali.springrestoauthandroidclient.api;

import com.tinmegali.springrestoauthandroidclient.models.User;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

/**
 * Rest API endpoint
 */
public interface ApiEndpointInterface {

    @GET("/user/{id}/?")
    Call<User> getUser(@Path("id") String id,
                       @QueryMap Map<String, String> token);

    @GET("/user/?")
    Call<List<User>> getUsers(@QueryMap Map<String, String> token);

    @POST("/user/?")
    Call<User> createUser(@QueryMap Map<String, String> token,
                          @Body User user);

    @PUT("/user/{id}/?")
    Call<User> updateUser(@Path("id") Integer id,
                          @QueryMap Map<String, String> token,
                          @Body User user);

    @DELETE("/user/{id}/?")
    Call<ResponseBody> deleteUser(@Path("id") String id,
                                  @QueryMap Map<String, String> token);

    @DELETE("/user/?")
    Call<ResponseBody> deleteAllUsers(@QueryMap Map<String, String> token);

}
