import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.util.ArrayList;

public class Station {
	private static Connect connect;
	private volatile static String state;
	private static byte slotatm;
	private static byte nextslot = 100;
	private static boolean start = true;
	private static long currentTime;
	private static String stationClass;
	private static Receiver receiver;
	private static Sender sender;
	private static FrameCheck frame;
	private static DataInput input;
	private static MessageList MessagesReceivedThisFrame;
	private static MessageList MessagesReceivedLastFrame;
	private static char[] inputData = new char[24];

	private static class DataInput extends Thread implements Runnable {
		@Override
		public void run() {
			BufferedReader Reader = new BufferedReader(new InputStreamReader(System.in));

			while (true) {

				try {
					char[] data = new char[24];
					while (Reader.read(data) != 24)
						;

					synchronized (inputData) {
						inputData = data;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static class Receiver extends Thread implements Runnable {
		@Override
		public void run() {

			while (true) {
				byte[] data = new byte[34];
				DatagramPacket p = new DatagramPacket(data, data.length);
				try {
					connect.receive(p);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				long receivedTime = System.currentTimeMillis();

				Message recvmsg = new Message();
				recvmsg = Message.ConvertReceivedMessage(data, receivedTime);
				synchronize(data);

				recvmsg.printMessage();

				accessMessageList("ADD", recvmsg);

			}
		}
	}

	private static class FrameCheck extends Thread implements Runnable {
		Transformer transformer = new Transformer();

		@Override
		public void run() {
			// boolean slotTaken = false;
			boolean reset = false;
			// Wait for start of next frame
			while (true) {
				currentTime = System.currentTimeMillis();
				if (currentTime % 1000 == 0) {
					System.out.println("Waiting for Frame");
					accessMessageList("CLEAR", null);
					break;
				}
			}
			long milliseconds = 0;
			while (true) {
				currentTime = System.currentTimeMillis();
				if (currentTime % 1000 == 0 && milliseconds != currentTime) {
					milliseconds = currentTime;
					MessageList thisFrame = (MessageList) accessMessageList("GET", null);
					MessageList lastFrame = MessagesReceivedLastFrame;

					boolean[] slotOccupations = new boolean[25];
					for (int i = 0; i < slotOccupations.length; i++) {
						slotOccupations[i] = false;
					}

					for (Message message : thisFrame.messageList) {
						byte givenSlot = message.slot;
//						int trueSlot = (int) Math.ceil((Math.abs(message.receivedTime%1000)) / 40);
//						System.out.println("received time " + Math.abs(message.receivedTime%1000) );
//						if (givenSlot != trueSlot) {
//							System.err.println("Station stated it sent in " + givenSlot + "but actually it sended in"
//									+ trueSlot + "\n");
//						} else {
							if (slotOccupations[givenSlot] == true) {
								System.err.println("Collision Found! In slot " + givenSlot);
							} else {
								slotOccupations[givenSlot] = true;
							}
						//}
					}

					byte[] checkSlot = new byte[1];
					checkSlot[0] = slotatm;
					byte[] checkSlot1 = new byte[1];

					ArrayList<Byte> freeslots = new ArrayList<Byte>();
					for (byte i = 0; i < slotOccupations.length; i++) {
						checkSlot1[0] = i;
						if (slotOccupations[i] == false) {
							freeslots.add(i);

						} else if (transformer.bytesToLong(checkSlot1) != transformer.bytesToLong(checkSlot)) {
							System.err.println("Slot " + i + " was not free!");

						}
					}

					int randomSlot = (int) (((double) (Math.random())) * ((double) freeslots.size()));
					nextslot = slotatm;
					slotatm = freeslots.get(randomSlot);
					System.out.println("old Slot: " + nextslot);
					System.out.println("new Slot picked " + slotatm);

					state("SENDING");
					// slotTaken = false;

					MessagesReceivedLastFrame = thisFrame;
					accessMessageList("CLEAR", null);

				}
			}
		}
	}

	private static class Sender extends Thread implements Runnable {
		@Override
		public void run() {
			long milliseconds = 0;
			while (true) {
				if (start == true) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				start = false; 
				if (state == "SENDING") {

					long currentTime = System.currentTimeMillis();
					if (((currentTime % 1000 == ((nextslot * 40) + (20)))) && (milliseconds != currentTime)) {
						// System.out.print();
						Message newmessage = new Message();
						newmessage.stationClass = stationClass;
						byte[] data = null;
						synchronized (inputData) {
							data = new byte[24];
							for (int b = 0; b < 24; b++) {
								data[b] = (byte) inputData[b];
							}
						}

						newmessage.input = data;
						newmessage.slot = slotatm;
						newmessage.time = currentTime;

						byte[] bytemsg = Message.convertSendMessage(newmessage);

						try {
							connect.send(bytemsg);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						milliseconds = currentTime;

					}
				}

			}

		}
	}

	private static void synchronize(byte[] packet) {
		if (packet[0] == 'A') {
			Transformer transformer = new Transformer();
			byte[] timeByte = new byte[8];
			int w = 0;
			for (int i = 26; i < 34; i++) {
				timeByte[w] = packet[i];
				w++;
			}
			currentTime = transformer.bytesToLong(timeByte);
			if (stationClass.equals("A")) {
				currentTime = (System.currentTimeMillis() + currentTime) / 2; // average
			}
		}
	}

	public synchronized static MessageList accessMessageList(String am, Message msg) {
		if (am == "ADD") {
			MessagesReceivedThisFrame.messageList.add(msg);
			return null;
		} else if (am == "GET") {
			return MessagesReceivedThisFrame.copy();
		} else {
			MessagesReceivedThisFrame.messageList.clear();
			return null;
		}

	}

	public synchronized static String state(String s) {
		if (s == null) {
			return state;
		} else {
			state = s;
			return null;
		}
	}

	public static void main(String[] args) {
		connect = new Connect();
		final String ip = args[0];
		final int port = Integer.parseInt(args[1]);
		stationClass = args[2];
		try {
			connect.init(ip, port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MessagesReceivedThisFrame = new MessageList();
		MessagesReceivedLastFrame = new MessageList();
		MessagesReceivedThisFrame.messageList = new ArrayList<Message>();
		MessagesReceivedLastFrame.messageList = new ArrayList<Message>();
		state = "LISTENING";
		frame = new FrameCheck();
		receiver = new Receiver();
		sender = new Sender();
		input = new DataInput();
		frame.start();
		sender.start();
		receiver.start();
		input.start();
	}
}
