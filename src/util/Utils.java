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
		String string = str.toString();
		string = string.substring(0, string.length() - 1);
		return string;
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
		String string = str.toString();
		string = string.substring(0, string.length() - 1);
		return string;
	}
	
	public static byte[] bitwiseAnd(byte[] bytes1, byte[] bytes2) {
	    if (bytes1.length < bytes2.length)
	       return bitwiseAnd(bytes2, bytes1);
	    // bytes1 is never shorter than bytes2
	    byte[] ret = bytes1.clone();
	    for (int i = 0; i < bytes2.length; i++)
	        ret[i] &= bytes2[i];
	    return ret;
	}
	
	public static byte[] bitwiseOr(byte[] bytes1, byte[] bytes2) {
	    if (bytes1.length < bytes2.length)
	       return bitwiseOr(bytes2, bytes1);
	    // bytes1 is never shorter than bytes2
	    byte[] ret = bytes1.clone();
	    for (int i = 0; i < bytes2.length; i++)
	        ret[i] |= bytes2[i];
	    return ret;
	}
}
