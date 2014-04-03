package tisseo.request;

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
	   prepareXML();
	   List<Element> listId = racine.getChildren("line");
	   String resultat = null;
	   //On crée un Iterator sur notre liste
	   Iterator<Element> i = listId.iterator();
	   while(i.hasNext())
	   {
	      //On recrée l'Element courant à chaque tour de boucle afin de
	      //pouvoir utiliser les méthodes propres aux Element comme :
	      //sélectionner un nœud fils, modifier du texte, etc...
	      Element courant = (Element)i.next();
	      if(param.equals(courant.getAttribute("shortName").getValue())) {
	    	  resultat = courant.getAttribute("id").getValue();
	      }
	   }
	   return resultat;
	}
}
