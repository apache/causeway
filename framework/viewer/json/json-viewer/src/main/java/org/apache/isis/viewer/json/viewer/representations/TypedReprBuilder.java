package org.apache.isis.viewer.json.viewer.representations;


public interface TypedReprBuilder<T> extends ReprBuilder {

    TypedReprBuilder<T> withSelf();

    TypedReprBuilder<T> with(T t);

}
