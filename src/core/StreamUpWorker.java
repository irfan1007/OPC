package core;

import java.io.InputStream;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import cloud.CloudAPI;

/** Callable to execute upload task
 * @author irfan
 *
 */
public class StreamUpWorker implements Callable<Boolean> {

	private InputStream stream;
	private String key;
	
	private static final Logger LOGGER = Logger.getLogger(StreamUpWorker.class.getName());
	
	public StreamUpWorker(InputStream stream, String key) {
		this.stream = stream;
		this.key = key;

	}

	@Override
	public Boolean call() throws Exception {
		LOGGER.info(" uploading for key :" + key);
		try {
			CloudAPI.upload(key.toString(), stream);
			return Boolean.TRUE;
		} catch (Exception e) {

		}
		return Boolean.FALSE;

	}
}
