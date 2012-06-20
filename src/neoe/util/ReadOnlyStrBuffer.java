package neoe.util;

/**
 * read-only stringbuffer.
 */
public class ReadOnlyStrBuffer {

	private StringBuffer sb;

	public ReadOnlyStrBuffer(StringBuffer sb) {
		this.sb = sb;
	}

	public char charAt(int i) {
		return sb.charAt(i);
	}

	public int length() {
		return sb.length();
	}

	public String substring(int i) {
		return sb.substring(i);
	}

	public String substring(int a, int b) {
		return sb.substring(a, b);
	}

	public String toString() {
		return sb.toString();
	}

	public String toString(boolean ignoreCase) {
		String s = sb.toString();
		if (ignoreCase) {
			return s.toLowerCase();
		} else {
			return s;
		}
	}

}