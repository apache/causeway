package org.apache.isis.viewer.json.viewer.representations;

import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;

public interface TypedReprBuilderFactory {

    RepresentationType getRepresentationType();

    TypedReprBuilder<?, ?> newBuilder(ResourceContext resourceContext);
    
    <R extends TypedReprBuilder<R, T>, T> R newBuilder(ResourceContext resourceContext, Class<T> cls);

}