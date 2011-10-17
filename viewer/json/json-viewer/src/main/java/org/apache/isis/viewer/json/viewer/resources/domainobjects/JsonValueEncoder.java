package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import java.math.BigDecimal;
import java.math.BigInteger;

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
        if(objectSpec == null) {
            throw new IllegalArgumentException("objectSpec cannot be null");
        }
        final EncodableFacet encodableFacet = objectSpec.getFacet(EncodableFacet.class);
        if(encodableFacet == null) {
            throw new IllegalArgumentException("objectSpec expected to have EncodableFacet");
        }
        if(representation == null) {
            throw new IllegalArgumentException("representation cannot be null");
        }
        if(!representation.isValue()) {
            throw new IllegalArgumentException("representation must be of a value");
        }

        // special case handling for JSON built-ins
        if(isBoolean(objectSpec)) {
            if(!representation.isBoolean()) {
                throwIncompatibleException(objectSpec, representation);
            } 
            String argStr = ""+representation.asBoolean();
            return encodableFacet.fromEncodedString(argStr);
        }
        
        if(isInteger(objectSpec)) {
            if(!representation.isInt()) {
                throwIncompatibleException(objectSpec, representation);
            } 
            String argStr = ""+representation.asInt();
            return encodableFacet.fromEncodedString(argStr);
        }
        
        if(isLong(objectSpec)) {
            if(!representation.isLong()) {
                throwIncompatibleException(objectSpec, representation);
            } 
            String argStr = ""+representation.asLong();
            return encodableFacet.fromEncodedString(argStr);
        }
        
        if(isBigInteger(objectSpec)) {
            if(!representation.isBigInteger()) {
                throwIncompatibleException(objectSpec, representation);
            } 
            String argStr = ""+representation.asBigInteger();
            return encodableFacet.fromEncodedString(argStr);
        }
        
        if(isBigDecimal(objectSpec)) {
            if(!representation.isBigDecimal()) {
                throwIncompatibleException(objectSpec, representation);
            } 
            String argStr = ""+representation.asBigDecimal();
            return encodableFacet.fromEncodedString(argStr);
        }
        
        if(isDouble(objectSpec)) {
            if(!representation.isDouble()) {
                throwIncompatibleException(objectSpec, representation);
            } 
            String argStr = ""+representation.asDouble();
            return encodableFacet.fromEncodedString(argStr);
        }
        
        if(!representation.isString()) {
            throw new ExpectedStringRepresentingValueException();
        }
        String argStr = representation.asString();
        return encodableFacet.fromEncodedString(argStr);
    }

    public Object asObject(ObjectAdapter objectAdapter) {
        if(objectAdapter == null) {
            throw new IllegalArgumentException("objectAdapter cannot be null");
        }
        final ObjectSpecification objectSpec = objectAdapter.getSpecification();

        // special case handling for JSON built-ins
        if(     isBoolean(objectSpec) || 
                isInteger(objectSpec) || isLong(objectSpec) || isBigInteger(objectSpec) || 
                isDouble(objectSpec) || isBigDecimal(objectSpec) ) {
            // simply return
            return objectAdapter.getObject();
        }
        
        final EncodableFacet encodableFacet = objectSpec.getFacet(EncodableFacet.class);
        if(encodableFacet == null) {
            throw new IllegalArgumentException("objectSpec expected to have EncodableFacet");
        }
        return encodableFacet.toEncodedString(objectAdapter);
    }

    
    private boolean isBoolean(final ObjectSpecification objectSpec) {
        return hasCorrespondingClass(objectSpec, boolean.class, Boolean.class);
    }

    private boolean isInteger(final ObjectSpecification objectSpec) {
        return hasCorrespondingClass(objectSpec, int.class, Integer.class);
    }

    private boolean isLong(final ObjectSpecification objectSpec) {
        return hasCorrespondingClass(objectSpec, long.class, Long.class);
    }

    private boolean isBigInteger(final ObjectSpecification objectSpec) {
        return hasCorrespondingClass(objectSpec, BigInteger.class);
    }

    private boolean isDouble(final ObjectSpecification objectSpec) {
        return hasCorrespondingClass(objectSpec, double.class, Double.class);
    }
    
    private boolean isBigDecimal(final ObjectSpecification objectSpec) {
        return hasCorrespondingClass(objectSpec, BigDecimal.class);
    }


    private boolean hasCorrespondingClass(ObjectSpecification objectSpec, Class<?>... candidates) {
        final Class<?> specClass = objectSpec.getCorrespondingClass();
        for(final Class<?> candidate: candidates) {
            if(specClass == candidate) {
                return true;
            }
        }
        return false;
    }

    private void throwIncompatibleException(final ObjectSpecification objectSpec, final JsonRepresentation representation) {
        throw new IllegalArgumentException(String.format("representation '%s' incompatible with objectSpec '%s'", representation.toString(), objectSpec.getCorrespondingClass().getName()));
    }

}
