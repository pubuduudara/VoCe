import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Multicast extends Thread {

    enum STATE {RECV, REC_SEND, PLAY}

    private STATE state;

    private static InetAddress multicastAddress = null;
    private static int clientPort = -1;
    private static MulticastSocket multicastSocket = null;
    private static RecordPlayback recordPlayback = new RecordPlayback();
    private static PacketNumbering packetNumbering = new PacketNumbering();

    private Multicast(STATE state) {
        this.state = state;
    }

    public static void main(String[] args) throws IOException {
        recordPlayback = new RecordPlayback();
        //System.out.println("Threshold " + Serialization.threshold);

        String usage = "usage:  $java Multicast <Multicast IP address>\n";
        if (args.length == 1) {
            try {
                multicastAddress = InetAddress.getByName(args[0]);
            } catch (Exception e) {
                System.out.println("Invalid IP address\n" + usage);
            }

        } else {
            System.out.println("Invalid format\n" + usage);
        }

        clientPort = 8888;

        try {
            //Prepare to join multicast group
            multicastSocket = new MulticastSocket(8888);
            multicastSocket.joinGroup(multicastAddress);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Thread recv = new Thread(new Multicast(STATE.RECV));
        Thread rec_send = new Thread(new Multicast(STATE.REC_SEND));
        Thread play = new Thread(new Multicast(STATE.PLAY));

        recv.start();
        rec_send.start();
        play.start();


    }

    public void run() {
        if (state == STATE.RECV) { //receiving
            while (true) {
                try {
                    // Prepare the packet for receive
                    int packetSize = 64;
                    DatagramPacket packet = new DatagramPacket(new byte[packetSize], packetSize);
                    // Wait for a response from the other peer
                    multicastSocket.receive(packet);
                    packetNumbering.removeNumber(packet.getData());

                } catch (Exception e) {
                    System.out.println("Receiving error");
                    e.printStackTrace();
                    break;
                }

            }

        } else if (state == STATE.REC_SEND) {
            while (true) {

                byte[] data = recordPlayback.captureAudio();
                byte[] temp_data = packetNumbering.addNumbers(data);

                try {

                    DatagramPacket packet = new DatagramPacket(temp_data, temp_data.length, multicastAddress, clientPort);

                    multicastSocket.send(packet);
                } catch (Exception e) {
                    System.out.println("sending error");
                    e.printStackTrace();
                    break;
                }


            }
        } else if (state == STATE.PLAY) {

            while (true) {

                byte[] temp = packetNumbering.getPacket();
                recordPlayback.playAudio(temp);

            }
        }
    }


}
