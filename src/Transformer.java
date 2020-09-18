
public class Transformer {

	public long bytesToLong(byte[] bytes) {

		long r = 0;
		for (int i = 0; i < bytes.length; i++) {
			r = r << 8;
			r += bytes[i];
		}

		return r;
	}

	public byte[] longToBytes(long l) {
		long num = l;
		byte[] bytes = new byte[8];
		for (int i = bytes.length - 1; i >= 0; i--) {
			bytes[i] = (byte) (num & 0xff);
			num >>= 8;
		}
		return bytes;
	}

	public byte[] stringToBytes(String str) {
		char[] buffer = str.toCharArray();
		byte[] b = new byte[buffer.length];
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) buffer[i];
		}
		return b;
	}

	public String bytesToString(byte[] bytes) {
		char[] buffer = new char[bytes.length >> 1];
		for (int i = 0; i < buffer.length; i++) {
			int bpos = i << 1;
			char c = (char) (((bytes[bpos] & 0x00FF) << 8) + (bytes[bpos + 1] & 0x00FF));
			buffer[i] = c;
		}
		return new String(buffer);
	}

}
