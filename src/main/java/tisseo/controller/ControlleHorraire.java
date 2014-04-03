package tisseo.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.ektorp.support.CouchDbRepositorySupport;
import org.jdom2.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


import tisseo.CalculPosition;
import tisseo.db.Ligne;
import tisseo.request.RequestArret;
import tisseo.request.RequestBBox;
import tisseo.request.RequestLigne;
import tisseo.request.RequestPosition;
import tisseo.request.RequestTemps;
import tisseo.request.RequestVelo;

@Controller
@Configuration
//@ComponentScan("tisseo.db")
public class ControlleHorraire {

   /* @Autowired
    CouchDbRepositorySupport<Ligne> dbRepo;
	*/
	
	private final String POS_BBOX = "1.461593%2C43.557055%2C1.467988%2C43.570054";
	private final double VITESSE_BUS = 0.83;//(km/s)
	public final static String URL = "jdbc:postgresql:Tisseo";
	public final static String NOM_BASE = "postgres";
	public final static String PWD = "romano";
	
	@RequestMapping("/")
	String home() {
	   return "index";
	}

	@RequestMapping("/calculHoraire")
	public String calculHoraire(@RequestParam(value="numLigne", required=true) String numLigne, 
			@RequestParam(value="arret", required=true) String arret,
			Model model) {
		String formatHeure;
		RequestLigne requete = new RequestLigne();
		RequestArret requeteArret = new RequestArret("lineId", requete.getResults(numLigne));
		RequestTemps requeteTemps;
		requeteTemps = new RequestTemps(requeteArret.getResults(arret));
		formatHeure = requeteTemps.getResults(numLigne);
		if (formatHeure != null){
		    model.addAttribute("temps", getAttente(formatHeure));
		}
		return "attente";
	}
	
	@RequestMapping("/dispoVelo")
	public String dispoVelo(@RequestParam(value="lieu", required=true) String lieu,
			Model model) {
		RequestVelo requete = new RequestVelo();
		String disponibilite = requete.getResults(lieu);
		model.addAttribute("dispo", disponibilite);
		return "dispoVelo";
	}
	
