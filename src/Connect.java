import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Connect {
	private MulticastSocket sendingSocket;
	private MulticastSocket receivingSocket;
	private int port;
	private InetAddress groupIP;

	// @throws IOException

	public void init(String IP, int port) throws IOException {
		this.port = port;
		sendingSocket = new MulticastSocket(port);
		receivingSocket = new MulticastSocket(port);

		groupIP = InetAddress.getByName(IP);

		sendingSocket.joinGroup(groupIP);
		receivingSocket.joinGroup(groupIP);

	}

	// @throws IOException

	public void send(byte[] data) throws IOException {
		if (data.length != 34) {
			System.err.println("data is not byte[34]");
		}
		DatagramPacket p = new DatagramPacket(data, data.length, groupIP, port);
		System.out.println("Sent a packet!");
		sendingSocket.send(p);
		//sendingSocket.close();

	}

	public void receive(DatagramPacket packet) throws IOException {
		if (packet.getData().length != 34) {
			System.err.println("data is not byte[34]");

		}

		receivingSocket.receive(packet);
		//receivingSocket.close();
	}
}
