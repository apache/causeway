package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;

public interface ObjectAdapterLinkTo {

    ObjectAdapterLinkTo usingResourceContext(ResourceContext resourceContext);
    
    ObjectAdapterLinkTo with(ObjectAdapter objectAdapter);

    LinkBuilder builder();

    LinkBuilder linkToMember(String rel, MemberType memberType, ObjectMember objectMember, String... parts);


}
