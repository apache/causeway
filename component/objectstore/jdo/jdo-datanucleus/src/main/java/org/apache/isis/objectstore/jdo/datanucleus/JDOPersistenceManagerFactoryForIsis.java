package org.apache.isis.objectstore.jdo.datanucleus;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.Constants;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOUserException;

import org.datanucleus.NucleusContext;
import org.datanucleus.PropertyNames;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.api.jdo.NucleusJDOHelper;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.metadata.PersistenceUnitMetaData;
import org.datanucleus.metadata.TransactionType;
import org.datanucleus.store.connection.ConnectionFactory;
import org.datanucleus.store.connection.ConnectionResourceType;

public class JDOPersistenceManagerFactoryForIsis extends
		JDOPersistenceManagerFactory {

	private static final long serialVersionUID = 1L;

	public JDOPersistenceManagerFactoryForIsis() {
		this(null);
	}

	public JDOPersistenceManagerFactoryForIsis(Map props) {
        // Extract any properties that affect NucleusContext startup
        Map startupProps = null;
        if (props != null)
        {
            // Possible properties to check for
            for (String startupPropName : NucleusContext.STARTUP_PROPERTIES)
            {
                if (props.containsKey(startupPropName))
                {
                    if (startupProps == null)
                    {
                        startupProps = new HashMap();
                    }
                    startupProps.put(startupPropName, props.get(startupPropName));
                }
            }
        }

        // Initialise the context for JDO (need nucleusContext to load persistence-unit)
        nucleusContext = createNucleusContext(startupProps);

        // Generate the properties to apply to the PMF
        Map pmfProps = new HashMap();

        PersistenceUnitMetaData pumd = null;
        if (props != null)
        {
            String persistenceUnitName = (String)props.get(PropertyNames.PROPERTY_PERSISTENCE_UNIT_NAME);
            if (persistenceUnitName == null)
            {
                persistenceUnitName = (String)props.get(Constants.PROPERTY_PERSISTENCE_UNIT_NAME);
            }
            if (persistenceUnitName != null)
            {
                // PMF for a "persistence-unit", so add property so the persistence mechanism knows this
                getConfiguration().setProperty(PropertyNames.PROPERTY_PERSISTENCE_UNIT_NAME, persistenceUnitName);

                try
                {
                    // Obtain any props defined for the persistence-unit
                    pumd = nucleusContext.getMetaDataManager().getMetaDataForPersistenceUnit(persistenceUnitName);
                    if (pumd != null)
                    {
                        // Add the properties for the unit
                        if (pumd.getProperties() != null)
                        {
                            pmfProps.putAll(pumd.getProperties());
                        }
                    }
                    else
                    {
                        throw new JDOUserException(LOCALISER.msg("012004", persistenceUnitName));
                    }
                }
                catch (NucleusException ne)
                {
                    throw new JDOUserException(LOCALISER.msg("012005", persistenceUnitName), ne);
                }
            }
        }

        // Append on any user properties
        if (props != null)
        {
            pmfProps.putAll(props);
            if (!pmfProps.containsKey(PropertyNames.PROPERTY_TRANSACTION_TYPE) &&
                !pmfProps.containsKey("javax.jdo.option.TransactionType"))
            {
                // Default to RESOURCE_LOCAL txns
                pmfProps.put(PropertyNames.PROPERTY_TRANSACTION_TYPE, TransactionType.RESOURCE_LOCAL.toString());
            }
            else
            {
                // let TransactionType.JTA imply ResourceType.JTA
                String transactionType = pmfProps.get(PropertyNames.PROPERTY_TRANSACTION_TYPE) != null ? 
                        (String)pmfProps.get(PropertyNames.PROPERTY_TRANSACTION_TYPE) : 
                        (String)pmfProps.get("javax.jdo.option.TransactionType");
                if (TransactionType.JTA.toString().equalsIgnoreCase(transactionType))
                {
                    pmfProps.put(ConnectionFactory.DATANUCLEUS_CONNECTION_RESOURCE_TYPE,
                        ConnectionResourceType.JTA.toString());
                    pmfProps.put(ConnectionFactory.DATANUCLEUS_CONNECTION2_RESOURCE_TYPE,
                        ConnectionResourceType.JTA.toString());
                }
            }
        }
        else
        {
            pmfProps.put(PropertyNames.PROPERTY_TRANSACTION_TYPE, TransactionType.RESOURCE_LOCAL.toString());
        }

        // Apply the properties to the PMF
        try
        {
            String propsFileProp = PropertyNames.PROPERTY_PROPERTIES_FILE;
            if (pmfProps.containsKey(propsFileProp))
            {
                // Apply properties file first
                getConfiguration().setPropertiesUsingFile((String)pmfProps.get(propsFileProp));
                pmfProps.remove(propsFileProp);
            }
            getConfiguration().setPersistenceProperties(pmfProps);
        }
        catch (IllegalArgumentException iae)
        {
            throw new JDOFatalUserException("Exception thrown setting persistence properties", iae);
        }
        catch (NucleusException jpe)
        {
            // Only throw JDOException and subclasses
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }

        // Initialise any metadata that needs loading + settings
        initialiseMetaData(pumd);

        // Enable any listeners that are specified via persistence properties
        processLifecycleListenersFromProperties(props);

	}

	public JDOPersistenceManagerFactoryForIsis(PersistenceUnitMetaData pumd,
			Map overrideProps) {
        // Build up map of all properties to apply (from persistence-unit + overridden + defaulted)
    	Map props = new HashMap();
    	if (pumd != null && pumd.getProperties() != null)
    	{
    	    props.putAll(pumd.getProperties());
    	}
    	if (overrideProps != null)
    	{
    		props.putAll(overrideProps);
    	}
        if (!props.containsKey(PropertyNames.PROPERTY_TRANSACTION_TYPE) && 
            !props.containsKey("javax.jdo.option.TransactionType"))
	    {
    		// Default to RESOURCE_LOCAL txns
    		props.put(PropertyNames.PROPERTY_TRANSACTION_TYPE, TransactionType.RESOURCE_LOCAL.toString());
    	}
    	else
        {
            // let TransactionType.JTA imply ResourceType.JTA
            String transactionType = props.get(PropertyNames.PROPERTY_TRANSACTION_TYPE) != null ? 
                    (String) props.get(PropertyNames.PROPERTY_TRANSACTION_TYPE) : 
                    (String) props.get("javax.jdo.option.TransactionType");
            if (TransactionType.JTA.toString().equalsIgnoreCase(transactionType))
            {
                props.put(ConnectionFactory.DATANUCLEUS_CONNECTION_RESOURCE_TYPE, ConnectionResourceType.JTA.toString());
                props.put(ConnectionFactory.DATANUCLEUS_CONNECTION2_RESOURCE_TYPE, ConnectionResourceType.JTA.toString());
            }
        }

    	// Initialise the context with all properties
        nucleusContext = createNucleusContext(props);

        initialiseMetaData(pumd);

        // Enable any listeners that are specified via persistence properties
        processLifecycleListenersFromProperties(props);
	}

	/**
	 * Factored out
	 */
	protected NucleusContext createNucleusContext(Map props) {
		return new NucleusContextForIsis("JDO", props);
	}

}
