package tisseo.request;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

import tisseo.request.generation.GenereAPITisseo;

public class RequestPosition extends Request{
	
	private final String AFFICHE_COORD = "displayOnlyStopAreas=1&";
	public RequestPosition(String adresse) {
		generateur = new GenereAPITisseo();
		format="xml";
		action ="placesList";
		arg = AFFICHE_COORD + "term";
		argValue = adresse;
	}

	public Map<String,String> getListeResults() {
		 prepareXML();
		   List<Element> listId = racine.getChildren("place");
		   Map<String, String> coordParAdresse = new HashMap<String, String>();
		   String ligne = null;
		   //On crée un Iterator sur notre liste
		   Iterator<Element> i = listId.iterator();
		   while(i.hasNext())
		   {
			   ligne = "";
		      //On recrée l'Element courant à chaque tour de boucle afin de
		      //pouvoir utiliser les méthodes propres aux Element comme :
		      //sélectionner un nœud fils, modifier du texte, etc...
		      Element courant = (Element)i.next();
		      ligne += courant.getAttributeValue("id");
		      ligne += ";";
		      ligne += courant.getAttributeValue("x");
		      ligne += ";";
		      ligne += courant.getAttributeValue("y");
		      coordParAdresse.put(courant.getAttributeValue("label"), ligne);
		   }
		   return coordParAdresse;
	}

	@Override
	public String getResults(String param) {
		return null;
	}

}
