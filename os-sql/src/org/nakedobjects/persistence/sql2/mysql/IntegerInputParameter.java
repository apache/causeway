package org.nakedobjects.persistence.sql2.mysql;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.nakedobjects.utility.NotImplementedException;

public class IntegerInputParameter implements Parameter {
	private static int type = Types.INTEGER;
	private int value;

	public IntegerInputParameter(int value) {
		this.value = value;
	}

	public String getRestoreString() {
		throw new NotImplementedException();
	}

	public void retrieve(int i, CallableStatement statement) {
	}

	public final void setupParameter(int parameter, CallableStatement statement) throws SQLException {
		statement.setInt(parameter, value);
	}

	public String toString() {
		return "Input - INTEGER: " + value;
	}
}