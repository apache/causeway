package org.apache.isis.viewer.bdd.common.story.bootstrapping;

import org.apache.isis.viewer.bdd.common.Story;

public abstract class AbstractHelper {

    private final Story story;

    public AbstractHelper(final Story story) {
        this.story = story;
    }

    protected Story getStory() {
		return story;
	}

}
