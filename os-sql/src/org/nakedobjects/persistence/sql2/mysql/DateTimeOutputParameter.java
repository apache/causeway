package org.nakedobjects.persistence.sql2.mysql;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

public class DateTimeOutputParameter implements Parameter {
	private String fieldName;
	private String value;

	public DateTimeOutputParameter(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getRestoreString() {
		return value == null ? "NULL" : value.substring(0, 4) + value.substring(5, 7) + value.substring(8, 10);
	}

	public void retrieve(int i, CallableStatement statement) throws SQLException {
		value = statement.getString(i);
	}

	public void setupParameter(int parameter, CallableStatement statement) throws SQLException {
		statement.registerOutParameter(parameter, Types.DATE);
	}

	public String toString() {
		return "Output - DATE: " + value + "(" + getRestoreString() + ")";
	}

}