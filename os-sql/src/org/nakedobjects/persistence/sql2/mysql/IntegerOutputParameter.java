package org.nakedobjects.persistence.sql2.mysql;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

public class IntegerOutputParameter implements Parameter {
	private String fieldName;
	private int value;

	public IntegerOutputParameter(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getRestoreString() {
		return String.valueOf(value);
	}

	public void retrieve(int i, CallableStatement statement) throws SQLException {
		value = statement.getInt(i);
	}

	public void setupParameter(int parameter, CallableStatement statement) throws SQLException {
		statement.registerOutParameter(parameter, Types.INTEGER);
	}

	public String toString() {
		return "Output - INTEGER: " + value;
	}

	public int getInt() {
		return value;
	}

}