package sempV2ConfigMgr;


import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;

class Utilities {
	
    public static String sendHttpRequest(Arguments args, String uri, String requestType, String payload) throws IOException {
    	
    	int responseCode = 99999;
  	
    	String userAgent = "Mozilla/5.0";
        StringBuffer response = new StringBuffer();
         
        URL obj = new URL(uri);
        String userpass = args.adminUser + ":" + args.adminPass;
        String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    	
        HttpsURLConnection  httpsURLConnection = (HttpsURLConnection) obj.openConnection();
        
        try {
        	httpsURLConnection.setRequestProperty ("Authorization", basicAuth);
        	httpsURLConnection.setRequestProperty("User-Agent", userAgent);
        	//httpsURLConnection.setRequestMethod(requestType);
        	
        	if (requestType.equals("POST") || requestType.equals("PATCH")) {
        		if(requestType.equals("PATCH")) {
        			httpsURLConnection.setRequestProperty("X-HTTP-Method-Override", requestType);
        			httpsURLConnection.setRequestMethod("POST");
        		} else {
        			httpsURLConnection.setRequestMethod(requestType);
        		}
        		
        		httpsURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        		httpsURLConnection.setRequestProperty("Accept", "application/json");
        		httpsURLConnection.setDoOutput(true);
        		httpsURLConnection.setDoInput(true);
        		OutputStreamWriter wr = new OutputStreamWriter(httpsURLConnection.getOutputStream());
        		wr.write(payload);
        		wr.flush();
        	} else {
        		httpsURLConnection.setRequestMethod(requestType);
            	
        	}
        	responseCode = httpsURLConnection.getResponseCode(); 
        	//System.out.println( requestType + " Response Code :: " + responseCode);
        	
        } catch (java.net.ConnectException e) {
        	System.out.println("Connect Exception :: " + e);
        } catch (Exception e) {
        	System.out.println("Exception :: " + e);
        }       
        
        if (responseCode == HttpsURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            } in.close();
            // return result
            //System.out.println("RESPONSE OK :: " + response.toString());
            //return(response.toString());
        } else {
        	/*
            BufferedReader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            } in.close();
            // return result
            System.out.println("RESPONSE ERROR :: " + response.toString());
        	//return FAIL
            //return("FAIL");
             */ 
             response.append("FAIL");
        }
        
        return(response.toString());
    }
	

}
