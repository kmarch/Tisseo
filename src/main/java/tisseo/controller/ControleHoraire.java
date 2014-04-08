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
import tisseo.request.RequestArret;
import tisseo.request.RequestBBox;
import tisseo.request.RequestLigne;
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

	@RequestMapping("/frmLignes")
	public String frmLigne() {
		return "frmLignes";
	}

	@RequestMapping("/frmVelo")
	public String frmVelo() {
		return "frmVelo";
	}
	
	@RequestMapping("/frmItineraire")
	public String frmItineraire() {
		return "frmItineraire";
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
	
	@RequestMapping("/listeAllLignes")
	public String listeAllLignes(Model model) {
		String resultat;
		boolean estPresent;
		DB baseLignes = new DB(URL, NOM_BASE, PWD);
		RequestLigne requete = new RequestLigne();
		ArrayList<Ligne> listeLignesBD = baseLignes.getLignes();
		ArrayList<String> listeLignesTisseo = requete.getResults();
		for(int i = 0; i < listeLignesTisseo.size(); i++) {
			estPresent = false;
			for(int j = 0; j< listeLignesBD.size() && !estPresent; j++) {
				if(listeLignesBD.get(j).getId().equals(listeLignesTisseo.get(i))) {
					estPresent = true;
				}
			}
			if(!estPresent) {
				baseLignes.insere(new Ligne(listeLignesTisseo.get(i)));
				listeLignesBD.add(new Ligne(listeLignesTisseo.get(i)));
			}
		}
		resultat = "";
		for(int i = 0; i < listeLignesBD.size(); i++) {
			resultat += "ligne " + listeLignesBD.get(i).getId() + ":" + 
					listeLignesBD.get(i).getLike()+ " likes " +
					"<a href ='incrementeLigne?id=" + listeLignesBD.get(i).getId()+
					"' data-ajax='false'><IMG SRC='images/green-plus-sign-md.png'/> </a>" +
					"<a href ='decrementeLigne?id=" + listeLignesBD.get(i).getId()+
					"' data-ajax='false'><IMG SRC='images/forbidden.png'/></a> " +
					"<BR/>";  
		}
		baseLignes.close();
		model.addAttribute("liste", resultat);
		return "listeAllLignes";
	}
	
	@RequestMapping("/incrementeLigne")
	public String incrementeLigne(@RequestParam(value="id", required=true) String id,
			Model model) {
		DB baseLigne = new DB(URL, NOM_BASE, PWD); 
		Ligne ligne = baseLigne.getLigne(id);
		ligne.incr();
		baseLigne.update(ligne);
		return "index";
	}
	
	@RequestMapping("/decrementeLigne")
	public String decrementeLigne(@RequestParam(value="id", required=true) String id,
			Model model) {
		DB baseLigne = new DB(URL, NOM_BASE, PWD); 
		Ligne ligne = baseLigne.getLigne(id);
		ligne.decr();
		baseLigne.update(ligne);
		return "index";
	}
	
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
		System.out.println(listeArretProximite.size()+"ici"+ listeArretProximite );
		requete = new RequestBBox(null, null, listeArgs);
		listeArgs.put("stopAreaId", id);
		listeLigneArretChoisi = requete.getResultsListeLignesZone(id);
		System.out.println(id+"la" + listeLigneArretChoisi);
		itineraire = calculPlusCourt(listeArretProximite, 
				listeLigneArretChoisi, id, x , y);
		model.addAttribute("bus", itineraire[0]);
		model.addAttribute("velo", itineraire[1]);
		model.addAttribute("like", itineraire[2]);
		return "reponseRapidite";
	}

	@SuppressWarnings("unused")
	private String [] calculPlusCourt(HashMap<String, String> listeArretProximite,
			HashMap<String, String> listeLigneArretChoisi, String id, String x, String y) {
		Map.Entry<String, String> resultat = null;
		ArrayList<String> listeLigneArret = new ArrayList<String>();
		String plusCourt = null;
		String nouvelleCle;
		String duree;
		String coordDepart = null;
		String veloDepart = null, veloArrivee = null;
		String [] tempsBusVeloLike = new String[3];
		RequestVelo requeteVelo;
		Integer tempsVelo = null,nouveauTemps = null;
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
						plusCourt = entry2.getKey();
						nbSeconde = getAttenteSeconde(duree);
						nbSeconde += (plusProche/VITESSE_BUS)*2;
						for(int i = 0; i < listeLignesBD.size(); i++) {
							if(plusAimee == null && duree != null&& 
									listeLignesBD.get(i).getId().
									equals(entry2.getKey().split("[:]")[1])) {
								plusAimee = listeLignesBD.get(i);
							} else if( duree != null && 
									listeLignesBD.get(i).getId().
									equals(entry2.getKey().split("[:]")[1]) &&
									listeLignesBD.get(i).getLike() > plusAimee.getLike()) {
								plusAimee = listeLignesBD.get(i);
							}
						}
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
							plusCourt = entry2.getKey();
							nbSeconde = nouveauTemps;
							resultat = entry2;
							coordDepart = entry2.getValue();
						}
						for(int i = 0; i < listeLignesBD.size(); i++) {
							if(plusAimee == null && duree != null&& 
									listeLignesBD.get(i).getId().
									equals(entry2.getKey().split("[:]")[1])) {
								plusAimee = listeLignesBD.get(i);
							} else if( duree != null && 
									listeLignesBD.get(i).getId().
									equals(entry2.getKey().split("[:]")[1]) &&
									listeLignesBD.get(i).getLike() > plusAimee.getLike()) {
								plusAimee = listeLignesBD.get(i);
							}
						}
					}
				}
			}
		}
		baseLignes.close();
		tempsBusVeloLike = genereResultatItineraire(veloDepart, 
				veloArrivee, tempsVelo, nbSeconde,
				resultat, plusAimee, x, y);
		
		return tempsBusVeloLike;
	}
	
	public String [] genereResultatItineraire(String veloDepart, 
			String veloArrivee, Integer tempsVelo, int nbSeconde,
			Map.Entry<String, String> resultat, Ligne plusAimee, String x,
			String y) {
		String [] tempsBusVeloLike = new String[3];
		veloDepart = new RequestVelo().getVelo(POS_X, POS_Y);
		veloArrivee = new RequestVelo().getVelo(Double.parseDouble(x), 
				Double.parseDouble(y));
		tempsVelo = calculTempsVelo(veloDepart, veloArrivee);
		if (nbSeconde != 0){
			tempsBusVeloLike[0] = resultat.getKey().split("[:]")[1] + " en " +
				transformationSecondeParHeure(nbSeconde) + " à l'arrêt "+
				resultat.getKey().split("[:]")[2];
		} else {
			tempsBusVeloLike[0] = "Impossible de rejoindre la destination";
		}
		if(tempsVelo != null) {
			tempsBusVeloLike[1] = "Temps à vélo:" +
				transformationSecondeParHeure((int)tempsVelo) + 
				" station: " + veloDepart.split("[;]")[0];
		} else {
			tempsBusVeloLike[1] = "Pas de vélo disponible dans la zone";
		}
		tempsBusVeloLike[2] =  plusAimee.toString();
		
		return tempsBusVeloLike;
	}

	private Integer calculTempsVelo(String veloDepart, String coordDepart) {
		double x1,x2,y1,y2, temps, distance;
		if(veloDepart != null && coordDepart != null) {
			x1 = Double.parseDouble(veloDepart.split("[;]")[1]);
			y1 = Double.parseDouble(veloDepart.split("[;]")[2]);
			x2 = Double.parseDouble(coordDepart.split("[;]")[1]);
			y2 = Double.parseDouble(coordDepart.split("[;]")[2]);
			distance = CalculPosition.HaversineInM(x1, y1, x2, y2);
			return (int) ((distance/VITESSE_VELO)*1.5);
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
