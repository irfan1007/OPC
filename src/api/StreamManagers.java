package api;

import core.StreamManagerImpl;
import util.ConfigLoader;

public class StreamManagers {

	static{
		new ConfigLoader().loadConfigFile();
	}
	public static StreamManager defaultStreamManager(){
		return new StreamManagerImpl();
	}
}
