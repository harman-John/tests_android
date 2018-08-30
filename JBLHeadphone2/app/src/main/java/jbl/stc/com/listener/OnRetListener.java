package jbl.stc.com.listener;


import jbl.stc.com.utils.EnumCommands;

public interface OnRetListener {
    void onReceive(EnumCommands enumCommands, Object... objects);
}
