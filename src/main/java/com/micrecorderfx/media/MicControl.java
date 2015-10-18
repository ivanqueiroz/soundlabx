package com.micrecorderfx.media;

import java.util.List;

/**
 *
 * @author Ivan Queiroz <ivanqueiroz@gmail.com>
 */
public interface MicControl {
    
    public List<Microphone> listAllMics();
    public void setSelectedMic(Microphone selectedMic);
    public void captureAudio();
    public void stopCapture();
    public void addObserver(MicControlObserver observer);
    public void removeObserver(MicControlObserver observer);
}
