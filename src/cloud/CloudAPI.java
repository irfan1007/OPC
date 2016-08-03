package cloud;

import static util.ConfigLoader.props;

import java.io.InputStream;

import org.apache.log4j.Logger;

import com.sun.jersey.core.util.Base64;

import oracle.cloud.storage.CloudStorage;
import oracle.cloud.storage.CloudStorageConfig;
import oracle.cloud.storage.CloudStorageFactory;

public class CloudAPI {

	private static CloudStorageConfig CONFIG;
	private static CloudStorage CONNECTION;
	private static String CONTAINER = props.getProperty("container");
	private static String CONTENT_TYPE = "text/plain";

	private static final Logger LOGGER = Logger.getLogger(CloudAPI.class.getName());

	static {
		try {
			CONFIG = new CloudStorageConfig();
			CONFIG.setServiceName(props.getProperty("serviceName")).setUsername(props.getProperty("username"))
					.setPassword(new String(Base64.decode(props.getProperty("password"))).toCharArray())
					.setServiceUrl(props.getProperty("serviceURL"));
			CONNECTION = CloudStorageFactory.getStorage(CONFIG);
		} catch (Exception e) {
			LOGGER.error("Error while creating config - " + e.getMessage(), e);
		}

	}

	/** Upload to container
	 * @param key
	 * @param is
	 * @return
	 */
	public static boolean upload(String key, InputStream is) {
		try {
			CONNECTION.storeObject(CONTAINER, key, CONTENT_TYPE, is);
			return true;
		} catch (Exception e) {
			LOGGER.error("Error while loading file for key - " + key + ", error - " + e.getMessage(), e);
		}
		return false;
	}

	/** Retrieve from container
	 * @param key
	 * @return
	 */
	public static InputStream download(String key) {
		try {
			return CONNECTION.retrieveObject(CONTAINER, key);
		} catch (Exception e) {
			LOGGER.error("Error while loading file for key - " + key + ", error - " + e.getMessage(), e);
		}
		return null;
	}

	/** Check if key is present in container
	 * @param key 
	 * @return true if present, false if not
	 */
	public static boolean containsKey(String key) {
		boolean isPresent = CONNECTION.listObjects(CONTAINER, null).stream().filter(s -> s.getKey().equals(key))
				.findAny().isPresent();
		return isPresent;
	}

}
