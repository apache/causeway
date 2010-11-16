package org.apache.isis.viewer.bdd.common.story.bootstrapping;

import org.apache.isis.core.runtime.installers.InstallerLookup;
import org.apache.isis.core.runtime.viewer.IsisViewer;
import org.apache.isis.core.runtime.viewer.IsisViewerInstaller;
import org.apache.isis.viewer.bdd.common.Story;

public class StartClient extends AbstractHelper {

    private static final String DND_VIEWER_NAME = "dnd";

    public StartClient(final Story story) {
        super(story);
    }

    public void run() {
        final InstallerLookup installerLookup = getStory().getInstallerLookup();

        final IsisViewerInstaller viewerInstaller = installerLookup.viewerInstaller(DND_VIEWER_NAME);
        final IsisViewer viewer = viewerInstaller.createViewer();

        viewer.init();
    }

}
