package org.apache.isis.viewer.json.viewer.representations;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;


public interface ReprRenderer<R extends ReprRenderer<R, T>, T> {

    RepresentationType getRepresentationType();
    
    R with(T t);

    public JsonRepresentation render();

}
