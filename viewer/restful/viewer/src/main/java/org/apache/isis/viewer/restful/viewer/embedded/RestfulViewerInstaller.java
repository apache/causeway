package org.apache.isis.viewer.restful.viewer.embedded;

import org.apache.isis.Isis;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.IsisViewerInstallerAbstract;
import org.apache.isis.runtimes.dflt.runtime.viewer.IsisViewer;
import org.apache.isis.runtimes.dflt.runtime.viewer.web.WebAppSpecification;

/**
 * Convenience implementation of a {@link IsisViewer} providing the ability to configured for the Restful viewer from
 * the {@link Isis command line} using <tt>--viewer restful</tt> command line option.
 * 
 * <p>
 * In a production deployment the configuration represented by the {@link WebAppSpecification} would be specified in the
 * <tt>web.xml<tt> file.
 */
public class RestfulViewerInstaller extends IsisViewerInstallerAbstract {

    static final String JAVAX_WS_RS_APPLICATION = "javax.ws.rs.Application";

    protected static final String EVERYTHING = "*";
    protected static final String ROOT = "/";
    protected static final String[] STATIC_CONTENT = new String[] { "*.js", "*.gif", "*.png" };

    public RestfulViewerInstaller() {
        super("restful");
    }

    @Override
    protected IsisViewer doCreateViewer() {
        return new EmbeddedWebViewerRestful();
    }

}
