package tisseo.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import tisseo.CalculPosition;
import tisseo.db.DB;
import tisseo.db.Ligne;
import tisseo.request.RequestBBox;
import tisseo.request.RequestPosition;
import tisseo.request.RequestTemps;
import tisseo.request.RequestVelo;

@Controller
@Configuration
public class ControleHoraire {
	
	private final String POS_BBOX = "1.461593%2C43.557055%2C1.467988%2C43.570054";
	private final double POS_X = 1.466170;
	private final double POS_Y = 43.562881;
	private final double VITESSE_BUS = 0.013;//(km/s)
	private final double VITESSE_VELO = 0.0055;//(km/s)
	public final static String URL = "jdbc:postgresql:Tisseo";
	public final static String NOM_BASE = "postgres";
	public final static String PWD = "romano";
	
	@RequestMapping("/")
	public String home() {
	   return "index";
	}
	
	@RequestMapping("/index")
	public String index() {
		return "index";
	}

	@RequestMapping("/frmVelo")
	public String frmVelo() {
		return "frmVelo";
	}
	
	@RequestMapping("/frmItineraire")
	public String frmItineraire() {
		return "frmItineraire";
	}
	
	/**
	 * Calcul des temps de départ des lignes de bus présentes dans la 
	 * zone de Paul Sabatier
	 * @param model
	 * @return
	 */
	@RequestMapping("/calculHoraire")
	public String calculHoraire(Model model) {
		String temps = "";
		String duree;
		HashMap<String,String> listeArretProximite;
		HashMap<String,String> listeArgs = new HashMap<String,String>();
		listeArgs.put("displayLines", "1");
		listeArgs.put("displayCoordXY", "1");
		RequestBBox requete = new RequestBBox("bbox", POS_BBOX, listeArgs);
		RequestTemps requeteTemps;
		listeArretProximite = requete.getResultsListeLignesZone(null);
		for (Map.Entry<String, String> entry : listeArretProximite.entrySet()) {
			requeteTemps = new RequestTemps(entry.getKey().split("[:]")[0]);
			duree = requeteTemps.getResults(entry.getKey().split("[:]")[1]);
			if(duree !=null){
				temps += "Ligne:" +entry.getKey().split("[:]")[1] + " arrêt:" +
						entry.getKey().split("[:]")[2] +" depart dans: " +
						getAttente(duree)+"<br/>";
			}
		}
		model.addAttribute("temps", temps);
		return "attente";
	}
	
	/**
	 * Cherche le nombre de vélo disponible pour l'endroit insiquer par
	 * l'utilisateur
	 * @param lieu
	 * @param model
	 * @return
	 */
	@RequestMapping("/dispoVelo")
	public String dispoVelo(@RequestParam(value="lieu", required=true) String lieu,
			Model model) {
		RequestVelo requete = new RequestVelo();
		String disponibilite = requete.getResults(lieu);
		if(disponibilite != null) {
			model.addAttribute("dispo", disponibilite);
		} else {
			model.addAttribute("dispo", "Aucun vélo disponible pour cet endroid");
		}
		return "dispoVelo";
	}
	
