package test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import api.StreamManagers;
import core.Key;
import util.Checksum;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StorageTest {

	@Test
	public void a_testPut() {
		try {
			Key<String> key1 = new Key<String>("A");
			Key<?> key2 = StreamManagers.defaultStreamManager().putObject(key1,
					Files.newInputStream(Paths.get("OPC_Test.log"), StandardOpenOption.READ));
			Assert.assertEquals(key1,key2);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void b_testGet() {
		try {
			Files.deleteIfExists(Paths.get("download.txt"));
			InputStream stream = StreamManagers.defaultStreamManager().getObject(new Key<String>("A"));
			Files.copy(stream, Paths.get("download.txt"));
		} catch (Exception e) {
			Assert.fail(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Test
	public void b_testChecksum() {
		try {
			//Checksum of original file
			InputStream stream1 = Files.newInputStream(Paths.get("b.txt"));
			String chkSum1 = Checksum.getSHA1(stream1);
			
			//Upload file
			StreamManagers.defaultStreamManager().putObject(new Key<String>("D"),
					Files.newInputStream(Paths.get("b.txt"), StandardOpenOption.READ));
			
			//Download file and get checksum
			InputStream stream2 = StreamManagers.defaultStreamManager().getObject(new Key<String>("D"));
			String chkSum2 = Checksum.getSHA1(stream2);
			
			Assert.assertTrue("Checksum mismatch!", chkSum1.equals(chkSum2));

		} catch (Exception e) {
			Assert.fail(e.getMessage());
			e.printStackTrace();
		}
	}
	
}
