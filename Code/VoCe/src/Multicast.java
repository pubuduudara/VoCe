import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/*
Multicast.java is written to facilitate communication between many-to-many
Compile:    $javac Multicast.java
Run:        $java Multicast <multicast IP address>

Multicast IP address range: 224.0.0.0 to 239.255.255.255
 */

public class Multicast extends Thread {


    enum STATE {RECV, REC_SEND, PLAY} //Holds 3 states of thread
    private STATE state;
    private static InetAddress multicastAddress = null;
    private static final int multicastPort = 8888, packetSize = 64;
    private static MulticastSocket multicastSocket = null;
    private static RecordPlayback recordPlayback = new RecordPlayback();
    private static PacketNumberingAndData packetNumberingAndData = new PacketNumberingAndData(); //Handles packet numbering and storing data

    private Multicast(STATE state) {
        this.state = state;
    }

    public static void main(String[] args) {
        recordPlayback = new RecordPlayback();


        //Filter invalid user inputs
        String usage = "usage:  $java Multicast <Multicast IP address>\nMulticast IP address range: 224.0.0.0 to 239.255.255.255\n";
        if (args.length == 1) {
            try {
                multicastAddress = InetAddress.getByName(args[0]);
            } catch (Exception e) {
                System.out.println("Invalid IP address\n" + usage);
            }

        } else {
            System.out.println("Invalid format\n" + usage);
        }

        //Try to create the multicast group
        try {
            multicastSocket = new MulticastSocket(8888);
            multicastSocket.joinGroup(multicastAddress);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //Separate threads to handle Receiving, Recording and Sending, Playback simultaneously
        Thread recv = new Thread(new Multicast(STATE.RECV));
        Thread rec_send = new Thread(new Multicast(STATE.REC_SEND));
        Thread play = new Thread(new Multicast(STATE.PLAY));

        //Start all 3 threads
        recv.start(); rec_send.start(); play.start();


    }

    public void run() {

        if (state == STATE.RECV) {//Packet receive
            while (true) {
                try {
                    //Receive packets
                    DatagramPacket packet = new DatagramPacket(new byte[packetSize], packetSize);
                    multicastSocket.receive(packet);

                    //Once received pass them for packetNumberingAndData handling
                    packetNumberingAndData.appendPacket(packet.getData());

                } catch (Exception e) {
                    System.out.println("Packet receive failed!");
                    e.printStackTrace();
                }

            }

        } else if (state == STATE.REC_SEND) {//Audio recording and Sending
            while (true) {
                //Record audion
                byte[] data = recordPlayback.captureAudio();

                //Give the packets to do numbering
                byte[] temp_data = packetNumberingAndData.addNumbers(data);

                //Try to send numbered packets
                try {
                    DatagramPacket packet = new DatagramPacket(temp_data, temp_data.length, multicastAddress, multicastPort);
                    multicastSocket.send(packet);
                } catch (Exception e) {
                    System.out.println("Packet sending failed!");
                    e.printStackTrace();
                }

            }
        } else if (state == STATE.PLAY) {//Playback

            //Keep getting received packets from packetNumberingAndData and play them
            while (true) {
                byte[] temp = packetNumberingAndData.getPacket();
                recordPlayback.playAudio(temp);
            }
        }
    }


}
