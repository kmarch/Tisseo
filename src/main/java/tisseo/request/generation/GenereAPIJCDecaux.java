package tisseo.request.generation;

import tisseo.request.generation.inter.GenereAPI;

public class GenereAPIJCDecaux implements GenereAPI {

	private final String key = "96eaf612db44a393a3178411bfda6fda4e66b40c";
	
	@Override
	public String genRequest(String action, String format, Object arg, String argValue) {
		String resultat ="https://api.jcdecaux.com/vls/v1/" + action + "?" +
				arg + "=" + argValue + "&apiKey=" + key;
		return resultat;
	}

	
}
