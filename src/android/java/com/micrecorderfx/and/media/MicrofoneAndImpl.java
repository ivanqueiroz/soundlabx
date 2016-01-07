package com.micrecorderfx.and.media;

import com.micrecorderfx.media.Microphone;
import javax.sound.sampled.Port;

/**
 *
 * @author Ivan Queiroz <ivanqueiroz@gmail.com>
 */
public class MicrofoneAndImpl extends Microphone {

    private Port micPort;

    public Port getMicPort() {
        return micPort;
    }

    public void setMicPort(Port micPort) {
        this.micPort = micPort;
    }

}
