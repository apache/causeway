package org.nakedobjects.persistence.sql2.mysql;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

public class VarcharOutputParameter implements Parameter {
	private String fieldName;
	private String value;

	public VarcharOutputParameter(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getRestoreString() {
		return  value == null ? "NULL" : value;
	}

	public void retrieve(int i, CallableStatement statement) throws SQLException {
		value = statement.getString(i);
	}

	public void setupParameter(int parameter, CallableStatement statement) throws SQLException {
		statement.registerOutParameter(parameter, Types.VARCHAR);
	}

	public String toString() {
		return "Output - VARCHAR: " + value;
	}

}