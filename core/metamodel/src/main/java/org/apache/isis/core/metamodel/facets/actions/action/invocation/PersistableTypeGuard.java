package org.apache.isis.core.metamodel.facets.actions.action.invocation;

import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

@Deprecated //TODO [2033] debug only
public class PersistableTypeGuard {
	
	private final static String[] persistableObjects = {
			"SimpleObject",
			"Customer"
	};
	
	private final static _Probe probe = _Probe.unlimited().label("PersistableTypeGuard"); 

	public static void instate(ObjectAdapter actualAdapter) {
		if(actualAdapter==null) {
			return;
		}
		String magicString = "" + actualAdapter.getOid();
		instate(magicString, probe, "ObjectAdapter");
	}
	
	public static void instate(ObjectSpecification spec) {
		if(spec==null) {
			return;
		}
		String magicString = (spec.isViewModel() ? "!" : "") + spec.getSpecId().asString();
		instate(magicString, probe, "ObjectSpecification");
	}
	
	// -- HELPER
	
	protected static void instate(String magicString, _Probe probe, String type) {
		
		if(isPersistable(magicString)) {
            if(magicString.contains("!")) {
    			probe.println("Intercepted by guard [%s] '%s'", type, magicString);
                throw _Exceptions.unexpectedCodeReach();
            }            
            probe.println("[%s] '%s'", type, magicString);
        }
	}
	
	protected static boolean isPersistable(String input) {
		for(String x : persistableObjects) {
			if(input.contains("."+x+":")) {
				return true;
			}
		}
		return false;
	}
	

}
