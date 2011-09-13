package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkToBuilder;

public interface ObjectAdapterLinkToBuilder {

    ObjectAdapterLinkToBuilder usingResourceContext(ResourceContext resourceContext);
    
    ObjectAdapterLinkToBuilder with(ObjectAdapter objectAdapter);

    LinkToBuilder linkToAdapter();

    LinkToBuilder linkToMember(String rel, MemberType memberType, ObjectMember objectMember, String... parts);


}
