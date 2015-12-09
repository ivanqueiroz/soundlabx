package com.micrecorderfx.dsk.media;

import com.micrecorderfx.media.Microphone;
import javax.sound.sampled.Port;

/**
 *
 * @author Ivan
 */
public class MicrofoneDskImpl extends Microphone {

    private Port micPort;

    public Port getMicPort() {
        return micPort;
    }

    public void setMicPort(Port micPort) {
        this.micPort = micPort;
    }
    
    

}
