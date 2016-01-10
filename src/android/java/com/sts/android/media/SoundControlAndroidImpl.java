package com.sts.android.media;

import android.media.MediaRecorder;
import com.sts.model.MicrophoneModel;
import com.sts.model.MicrophoneModel;
import java.util.ArrayList;
import java.util.List;
import com.sts.media.SoundControl;
import com.sts.media.SoundControl;
import com.sts.media.SoundControlObserver;
import com.sts.media.SoundControlObserver;

/**
 *
 * @author Ivan Queiroz <ivanqueiroz@gmail.com>
 */
public class SoundControlAndroidImpl implements SoundControl{
    
     private MediaRecorder mRecorder = null;

    @Override
    public List<MicrophoneModel> listAllMics() {
        mRecorder = new MediaRecorder();
        return new ArrayList<>();
    }

    @Override
    public void setSelectedMic(MicrophoneModel selectedMic) {
        
    }

    @Override
    public float getMicVolume() {
        return 0;
    }

    @Override
    public void setMicVolume(float value) {
        
    }

    @Override
    public void captureAudio() {
        
    }

    @Override
    public void stopCapture() {
        
    }

    @Override
    public void addObserver(SoundControlObserver observer) {
        
    }

    @Override
    public void removeObserver(SoundControlObserver observer) {
        
    }
    
}
