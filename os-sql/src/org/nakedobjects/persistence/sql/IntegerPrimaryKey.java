package org.nakedobjects.persistence.sql;

import org.nakedobjects.object.io.TransferableReader;
import org.nakedobjects.object.io.TransferableWriter;




public class IntegerPrimaryKey implements PrimaryKey{
	private final int primaryKey;

	public IntegerPrimaryKey(int primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	public IntegerPrimaryKey(TransferableReader reader) {
		this.primaryKey = reader.readInt();
	}

	public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof IntegerPrimaryKey) {
            IntegerPrimaryKey o = ((IntegerPrimaryKey) obj);
			return primaryKey == o.primaryKey;
        }
        return false;
    }

	public String stringValue() {
		return "" + primaryKey;
	}
	
    public int hashCode() {
    	int hash = 17;
    	hash = 37 * hash + primaryKey;
         return hash;
    }
    
    public String toString() {
		return "" + primaryKey;
	}
    
    public void writeData(TransferableWriter writer) {
        writer.writeInt(primaryKey);
    }
}