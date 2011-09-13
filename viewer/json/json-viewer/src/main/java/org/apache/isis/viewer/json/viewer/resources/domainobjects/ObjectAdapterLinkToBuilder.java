package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.RepBuilder;

public interface ObjectAdapterLinkToBuilder extends RepBuilder {

    ObjectAdapterLinkToBuilder usingResourceContext(ResourceContext resourceContext);
    
    ObjectAdapterLinkToBuilder with(ObjectAdapter objectAdapter);
    
}
