package genius.core.tournament.VariablesAndValues;

/**
 * @author mark
 * Store the database location
 */
public class DBLocationValue extends TournamentValue
{
	private static final long serialVersionUID = 1L;
	String value;	
	
	public DBLocationValue(String val) { value=val; }
	
	public String toString() { return value; }
	
	public String getValue(){ return value;	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DBLocationValue other = (DBLocationValue) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}