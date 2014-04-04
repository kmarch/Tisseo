package tisseo.db;



import java.sql.DriverManager;  // gestion des pilotes
import java.sql.Connection;     // une connexion à la BD
import java.sql.Statement;      // une instruction 
import java.sql.ResultSet;      // un résultat (lignes/colonnes)
import java.sql.SQLException;   // une erreur
import java.util.ArrayList;

/**
 * classe de manipulation dde base de donnée postgres
 * @author kevin marchois
 *
 */
public class DB {

    /** objet de connexion pour la bd*/
	private Connection connexion;
	
	private Statement st;
	/**
	 * initialisation de la classe de connexion
	 */
	public DB (String url, String bd, String pwd) {
		try {
			loadDriver();
			connection(url, bd, pwd);
		} catch (SQLException e) {
			System.out.println("erreur sql");
		} catch (ClassNotFoundException e) {
			System.out.println("erreur bd");
			e.printStackTrace();
		}
	}
	
	/**
	 * chargement de driver
	 */
	public void loadDriver() throws ClassNotFoundException {
	    Class.forName("org.postgresql.Driver");
	}
	
	/**
	 * initialisation de la connexion à une bd
	 * @param url, url de la base de donnée
	 * @param bd, nom de la base de donnée
	 * @pwd, mot de passe de la bd
	 * @throws SQLException
	 */
	public void connection(String url, String bd, String pwd) throws SQLException {
		connexion = DriverManager.getConnection(url, bd, pwd);
		st = connexion.createStatement();
	}
	
	/**
	 * returne une liste de résultat dee la requete passée en argument
	 * @param requette, requette à soumettre
	 * @return la liste de résultat
	 * @throws SQLException 
	 */
	public ResultSet query(String requette) throws SQLException {
		 return st.executeQuery(requette);
	}
	
	public ArrayList<Ligne> getLignes() {
		String requete = "select numligne, nombre from ligne";
		ArrayList<Ligne> listeLignes = new ArrayList<Ligne>();
		try {
			ResultSet resultatQ = st.executeQuery(requete);
			while(resultatQ.next()){
				Ligne courante = new Ligne(resultatQ.getString("numligne"),
						Integer.parseInt(resultatQ.getString("nombre")));
				listeLignes.add(courante);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return listeLignes;
	}
	
	public void update(Ligne aUpdate) {
		String requete = String.format("update ligne set nombre = %s where " +
				"numligne = %s", aUpdate.getLike(), aUpdate.getId());
		try {
			st.executeQuery(requete);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void insere(Ligne aInsere) {
		String requete = String.format("insert into ligne values(%s, %s)",
				aInsere.getId(), aInsere.getLike());
		try {
			st.executeQuery(requete);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void close () {
		try {
			connexion.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}	
}
