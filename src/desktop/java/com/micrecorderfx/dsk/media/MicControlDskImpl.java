package com.micrecorderfx.dsk.media;

import com.micrecorderfx.media.AudioFormatEnum;
import com.micrecorderfx.media.MicControl;
import com.micrecorderfx.media.Microphone;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
        File audioFile = null;

        stopped = false;

        if (!AudioSystem.isLineSupported(line.getLineInfo())) {
            System.out.println("Line not supported");
            System.exit(0);

        }

        System.out.println("Selecao de formato " + selectedMic);
        if (selectedMic.getFormat() == AudioFormatEnum.AIFC) {
            System.out.println("Selecao de AIFC");
            fileType = AudioFileFormat.Type.AIFC;
            audioFile = new File("/Users/ivanqueiroz/junk.aifc");
        } else if (selectedMic.getFormat() == AudioFormatEnum.AIFF) {
            System.out.println("Selecao de AIFF");
            fileType = AudioFileFormat.Type.AIFF;
            audioFile = new File("/Users/ivanqueiroz/junk.aif");
        } else if (selectedMic.getFormat() == AudioFormatEnum.AU) {
            System.out.println("Selecao de AU");
            fileType = AudioFileFormat.Type.AU;
            audioFile = new File("/Users/ivanqueiroz/junk.au");
        } else if (selectedMic.getFormat() == AudioFormatEnum.SND) {
            System.out.println("Selecao de SND");
            fileType = AudioFileFormat.Type.SND;
            audioFile = new File("/Users/ivanqueiroz/junk.snd");
        } else if (selectedMic.getFormat() == AudioFormatEnum.WAVE) {
            fileType = AudioFileFormat.Type.WAVE;
            System.out.println("Selecao de WAVE");
            audioFile = new File("/Users/ivanqueiroz/junk.wav");
        }
        System.out.println("Iniciando captura");
        try {
            line.open(getAudioFormat());

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                int numBytesRead;

                byte[] data = new byte[line.getBufferSize() / 5];

                line.start();

                while (!stopped) {
                    numBytesRead = line.read(data, 0, data.length);
                    out.write(data, 0, numBytesRead);
                }
            }

            //AudioSystem.write(new AudioInputStream(line),fileType,audioFile);
        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }

    private void processSound(byte[] data) {
        double vol;
        double time = 2205;

        for (int x = 0; x < data.length; x++) {
            // printing the characters a cada 2205
            System.out.print((char) data[x] + "   ");
        }
        System.out.println("   ");

    }

    @Override
    public void stopCapture() {
        line.stop();
        stopped = true;
        line.close();
    }
}
