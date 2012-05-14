package org.apache.isis.core.metamodel.spec;

import static org.hamcrest.CoreMatchers.is;

import java.io.Serializable;

import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.core.metamodel.facets.object.objecttype.ObjectSpecIdFacet;

/**
 * Represents an {@link ObjectSpecification}, as determined by
 * an {@link ObjectSpecIdFacet}.
 * 
 * <p>
 * Has value semantics.
 */
public final class ObjectSpecId implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private final String specId;

    public static ObjectSpecId of(String specId) {
        return new ObjectSpecId(specId);
    }

    public ObjectSpecId(String specId) {
        Ensure.ensureThatArg(specId, is(IsisMatchers.nonEmptyString()));
        this.specId = specId;
    }

    public String asString() {
        return specId;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((specId == null) ? 0 : specId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ObjectSpecId other = (ObjectSpecId) obj;
        if (specId == null) {
            if (other.specId != null)
                return false;
        } else if (!specId.equals(other.specId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return asString();
    }
    
    
}
