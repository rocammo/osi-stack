package util;

public class Utils {
	public static String macBytesToString(byte[] arr) {
		StringBuilder str = new StringBuilder();

		for (byte b : arr) {
			Integer i = new Integer(b);
			if (i < 0)
				i = i + 256;

			String s = Integer.toHexString(i);

			str.append(s);
			str.append(":");
		}

		return str.toString();
	}

	public static String ipBytesToString(byte[] arr) {
		StringBuilder str = new StringBuilder();

		for (byte b : arr) {
			Integer i = new Integer(b);
			if (i < 0)
				i = i + 256;

			String s = "" + i;

			str.append(s);
			str.append(".");
		}

		return str.toString();
	}
}
