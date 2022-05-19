package org.apache.isis.persistence.jdo.datanucleus;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jdo.PersistenceManagerFactory;

import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.store.schema.SchemaAwareStoreManager;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.context._Context;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class _LegacyUtil {

    /**
     * This is legacy code, no longer required since <i>Datanucleus</i> setting
     * {@code datanucleus.schema.generateDatabase.mode=create}.
     * <p>
     * Kept for reference.
     * <p>
     * Usage
     * <pre>
     * createTablesEagerly(pmf, beanTypeRegistry.getEntityTypesJdo(), dnSettings);
     * </pre>
     * @see "https://issues.apache.org/jira/browse/ISIS-2651"
     */
    public void createTablesEagerly(
            final @NonNull PersistenceManagerFactory persistenceManagerFactory,
            final @NonNull Set<Class<?>> entityTypes,
            final @NonNull Map<String, Object> dnSettings) {

        if(_NullSafe.isEmpty(entityTypes)) {
            return; // skip
        }

        val pmf = (JDOPersistenceManagerFactory) persistenceManagerFactory;
        val nucleusContext = pmf.getNucleusContext();
        val schemaAwareStoreManager = (SchemaAwareStoreManager)nucleusContext.getStoreManager();

        val classNames = entityTypes
                .stream()
                .map(Class::getName)
                .collect(Collectors.toSet());

        val properties = new Properties();
        properties.putAll(dnSettings);
        schemaAwareStoreManager.createSchemaForClasses(classNames, properties);

        val clr = nucleusContext.getClassLoaderResolver(_Context.getDefaultClassLoader());
        val metaDataManager = nucleusContext.getMetaDataManager();

        metaDataManager
        .getClassesWithMetaData()
        .forEach(className->{
            val meta = metaDataManager.getMetaDataForClass(className, clr);
            _NullSafe.stream(meta.getQueries())
            .forEach(metaDataManager::registerNamedQuery);
        });

    }

}
