package agents.similarity;


public enum SIMILARITYTYPE {
	CUSTOM_DEFINED, BINARY;

	public static SIMILARITYTYPE  convertToType(String typeString) {
		if (typeString==null) {
			return CUSTOM_DEFINED;
		} else
		if (typeString.equalsIgnoreCase("binary"))
        	return BINARY;
        else {
        	// Type specified incorrectly!
        	//System.out.println("Similarity  type specified incorrectly.");
        	// For now return DISCRETE type.
        	return CUSTOM_DEFINED;
        	// TODO: Define corresponding exception.
        }
	}


}
