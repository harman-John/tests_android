package jblcontroller.hcs.soundx.data.remote.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProfileRequest {

    @SerializedName("bass")
    @Expose
    private String bass;
    @SerializedName("treble")
    @Expose
    private String treble;
    @SerializedName("deviceType")
    @Expose
    private String[] deviceType;
    @SerializedName("listningExp")
    @Expose
    private String listningExp;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("yearOfBirth")
    @Expose
    private String yearOfBirth;

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

    public String[] getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String[] deviceType) {
        this.deviceType = deviceType;
    }

    public String getListningExp() {
        return listningExp;
    }

    public void setListningExp(String listningExp) {
        this.listningExp = listningExp;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(String yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

}


