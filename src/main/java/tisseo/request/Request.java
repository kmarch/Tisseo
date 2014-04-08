package tisseo.request;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import tisseo.request.generation.inter.GenereAPI;

/**
 * Classse représentant une requête vers le webservice de l'API Tisséo
 * ou JCDecaux
 * @author kevin marchois
 *
 */
public abstract class Request {

	protected String format;
	protected String action;
	protected Document document;
	protected Element racine;
	protected String arg;
	protected String argValue;
	// génère la requete en fonction de l'API
	protected GenereAPI generateur;
	
	public String genRequest() {
		return generateur.genRequest(action, format, arg, argValue);
	}
	
	/**
	 * Récupération du fichier xml en fonction de la requête générée
	 */
	public void prepareXML() {
		//On crée une instance de SAXBuilder
	      SAXBuilder sxb = new SAXBuilder();
	      try
	      {
	         //On crée un nouveau document JDOM avec en argument le fichier XML
	         //Le parsing est terminé ;)

	         document = sxb.build(genRequest());
	      }
	      catch(Exception e){
	    	  System.out.println("erreur xml");
	    	  e.printStackTrace();
	      }
	      //On initialise un nouvel élément racine avec l'élément racine du document.
	      racine = document.getRootElement();
	}
	
	public abstract String getResults(String param);
}
