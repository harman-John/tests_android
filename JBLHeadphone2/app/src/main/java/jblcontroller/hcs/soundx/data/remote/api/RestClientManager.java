package jblcontroller.hcs.soundx.data.remote.api;


import com.avnera.smartdigitalheadset.Log;

import org.json.JSONObject;

import java.util.List;

import jbl.stc.com.activity.JBLApplication;
import jblcontroller.hcs.soundx.data.local.SoundXSharedPreferences;
import jblcontroller.hcs.soundx.data.remote.model.GetAudioProfile;
import jblcontroller.hcs.soundx.data.remote.model.ProfileRequest;
import jblcontroller.hcs.soundx.data.remote.model.SignupRequest;
import jblcontroller.hcs.soundx.utils.SoundXUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestClientManager {

    private ResponseListener mResponseListener;
    private ApiService apiService;

    public RestClientManager(ResponseListener listener) {
        mResponseListener = listener;
        apiService = NoAuthAPIClient.getClient(JBLApplication.getJBLApplicationContext()).create(ApiService.class);
    }

    /**
     * This method will used to login the user.
     */
    public void login(String username, String password) {
        apiService.loginUser("Basic "+SoundXUtils.getBase64(username, password)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 200) {
                    //TODO - save x-auth_token
                    mResponseListener.onSuccess(response);
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        mResponseListener.onFailure(jObjError.getString("message"));
                    } catch (Exception e) {
                        mResponseListener.onFailure("Something went wrong, Please try again");
                    }
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                mResponseListener.onFailure("Something went wrong, Please try again");
            }
        });
    }

    /**
     * This method used  for user sign up
     *
     * @param data - Signup request
     */
    public void signUp(SignupRequest data) {
        apiService.createUserAccount(data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 200) {
                    mResponseListener.onSuccess(response);
                }
                else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        mResponseListener.onFailure(jObjError.getString("message"));
                    } catch (Exception e) {
                        mResponseListener.onFailure("Something went wrong, Please try again");
                    }
                }

                /*else if (response.code() == 500) {
                    mResponseListener.onFailure("Email already exists");
                } else {
                    mResponseListener.onFailure("Something went wrong");
                }
*/            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                mResponseListener.onFailure("Something went wrong, Please try again");
            }
        });
    }

    /**
     * This method used  for Creating Audio Profile
     *
     * @param data - Set Audio Profile request
     */
    public void createAudioProfile(ProfileRequest data) {
        apiService.createAudioProfile(SoundXSharedPreferences.getAuthToken(JBLApplication.getJBLApplicationContext()), data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 200) {
                    Log.e("Audio Profile Updated");
                    mResponseListener.onSuccess(response);
                }
                else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        mResponseListener.onFailure(jObjError.getString("message"));
                    } catch (Exception e) {
                        mResponseListener.onFailure("Something went wrong, Please try again");
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                mResponseListener.onFailure("Something went wrong, Please try again");
            }
        });
    }

    /**
     * This method is to get User Audio profile
     */
    public void getUserAudioProfile() {
        apiService.getAudioProfile(SoundXSharedPreferences.getAuthToken(JBLApplication.getJBLApplicationContext())).enqueue(new Callback<List<GetAudioProfile>>() {
            @Override
            public void onResponse(Call<List<GetAudioProfile>> call, Response<List<GetAudioProfile>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().size() > 0) {
                        List<GetAudioProfile> audioProfile = response.body();
                        mResponseListener.onSuccess(audioProfile);
                    } else
                        mResponseListener.onFailure("No Audio profile found.");
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        mResponseListener.onFailure(jObjError.getString("message"));
                    } catch (Exception e) {
                        mResponseListener.onFailure("Something went wrong, Please try again");
                    }
                }
            }
            @Override
            public void onFailure(Call<List<GetAudioProfile>> call, Throwable t) {
                mResponseListener.onFailure("Something went wrong, Please try again");
            }
        });
    }
}
