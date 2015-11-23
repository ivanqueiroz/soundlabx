package com.micrecorderfx.media;

import static java.lang.String.format;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;

/**
 *
 * @author Ivan Queiroz <ivanqueiroz@gmail.com>
 */
public class MicControlService {
    
    private static MicControlService instance;
    
    private final ServiceLoader<MicControl> serviceLoader;
    
    private MicControl micControl;
    
    private static final Logger LOG = Logger.getLogger(MicControlService.class.getName());
    
    private MicControlService() {
        serviceLoader = ServiceLoader.load(MicControl.class);
        
        Iterator<MicControl> iterator = serviceLoader.iterator();
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
    
    public static synchronized MicControlService getInstance() {
        if (instance == null) {
            instance = new MicControlService();
        }
        return instance;
    }
    
    public List<Microphone> listAllMics() {
        return micControl == null ? new ArrayList<>() : micControl.listAllMics();
    }
    
    public void setSelectedMic(Microphone selectedMic) {
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
    
    public void addObserver(MicControlObserver observer){
        if (micControl != null) {
            micControl.addObserver(observer);
        }
    }
    
    public void setMicVolume(float value){
        if (micControl != null) {
            micControl.setMicVolume(value);
        }
    }
    
    public float gettMicVolume(){
        if (micControl != null) {
            return micControl.getMicVolume();
        }
        
        return 0;
    }
}
