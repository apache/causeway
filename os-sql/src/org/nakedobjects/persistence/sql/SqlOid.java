package org.nakedobjects.persistence.sql;

import java.io.Serializable;


public final class SqlOid implements Serializable {
	private final String className;
    private final PrimaryKey primaryKey;

	public SqlOid(String className, PrimaryKey primaryKey) {
		this.className = className;
		this.primaryKey = primaryKey;
	}

	public String getClassName() {
		return className;
	}

	public PrimaryKey getPrimaryKey() {
	    return primaryKey;
	}
	
	public String stringValue() {
	    return primaryKey.stringValue();    
	}
		
	public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof SqlOid) {
            SqlOid o = ((SqlOid) obj);
			return className.equals(o.className) && primaryKey.equals(o.primaryKey);
        }
        return false;
    }
	
    public int hashCode() {
    	int hash = 17;
    	hash = 37 * hash + primaryKey.hashCode();
    	hash = 37 * hash + className.hashCode();
        return hash;
    }
    
    public String toString() {
		return "DOID#" + primaryKey + "/" + className;
	}
}