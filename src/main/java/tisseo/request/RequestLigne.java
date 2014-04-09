package tisseo.request;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Element;

import tisseo.request.generation.GenereAPITisseo;

public class RequestLigne extends Request{

	
	public RequestLigne() {
		generateur = new GenereAPITisseo();
		format="xml";
		action ="linesList";
	}
	
	public String getResults(String param) {
	   return null;
	}
	
	/**
	 * Récupère la liste des lignes
	 * @return
	 */
	public ArrayList<String> getResults() {
		prepareXML();
	    List<Element> listId = racine.getChildren("line");
	    ArrayList<String> resultat = new ArrayList<String>();
	    //On crée un Iterator sur notre liste
	    Iterator<Element> i = listId.iterator();
	    while(i.hasNext())
	    {
	        //On recrée l'Element courant à chaque tour de boucle afin de
	        //pouvoir utiliser les méthodes propres aux Element comme :
	        //sélectionner un nœud fils, modifier du texte, etc...
	        Element courant = (Element)i.next();
	        resultat.add(courant.getAttribute("shortName").getValue());
	    }
	    return resultat;	
	}
}
