package sempV2ConfigMgr;


import java.io.IOException;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.FileWriter;  


class GetVirtualBrokerConfiguration {
	
	private String usage() {
		return ("Usage : VirtualBrokerConfiguration get <host> <admin-user> <admin-pass> <vpnname> <filename-prefix> <opaque-data-pwd>\n" +
	            "\n" +
				"Where : \n" +
	            "host		     = hostname:port or hostip:port - only TLS connections are supported so this should\n" +
	            "                     directed at the secure SEMP port of the broker\n" +
	            "admin-user      = admin username \n" +
	            "admin-pass      = admin password \n" +
	            "vpn-name	     = vpn name to get \n" +
	            "filename-prefix = file prefix to use to build the file \n" +
	            "opaque-data-pwd = password used to encyrpt opaque data (passwords) \n" +
	            " \n" +
	            "This process will connect to the message broker and message VPN listed in host and vpn-name using the \n" +
	            "admin-user and admin-pass credentials. The process will extract the configuration of the VPN and its \n" +
	            "child objects and write the configuration using a JSON representation to a file with the name \n" +
	            "obtained from the file prefix, vpn-name and the current timestamp \n"
	           );
	}
	
  	
	private void getConfig(Arguments args, JSONObject configJson) {
		
		//ask for the api version so that we can compete the meta data for the object
		String initUri = "https://" + args.host + "/SEMP/v2/config/about/api";
		getAttributesJson(args, initUri, configJson); 
		
		JSONObject vpnConfigJson = new JSONObject();

		//Ask for VPNs and filter to get array back rather than a JSON object
		initUri = "https://" + args.host + "/SEMP/v2/config/msgVpns?where=msgVpnName==" + args.vpnName;
		getJson(args, initUri, vpnConfigJson);
		try {
			configJson.put("config", vpnConfigJson);
		} catch (Exception e) {
			System.out.println("Exception : " + e);
		}
	}
	
	private void getAttributesJson(Arguments args, String uriString, JSONObject configJson) {
		
		String response;		    
		
		//System.out.println("PROCESSING JSON OBJECT :: " + uriString);
		
		JSONObject attributesJson = new JSONObject();
	        
		try {
			attributesJson.put("host", args.host);
			attributesJson.put("msgVpnName", args.vpnName);
			attributesJson.put("timestamp", java.time.LocalDateTime.now());
			attributesJson.put("opaquePassword", args.opaquePassword);

			response = Utilities.sendHttpRequest(args, uriString, "GET", null);
			JSONObject jsonObj = new JSONObject(response);
			
			attributesJson.put("sempVersion", jsonObj.getJSONObject("data").get("sempVersion"));
			
			configJson.put("attributes", attributesJson);
			
		} catch (Exception e) {
			System.out.println("Exception : " + e);
		}
		
		
	}
	
