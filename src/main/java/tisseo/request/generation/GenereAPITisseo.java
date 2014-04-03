package tisseo.request.generation;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import tisseo.request.generation.inter.GenereAPI;

public class GenereAPITisseo implements GenereAPI {

	private final String key = "a03561f2fd10641d96fb8188d209414d8";

	protected Map<String,String> listeArgs;

	public GenereAPITisseo(Map<String,String> listeArgs) {
		this.listeArgs = listeArgs;
	}
	
	public GenereAPITisseo() {
	}

	public String genRequest(String action, String format, Object arg, String argValue) {
		String resultat = "http://pt.data.tisseo.fr/"+ action + "?" + "format=" + format +
				"&key=" + key;
		if(arg != null) {
			resultat += "&" + arg + "=" +argValue;
		} else if(listeArgs != null) {
			for (Map.Entry<String, String> entry : listeArgs.entrySet()) {
				resultat += "&" + entry.getKey() + "=" + entry.getValue();
			}
		}
		return resultat; 
	}
}
