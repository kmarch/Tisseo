package tisseo.request;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import tisseo.CalculPosition;
import tisseo.request.generation.GenereAPIJCDecaux;

public class RequestVelo extends Request {

	public RequestVelo() {
		generateur = new GenereAPIJCDecaux();
		action = "stations";
		arg = "ville";
		argValue = "Toulouse";
	}

	/**
	 * Récupère le parc à vélo en fonction du nom à condition qu'il
	 * y reste des vélo disponible
	 */
	@Override
	public String getResults(String param) {
		URL url = null;
		String resultat = null;
		try {
			url = new URL(genRequest());
			InputStream is = url.openStream();
    	    JsonReader rdr = Json.createReader(is);
    	    JsonArray obj = rdr.readArray();
    	    for (JsonObject result : obj.getValuesAs(JsonObject.class)) {
    	        if(result.getString("name").toLowerCase().contains(param) && 
    	        		result.getInt("available_bikes") != 0) {
    	        	resultat = "Il y a " + result.getInt("available_bikes") +
    	        			" vélo(s) disponible(s) à cet endroit";
    	        }
    	    }
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return resultat;
	}

	/**
	 * Récupère le parc à vélo le plus proche des coordonnées passées en 
	 * arguments (500 mètres aux alentours) et qui possède encore des vélos
	 * @param posX
	 * @param posY
	 * @return
	 */
	public String getVelo(double posX, double posY) {
		URL url = null;
		String resultat = null;
		Double distance,plusCourt = 10000000000.;
		try {
			url = new URL(genRequest());
			InputStream is = url.openStream();
    	    JsonReader rdr = Json.createReader(is);
    	    JsonArray obj = rdr.readArray();
    	    for (JsonObject result : obj.getValuesAs(JsonObject.class)) {
    	    	distance = (double) CalculPosition.HaversineInM(posX, posY,
    	    			Double.parseDouble(result.getJsonObject("position").get("lng").toString()),
    	    			Double.parseDouble(result.getJsonObject("position").get("lat").toString()));
    	    	if(result.getInt("available_bikes") != result.getInt("bike_stands") &&
    	    			distance < 500 && distance < plusCourt) {
    	    		plusCourt = distance;
    	    		resultat = result.getString("name") + ";" +
    	    				result.getJsonObject("position").get("lng").toString() +
    	    				";" +result.getJsonObject("position").get("lat").toString();
    	    	}
    	    }
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return resultat;
	}

	/**
	 * Même méthode que celle au dessus sauf que l'on cherche des parcs où
	 * l'on peut déposer des vélos
	 * @param posX
	 * @param posY
	 * @return
	 */
	public String getVelo2(double posX, double posY) {
		URL url = null;
		String resultat = null;
		Double distance,plusCourt = 10000000000.;
		try {
			url = new URL(genRequest());
			InputStream is = url.openStream();
    	    JsonReader rdr = Json.createReader(is);
    	    JsonArray obj = rdr.readArray();
    	    for (JsonObject result : obj.getValuesAs(JsonObject.class)) {
    	    	distance = (double) CalculPosition.HaversineInM(posX, posY,
    	    			Double.parseDouble(result.getJsonObject("position").get("lng").toString()),
    	    			Double.parseDouble(result.getJsonObject("position").get("lat").toString()));
    	    	if(result.getInt("available_bikes") != 0 &&
    	    			distance < 500 && distance < plusCourt) {
    	    		plusCourt = distance;
    	    		resultat = result.getString("name") + ";" +
    	    				result.getJsonObject("position").get("lng").toString() +
    	    				";" +result.getJsonObject("position").get("lat").toString();
    	    	}
    	    }
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return resultat;
	}
}
