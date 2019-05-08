package org.apache.isis.core.metamodel.services.metamodel;

/**
 * Allows for the MetaModelExporter to know for any implementing class how to convert instances 
 * to literals.  
 * 
 * @since 2.0.0-M3
 */
public interface MetaModelExportSupport {

	public String asString();
	
}
