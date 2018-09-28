package jblcontroller.hcs.soundx.data.remote.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("firstName")
    @Expose
    private String firstName;
    @SerializedName("lastName")
    @Expose
    private String lastName;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("age")
    @Expose
    private String age;
    @SerializedName("deviceType")
    @Expose
    private String deviceType;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("mobile")
    @Expose
    private String mobile;
    @SerializedName("bass")
    @Expose
    private String bass;
    @SerializedName("treble")
    @Expose
    private String treble;



    @SerializedName("password")
    @Expose
    private String password;

    public User(String firstName, String lastName, String email, String age, String deviceType, String gender,
                String mobile, String bass, String treble) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.age = age;
        this.deviceType = deviceType;
        this.gender = gender;
        this.mobile = mobile;
        this.bass = bass;
        this.treble = treble;
    }
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

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
}
