package com.micrecorderfx.dsk.media;

import com.micrecorderfx.media.AudioFormatEnum;
import com.micrecorderfx.media.MicControl;
import com.micrecorderfx.media.MicControlObserver;
import com.micrecorderfx.media.Microphone;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
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
    private List<MicControlObserver> observers = new ArrayList<>();
    private double volValue;

    private HashMap<String, Line.Info> enumerateMicrophones() {
        HashMap<String, Line.Info> out = new HashMap<>();
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            Mixer m = AudioSystem.getMixer(info);
            Line.Info[] lineInfos = m.getTargetLineInfo();
            if (lineInfos.length >= 1 && lineInfos[0].getLineClass().equals(TargetDataLine.class)) {
                out.put(info.getName(), lineInfos[0]);
            }
        }
        return out;
    }

    private TargetDataLine getTargetDataLine(Line.Info lineInfo) {
        try {
            System.out.println("Chamado getTargetDataLine()");
            return (TargetDataLine) AudioSystem.getLine(lineInfo);
        } catch (LineUnavailableException ex) {
            System.err.println("Erro: " + ex);
            return null;
        }
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
        System.out.println("Chamou setSelectedMic()");
        if (selectedMic != null) {
            System.out.println("Chamou setSelectedMic()"+selectedMic.getName());
            final Line.Info info = enumerateMicrophones().get(selectedMic.getName());
            this.line = getTargetDataLine(info);
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
        AudioFileFormat.Type fileType = null;

        stopped = false;

        if (!AudioSystem.isLineSupported(line.getLineInfo())) {
            System.out.println("Line not supported");
            System.exit(0);

        }

        System.out.println("Selecao de formato " + selectedMic);
        if (selectedMic.getFormat() == AudioFormatEnum.AIFC) {
            System.out.println("Selecao de AIFC");
            fileType = AudioFileFormat.Type.AIFC;
        } else if (selectedMic.getFormat() == AudioFormatEnum.AIFF) {
            System.out.println("Selecao de AIFF");
            fileType = AudioFileFormat.Type.AIFF;
        } else if (selectedMic.getFormat() == AudioFormatEnum.AU) {
            System.out.println("Selecao de AU");
            fileType = AudioFileFormat.Type.AU;
        } else if (selectedMic.getFormat() == AudioFormatEnum.SND) {
            System.out.println("Selecao de SND");
            fileType = AudioFileFormat.Type.SND;
        } else if (selectedMic.getFormat() == AudioFormatEnum.WAVE) {
            fileType = AudioFileFormat.Type.WAVE;
            System.out.println("Selecao de WAVE");
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
}
