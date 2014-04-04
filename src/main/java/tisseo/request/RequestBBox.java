package tisseo.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

import tisseo.request.generation.GenereAPITisseo;

public class RequestBBox extends Request {

	public ArrayList<String> listeLigneMetro = new ArrayList<String>();

	public RequestBBox(String arg, String argValue, Map<String,String> listeArgs) {
		generateur = new GenereAPITisseo(listeArgs);
		format="xml";
		action ="stopPointsList";
		this.arg = arg;
		this.argValue = argValue;
		listeLigneMetro.add("A");
		listeLigneMetro.add("B");
	}
	
	public HashMap<String,String> getResultsListeLignesZone(String param) {
		System.out.println("ca commence");
		prepareXML();
		HashMap<String,String> mapLignes = new HashMap<String,String>();
	    List<Element> listId = racine.getChildren("physicalStop");
	    //On crée un Iterator sur notre liste
	    Iterator<Element> i = listId.iterator();
 	    while(i.hasNext())
	    {
	       //On recrée l'Element courant à chaque tour de boucle afin de
	       //pouvoir utiliser les méthodes propres aux Element comme :
	       //sélectionner un nœud fils, modifier du texte, etc...
	       Element courant = (Element)i.next();
	       List<Element> courrant2 = courant.getChildren("destination");
	       List<Element> courrant3;
	       for(int j = 0; j < courrant2.size(); j++) {
	    	   courrant3  = courrant2.get(j).getChildren("line"); 
	    	   for(int k = 0; k < courrant3.size(); k++) {
	    		   if(param == null && !listeLigneMetro.contains(courrant3.get(k).getAttributeValue("shortName"))) {
		    		   mapLignes.put(courant.getChildren("stopArea").get(0).getAttributeValue("id") +
		    				   ":" +courrant3.get(k).getAttributeValue("shortName"),
		    				   courant.getChildren("stopArea").get(0).getAttributeValue("x") + 
		    				   ";" + courant.getChildren("stopArea").get(0).getAttributeValue("y"));
	    		   } else if(param != null && courant.getChildren("stopArea").get(0).getAttributeValue("id").equals(param)) {
	    			   mapLignes.put(courant.getChildren("stopArea").get(0).getAttributeValue("id") +
		    				   ":" +courrant3.get(k).getAttributeValue("shortName"),
		    				   courant.getChildren("stopArea").get(0).getAttributeValue("x") + 
		    				   ";" + courant.getChildren("stopArea").get(0).getAttributeValue("y"));
		    	   }
	    	   }
	       }
	    }
	    return mapLignes;
	}

	@Override
	public String getResults(String param) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
