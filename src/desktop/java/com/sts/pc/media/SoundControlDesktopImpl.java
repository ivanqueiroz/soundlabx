package com.sts.pc.media;

import com.sts.media.SoundControl;
import com.sts.media.SoundControlObserver;
import com.sts.model.MicrophoneModel;
import com.sts.pc.model.MicrophoneDesktopModel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.CompoundControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author Ivan Queiroz <ivanqueiroz@gmail.com>
 */
public class SoundControlDesktopImpl implements SoundControl {

    private TargetDataLine targetDataLine;
    private MicrophoneModel selectedMic;
    private boolean stopped = true;
    private Port micPort = null;

    //11025/5 = 2205 samples em 200ms com samplesize de 16bits = 4410
    private static final int BUFFER_LENGTH = 4410;
    private final List<SoundControlObserver> observers = new ArrayList<>();
    private double voiceSample;
    private static final Logger LOG = Logger.getLogger(SoundControlDesktopImpl.class.getName());

    private HashMap<String, Mixer.Info> getMixerInfo() {
        HashMap<String, Mixer.Info> out = new HashMap<>();
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            out.put(info.getName(), info);
        }
        return out;
    }

    private Port getMicPort(String strMixerName) {
        Port portMic = null;
        StringBuilder chave = new StringBuilder("Port ");
        chave.append(strMixerName);
        if (chave.length() > 36) {
            chave = new StringBuilder(chave.substring(0, 36));
        }
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();

        for (Mixer.Info mixerInfo : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            int maxLines = mixer.getMaxLines(Port.Info.MICROPHONE);
            String nomePort = mixerInfo.getName();
            if (maxLines > 0 && chave.toString().equals(nomePort)) {

                try {
                    portMic = (Port) mixer.getLine(Port.Info.MICROPHONE);
                } catch (LineUnavailableException ex) {
                    LOG.log(Level.SEVERE, "ERROR WHEN GET MIC PORT", ex);
                }

            }
        }

        return portMic;
    }

    private TargetDataLine getTargetDataLine(String strMixerName) {
        TargetDataLine micLine = null;
        DataLine.Info info = new DataLine.Info(TargetDataLine.class,
                getAudioFormat());
        try {
            if (strMixerName != null) {
                Mixer.Info mixerInfo = getMixerInfo().get(strMixerName);
                if (mixerInfo == null) {
                    return null;
                }
                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                micLine = (TargetDataLine) mixer.getLine(info);
            } else {
                micLine = (TargetDataLine) AudioSystem.getLine(info);
            }
        } catch (Exception e) {
           LOG.log(Level.SEVERE, "ERROR WHEN GET TARGET DATA LINE", e);
        }

        return micLine;
    }

    @Override
    public List<MicrophoneModel> listAllMics() {
        List<MicrophoneModel> micsInfo = new ArrayList<>();
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        //Percorrendo lista de mixers do computador
        for (Mixer.Info info : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(info);
            Line.Info[] lineInfos = mixer.getTargetLineInfo();
            MicrophoneDesktopModel micInfo = new MicrophoneDesktopModel();
            if (lineInfos.length > 0 && lineInfos[0].getLineClass().equals(TargetDataLine.class)) {
                micInfo.setDescription(info.getDescription());
                micInfo.setName(info.getName());
                micsInfo.add(micInfo);
            }

        }

        return micsInfo;
    }

    @Override
    public void setSelectedMic(MicrophoneModel selectedMic) {
        System.out.println("Chamou setSelectedMic() ");
        if (selectedMic != null) {
            final Mixer.Info info = getMixerInfo().get(selectedMic.getName());
            System.out.println("Info: " + info.getName());
            this.targetDataLine = getTargetDataLine(info.getName());
            this.selectedMic = selectedMic;
            this.micPort = getMicPort(info.getName());
        }

    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 11025;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                channels, signed, bigEndian);
        return format;
    }

    @Override
    public void captureAudio() {
        stopped = false;

        if (!AudioSystem.isLineSupported(targetDataLine.getLineInfo())) {
            LOG.log(Level.SEVERE, "LINE NOT SUPPORTED");
            System.exit(1);

        }

        try {
            targetDataLine.open(getAudioFormat());

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                int numBytesRead;

                int nBufferSize = BUFFER_LENGTH;

                byte[] data = new byte[nBufferSize];

                targetDataLine.start();

                while (!stopped) {
                    numBytesRead = targetDataLine.read(data, 0, data.length);
                    out.write(data, 0, numBytesRead);
                    short[] shortBuffer = byteArrayToShortArray(data);
                    setVoiceSample(volProcess(shortBuffer));
                }
            }

        } catch (LineUnavailableException | IOException e) {
           LOG.log(Level.SEVERE, "ERROR WHEN CAPTURE AUDIO", e);
        }
    }

    private short[] byteArrayToShortArray(byte[] byteArray) {
        int size = byteArray.length;
        short[] shortArray = new short[size / 2];

        for (int i = 0; i < shortArray.length; i++) {
            ByteBuffer bb = ByteBuffer.allocate(2);
            bb.order(ByteOrder.BIG_ENDIAN);
            bb.put(byteArray[2 * i]);
            bb.put(byteArray[2 * i + 1]);
            shortArray[i] = bb.getShort(0);
        }

        return shortArray;
    }

    private double volProcess(short[] data) {
        int v;
        double vol = 0;

        for (int i = 0; i < data.length; i++) {
            v = data[i];
            vol += v * v;
        }
        vol = (Math.sqrt(vol / data.length) / 327.68);

        return vol;

    }

    @Override
    public void stopCapture() {
        targetDataLine.stop();
        stopped = true;
        targetDataLine.close();
    }

    public void setVoiceSample(double voiceSample) {
        this.voiceSample = voiceSample;
        notifyObservers();
    }

    @Override
    public void addObserver(SoundControlObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(SoundControlObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        observers.stream().forEach((observer) -> {
            observer.voiceSampleAsDouble(this.voiceSample);
        });
    }

    @Override
    public void setMicVolume(float value) {
        if (micPort != null && micPort.isOpen()) {
            try {
                setVolume(value);
            } catch (LineUnavailableException ex) {
                Logger.getLogger(SoundControlDesktopImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void setVolume(final float volume) throws LineUnavailableException {
        FloatControl volCtrl;
        this.micPort.open();
        final Control control = this.micPort.getControls()[0];
        if (control instanceof CompoundControl) {
            CompoundControl cc = (CompoundControl) this.micPort.getControls()[0];
            Control[] controls = cc.getMemberControls();
            for (Control c : controls) {
                if (c instanceof FloatControl) {
                    volCtrl = (FloatControl) c;
                    volCtrl.setValue((float) volume / 100);
                }
            }
        } else if (control instanceof FloatControl) {
            volCtrl = (FloatControl) this.micPort.getControls()[0];
            volCtrl.setValue((float) volume / 100);
        }

    }

    @Override
    public float getMicVolume() {

        FloatControl volCtrl;
        try {
            this.micPort.open();
        } catch (LineUnavailableException ex) {
            Logger.getLogger(SoundControlDesktopImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        final Control control = this.micPort.getControls()[0];
        if (control instanceof CompoundControl) {
            CompoundControl cc = (CompoundControl) this.micPort.getControls()[0];
            Control[] controls = cc.getMemberControls();
            for (Control c : controls) {
                if (c instanceof FloatControl) {
                    volCtrl = (FloatControl) c;
                    return volCtrl.getValue();
                }
            }
        } else if (control instanceof FloatControl) {
            volCtrl = (FloatControl) this.micPort.getControls()[0];
            return volCtrl.getValue();
        }
        return 0;
    }
}
