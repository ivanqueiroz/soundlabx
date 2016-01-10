package com.sts.android.model;

import com.sts.model.MicrophoneModel;
import javax.sound.sampled.Port;

/**
 *
 * @author Ivan Queiroz <ivanqueiroz@gmail.com>
 */
public class MicrofoneAndroidModel extends MicrophoneModel {

    private Port micPort;

    public Port getMicPort() {
        return micPort;
    }

    public void setMicPort(Port micPort) {
        this.micPort = micPort;
    }

}
