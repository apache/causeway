package org.apache.isis.viewer.json.viewer.resources.user;

import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.TypedReprBuilder;
import org.apache.isis.viewer.json.viewer.representations.TypedReprBuilderFactoryAbstract;

public class UserReprBuilderFactory extends TypedReprBuilderFactoryAbstract {

    public UserReprBuilderFactory() {
        super(RepresentationType.USER);
    }

    @Override
    public TypedReprBuilder<?, ?> newBuilder(ResourceContext resourceContext) {
        return UserReprBuilder.newBuilder(resourceContext);
    }
    
}