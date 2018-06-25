package jbl.stc.com.listener;

/**
 * Created by intahmad on 7/6/2015.
 */
public interface OnHeadphoneconnectListener {

    /**
     *
     * @param isConnect
     * @param headphoneName pass null if isConnect=false
     */
    public void onHeadPhoneState(boolean isConnect, String headphoneName);
}
