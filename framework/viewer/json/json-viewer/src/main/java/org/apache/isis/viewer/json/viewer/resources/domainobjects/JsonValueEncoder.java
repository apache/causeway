package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.json.applib.JsonRepresentation;

/**
 * Similar to Isis' value encoding, but with additional support for JSON primitives. 
 */
public final class JsonValueEncoder {

    static class ExpectedStringRepresentingValueException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
    }

    public ObjectAdapter asAdapter(final ObjectSpecification objectSpec, final JsonRepresentation representation) {
        EncodableFacet encodableFacet = objectSpec.getFacet(EncodableFacet.class);

        // special case handling for JSON built-ins
        if(isBoolean(objectSpec)) {
            if(representation.isBoolean()) {
                String argStr = ""+representation.asBoolean();
                return encodableFacet.fromEncodedString(argStr);
            } else {
                throw new IllegalArgumentException("ObjectSpec is Boolean.class or boolean.class but representation is not a JSON boolean");
            }
        }
        
        if(!representation.isString()) {
            throw new ExpectedStringRepresentingValueException();
        }
        String argStr = representation.asString();
        return encodableFacet.fromEncodedString(argStr);
    }

    public Object asObject(ObjectAdapter objectAdapter) {
        
        final ObjectSpecification objectSpec = objectAdapter.getSpecification();

        // special case handling for JSON built-ins
        if(isBoolean(objectSpec)) {
            // simply return
            return objectAdapter.getObject();
        }
        
        EncodableFacet encodeableFacet = objectSpec.getFacet(EncodableFacet.class);
        return encodeableFacet.toEncodedString(objectAdapter);
    }



    private boolean isBoolean(ObjectSpecification objectSpec) {
        final Class<?> specClass = objectSpec.getCorrespondingClass();
        return specClass == boolean.class || specClass == Boolean.class;
    }


}
