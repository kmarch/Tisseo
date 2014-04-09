package tisseo.controller;

import java.util.ArrayList;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import tisseo.db.DB;
import tisseo.db.Ligne;
import tisseo.request.RequestLigne;

@Controller
@Configuration
public class ControleListeLigne {

	public final static String URL = "jdbc:postgresql:Tisseo";
	public final static String NOM_BASE = "postgres";
	public final static String PWD = "romano";
	
	/**
	 * Liste les lignes de bus de l'api Tisséo et les ajoute dans la base 
	 * de données si elles n'y sont pas présentes
	 * @param model
	 * @return
	 */
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
		return "redirect:/listeAllLignes";
	}
	
	@RequestMapping("/decrementeLigne")
	public String decrementeLigne(@RequestParam(value="id", required=true) String id,
			Model model) {
		DB baseLigne = new DB(URL, NOM_BASE, PWD); 
		Ligne ligne = baseLigne.getLigne(id);
		ligne.decr();
		baseLigne.update(ligne);
		return "redirect:/listeAllLignes";
	}
}
