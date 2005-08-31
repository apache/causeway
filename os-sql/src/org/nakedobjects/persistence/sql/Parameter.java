package org.nakedobjects.persistence.sql;

import org.nakedobjects.object.persistence.ObjectManagerException;

public interface Parameter {
	void setupParameter(int parameter, StoredProcedure procedure) throws ObjectManagerException;

//	String getRestoreString();

	void retrieve(int parameter, StoredProcedure procedure) throws ObjectManagerException;
}
