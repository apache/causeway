package org.apache.isis.viewer.bdd.common.story.bootstrapping;

import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.IsisSystem;
import org.apache.isis.viewer.bdd.common.Scenario;

public class ShutdownIsis extends AbstractHelper {

    public ShutdownIsis(final Scenario story) {
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
