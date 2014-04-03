package tisseo.request;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import tisseo.request.generation.GenereAPIJCDecaux;

public class RequestVelo extends Request {

	public RequestVelo() {
		generateur = new GenereAPIJCDecaux();
		action = "stations";
		arg = "ville";
		argValue = "Toulouse";
		
	}
	
	@Override
	public String getResults(String param) {
		URL url = null;
		String resultat = "Aucun vélo disponible pour cet endroid";
		try {
			url = new URL(genRequest());
			InputStream is = url.openStream();
    	    JsonReader rdr = Json.createReader(is);
    	    JsonArray obj = rdr.readArray();
    	    for (JsonObject result : obj.getValuesAs(JsonObject.class)) {
    	        if(result.getString("name").toLowerCase().contains(param) && 
    	        		result.getInt("available_bikes") != 0) {
    	        	resultat = "Il y a " + result.getInt("available_bikes") +
    	        			" vélos disponibles";
    	        }
    	    }
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return resultat;
	}

}
