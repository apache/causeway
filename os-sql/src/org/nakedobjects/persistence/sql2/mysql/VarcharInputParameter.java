package org.nakedobjects.persistence.sql2.mysql;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.nakedobjects.utility.NotImplementedException;

public class VarcharInputParameter implements Parameter {
	private static int type = Types.VARCHAR;
	private String value;

	public VarcharInputParameter(String value) {
		this.value = value;
	}

	public String getRestoreString() {
		throw new NotImplementedException();
	}

	public void retrieve(int i, CallableStatement statement) throws SQLException {
	}

	public final void setupParameter(int parameter, CallableStatement statement) throws SQLException {
		statement.setString(parameter, value);
	}

	public String toString() {
		return "Input - VARCHAR: " + value;
	}
}