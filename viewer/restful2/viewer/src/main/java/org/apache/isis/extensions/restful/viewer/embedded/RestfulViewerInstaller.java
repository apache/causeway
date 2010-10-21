package org.apache.isis.extensions.restful.viewer.embedded;

import org.apache.isis.runtime.viewer.IsisViewer;
import org.apache.isis.runtime.viewer.IsisViewerInstallerAbstract;
import org.apache.isis.runtime.web.WebAppSpecification;

/**
 * Convenience implementation of a {@link NakedObjectsViewer} providing the
 * ability to configured for the Restful viewer from the {@link NakedObjects command line} using
 * <tt>--viewer org.starobjects.restful.viewer.embedded.RestfulViewerInstaller</tt> flag.
 * 
 * <p>
 * In a production deployment the configuration represented by the
 * {@link WebAppSpecification} would be specified in the <tt>web.xml<tt> file.
 */
public class RestfulViewerInstaller extends IsisViewerInstallerAbstract {

	static final String JAVAX_WS_RS_APPLICATION = "javax.ws.rs.Application";
	
	protected static final String EVERYTHING = "*";
	protected static final String ROOT = "/";
	protected static final String[] STATIC_CONTENT = new String[]{"*.js", "*.gif", "*.png"};

	public RestfulViewerInstaller() {
		super("restful");
	}
	
	@Override
	protected IsisViewer doCreateViewer() {
		return new EmbeddedWebViewerRestful();
	}
	
	

}
