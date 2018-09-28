package jblcontroller.hcs.soundx.data.remote.api;

import android.util.Log;

import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;

public class BasicAuthInterceptor implements Interceptor {

    private String credentials;

    public BasicAuthInterceptor(String user, String password) {
        this.credentials = Credentials.basic(user, password);
        Log.d("SARDAR","constructor :: "+ user + "   "+ password);
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request authenticatedRequest = request.newBuilder()
                .header("Authorization", credentials).build();
        Log.d("SARDAR","intercept :: ");
        return chain.proceed(authenticatedRequest);
    }

}