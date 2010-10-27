package org.apache.isis.viewer.bdd.common.story.bootstrapping;

import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.system.IsisSystem;
import org.apache.isis.viewer.bdd.common.Story;

public class ShutdownNakedObjects extends AbstractHelper {

    public ShutdownNakedObjects(final Story story) {
        super(story);
    }

    public void shutdown() {
        final IsisSystem system = getStory().getSystem();

        IsisContext.closeAllSessions();

        if (system != null) {
            system.shutdown();
        }

    }

}
