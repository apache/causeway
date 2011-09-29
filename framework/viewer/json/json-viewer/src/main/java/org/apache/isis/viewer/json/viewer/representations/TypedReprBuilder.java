package org.apache.isis.viewer.json.viewer.representations;


public interface TypedReprBuilder<R extends TypedReprBuilder<R, T>, T> extends ReprBuilder {

    R with(T t);

}
