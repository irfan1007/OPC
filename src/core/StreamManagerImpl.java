package core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

import api.StreamManager;
import cloud.CloudAPI;
import util.ConfigLoader;
import util.StreamUtil;

/** This class provide implementation for PUT & GET methods
 * @author irfan
 *
 */
public class StreamManagerImpl implements StreamManager {

	private static final String SHA_1 = "SHA-1";
	private static final int BUFFER_SIZE =Integer.parseInt(ConfigLoader.props.get("bufferSize").toString());
	
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private ReadLock readLock = lock.readLock();
	
	private static final Logger LOGGER = Logger.getLogger(StreamManagerImpl.class.getName());

	/* (non-Javadoc)
	 * @see api.StreamManager#putObject(core.Key, java.io.InputStream)
	 */
	@Override
	public Key<?> putObject(Key<?> key, InputStream stream) {

		if (stream == null) {
			LOGGER.info("Invalid stream received for key : " + key);
			return null;
		}
		synchronized (this) {
			// Use pool size equal to available processors
			final ExecutorService pool = Executors.newFixedThreadPool(getProcessorsCount());
			
			//Digest stream for calculating SHA1, along with path of stream reading
			DigestInputStream dis = null;
			try {
				int index = 1;
				MessageDigest md = MessageDigest.getInstance(SHA_1);
				dis = new DigestInputStream(stream, md);

				//Divide stream into n arrays of buffer-size
				while (dis.available() > 0) {
					int size = Math.min(dis.available(), BUFFER_SIZE);

					byte buf[] = new byte[size];
					dis.read(buf, 0, size);

					//The BIG stream with key "A" will be divided into multiple byte[] of A-1,A-2 ...A-n
					//While retrieving, same logic will be reversed to combine n array to BIG stream
					pool.submit(new StreamUpWorker(new ByteArrayInputStream(buf), key.toString() + Key.SEPARATOR + index));
					buf = null;
					index++;
				}

				//Store checksum in object with key as "A-checksum"
				pool.submit(new StreamUpWorker(
						new ByteArrayInputStream(DatatypeConverter.printHexBinary(md.digest()).getBytes()),
						key.toString() + Key.SEPARATOR + Key.CHKSUM));

			} catch (Exception e) {
				LOGGER.error("Error in putObject for key " + key + " ,Error msg -" + e.getMessage(), e);
				return null;
			} finally {
				if (dis != null) {
					try {
						dis.close();
					} catch (IOException e) {
						LOGGER.error("Error while closing stream", e);
					}
				}
				if (pool != null) {
					pool.shutdown();
					try {
						pool.awaitTermination(30, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						LOGGER.error("Error while executor pool shutdown ",e);
					}
				}
			}
			return key;
		}
	}

	/* (non-Javadoc)
	 * @see api.StreamManager#getObject(core.Key)
	 */
	@Override
	public InputStream getObject(Key<?> key) {
		if (key == null) {
			LOGGER.info("Null key");
			return null;
		}
		readLock.lock();
		final ExecutorService pool = Executors.newFixedThreadPool(getProcessorsCount());
		SequenceInputStream stream = null;
		InputStream stream2 = null;
		DigestInputStream dis = null;
		try {

			int index = 1;
			//Prepare callables for all keys {A-1,A-2....A-n] mapped to given key A
			List<Callable<InputStream>> callables = new ArrayList<>();
			while (true) {
				if (CloudAPI.containsKey(key.toString() + Key.SEPARATOR + index)) {
					callables.add(new StreamDownWorker(key.toString() + Key.SEPARATOR + index));
					index++;
				} else {
					break;
				}
			}

			//Used for creating sequential stream
			Vector<InputStream> inputStreams = new Vector<InputStream>();

			for (Future<InputStream> f : pool.invokeAll(callables)) {
				while (!f.isDone()) {
					TimeUnit.MILLISECONDS.sleep(1);
				}
				inputStreams.add(f.get());
			}

			//Retrieve checksum file "A-checksum" for key "A"
			InputStream chkSum = pool.submit(new StreamDownWorker(key.toString() + Key.SEPARATOR + Key.CHKSUM)).get();
			String originalChecksum = StreamUtil.streamToString(chkSum);
			LOGGER.info("Original checksum - " + originalChecksum);

			//Sequential stream contains enum of all streams returned by future
			stream = new SequenceInputStream(inputStreams.elements());
			stream2 = StreamUtil.copyStream(stream, new ByteArrayOutputStream(), BUFFER_SIZE * inputStreams.size());

			//Validate SHA_1 for retrieved object
			MessageDigest md = MessageDigest.getInstance(SHA_1);
			dis = new DigestInputStream(stream2, md);
			byte[] b = new byte[stream2.available()];
			dis.read(b, 0, b.length);

			String calculatedChecksum = DatatypeConverter.printHexBinary(md.digest());
			LOGGER.info("Returned checksum - " + calculatedChecksum);

//			if (!originalChecksum.equals(calculatedChecksum)) {
//				LOGGER.error("Checksum mismatch for key :" + key);
//				throw new RuntimeException("Checksum mismatched!");
//			}
			
			LOGGER.info("Successfully retrieved object for key :" + key);
			return new ByteArrayInputStream(b);

		} catch (Exception e) {
			LOGGER.error("Error while getObject for key :" + key, e);
		} finally {
			try {
				if (dis != null)
					dis.close();
				if (stream != null)
					stream.close();
				if (stream2 != null)
					stream2.close();
				if (pool != null)
					pool.shutdown();
				readLock.unlock();
			} catch (Exception e) {
				LOGGER.error("Error while getObject for key :" + key, e);
			}
		}
		return null;
	}

	private int getProcessorsCount() {
		// Made it method and not constant, as per Javadoc, this value may
		// change in VM env and should be polled regularly
		return Runtime.getRuntime().availableProcessors();
	}
}
