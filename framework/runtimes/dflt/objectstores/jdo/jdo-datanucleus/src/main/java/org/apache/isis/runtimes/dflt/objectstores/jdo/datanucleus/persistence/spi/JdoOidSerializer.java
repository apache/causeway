package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.spi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.datanucleus.identity.OID;

public final class JdoOidSerializer {
    
    private static final char SEPARATOR = '~';

    private JdoOidSerializer(){}

    public static class Exception extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public Exception(java.lang.Exception ex) {
            super(ex);
        }
    }

    public static String toString(Object jdoOid) {
        if(jdoOid instanceof javax.jdo.identity.IntIdentity) {
            return "i" + SEPARATOR + jdoOid; 
        }
        if(jdoOid instanceof javax.jdo.identity.StringIdentity) {
            return "s" + SEPARATOR + jdoOid; 
        }
        if(jdoOid instanceof javax.jdo.identity.LongIdentity) {
            return "l" + SEPARATOR + jdoOid; 
        }
        if(jdoOid instanceof OID) {
            OID dnOid = (OID) jdoOid;
            Object keyValue = dnOid.getKeyValue();
            
            // prettier handling of these common cases
            if(keyValue instanceof String) {
                return "S" + SEPARATOR + keyValue; 
            }

            if(keyValue instanceof Long) {
                return "L" + SEPARATOR + keyValue; 
            }

            if(keyValue instanceof BigInteger) {
                return "B" + SEPARATOR + keyValue; 
            }

            if(keyValue instanceof Integer) {
                return "I" + SEPARATOR + keyValue; 
            }

        }
        
        // the JDO spec (5.4.3) requires that OIDs are serializable toString and 
        // recreatable through the constructor
        return jdoOid.getClass().getName().toString() + SEPARATOR + jdoOid.toString();
    }

    private static List<String> prefixes = Arrays.asList("S", "I", "L", "B", "s", "i", "l");
    
    public static String toOidStr(RootOid oid) {
    	String idStr = oid.getIdentifier();
        final int colonIdx = idStr.indexOf(SEPARATOR);
        final String oidAsString = idStr.substring(colonIdx+1);
        
        final String firstPart = idStr.substring(0, colonIdx);
        if(prefixes.contains(firstPart)) {
            ObjectSpecId objectSpecId = oid.getObjectSpecId();
            ObjectSpecification spec = getSpecificationLoader().lookupBySpecId(objectSpecId);
			return oidAsString + "[OID]" + spec.getFullIdentifier(); 
        }
        
        final String clsName = firstPart;
        try {
            final Class<?> cls = Thread.currentThread().getContextClassLoader().loadClass(clsName);
            final Constructor<?> cons = cls.getConstructor(String.class);
            final Object dnOid = cons.newInstance(oidAsString);
            return dnOid.toString();
        } catch (ClassNotFoundException e) {
            throw new JdoOidSerializer.Exception(e);
        } catch (IllegalArgumentException e) {
            throw new JdoOidSerializer.Exception(e);
        } catch (InstantiationException e) {
            throw new JdoOidSerializer.Exception(e);
        } catch (IllegalAccessException e) {
            throw new JdoOidSerializer.Exception(e);
        } catch (InvocationTargetException e) {
            throw new JdoOidSerializer.Exception(e);
        } catch (SecurityException e) {
            throw new JdoOidSerializer.Exception(e);
        } catch (NoSuchMethodException e) {
            throw new JdoOidSerializer.Exception(e);
        }
    }

	private static SpecificationLoaderSpi getSpecificationLoader() {
		return IsisContext.getSpecificationLoader();
	}
}
