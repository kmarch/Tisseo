package tisseo.request;

import java.util.Iterator;
import java.util.List;

import org.jdom2.Element;

import tisseo.request.generation.GenereAPITisseo;

public class RequestTemps extends Request{

	
	public RequestTemps(String arret) {
		generateur = new GenereAPITisseo();
		format="xml";
		action ="departureBoard";
		arg = "stopPointId";
		argValue = arret;
	}
	
	/**
	 * Obtention du prochain départ d'une ligne
	 */
	@Override
	public String getResults(String param) {
		prepareXML();
	    List<Element> listId = racine.getChildren("departure");
	    String resultat = null;
	    boolean fin = false;
	    //On crée un Iterator sur notre liste
	    Iterator<Element> i = listId.iterator();
	    while(i.hasNext() && !fin) {
		    Element courant = (Element)i.next();
		    if(courant.getChild("line").getAttributeValue("shortName").equals(param)){
		    	resultat = courant.getAttributeValue("dateTime");
		    	fin = true;
		    }
	    }
	    return resultat;
	}
}
