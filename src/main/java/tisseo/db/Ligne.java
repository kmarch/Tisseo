package tisseo.db;

import org.ektorp.support.CouchDbDocument;
import org.ektorp.support.TypeDiscriminator;


public class Ligne extends CouchDbDocument {
	
	@TypeDiscriminator
	private String id;
	private int like;
	
	public Ligne(String ligne) {
		this.id = ligne;
		this.like = 0;
	}
	
	public Ligne(String ligne, int nombre) {
		this.id = ligne;
		this.like = nombre;
	}

	public void incr() {
		this.like++;
	}
	
	public void decr(){
		this.like--;
	}
	
	public String toString() {
		return id + " " + like; 
	}
	
	public String getId() {
		return id;
	}
	
	public int getLike() {
		return like;
	}
}
