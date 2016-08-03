package api;

import java.io.InputStream;

import core.Key;

public interface StreamManager {

	/** Upload object stream , mapped to key k
	 * @param k - key
	 * @param input - stream object
	 * @return - same key if success, null on failure
	 */
	Key<?> putObject(Key<?> k, InputStream input);

	/** Retrieve object using previously stored key k
	 * @param k - key to retrieves
	 * @return mapped object
	 */
	InputStream getObject(Key<?> k);
}
