import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Multicast extends Thread {

    enum STATUS {RECV, REC_SEND, PLAY}

    private STATUS state;

    private static InetAddress clientAddress = null;
    private static int clientPort = -1;
    private static DatagramSocket uplinkSocket = null;
    private static MulticastSocket multicastSocket = null;
    private static RecordPlayback recordPlayback = new RecordPlayback();
    private static PacketNumbering serial = new PacketNumbering();

    private Multicast(STATUS state) {
        this.state = state;
    }

    public static void main(String[] args) throws IOException {
        recordPlayback = new RecordPlayback();
        //System.out.println("Threshold " + Serialization.threshold);

        Scanner sc = new Scanner(System.in);

        System.out.println("Enter group's multicast IP address: ");
        InetAddress server_address = InetAddress.getByName(sc.next());

        uplinkSocket = new DatagramSocket();
        clientAddress = server_address;
        clientPort = 8888;
        try {
            //Prepare to join multicast group
            multicastSocket = new MulticastSocket(8888);
            multicastSocket.joinGroup(server_address);

        } catch (Exception e) {
            e.printStackTrace();
        }


        Thread recv = new Thread(new Multicast(STATUS.RECV));
        Thread rec_send = new Thread(new Multicast(STATUS.REC_SEND));
        Thread play = new Thread(new Multicast(STATUS.PLAY));

        recv.start();
        rec_send.start();
        play.start();


    }

    public void run() {
        if (state == STATUS.RECV) { //receiving
            while (true) {
                try {
                    // Prepare the packet for receive
                    int packetSize = 64;
                    DatagramPacket packet = new DatagramPacket(new byte[packetSize], packetSize);
                    // Wait for a response from the other peer
                    multicastSocket.receive(packet);
                    serial.removeNumber(packet.getData());

                } catch (Exception e) {
                    System.out.println("Receiving error");
                    e.printStackTrace();
                    break;
                }

            }

        } else if (state == STATUS.REC_SEND) {
            while (true) {

                byte[] data = recordPlayback.captureAudio();
                byte[] temp_data = serial.addNumbers(data);

                try {

                    DatagramPacket packet = new DatagramPacket(temp_data, temp_data.length, clientAddress, clientPort);

                    uplinkSocket.send(packet);
                } catch (Exception e) {
                    System.out.println("sending error");
                    e.printStackTrace();
                    break;
                }


            }
        } else if (state == STATUS.PLAY) {

            while (true) {

                byte[] temp = serial.getPacket();
                recordPlayback.playAudio(temp);

            }
        }
    }


}
