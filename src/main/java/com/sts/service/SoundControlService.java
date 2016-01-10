package com.sts.service;

import com.sts.media.SoundControl;
import com.sts.media.SoundControlObserver;
import com.sts.model.MicrophoneModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import static java.lang.String.format;
import static java.lang.String.format;
import static java.lang.String.format;
import static java.lang.String.format;

/**
 *
 * @author Ivan Queiroz <ivanqueiroz@gmail.com>
 */
public class SoundControlService {
    
    private static SoundControlService instance;
    
    private final ServiceLoader<SoundControl> serviceLoader;
    
    private SoundControl micControl;
    
    private static final Logger LOG = Logger.getLogger(SoundControlService.class.getName());
    
    private SoundControlService() {
        serviceLoader = ServiceLoader.load(SoundControl.class);
        
        Iterator<SoundControl> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            if (micControl == null) {
                micControl = iterator.next();
                LOG.info(format("Using MicControl: %s", micControl.getClass().getName()));
            } else {
                LOG.info(format("This MicControl is ignored: %s", iterator.next().getClass().getName()));
            }
        }
        
        if (micControl == null) {
            LOG.severe("No MicControl implementation could be found!");
        }
    }
    
    public static synchronized SoundControlService getInstance() {
        if (instance == null) {
            instance = new SoundControlService();
        }
        return instance;
    }
    
    public List<MicrophoneModel> listAllMics() {
        return micControl == null ? new ArrayList<>() : micControl.listAllMics();
    }
    
    public void setSelectedMic(MicrophoneModel selectedMic) {
        if (micControl != null) {
            micControl.setSelectedMic(selectedMic);
        }
    }

    public void captureAudio(){
        if (micControl != null) {
            System.out.println("Chamado captureAudio na interface");
            micControl.captureAudio();
        }
    }

    public void stopCapture(){
        if (micControl != null) {
            micControl.stopCapture();
        }
    }
    
    public void addObserver(SoundControlObserver observer){
        if (micControl != null) {
            micControl.addObserver(observer);
        }
    }
    
    public void setMicVolume(float value){
        if (micControl != null) {
            micControl.setMicVolume(value);
        }
    }
    
    public float getMicVolume(){
        if (micControl != null) {
            return micControl.getMicVolume();
        }
        
        return 0;
    }
}
