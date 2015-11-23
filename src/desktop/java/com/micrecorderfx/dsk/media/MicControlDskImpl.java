package com.micrecorderfx.dsk.media;

import com.micrecorderfx.dsk.util.OSUtils;
import com.micrecorderfx.media.MicControl;
import com.micrecorderfx.media.MicControlObserver;
import com.micrecorderfx.media.Microphone;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.CompoundControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;
import javax.sound.sampled.Line;
import javax.sound.sampled.Line.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author Ivan Queiroz <ivanqueiroz@gmail.com>
 */
public class MicControlDskImpl implements MicControl {

    private TargetDataLine line;
    private Microphone selectedMic;
    private boolean stopped = true;

    //11025/5 = 2205 samples em 200ms com samplesize de 16bits = 4410
    private static final int BUFFER_LENGTH = 4410;
    private final List<MicControlObserver> observers = new ArrayList<>();
    private double volValue;

    private HashMap<String, Mixer.Info> getMixerInfo() {
        HashMap<String, Mixer.Info> out = new HashMap<>();
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            out.put(info.getName(), info);

        }
        return out;
    }

    private TargetDataLine getTargetDataLine(String strMixerName) {
        TargetDataLine targetDataLine = null;
        DataLine.Info info = new DataLine.Info(TargetDataLine.class,
                getAudioFormat());
        try {
            if (strMixerName != null) {
                Mixer.Info mixerInfo = getMixerInfo().get(strMixerName);
                if (mixerInfo == null) {
                    return null;
                }
                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                targetDataLine = (TargetDataLine) mixer.getLine(info);
            } else {
                targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return targetDataLine;
    }

    @Override
    public List<Microphone> listAllMics() {
        List<Microphone> micsInfo = new ArrayList<>();
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            Mixer m = AudioSystem.getMixer(info);
            Line.Info[] lineInfos = m.getTargetLineInfo();
            if (lineInfos.length > 0 && lineInfos[0].getLineClass().equals(TargetDataLine.class)) {
                Microphone micInfo = new Microphone();
                micInfo.setDescription(info.getDescription());
                micInfo.setName(info.getName());
                micsInfo.add(micInfo);
            }
        }

        return micsInfo;
    }

    @Override
    public void setSelectedMic(Microphone selectedMic) {
        System.out.println("Chamou setSelectedMic() ");
        if (selectedMic != null) {
            System.out.println("Chamou setSelectedMic() " + selectedMic.getName());
            final Mixer.Info info = getMixerInfo().get(selectedMic.getName());
            System.out.println("Info: " + info.getName());
            this.line = getTargetDataLine(info.getName());
            this.selectedMic = selectedMic;
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
        System.out.println("Chamando captureAudio() da implementacao");
        stopped = false;

        if (!AudioSystem.isLineSupported(line.getLineInfo())) {
            System.out.println("Line not supported");
            System.exit(0);

        }

        System.out.println("Iniciando captura");
        try {
            line.open(getAudioFormat());

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                int numBytesRead;

                int nBufferSize = BUFFER_LENGTH;

                byte[] data = new byte[nBufferSize];

                line.start();

                while (!stopped) {
                    numBytesRead = line.read(data, 0, data.length);
                    out.write(data, 0, numBytesRead);
                    short[] shortBuffer = byteArrayToShortArray(data);
                    setVolValue(volProcess(shortBuffer));
                }
            }

        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
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
        line.stop();
        stopped = true;
        line.close();
    }

    public void setVolValue(double volValue) {
        this.volValue = volValue;
        notifyObservers();
    }

    @Override
    public void addObserver(MicControlObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(MicControlObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        observers.stream().forEach((observer) -> {
            observer.update(this.volValue);
        });
    }

    @Override
    public void setMicVolume(float value) {
        if (line != null && line.isOpen()) {
            if (OSUtils.isMac()) {
                setMasterVolumeOsx(value);
            }
            if (OSUtils.isWindows()) {
                try {
                    setVolume(value);
                } catch (LineUnavailableException ex) {
                    Logger.getLogger(MicControlDskImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } else {
            System.out.println("Line is not open!");
        }
    }

    public void setMasterVolumeOsx(float value) {
        String command = "set volume input volume " + value;
        try {
            ProcessBuilder pb = new ProcessBuilder("osascript", "-e", command);
            pb.directory(new File("/usr/bin"));
            System.out.println(command);
            StringBuffer output = new StringBuffer();
            Process p = pb.start();
            p.waitFor();

            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String lineStr;
            while ((lineStr = reader.readLine()) != null) {
                output.append(lineStr).append("\n");
            }
            System.out.println(output);
        } catch (IOException | InterruptedException e) {
            System.out.println(e);
        }
    }

    private void setVolume(final float volume) throws LineUnavailableException {
        javax.sound.sampled.Mixer.Info[] mixerList = AudioSystem.getMixerInfo();
        for (javax.sound.sampled.Mixer.Info mixerInfo : mixerList) {

            Mixer mixer = AudioSystem.getMixer(mixerInfo);

            if (mixer.isLineSupported(Port.Info.LINE_IN)) {
                setLineVolume(mixer.getLine(Port.Info.LINE_IN), volume);
            }
            if (mixer.isLineSupported(Port.Info.MICROPHONE)) {
                setLineVolume(mixer.getLine(Port.Info.MICROPHONE), volume);
            }

            Info[] infoList = mixer.getTargetLineInfo();
            for (Info info : infoList) {
                Line lineIn = mixer.getLine(info);

                if (!lineIn.getLineInfo().toString().startsWith("SPEAKER")) {
                    setLineVolume(lineIn, volume);
                }
            }
        }
    }

    private void setLineVolume(final Line line, final float volume) throws LineUnavailableException {
        try {
            if (line != null) {
                line.open();  // open line needed to access all controls
                Control[] controls = line.getControls();
                for (Control control : controls) {
                    if (control.getType() == FloatControl.Type.VOLUME
                            && control instanceof FloatControl) {
                        setControlVolume((FloatControl) control, volume);
                    } else if (control instanceof CompoundControl) {
                        setControlVolume(((CompoundControl) control).getMemberControls(), volume);
                    }
                }
            }
        } finally {
            if (line != null) {
                line.close();
            }
        }
    }

    private void setControlVolume(final Control[] memberControls, float volume) {
        // look for boolean controls to capture which control is selected
        int booleanIndex = -1;
        for (final Control control : memberControls) {
            if (control instanceof BooleanControl) {
                booleanIndex++;
                BooleanControl booleanControl = (BooleanControl) control;
                if (booleanControl.getValue()) {
                    // the nth boolean control maps to the nth volume control (hopefully!)
                    int index = -1;
                    for (final Control volumeControl : memberControls) {
                        if (volumeControl instanceof FloatControl && volumeControl.getType() == Type.VOLUME) {
                            index++;
                            if (index == booleanIndex) {
                                setControlVolume((FloatControl) volumeControl, volume);
                            }
                        }
                    }
                    return;
                }
            }
        }
    }

    private void setControlVolume(final FloatControl control, final float volume) {
        float min = control.getMinimum();
        float range = control.getMaximum() - min;
        float newValue = min + (volume * range);
        control.setValue(newValue);
    }

    @Override
    public float getMicVolume() {
        if (OSUtils.isMac()) {
            return getMicVolumeOsx();
        }
        if (OSUtils.isWindows()) {
            return 0;
        }

        return 0;
    }

    public float getMicVolumeOsx() {
        String command = "set ovol to input volume of (get volume settings)";
        try {
            ProcessBuilder pb = new ProcessBuilder("osascript", "-e", command);
            pb.directory(new File("/usr/bin"));
            System.out.println(command);
            StringBuilder output = new StringBuilder();
            Process p = pb.start();
            p.waitFor();

            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String lineStr;
            while ((lineStr = reader.readLine()) != null) {
                output.append(lineStr).append("\n");
            }
            return Float.valueOf(output.toString());
        } catch (IOException | InterruptedException e) {
            System.out.println(e);
        }
        return 0;
    }
}
