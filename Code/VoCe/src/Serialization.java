import java.util.Arrays;
import java.net.*;
import java.nio.ByteBuffer;


public class Serialization {


    static int threshold = 32;

    private static int packet_receive = 0, packet_reorder = 0, packet_sent = 0;

    private int curr_sending = 0, curr_playing = -1;

    private byte[][] tempBuffer = new byte[1024][VoCe.packet_size];

    private static volatile long startTime = System.currentTimeMillis();


    byte[] serialize(byte[] buff) {        //method fro serialization of data; it adds sequence number to the packet


        byte[] temp = Arrays.copyOf(buff, VoCe.packet_size);
        ByteBuffer bytebuffer = ByteBuffer.allocate(4);
        bytebuffer.putInt(curr_sending);
        byte[] data = bytebuffer.array();
        System.arraycopy(data, 0, temp, VoCe.packet_size - 4, 4);
        packet_sent++;
        curr_sending++;
        return temp;

    }

    /*
        *mwthod for deserialization split the original sound packet and sequence no. then check for errors and if no
            error it will add the current packet to the queue.if unrecoverable error happens it will drop the packet
    */
    void deSerialize(byte[] buff) {

        int receive_num;
        packet_receive++;

        byte[] temp = new byte[4];
        System.arraycopy(buff, VoCe.packet_size - 4, temp, 0, 4);
        ByteBuffer wrapped = ByteBuffer.wrap(temp);

        receive_num = wrapped.getInt();
        if (receive_num > curr_playing) {
            tempBuffer[receive_num % 1024] = Arrays.copyOf(buff, buff.length);

        } else packet_reorder++;
        if ((long) System.currentTimeMillis() > startTime + 10000) {
            System.out.println("Packet Size " + VoCe.packet_size + " Packet Loss " + (packet_sent - packet_receive) + " Reorderd Packets " + packet_reorder);
            startTime = System.currentTimeMillis();
            packet_sent = 0;
            packet_receive = 0;
            packet_reorder = 0;

        }


    }

    byte[] getPacket() {        //returns the first packet from the audio packet buffer wich contains the packets recived.

        byte[] buff = new byte[VoCe.packet_size - 4];
        int i = curr_playing + 1;
        int k = 0;
        //System.out.println("A");
        while (true) {
            int counter_buff = 0;
            for (int j = 0; j < 1024; j++) {
                if (tempBuffer[j] != null) counter_buff++;
            }
            if (counter_buff > threshold) break;


        }


        for (int p = 0; p < 1024; p++) {
            //System.out.println("A");

            if (tempBuffer[p] != null) {

                //System.out.println("B");
                int receive_num;

                byte[] temp = new byte[4];
                System.arraycopy(tempBuffer[p], VoCe.packet_size - 4, temp, 0, 4);
                ByteBuffer wrapped = ByteBuffer.wrap(temp);

                receive_num = wrapped.getInt();
                //System.out.println("IMPORTANT rec_no "+receive_num +"  "+curr_playing);


                if (receive_num >= curr_playing) {
                    curr_playing = receive_num;
                    buff = Arrays.copyOf(tempBuffer[p], tempBuffer[p].length - 4);

                    tempBuffer[p] = null;
                    //System.out.println("Receive number HERE = "   +curr_playing + "  " +played_loops );

                    break;
                } else {
                    //System.out.println("else");
                    tempBuffer[p] = null;

                }
            }


            //System.out.println(played_loops);
        }


        return buff;
    }

/*
	main class written for unit test the serialization and deserialization part
*/

    public static void main(String[] args) {
        Serialization s1 = new Serialization();

        int server_port = 9876;

        try {
            InetAddress server_address = InetAddress.getByName("localhost");
            if (args.length == 0) {
                System.out.println("Running unit testing client for testing Serialization and deSerialization");
                DatagramSocket socket = new DatagramSocket();
                try {
                    Thread.sleep(10);
                } catch (Exception ignored) {
                }
                for (int i = 2000; i < 2100; i++) {


                    ByteBuffer b = ByteBuffer.allocate(4);
                    b.putInt(i);
                    byte[] data = b.array();

                    byte[] data_serial = s1.serialize(data);
                    DatagramPacket packet = new DatagramPacket(data_serial, data_serial.length, server_address, server_port);
                    System.out.println("sending packet containing int value of" + i);
                    socket.send(packet);
                }
            } else if (args.length == 1) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }

                System.out.println("AD");
                DatagramSocket socket = new DatagramSocket(server_port);
                while (true) {
                    System.out.println("reciving packet");
                    DatagramPacket packet = new DatagramPacket(new byte[VoCe.packet_size], VoCe.packet_size);                // Prepare the packet for receive


                    socket.receive(packet);


                    s1.deSerialize(packet.getData());
                    byte[] temp = s1.getPacket();

                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }

                    ByteBuffer wrapped = ByteBuffer.wrap(temp);
                    int a = wrapped.getInt();

                    System.out.println("Packet Contains : " + a);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ;

    }

}

