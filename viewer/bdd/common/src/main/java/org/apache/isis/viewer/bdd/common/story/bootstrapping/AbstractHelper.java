package org.apache.isis.viewer.bdd.common.story.bootstrapping;

import org.apache.isis.viewer.bdd.common.Scenario;

public abstract class AbstractHelper {

    private final Scenario story;

    public AbstractHelper(final Scenario story) {
        this.story = story;
    }

    protected Scenario getStory() {
		return story;
	}

}
