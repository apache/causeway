package org.apache.isis.viewer.bdd.common.fixtures.perform;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;

public interface Perform {

    public static enum Mode {
		SETUP, TEST
	}

	String getKey();

    void perform(PerformContext performContext) throws StoryBoundValueException;

    ObjectAdapter getResult();

    boolean requiresMember();
}