	private void getJson(Arguments args, String uriString, JSONObject configJson) {
		
		String response;	

		//System.out.println("PROCESSING JSON OBJECT :: " + uriString);

		try {
			response = Utilities.sendHttpRequest(args, uriString, "GET", null);

			// The response will have a Data portion and a Links portion
			JSONObject jsonObj = new JSONObject(response);	
			JSONArray jsonDataArray  = new JSONArray();
			JSONArray jsonLinksArray  = new JSONArray();
			JSONArray tempDataArray  = new JSONArray();

			for(int i = 0; i<jsonObj.getJSONArray("links").length(); i++){
				jsonDataArray.put(jsonObj.getJSONArray("data").getJSONObject(i));
				jsonLinksArray.put(jsonObj.getJSONArray("links").getJSONObject(i));	            
			}

			String label;

			if (uriString.indexOf("?") != -1) {
				label = uriString.substring(0, uriString.indexOf("?") ); 
			} else {
				label = uriString;
			}

			if (label.indexOf(args.vpnName) != -1) {
				label = label.substring(label.indexOf(args.vpnName) + args.vpnName.length() + 1);
			} else {
				label = "_" + label.substring(label.indexOf("msgVpns"));
			}

			//System.out.println(label);
			String objectName = "";
			Boolean skip = false;

			// Need to filter out reserved objects that begin with '#' and also take care of 'default'
			// as these cannot be added via a 'put'
			// May need to rework so that default is added as a patch if possible?
			switch (label) {
				case "clientProfiles": objectName = "clientProfileName";
					break;
				case "clientUsernames": objectName = "clientUsername";
					break;
				case "aclProfiles": objectName = "aclProfileName";
					break;	
				/*
				case "queues": objectName = "queueName";
					break;	
				*/
				case "aclProfiles/%23acl-profile/clientConnectExceptions": 
				case "aclProfiles/%23acl-profile/publishTopicExceptions": 
				case "aclProfiles/%23acl-profile/publishExceptions": 
				case "aclProfiles/%23acl-profile/subscribeTopicExceptions": 
				case "aclProfiles/%23acl-profile/subscribeExceptions":
				case "aclProfiles/%23acl-profile/subscribeShareNameExceptions":
					skip = true;
					break;
			}
			
			if (label.endsWith("publishExceptions") || label.endsWith("subscribeExceptions")){
				skip = true;
			}

			if (!skip) { // skip all processing for the ACLs that start with '#' or are deprecated

				if (configJson.has(label)) {
					//need to copy current contents into tempData
					//followed by the new batch and then put all in configJson

					//start copying out existing data
					JSONArray temp = (JSONArray)configJson.get(label);
					for (int j = 0; j < temp.length(); j++) {                 	
						tempDataArray.put(temp.get(j));
					}
				}

				for (int j = 0; j < jsonDataArray.length(); j++) {   
					//Can I pick out the name to determine if it starts with '#'

					//System.out.println("###################" + jsonDataArray.get(j));
					if (objectName != "") {
						String objName = jsonDataArray.getJSONObject(j).getString(objectName);
						//System.out.println("################### " + objectName + " == " + jsonDataArray.getJSONObject(j).getString(objectName));
					    if(objName.startsWith("#") ) {   //|| objName.equals("default") <<== no longer needed as default objects are now PATCHed on application
					    	//System.out.println("DROPPING");
					    	continue;
					    }
					}
					//if (jsonDataArray.getJSONObject(j).getString(objectName).substring(0,0) == "#") {
					//	System.out.println("FOUND OBJECT OF NAME ====== " + objectName + " == " + jsonDataArray.getJSONObject(j).getString(objectName) + " skipping.........");
					//	}

					tempDataArray.put(jsonDataArray.get(j));
				}
				
				configJson.put(label, tempDataArray);
			}
			

			//} else {
				//configJson.put(label, jsonDataArray);
			//}

			for(int i = 0; i<jsonLinksArray.length(); i++){

				JSONObject links = jsonLinksArray.getJSONObject(i);

				for (int j = 0; j < links.names().length(); j++) {
					if (!(links.names().getString(j).equals("uri"))) {	        	
						getJson(args, (links.get(links.names().getString(j))).toString() + args.countLabel +"&opaquePassword=" + args.opaquePassword, configJson);
					}
				}
			}		    

			JSONObject meta = (JSONObject) jsonObj.get("meta");
			if (meta.has("paging")) {
				JSONObject paging = (JSONObject) meta.get("paging");
				if (paging.has("nextPageUri")) {
					String nextPageUri = (String) paging.get("nextPageUri");
					//System.out.println(nextPageUri);
					getJson(args, nextPageUri, configJson);
				}
			}

		}catch(Exception e) {
			System.out.println("Exception : " + e);
		}
		
	}
	
	
	protected void run(String[] args) {
		
		Arguments cargs = new Arguments();

        if (args.length != 7) {
        	System.out.println(usage());
        	System.exit(0);
        }
        
        cargs.host = args[1];
        cargs.adminUser = args[2];
        cargs.adminPass = args[3];
        cargs.vpnName = args[4];
        cargs.fileNamePrefix = args[5];
        cargs.opaquePassword = args[6];
        
        JSONObject configJson = new JSONObject();
	
        getConfig(cargs, configJson);
        
        String filename = cargs.fileNamePrefix + "_" + cargs.vpnName;
        
        try {
        	String jsonToFile = configJson.toString(4);
        	FileWriter myWriter = new FileWriter(filename);
        	myWriter.write(jsonToFile);
        	myWriter.close();
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
