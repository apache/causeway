package org.apache.isis.viewer.restfulobjects.viewer;

import java.util.List;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;

public interface RendererContext {

    public String urlFor(final String url);

    public AuthenticationSession getAuthenticationSession();
    
    public AdapterManager getAdapterManager();

    public List<List<String>> getFollowLinks();
    
    public Where getWhere();
    
    public Localization getLocalization();
}
