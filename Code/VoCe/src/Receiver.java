import javax.sound.sampled.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Receiver {

    SourceDataLine sourceDataLine;
    AudioFormat audioFormat;

    private AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        int sampleSizeInBits = 16;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    public void receive() throws IOException, LineUnavailableException {
        audioFormat = getAudioFormat();     //get the audio format
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

        DataLine.Info dataLineInfo1 = new DataLine.Info(SourceDataLine.class, audioFormat);
        sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo1);
        sourceDataLine.open(audioFormat);
        sourceDataLine.start();


        // Step 1 : Create a socket to listen at port 1234
        DatagramSocket ds = new DatagramSocket(1234);
        byte[] receive = new byte[500];

        DatagramPacket DpReceive = null;
        while (true)
        {

            // Step 2 : create a DatgramPacket to receive the data.
            DpReceive = new DatagramPacket(receive, receive.length);

            // Step 3 : revieve the data in byte buffer.
            ds.receive(DpReceive);

            //System.out.println("Client:-" + data(receive));
            sourceDataLine.write(receive, 0, 500);   //playing audio available in tempBuffer

            // Clear the buffer after every message.
            receive = new byte[receive.length];
        }
    }


    // A utility method to convert the byte array
    // data into a string representation.
    public static StringBuilder data(byte[] a)
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }
}