	@RequestMapping("/trajetPlusRapide")
	public String trajetPlusRapide(@RequestParam(value="adresse", required=true) String adresse,
			Model model) {
		RequestPosition requete = new RequestPosition(adresse);
		Map<String, String> listeCoordAdresse = requete.getListeResults();
		String resultat = "<br/>";
		Iterator<Map.Entry<String,String>> it = listeCoordAdresse.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<String, String> entry = it.next();
			System.out.println(entry.getValue());
			resultat += "<a href=plusRapideTrajet?x=" + entry.getValue().split("[;]")[1]
					+ "&y=" + entry.getValue().split("[;]")[2] + 
					"&id=" + entry.getValue().split("[;]")[0] + ">" + entry.getKey() +
					"</a><BR/>";
		}
		model.addAttribute("liste", resultat);
		return "plusRapide";
	}
	
	@RequestMapping("/plusRapideTrajet")
	public String plusRapide(@RequestParam(value="x", required=true) String x,
			@RequestParam(value="y", required=true) String y,
			@RequestParam(value="id", required=true) String id,
	Model model) {
		HashMap<String,String> listeArretProximite;
		HashMap<String,String> listeLigneArretChoisi;
		String itineraire;
		RequestBBox requete = new RequestBBox("bbox", POS_BBOX, null);
		listeArretProximite = requete.getResultsListeLignesZone(null);
		System.out.println("ici" +listeArretProximite);
		HashMap<String,String> listeArgs = new HashMap<String,String>();
		listeArgs.put("displayLines", "1");
		listeArgs.put("displayCoordXY", "1");
		requete = new RequestBBox(null, null, listeArgs);
		listeLigneArretChoisi = requete.getResultsListeLignesZone(id);
		System.out.println(id+"la" + listeLigneArretChoisi);
		itineraire = calculPlusCourt(listeArretProximite, listeLigneArretChoisi, id);
		model.addAttribute("plusCourt", itineraire);
		return "reponseRapidite";
	}

	private String calculPlusCourt(HashMap<String, String> listeArretProximite,
			HashMap<String, String> listeLigneArretChoisi, String id) {
		Map.Entry<String, String> resultat = null;
		String plusCourt = null;
		String nouvelleCle;
		String duree;
		int nouveauTemps, nbSeconde = 0;
		double plusProche = 0;//distance
		double courrante;
		double temps = 0.0;
		Double [] coord1;
		RequestTemps requete;
		for (Map.Entry<String, String> entry : listeLigneArretChoisi.entrySet()) {
			coord1 = getCoord(entry.getValue());
			for (Map.Entry<String, String> entry2 : listeArretProximite.entrySet()) {
				if(plusCourt == null) {
					System.out.println("coucou" );
					resultat = entry2;
					plusCourt = entry2.getKey();
					Double [] coord2 = getCoord(entry2.getValue());
					plusProche = CalculPosition.distanceVolOiseauEntre2PointsSansPrécision(
							coord1[0], coord1[1], coord2[0], coord2[1]);
					requete = new RequestTemps(plusCourt.split("[;]")[0]);
					duree = requete.getResults(null);
					nbSeconde = getAttenteSeconde(duree);
					nbSeconde += plusProche/ VITESSE_BUS;
				} else {
					nouvelleCle = entry2.getKey();
					Double [] coord2 = getCoord(entry2.getValue());
					courrante =  CalculPosition.distanceVolOiseauEntre2PointsSansPrécision(
							coord1[0], coord1[1], coord2[0], coord2[1]);
					requete = new RequestTemps(nouvelleCle.split("[;]")[0]);
					duree = requete.getResults(null);
					nouveauTemps = getAttenteSeconde(duree);
					nouveauTemps += courrante/VITESSE_BUS;
					if(nouveauTemps < nbSeconde) {
						plusCourt = entry2.getKey();
						nbSeconde = nouveauTemps;
						resultat = entry2;
					}
				}
			}
		}
		if(resultat== null) {
			return null;
		} 
		return resultat.getKey().split("[;]")[1];
	}

	public String getAttente(String formatHeure) {
		int heure, min, sec, annee, mois, jour, duree;
		Calendar tempsAttente;

		annee = Integer.parseInt(formatHeure.split("[ ]")[0].split("[-]")[0]);
		mois = Integer.parseInt(formatHeure.split("[ ]")[0].split("[-]")[1]);
		jour = Integer.parseInt(formatHeure.split("[ ]")[0].split("[-]")[2]);
		formatHeure =  formatHeure.split("[ ]")[1];
		heure = Integer.parseInt(formatHeure.split("[:]")[0]);
		min = Integer.parseInt(formatHeure.split("[:]")[1]);
		sec = Integer.parseInt(formatHeure.split("[:]")[2]);
		tempsAttente = GregorianCalendar.getInstance();
		tempsAttente.set(annee, mois-1,jour,heure, min, sec);
		duree  = (int) ((tempsAttente.getTime().getTime() -Calendar.getInstance().getTime().getTime())/1000);
		heure = duree/3600;
		duree -= 3600 * heure;
		min = duree / 60;
		duree -= min * 60;
		sec = duree;
		return "attente " + heure+"h" + min + "min" + sec + "sec";
	}
	
	public int getAttenteSeconde(String formatHeure) {
		int heure, min, sec, annee, mois, jour;
		Calendar tempsAttente;

		annee = Integer.parseInt(formatHeure.split("[ ]")[0].split("[-]")[0]);
		mois = Integer.parseInt(formatHeure.split("[ ]")[0].split("[-]")[1]);
		jour = Integer.parseInt(formatHeure.split("[ ]")[0].split("[-]")[2]);
		formatHeure =  formatHeure.split("[ ]")[1];
		heure = Integer.parseInt(formatHeure.split("[:]")[0]);
		min = Integer.parseInt(formatHeure.split("[:]")[1]);
		sec = Integer.parseInt(formatHeure.split("[:]")[2]);
		tempsAttente = GregorianCalendar.getInstance();
		tempsAttente.set(annee, mois-1,jour,heure, min, sec);
		return (int) ((tempsAttente.getTime().getTime() -Calendar.getInstance().getTime().getTime())/1000);
	}
	
	public  Double[] getCoord(String param) {
		Double[] rez = new Double[2];
    	rez[0] = Double.parseDouble(param.split(("[;]"))[0]);
    	rez[1] = Double.parseDouble(param.split(("[;]"))[1]);
    	return rez;
    }
}
