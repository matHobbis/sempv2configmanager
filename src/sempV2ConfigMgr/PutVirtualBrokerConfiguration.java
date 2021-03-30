package sempV2ConfigMgr;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class PutVirtualBrokerConfiguration {

	private String usage() {
		return ("Usage : VirtualBrokerConfiguration put <host> <admin-user> <admin-pass> <filename> <opaque-data-pwd>\n" +
	            "\n" +
				"Where : \n" +
	            "host		     = hostname:port or hostip:port \n" +
	            "admin-user      = admin username \n" +
	            "admin-pass      = admin password \n" +
	            "filename        = filename that holds the json configuration for the VPN \n" +
	            "opaque-data-pwd = password used to encyrpt opaque data (passwords) \n" +
	            " \n" +
	            "This process will connect to the message broker and message VPN listed in host and vpn-name using the \n" +
	            "admin-user and admin-pass credentials. The process will extract the configuration from the given file  \n" +
	            "and apply the config to the vpn on the broker.\n"
	           );
	}
	
	
	private void putConfig(Arguments args, JSONObject configJson) {
		
		Boolean opaquePw = false;
		
		String baseUri = "https://" + args.host + "/SEMP/v2/config/msgVpns";
		
		try {
			//System.out.println("File = " + configJson.toString());

			//String opaquePassword =  (String) (configJson.getJSONObject("attributes").get("opaquePassword"));
			String sempVersion =  (String) (configJson.getJSONObject("attributes").get("sempVersion"));
			args.vpnName = (String) (configJson.getJSONObject("attributes").get("msgVpnName"));
			
			//System.out.println("opaquePassword = " + opaquePassword + "    sempVersion = " + sempVersion);
			
			//ask for the api version so that we can compete the meta data for the object
			String initUri = "https://" + args.host + "/SEMP/v2/config/about/api";
			
			String response = Utilities.sendHttpRequest(args, initUri, "GET", null);
			JSONObject jsonObj = new JSONObject(response);
			
			String brokerSempVersion = (String) jsonObj.getJSONObject("data").get("sempVersion");

			if (brokerSempVersion.equals(sempVersion)) {
				System.out.println("SEMP PASS :: SEMP versions are the same - opaque configuration will be applied");
				opaquePw = true;
			} 
						
			JSONObject sempConfig = configJson.getJSONObject("config");
			List<String> stringList = new ArrayList<String>();
			
			for (int j = 0; j < sempConfig.names().length(); j++) {
				if(sempConfig.names().getString(j).indexOf("%23") == -1) {
					stringList.add(sempConfig.names().getString(j));
				}
			}
			
		
			Collections.sort(stringList);
		
			//System.out.println(stringList);
			
			try {
				for(String s : stringList) {
					
					System.out.println(s);
					
					String objectName = "";
					
					//create uri for the call
					String sempUri = "";
					if (s.equals("_msgVpns")) {
						sempUri = baseUri;
					} else {
						sempUri = baseUri + "/" + args.vpnName + "/" + s;
						//if (opaquePw == true) {
						//	sempUri = sempUri + "?opaquePassword=" + args.opaquePassword;
						//}
					}

					JSONArray temp = (JSONArray)sempConfig.get(s);
					
					//System.out.println(temp);
					
					for (int j = 0; j < temp.length(); j++) {  
						
						String sempUriToUse = sempUri;
						
						String restVerb = "POST";
						//System.out.println(temp.getJSONObject(j));
						
						/*
						// Need to filter out default objects for clientProfile, aclProfile and clientUsername
			            // as these cannot be added via a 'put' as they are 'Solace' objects
			            // so will they need to be set as a patch. For items set as a PATCH the item name "default"
			            // must also be supplied in the URI
			             * */
						
						if (s.equals("_msgVpns") && args.vpnName.equals("default")) {
							sempUriToUse = sempUriToUse + "/default";
							restVerb = "PATCH";
						}

						switch (s) {
							case "clientProfiles": objectName = "clientProfileName";
								if (temp.getJSONObject(j).get(objectName).equals("default")) {
									sempUriToUse = sempUriToUse + "/default";
									restVerb = "PATCH";
								}
								break;	
							case "clientUsernames": objectName = "clientUsername";
								if (temp.getJSONObject(j).get(objectName).equals("default")) {
									sempUriToUse = sempUriToUse + "/default";
									restVerb = "PATCH";
								}
								break;	
							case "aclProfiles": objectName = "aclProfileName";
								if (temp.getJSONObject(j).get(objectName).equals("default")) {
									sempUriToUse = sempUriToUse + "/default";
									restVerb = "PATCH";
								}
							break;	
							case "aclProfiles/default/clientConnectExceptions": 
							case "aclProfiles/default/publishTopicExceptions":
							case "aclProfiles/default/publishExceptions": 
							case "aclProfiles/default/subscribeTopicExceptions":
							case "aclProfiles/default/subscribeExceptions": 
							case "aclProfiles/default/subscribeShareNameExceptions": 
								restVerb = "PATCH";
								
								break;
						}
						

						if (opaquePw == true) {
							sempUriToUse = sempUriToUse + args.opaquePasswordLabel + args.opaquePassword;
						}
						
							
						//System.out.println(sempUriToUse + "  ::   " + restVerb + " :: " + temp.get(j));
						
						response = Utilities.sendHttpRequest(args, sempUriToUse, restVerb, temp.get(j).toString());

						//System.out.println("RESPONSE :: " + response);
						
					}

				}
	        } catch (JSONException e) {
	        	System.out.println("A JSONException error occurred.");
	        	e.printStackTrace();
	        } catch (Exception e) {
	        	System.out.println("An error occurred - Error 1.");
	        	e.printStackTrace();
	        }
		
        } catch (JSONException e) {
        	System.out.println("A JSONException error occurred.");
        	e.printStackTrace();
        } catch (Exception e) {
        	System.out.println("An error occurred - Error 2.");
        	e.printStackTrace();
        }
		
	}
	
	
	
	protected void run(String[] args) {
		
		Arguments cargs = new Arguments();

        if (args.length != 6) {
        	System.out.println(usage());
        	System.exit(0);
        }
        
        cargs.host = args[1];
        cargs.adminUser = args[2];
        cargs.adminPass = args[3];
        cargs.fileNamePrefix = args[4];
        cargs.opaquePassword = args[5];
        
        JSONObject configJson;
	   
        String filename = cargs.fileNamePrefix;
        try {
        	//Read ethe file into a JSON object
        	String content = new String(Files.readAllBytes(Paths.get(filename)));
        	configJson = new JSONObject(content);
        	
        	//use the JSON object to drive the broker configuration
            putConfig(cargs, configJson);
            
        } catch (IOException e) {
        	System.out.println("An error occurred.");
        	e.printStackTrace();
        } catch (JSONException e) {
        	System.out.println("An error occurred.");
        	e.printStackTrace();
        }
           
        System.out.println("DONE");	

	}

}
