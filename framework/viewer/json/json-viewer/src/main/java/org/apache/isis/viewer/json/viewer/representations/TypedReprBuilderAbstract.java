package org.apache.isis.viewer.json.viewer.representations;

import org.apache.isis.viewer.json.viewer.ResourceContext;

public abstract class TypedReprBuilderAbstract<R extends TypedReprBuilderAbstract<R, T>, T> extends ReprBuilderAbstract<R>  implements TypedReprBuilder<R, T> {

    public TypedReprBuilderAbstract(ResourceContext resourceContext) {
        super(resourceContext);
    }
    

}
