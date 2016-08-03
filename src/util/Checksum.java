package util;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

public class Checksum {
	private static final String SHA_1 = "SHA-1";
	private static final Logger LOGGER = Logger.getLogger(Checksum.class.getName());
	
	public static String getSHA1(InputStream stream) {
		if (stream == null)
			return null;
		try {
			MessageDigest md = MessageDigest.getInstance(SHA_1);
			DigestInputStream dis = new DigestInputStream(stream, md);
			byte[] b = new byte[stream.available()];
			dis.read(b, 0, b.length);
			return DatatypeConverter.printHexBinary(md.digest());
		} catch (Exception e) {
			LOGGER.error("Error while calculating SHA1 ",e);
		}
		return null;
	}
}
