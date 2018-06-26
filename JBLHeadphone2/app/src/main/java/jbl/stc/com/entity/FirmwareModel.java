package jbl.stc.com.entity;

import java.io.File;

import jbl.stc.com.data.FwTYPE;

public class FirmwareModel {

    private File file;
    private String mURL;
    private String version;
    private String name;
    private FwTYPE fwtype;
    private boolean success;

    public FwTYPE getFwtype() {
        return fwtype;
    }

    /**
     * Sets Firmware type
     *
     * @param fwtype
     */
    public void setFwtype(FwTYPE fwtype) {
        this.fwtype = fwtype;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if (name.equalsIgnoreCase("app"))
            setFwtype(FwTYPE.APP);
        else if (name.equalsIgnoreCase("rsrc"))
            setFwtype(FwTYPE.RSRC);
        else if (name.equalsIgnoreCase("params")){
            setFwtype(FwTYPE.PARAM);
        }else if (name.equalsIgnoreCase("firmware")){
            setFwtype(FwTYPE.FIRMWARE);
        }else if (name.equalsIgnoreCase("data")){
            setFwtype(FwTYPE.DATA);
        }

    }

    public String getmURL() {
        return mURL;
    }

    public void setmURL(String mURL) {
        this.mURL = mURL;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
