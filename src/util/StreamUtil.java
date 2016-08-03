package util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class StreamUtil {

	/** Util method for creating copy of InputStream, also reads the stream in byte[]
	 * @param input
	 * @param output
	 * @param size
	 * @return byte[] with given input stream
	 * @throws IOException
	 */
	public static InputStream copyStream(InputStream input, ByteArrayOutputStream output, int size) throws IOException {
		byte[] buffer = new byte[size];
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
		}
		return new ByteArrayInputStream(output.toByteArray());
	}

	/** Util method to convert stream to String
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public static String streamToString(InputStream input) throws IOException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
			return buffer.lines().collect(Collectors.joining("\n"));
		}
	}
}
