package core;

import java.io.InputStream;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import cloud.CloudAPI;

/** Callable to execute download task
 * @author irfan
 *
 */
public class StreamDownWorker implements Callable<InputStream> {

	private String key;
	private static final Logger LOGGER = Logger.getLogger(StreamDownWorker.class.getName());

	public StreamDownWorker(String key) {
		this.key = key;

	}

	@Override
	public InputStream call() throws Exception {
		LOGGER.info("Downloading for key :" + key);
		return CloudAPI.download(key);
	}

}
