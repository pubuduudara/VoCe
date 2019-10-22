import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


class RecordPlayback {
    private int packetSize = 64;
    private TargetDataLine targetDataLine;
    private SourceDataLine sourceDataLine;
    private byte[] tempBuffer = new byte[packetSize - 4];


    private AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        int sampleSizeInBits = 16;
        int channels = 2;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, true, true);
    }

    RecordPlayback() {
        try {

            Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
            System.out.println("Available mixers:");

            Mixer mixer = null;
            for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
                System.out.println(cnt + " " + mixerInfo[cnt].getName());
                mixer = AudioSystem.getMixer(mixerInfo[cnt]);

                Line.Info[] lineInfos = mixer.getTargetLineInfo();
                if (lineInfos.length >= 1 && lineInfos[0].getLineClass().equals(TargetDataLine.class)) {
                    System.out.println(cnt + " Mic is supported!");
                    break;
                }
            }

            AudioFormat audioFormat = getAudioFormat();     //get the audio format
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

            assert mixer != null;
            targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
            targetDataLine.open(audioFormat);
            targetDataLine.start();

            DataLine.Info dataLineInfo1 = new DataLine.Info(SourceDataLine.class, audioFormat);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo1);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();

            FloatControl control = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
            control.setValue(control.getMaximum());

        } catch (LineUnavailableException e) {
            e.printStackTrace();
            System.exit(0);
        }

    }


    byte[] captureAudio() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        boolean stopCapture = false;
        try {
            int readCount;

            readCount = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
            if (readCount > 0) {
                byteArrayOutputStream.write(tempBuffer, 0, readCount);

            }

            byteArrayOutputStream.close();
            return tempBuffer;
        } catch (IOException e) {
            e.printStackTrace();
            return tempBuffer;
        }
    }

    void playAudio(byte[] tempBuffer) {
        sourceDataLine.write(tempBuffer, 0, tempBuffer.length);
    }

}


