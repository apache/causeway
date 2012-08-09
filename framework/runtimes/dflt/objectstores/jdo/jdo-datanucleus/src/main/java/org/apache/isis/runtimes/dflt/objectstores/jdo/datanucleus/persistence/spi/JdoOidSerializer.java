package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.spi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
        if(jdoOid instanceof OID) {
            OID dnOid = (OID) jdoOid;
            Object keyValue = dnOid.getKeyValue();
            
            // prettier handling of these common cases
            if(keyValue instanceof String) {
                return "S" + SEPARATOR + keyValue; 
            }
            
            if(keyValue instanceof Integer) {
                return "I" + SEPARATOR + keyValue; 
            }
        }
        
        // the JDO spec (5.4.3) requires that OIDs are serializable toString and 
        // recreatable through the constructor
        return jdoOid.getClass().getName().toString() + SEPARATOR + jdoOid.toString();
    }

    public static String toOidStr(String jdoStr) {
        final int colonIdx = jdoStr.indexOf(SEPARATOR);
        final String oidAsString = jdoStr.substring(colonIdx+1);
        
        final String firstPart = jdoStr.substring(0, colonIdx);
        if("S".equals(firstPart) || "I".equals(firstPart)) {
            return oidAsString; 
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
}
