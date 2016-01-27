package com.sts.media;

import com.sts.model.MicrophoneModel;
import java.util.List;

public interface SoundControl {

    public List<MicrophoneModel> listAllMics();

    public void setSelectedMic(MicrophoneModel selectedMic);

    public float getMicVolume();

    public void setMicVolume(float value);

    public void captureAudio();

    public void stopCapture();

    public void addObserver(SoundControlObserver observer);

    public void removeObserver(SoundControlObserver observer);
}
