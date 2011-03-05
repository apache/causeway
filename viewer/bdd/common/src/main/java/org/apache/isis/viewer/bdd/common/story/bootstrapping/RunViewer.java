package org.apache.isis.viewer.bdd.common.story.bootstrapping;

import org.apache.isis.runtimes.dflt.runtime.installers.InstallerLookup;
import org.apache.isis.runtimes.dflt.runtime.viewer.IsisViewer;
import org.apache.isis.runtimes.dflt.runtime.viewer.IsisViewerInstaller;
import org.apache.isis.viewer.bdd.common.Scenario;

public class RunViewer extends AbstractHelper {

    private static final String DND_VIEWER_NAME = "dnd";

    public RunViewer(final Scenario story) {
        super(story);
    }

    public void run() {
        final InstallerLookup installerLookup = getStory().getInstallerLookup();

        final IsisViewerInstaller viewerInstaller = installerLookup.viewerInstaller(DND_VIEWER_NAME);
        final IsisViewer viewer = viewerInstaller.createViewer();

        viewer.init();
    }

}
