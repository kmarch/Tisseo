package tisseo.request;

import java.util.Iterator;
import java.util.List;

import org.jdom2.Element;

import tisseo.request.generation.GenereAPITisseo;

public class RequestArret extends Request{

	public RequestArret(String arg, String argValue) {
		generateur = new GenereAPITisseo();
		format="xml";
		action ="stopAreasList";
		this.arg = arg;
		this.argValue = argValue;
	}
	
	@Override
	public String getResults(String param) {
		prepareXML();
	    List<Element> listId = racine.getChildren("stopArea");
	    String resultat = null;
	    //On crée un Iterator sur notre liste
	    Iterator<Element> i = listId.iterator();
	    while(i.hasNext())
	    {
	       //On recrée l'Element courant à chaque tour de boucle afin de
	       //pouvoir utiliser les méthodes propres aux Element comme :
	       //sélectionner un nœud fils, modifier du texte, etc...
	       Element courant = (Element)i.next();
	       if(courant.getAttribute("name").getValue().toLowerCase().contains(param.toLowerCase())) {
	     	   resultat = courant.getAttribute("id").getValue();
	       } 
	    }
	     return resultat;
	}

}
