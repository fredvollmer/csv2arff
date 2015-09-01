package csv2arff;

import java.util.HashSet;
import java.util.Set;

public class Attribute {
	
	public String name;
	private String type;
	private Set<String> categories;

	public Attribute (String attName, String typeName) {
		categories = new HashSet<String> ();
		this.name = attName;
		this.type = typeName;
	}
	
	// Return category names in {}
	public String categories () {
		return "{" + categories.toString().replace("[", "").replace("]", "").replace(" ", "") + "}";
	}
	
	public Boolean hasCats() {
		if (this.categories.size() > 0) return true;
		return false;
	}
	
	public String type () {
		return this.type.toUpperCase();
	}
	
	public void addCategory(String cat) {
		this.categories.add(cat);
	}
}
