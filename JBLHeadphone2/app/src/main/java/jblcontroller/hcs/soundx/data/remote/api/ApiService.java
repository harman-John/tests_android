package jblcontroller.hcs.soundx.data.remote.api;



import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import jblcontroller.hcs.soundx.data.remote.model.GetAudioProfile;
import jblcontroller.hcs.soundx.data.remote.model.ProfileRequest;
import jblcontroller.hcs.soundx.data.remote.model.SignupRequest;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    // Create User Account
    @Headers("Content-Type: application/json")
    @POST("audio-personalized/users/signup")
    Call<Void> createUserAccount(@Body SignupRequest user);

    //Login User
    @Headers("Content-Type: application/json")
    @POST("audio-personalized/users/login")
    Call<Void> loginUser(@Header("Authorization") String credentials);


    // Create Audio profile
    @Headers("Content-Type: application/json")
    @POST("audio-personalized/profile")
    Call<Void> createAudioProfile(@Header("x-auth-token") String token, @Body ProfileRequest user);


    // Get audio profile
    @GET("http://52.14.158.154:8080/audio-personalized/profile")
    Call<List<GetAudioProfile>> getAudioProfile(@Header("x-auth-token") String token);

}