	/**
	 * Recherche des arrêts proche de la zone indiquée par l'utilisateur
	 * (à 300 mètres environ)
	 * @param adresse, l'endroit où veut aller l'utilisateur
	 * @param model
	 * @return
	 */
	@RequestMapping("/trajetPlusRapide")
	public String trajetPlusRapide(@RequestParam(value="adresse", required=true) String adresse,
			Model model) {
		RequestPosition requete = new RequestPosition(adresse);
		Map<String, String> listeCoordAdresse = requete.getListeResults();
		String resultat = "<br/>";
		Iterator<Map.Entry<String,String>> it = listeCoordAdresse.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<String, String> entry = it.next();
			resultat += "<a href=plusRapideTrajet?x=" + entry.getValue().split("[;]")[1]
					+ "&y=" + entry.getValue().split("[;]")[2] + 
					"&id=" + entry.getValue().split("[;]")[0] + 
					" data-ajax='false'>" + entry.getKey() +
					"</a><BR/>";
		}
		model.addAttribute("liste", resultat);
		return "plusRapide";
	}
	
	/**
	 * Recherche de l'itinéraire le plus rapide pour se rendre à une adresse
	 * on va chercher les lignes qui déservent l'arrêt choisi par l'utilisateur
	 * pour celles qui sont dans la zones de l'utilisateur
	 * @param x coordonnée de l'arrêt choisi par l'utilisateur
	 * @param y coordonnée de l'arrêt choisi par l'utilisateur
	 * @param id, id de l'arrêt choisi par l'utilisateur
	 * @param model
	 * @return
	 */
	@RequestMapping("/plusRapideTrajet")
	public String plusRapide(@RequestParam(value="x", required=true) String x,
			@RequestParam(value="y", required=true) String y,
			@RequestParam(value="id", required=true) String id,
	Model model) {
		HashMap<String,String> listeArretProximite;
		HashMap<String,String> listeLigneArretChoisi;
		HashMap<String,String> listeArgs = new HashMap<String,String>();
		listeArgs.put("displayLines", "1");
		listeArgs.put("displayCoordXY", "1");
		String[] itineraire;
		RequestBBox requete = new RequestBBox("bbox", POS_BBOX, listeArgs);
		listeArretProximite = requete.getResultsListeLignesZone(null);
		requete = new RequestBBox(null, null, listeArgs);
		listeArgs.put("stopAreaId", id);
		listeLigneArretChoisi = requete.getResultsListeLignesZone(id);
		itineraire = calculPlusCourt(listeArretProximite, 
				listeLigneArretChoisi, id, x , y);
		model.addAttribute("bus", itineraire[0]);
		model.addAttribute("velo", itineraire[1]);
		model.addAttribute("like", itineraire[2]);
		return "reponseRapidite";
	}

	/**
	 * Calcul de l'itinéraire le plus rapide entre les lignes qui déservent 
	 * l'arrêt choisi par l'utilisateur et celle qui sont dans la zone de 
	 * l'utilisateur, on va d'abord chercher les lignes qu'on ces deux zone en
	 * commun, puis en fonction des temps de départ on va calculer le plus 
	 * rapide et le retouner, avec le temps de parcours à vélo entre la zone 
	 * de vélo la plus proche de l'utilisateur(possédant des vélo disponible)
	 * et la zone de vélo la plus proche de l'arrêt où veut aller l'utilisateur
	 * @param listeArretProximite, les lignes proches de l'utilisateur
	 * @param listeLigneArretChoisi, les lignes qui déservent l'arrêt choisi
	 * @param id, identificateur de l'arrêt choisi
	 * @param x
	 * @param y
	 * @return
	 */
	@SuppressWarnings("unused")
	private String [] calculPlusCourt(HashMap<String, String> listeArretProximite,
			HashMap<String, String> listeLigneArretChoisi, String id, String x, String y) {
		Map.Entry<String, String> resultat = null;
		ArrayList<String> listeLigneArret = new ArrayList<String>();
		ArrayList<String> listeLigneCommunes = new ArrayList<String>();
		String plusCourt = null;
		String nouvelleCle, duree = null, prochainDepartPlusRapide = null;
		String coordDepart = null;
		String [] tempsBusVeloLike = new String[3];
		RequestVelo requeteVelo;
		Integer nouveauTemps = null;
		int nbSeconde = 0;
		double plusProche = 0;//distance
		double courrante;
		double temps = 0.0;
		Double [] coord1;
		DB baseLignes = new DB(URL, NOM_BASE, PWD);
		ArrayList<Ligne> listeLignesBD = baseLignes.getLignes();
		Ligne plusAimee = null;
		RequestTemps requete;
		for (Map.Entry<String, String> entry : listeLigneArretChoisi.entrySet()) {
			coord1 = getCoord(entry.getValue());
			listeLigneArret.add(entry.getKey().split("[:]")[1]);
			for (Map.Entry<String, String> entry2 : listeArretProximite.entrySet()) {
				if(plusCourt == null && 
						listeLigneArret.contains(entry2.getKey().split("[:]")[1])) {
					resultat = entry2;
					coordDepart = entry2.getValue();
					Double [] coord2 = getCoord(entry2.getValue());
					plusProche = CalculPosition.HaversineInM(
							coord1[0], coord1[1], coord2[0], coord2[1]);
					requete = new RequestTemps(entry2.getKey().split("[:]")[0]);
					duree = requete.getResults(entry.getKey().split("[:]")[1]);
					if(duree != null) {
						prochainDepartPlusRapide = duree;
						plusCourt = entry2.getKey();
						nbSeconde = getAttenteSeconde(duree);
						nbSeconde += (plusProche/VITESSE_BUS)*2;
						listeLigneCommunes.add(entry.getKey().split("[:]")[1]);
					}
				} else if(listeLigneArret.contains(entry2.getKey().split("[:]")[1])) {
					nouvelleCle = entry2.getKey();
					Double [] coord2 = getCoord(entry2.getValue());
					courrante =  CalculPosition.HaversineInM(
							coord1[0], coord1[1], coord2[0], coord2[1]);
					requete = new RequestTemps(entry2.getKey().split("[:]")[0]);
					duree = requete.getResults(entry.getKey().split("[:]")[1]);
					nouveauTemps = getAttenteSeconde(duree);
					if(nouveauTemps != null) {
						nouveauTemps += (int)(courrante/VITESSE_BUS)*2;
						if(entry2.getKey().split("[:]")[1].equals(entry.getKey().split("[:]")[1]) && 
								nouveauTemps > -1 && nouveauTemps < nbSeconde) {
							prochainDepartPlusRapide = duree;
							plusCourt = entry2.getKey();
							nbSeconde = nouveauTemps;
							resultat = entry2;
							coordDepart = entry2.getValue();
						}
						listeLigneCommunes.add(entry.getKey().split("[:]")[1]);
					}
				}
			}
		}
		System.out.println(listeLignesBD);
		System.out.println(listeLigneCommunes);
		for(int i = 0; i < listeLignesBD.size(); i++) {
			if(listeLigneCommunes.contains(listeLignesBD.get(i).getId())) {
				System.out.println();
				plusAimee = listeLignesBD.get(i);
				i = listeLignesBD.size();
			} 
		}
		baseLignes.close();
		tempsBusVeloLike = genereResultatItineraire(nbSeconde,
				resultat, prochainDepartPlusRapide ,plusAimee, x, y);
		return tempsBusVeloLike;
	}
	
	/**
	 * Génèration du résultat du calcul de l'itinéraire
	 * @param tempsVelo
	 * @param nbSeconde
	 * @param resultat
	 * @param prochainDepartPlusRapide
	 * @param plusAimee
	 * @param x
	 * @param y
	 * @return Ligne de bus la plus rapide, départ de vélo le plus proche
	 * pour rejoindre l'arrêt de vélo le plus proche, ligne de bus la plus liker
	 * desservant cet arret
	 */
	public String [] genereResultatItineraire( int nbSeconde,
			Map.Entry<String, String> resultat, String prochainDepartPlusRapide,
			Ligne plusAimee, String x, String y) {
		Double tempsVelo;
		String veloDepart, veloArrivee; 
		String [] tempsBusVeloLike = new String[3];
		veloDepart = new RequestVelo().getVelo(POS_X, POS_Y);
		veloArrivee = new RequestVelo().getVelo2(Double.parseDouble(x), 
				Double.parseDouble(y));
		tempsVelo = calculTempsVelo(veloDepart, veloArrivee);
		if (nbSeconde != 0){
			tempsBusVeloLike[0] = resultat.getKey().split("[:]")[1] + " en " +
				transformationSecondeParHeure(nbSeconde) + " à l'arrêt "+
				resultat.getKey().split("[:]")[2] +" départ dans " +
				getAttente(prochainDepartPlusRapide);
		} else {
			tempsBusVeloLike[0] = "Impossible de rejoindre la destination";
		}
		if(tempsVelo != null && tempsVelo != 0) {
			tempsBusVeloLike[1] = 
					transformationSecondeParHeure(tempsVelo.intValue()) + 
				" station: " + veloDepart.split("[;]")[0] + " arrivée " + 
				veloArrivee.split("[;]")[0];
		} else {
			tempsBusVeloLike[1] = "Pas de vélo disponible dans la zone ou zone de vélo pleine";
		}
		if(plusAimee != null){
			tempsBusVeloLike[2] =  plusAimee.toString();
		} else {
			tempsBusVeloLike[2] = "Pas de transport disponible";
		}
		return tempsBusVeloLike;
	}

	private Double calculTempsVelo(String veloDepart, String coordDepart) {
		double x1,x2,y1,y2, distance;
		if(veloDepart != null && coordDepart != null) {
			x1 = Double.parseDouble(veloDepart.split("[;]")[1]);
			y1 = Double.parseDouble(veloDepart.split("[;]")[2]);
			x2 = Double.parseDouble(coordDepart.split("[;]")[1]);
			y2 = Double.parseDouble(coordDepart.split("[;]")[2]);
			distance = CalculPosition.HaversineInM(x1, y1, x2, y2);
			return ((distance/VITESSE_VELO)*1.5);
		}
		return null;
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
		return heure+"h" + min + "min" + sec + "sec";
	}
	
	public Integer getAttenteSeconde(String formatHeure) {
		int heure, min, sec, annee, mois, jour;
		Calendar tempsAttente;
		if(formatHeure != null) {
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
		return null;
	}
	
	public String transformationSecondeParHeure(int nbSec) {
		int nbMin, nbHeure = nbSec/3600;
		nbSec -= nbHeure*3600;
		nbMin = nbSec/60;
		nbSec -= nbMin*60;
		return nbHeure + "h" + nbMin + "min" + nbSec + "sec";
	}
	
	public  Double[] getCoord(String param) {
		Double[] rez = new Double[2];
    	rez[0] = Double.parseDouble(param.split(("[;]"))[0]);
    	rez[1] = Double.parseDouble(param.split(("[;]"))[1]);
    	return rez;
    }
}
