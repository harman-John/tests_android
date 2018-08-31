package jbl.stc.com.entity;

public class GlobalEqInfo {
    public boolean eqOn = false;
    public boolean hasEq = false;
    public int maxEqId = 0;

    @Override
    public String toString() {
        return "DeviceInfo{" +
                ", eqOn=" + eqOn +
                ", hasEq=" + hasEq +
                ", maxEqId=" + maxEqId +
                '}';
    }
}
