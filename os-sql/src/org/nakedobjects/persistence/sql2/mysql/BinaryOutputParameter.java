package org.nakedobjects.persistence.sql2.mysql;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

public class BinaryOutputParameter implements Parameter {
	private String fieldName;
	private boolean value;

	public BinaryOutputParameter(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getRestoreString() {
		return String.valueOf(value);
	}

	public void retrieve(int i, CallableStatement statement) throws SQLException {
		value = statement.getBoolean(i);
	}

	public void setupParameter(int parameter, CallableStatement statement) throws SQLException {
		statement.registerOutParameter(parameter, Types.BIT);
	}

	public String toString() {
		return "Output - BIT: " + value;
	}
}