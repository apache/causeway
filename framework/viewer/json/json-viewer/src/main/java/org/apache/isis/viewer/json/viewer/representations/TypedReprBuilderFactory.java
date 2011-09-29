package org.apache.isis.viewer.json.viewer.representations;

import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;

public interface TypedReprBuilderFactory {

    public abstract RepresentationType getRepresentationType();

    public abstract <T> TypedReprBuilder<T> newBuilder(ResourceContext resourceContext, Class<T> cls);

}