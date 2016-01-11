package com.sts.pc.model;

import com.sts.model.MicrophoneModel;
import javax.sound.sampled.Port;


public class MicrophoneDesktopModel extends MicrophoneModel {

    private Port micPort;

    public Port getMicPort() {
        return micPort;
    }

    public void setMicPort(Port micPort) {
        this.micPort = micPort;
    }
    
    

}
