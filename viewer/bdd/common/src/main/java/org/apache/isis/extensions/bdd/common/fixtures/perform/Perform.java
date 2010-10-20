package org.apache.isis.extensions.bdd.common.fixtures.perform;

import org.apache.isis.extensions.bdd.common.StoryBoundValueException;
import org.apache.isis.metamodel.adapter.ObjectAdapter;

public interface Perform {

    public static enum Mode {
		SETUP, TEST
	}

	String getKey();

    void perform(PerformContext performContext) throws StoryBoundValueException;

    ObjectAdapter getResult();

    boolean requiresMember();
}
