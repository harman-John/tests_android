package jblcontroller.hcs.soundx.data.remote.model;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetAudioProfile {

    @SerializedName("id")
    @Expose
    private String id;
    //@SerializedName("deviceType")
    //@Expose
    //private String deviceType;
    @SerializedName("bass")
    @Expose
    private String bass;
    @SerializedName("treble")
    @Expose
    private String treble;
    @SerializedName("createdTime")
    @Expose
    private String createdTime;
    @SerializedName("userId")
    @Expose
    private String userId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

//    public String getDeviceType() {
//        return deviceType;
//    }
//
//    public void setDeviceType(String deviceType) {
//        this.deviceType = deviceType;
//    }

    public String getBass() {
        return bass;
    }

    public void setBass(String bass) {
        this.bass = bass;
    }

    public String getTreble() {
        return treble;
    }

    public void setTreble(String treble) {
        this.treble = treble;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
