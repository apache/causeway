package org.nakedobjects.persistence.sql2.mysql;

import java.sql.CallableStatement;
import java.sql.SQLException;

public interface Parameter {
	void setupParameter(int parameter, CallableStatement statement) throws SQLException;

	String getRestoreString();

	void retrieve(int i, CallableStatement statement) throws SQLException;
}
