package org.nakedobjects.persistence.sql;



public class StringPrimaryKey implements PrimaryKey{
	private final String primaryKey;

	public StringPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof StringPrimaryKey) {
            StringPrimaryKey o = ((StringPrimaryKey) obj);
			return primaryKey == o.primaryKey;
        }
        return false;
    }

	public String stringValue() {
		return "" + primaryKey;
	}
	
    public int hashCode() {
    	int hash = 17;
    	hash = 37 * hash + primaryKey.hashCode();
         return hash;
    }
    
    public String toString() {
		return "" + primaryKey;
	}
}