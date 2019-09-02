import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, LineUnavailableException {

        //RecordPlayback playback = new RecordPlayback();
        //playback.captureAudio();

        Receiver receiver = new Receiver();
        receiver.receive();
    }

}
