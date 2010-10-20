package org.apache.isis.extensions.bdd.common.story.bootstrapping;

import org.apache.isis.extensions.bdd.common.Story;
import org.apache.isis.runtime.installers.InstallerLookup;
import org.apache.isis.runtime.viewer.IsisViewer;
import org.apache.isis.runtime.viewer.IsisViewerInstaller;

public class StartClient extends AbstractHelper {

	private static final String DND_VIEWER_NAME = "dnd";

	public StartClient(final Story story) {
		super(story);
	}

	public void run() {
		final InstallerLookup installerLookup = getStory().getInstallerLookup();

		final IsisViewerInstaller viewerInstaller = installerLookup
				.viewerInstaller(DND_VIEWER_NAME);
		final IsisViewer viewer = viewerInstaller.createViewer();

		viewer.init();
	}

}
