package com.micrecorderfx.and.media;

import android.media.MediaRecorder;
import com.micrecorderfx.media.MicControl;
import com.micrecorderfx.media.MicControlObserver;
import com.micrecorderfx.media.Microphone;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ivan Queiroz <ivanqueiroz@gmail.com>
 */
public class MicControlAndImpl implements MicControl{
    
     private MediaRecorder mRecorder = null;

    @Override
    public List<Microphone> listAllMics() {
        mRecorder = new MediaRecorder();
        return new ArrayList<>();
    }

    @Override
    public void setSelectedMic(Microphone selectedMic) {
        
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
    public void addObserver(MicControlObserver observer) {
        
    }

    @Override
    public void removeObserver(MicControlObserver observer) {
        
    }
    
}
