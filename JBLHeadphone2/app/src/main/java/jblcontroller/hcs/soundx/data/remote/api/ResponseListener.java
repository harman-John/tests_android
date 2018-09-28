package jblcontroller.hcs.soundx.data.remote.api;

public interface ResponseListener {
    void onSuccess(Object response);
    void onFailure(String message);
}
