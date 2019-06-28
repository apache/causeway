package org.apache.isis.metamodel.services.metamodel;

/**
 * Allows for the MetaModelExporter to know for any implementing class how to convert instances 
 * to literals.  
 * 
 * @since 2.0
 */
public interface MetaModelExportSupport {

	public String toMetamodelString();
	
}
