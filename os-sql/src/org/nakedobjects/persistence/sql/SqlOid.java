package org.nakedobjects.persistence.sql;


public class SqlOid {
	private final String className;
	private final int primaryKey;

	public SqlOid(int primaryKey, String className) {
		//super(primaryKey);
		this.primaryKey = primaryKey;
		this.className = className;
	}

	public String getClassName() {
		return className;
	}
	
	public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof SqlOid) {
            SqlOid o = ((SqlOid) obj);
			return primaryKey == o.primaryKey && className.equals(o.className);
        }
        return false;
    }

	public int getPrimaryKey() {
		return primaryKey;
	}
	
    public int hashCode() {
    	int hash = 17;
    	hash = 37 * hash + primaryKey;
    	hash = 37 * hash + className.hashCode();
        return hash;
    }
    
    public String toString() {
		return "DOID#" + primaryKey + "/" + className;
	}
}