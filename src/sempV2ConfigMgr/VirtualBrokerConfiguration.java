package sempV2ConfigMgr;



public class VirtualBrokerConfiguration {
	
	private static String usage() {
		return (" Usage : VirtualBrokerConfiguration put|get \n" +
	            "         Add --help  for put / get arguments");
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		PutVirtualBrokerConfiguration putBrokerConfig;
		GetVirtualBrokerConfiguration getBrokerConfig;
		
		if (args.length < 1) {
			System.out.println(usage());
			return;
		}
		
		if (args[0].equals("put")) {
			
			putBrokerConfig = new PutVirtualBrokerConfiguration();
			putBrokerConfig.run(args);
			
		} else if (args[0].equals("get")) {
			
			getBrokerConfig = new GetVirtualBrokerConfiguration();
			getBrokerConfig.run(args);
			
		} else {
			System.out.println(usage());
		}
		
	}

}
