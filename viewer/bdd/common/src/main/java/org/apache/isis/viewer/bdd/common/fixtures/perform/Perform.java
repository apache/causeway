package org.apache.isis.viewer.bdd.common.fixtures.perform;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;

public interface Perform {

    public static enum Mode {
		SETUP, TEST
	}

	String getKey();

    void perform(PerformContext performContext) throws ScenarioBoundValueException;

    ObjectAdapter getResult();

    boolean requiresMember();
}
