
public class Message {

	public String stationClass;
	public byte[] input;
	public byte slot;
	public long time;
	public long receivedTime;

	public Message copy() {
		Message copy = new Message();
		copy.stationClass = stationClass;
		copy.input = input;
		copy.slot = slot;
		copy.time = time;
		copy.receivedTime = receivedTime;

		return copy;

	}

	public void printMessage() {

		System.out.println("\nStation Class:   " + stationClass);
		System.out.print("Data: ");
		for (int i = 0; i < input.length; i++) {
			System.out.print((char) input[i]);
		}
		System.out.println();
		System.out.flush();

		System.out.println("Slot number: " + slot);
		System.out.println("Timestamp: " + time);
		System.out.println("received time: " + receivedTime);
	}

	public static Message ConvertReceivedMessage(byte[] data, long receivedTime) { //(byte[] data)
		Transformer transformer = new Transformer();
		Message receivedMessage = new Message();
		byte[] stationClass = new byte[2];
		stationClass[1] = data[0];
		receivedMessage.stationClass = transformer.bytesToString(stationClass);

		
//		if(data[0] == 0x00)
//		{
//			receivedMessage.stationClass = "A";
//		}
//		else if(data[0] == 0xFF)
//		{
//			receivedMessage.stationClass = "B";
//		}

		// byte 1-24: data
		byte[] ReceivedData = new byte[24];

		int y = 0;
		for (int i = 1; i < 25; i++) {
			ReceivedData[y] = data[i];
			y++;
		}

		receivedMessage.input = ReceivedData;

		// byte 25: slot
		receivedMessage.slot = data[25];

		// byte 26 - byte 33
		byte[] timeByte = new byte[8];
		int w = 0;
		for (int i = 26; i < 34; i++) {
			timeByte[w] = data[i];
			w++;
		}

		receivedMessage.time = transformer.bytesToLong(timeByte);

		receivedMessage.receivedTime = receivedTime;

		return receivedMessage;
	}

	public static byte[] convertSendMessage(Message message) {
		byte[] result = new byte[34];
		Transformer transformer = new Transformer();

		// byte 0: stationclass
		if (message.stationClass.equals("A")) {
			byte[] stationClass = transformer.stringToBytes("A");
			//result[0] = 0x00;
			result[0] = stationClass[0];
		} else {
			byte[] stationClass = transformer.stringToBytes("B");
			//result[0] = (byte) 0xFF;
			result[0] = stationClass[0];
		}

		// byte 1-24: data
		int y = 1;
		for (int i = 0; i < message.input.length; i++) {
			result[y] = message.input[i];
			y++;
		}

		// byte 25: slot number
		result[25] = message.slot;

		// byte 26-33: timestamp

		byte[] timestamp = transformer.longToBytes(message.time);

		for (int i = 0; i < timestamp.length; i++) {
			result[i + 26] = timestamp[i];
		}
		
		int w = 26;
		for (int i = 0; i < timestamp.length; i++) {
			result[w] = timestamp[i];
			w++;
		}

		return result;
	}


}
