package org.nakedobjects.persistence.sql;

import org.nakedobjects.object.ObjectStoreException;

public interface Parameter {
	void setupParameter(int parameter, StoredProcedure procedure) throws ObjectStoreException;

//	String getRestoreString();

	void retrieve(int parameter, StoredProcedure procedure) throws ObjectStoreException;
}
