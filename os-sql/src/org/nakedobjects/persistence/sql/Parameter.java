package org.nakedobjects.persistence.sql;

import org.nakedobjects.object.ObjectPerstsistenceException;

public interface Parameter {
	void setupParameter(int parameter, StoredProcedure procedure) throws ObjectPerstsistenceException;

//	String getRestoreString();

	void retrieve(int parameter, StoredProcedure procedure) throws ObjectPerstsistenceException;
}
