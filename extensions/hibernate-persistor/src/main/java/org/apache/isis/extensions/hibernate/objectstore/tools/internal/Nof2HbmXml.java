/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.extensions.hibernate.objectstore.tools.internal;

import java.beans.Introspector;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.DOMWriter;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Mappings;
import org.hibernate.type.CompositeCustomType;
import org.hibernate.type.TypeFactory;
import org.apache.isis.applib.value.Date;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.metamodel.commons.exceptions.UnknownTypeException;
import org.apache.isis.metamodel.config.IsisConfiguration;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.ObjectSpecificationException;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.extensions.hibernate.objectstore.HibernateConstants;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.accessor.ConverterFactory;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.accessor.ObjectPropertyAccessor;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.accessor.OidAccessor;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.accessor.PropertyConverter;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.accessor.PropertyHelper;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.accessor.TimestampAccessor;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.accessor.TitleAccessor;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.accessor.UserAccessor;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.accessor.VersionAccessor;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.usertype.DateType;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.usertype.DomainModelResourceType;
import org.apache.isis.extensions.hibernate.objectstore.util.HibernateUtil;
import org.apache.isis.runtime.context.IsisContext;


/**
 * Create hbm.xml mapping document for a [[NAME]] domain class.
 * <p>
 * If property valueFieldAccess is true values are mapped with access="field", but field names must be the
 * same as the property name (i.e. method getXxx() and field xxx). This will allow Hibernate to circumvent any
 * container.resolve() calls inside the getters, and dirty setting in setters.
 * <p>
 * Similarly, if property associationFieldAccess is true assocations are mapped with access="field".
 */
public class Nof2HbmXml {
    private static final String PRIMARY_KEY_PREFIX = "PK";
    private static final String PRIMARY_KEY_SUFFIX = "ID";
    private static final String FOREIGN_KEY_PREFIX = "FK";
    private static final String COLUMN_PROPERTY_PREFIX = HibernateConstants.PROPERTY_PREFIX + "column.";
    private static final String RESERVED_COL_SUFFIX = HibernateConstants.PROPERTY_PREFIX + "reservedColumnNameSuffix";
    private static final String RESERVED_TAB_SUFFIX = HibernateConstants.PROPERTY_PREFIX + "reservedTableNameSuffix";
    private static final String ID_TYPE = "long";
    private static final String FILE_SEPERATOR = System.getProperty("file.separator");
    private final static Logger LOG = Logger.getLogger(Nof2HbmXml.class);
    private final ConverterFactory converterFactory;
    private final PersistentSpecifications persistentClasses;
    private static final int colType = 0;
    private static final int tabType = 1;
    private static String[] reservedNameSuffixes = new String[] { "_column", "_TABLE" }; // defaults

    /**
     * Hibernate type for lists and arrays - "list" by default (generates "position" column on database) or
     * "bag" (no "position" column)
     */
    private final String listType;
    /**
     * If "one" will not create a seperate collection table for each collection, if "many" it will
     */
    private final String collections;
    /**
     * Do we assume all relationships are bidirectional, so they are mapped with "inverse" in Hibernate
     */
    private final boolean defaultAssociationFieldAccess;
    private final boolean defaultValueFieldAccess;
    private final String versionProperty;
    private final String modifiedByProperty;
    private final String modifiedOnProperty;
    private final String versionAccess;
    private final String modifiedByAccess;
    private final String modifiedOnAccess;
    private final String exportDirectory;
    /**
     * If mapping associations/properties using field access then the prefix can be set in the configutation.
     */
    private final String fieldPrefix;
    private final IsisConfiguration columnNames;
    private final String applibPrefix;
    private final String applibReplacementPrefix;

    // TODO make configurable - when they work !
    private final boolean lazyLoadObjects = true;
    private final boolean lazyLoadCollections = false;

    public Nof2HbmXml() {
        converterFactory = ConverterFactory.getInstance();
        final IsisConfiguration config = IsisContext.getConfiguration();
        listType = config.getString(HibernateConstants.PROPERTY_PREFIX + "list", "bag");
        collections = config.getString(HibernateConstants.PROPERTY_PREFIX + "collections", "many");
        defaultAssociationFieldAccess = config.getBoolean(HibernateConstants.PROPERTY_PREFIX + "associationFieldAccess", false);
        defaultValueFieldAccess = config.getBoolean(HibernateConstants.PROPERTY_PREFIX + "valueFieldAccess", false);
        versionProperty = config.getString(HibernateConstants.PROPERTY_PREFIX + "version");
        modifiedByProperty = config.getString(HibernateConstants.PROPERTY_PREFIX + "modified_by");
        modifiedOnProperty = config.getString(HibernateConstants.PROPERTY_PREFIX + "modified_on");
        versionAccess = config.getString(HibernateConstants.PROPERTY_PREFIX + "version.access");
        modifiedByAccess = config.getString(HibernateConstants.PROPERTY_PREFIX + "modified_by.access");
        modifiedOnAccess = config.getString(HibernateConstants.PROPERTY_PREFIX + "modified_on.access");
        fieldPrefix = config.getString(HibernateConstants.PROPERTY_PREFIX + "fieldPrefix", "");
        columnNames = config.getProperties(COLUMN_PROPERTY_PREFIX);
        reservedNameSuffixes[colType] = config.getString(RESERVED_COL_SUFFIX, reservedNameSuffixes[colType]);
        reservedNameSuffixes[tabType] = config.getString(RESERVED_TAB_SUFFIX, reservedNameSuffixes[tabType]);
        exportDirectory = config.getString(HibernateConstants.PROPERTY_PREFIX + "hbm-export", new File(".").getAbsolutePath() + FILE_SEPERATOR
                + HibernateUtil.MAPPING_DIR);
        MappingHelper.loadRequiredClasses();
        persistentClasses = PersistentSpecifications.buildPersistentSpecifications(null);
        String name = Date.class.getName();
        int pos = name.lastIndexOf('.') + 1;
        applibPrefix = name.substring(0, pos);
        name = DateType.class.getName();
        pos = name.lastIndexOf('.') + 1;
        applibReplacementPrefix = name.substring(0, pos);
        LOG.debug("mapping tree:" + persistentClasses.debugString());
    }

    private String capitalizedPropertyName(final String propertyName) {
        return propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }

    private String javaBeanGetterName(final String propertyName) {
        return "get" + capitalizedPropertyName(propertyName);
    }

    private String dotNetBeanGetterName(final String propertyName) {
        return "get_" + capitalizedPropertyName(propertyName);
    }

    private void writeMappingDoc(final Document doc, final String className) {
        final OutputFormat outformat = OutputFormat.createPrettyPrint();
        final File basedirFile = new File(exportDirectory);
        String dir = "";
        String file;
        final int pos = className.lastIndexOf(".");
        if (pos < 0) {
            file = className;
        } else {
            file = className.substring(pos + 1);
            dir = className.substring(0, pos).replace(".", FILE_SEPERATOR);
        }
        file += ".hbm.xml";
        final File outputDir = new File(basedirFile, dir);
        outputDir.mkdirs();

        final File outFile = new File(outputDir, file);
        LOG.info("writing " + outFile.getAbsolutePath());
        serializeToXML(doc, outFile, outformat);
        LOG.info("Writing mapping file: " + outFile.getAbsolutePath());
    }

    /**
     * Add all classes loaded in [[NAME]] to the hibernate configuration
     */
    public void configure(final Configuration cfg) {
        final Mappings mappings = cfg.createMappings();
        for (final Iterator<PersistentSpecification> iter = persistentClasses.getPersistentClasses(); iter.hasNext();) {
            final PersistentSpecification persistentClass = iter.next();
            final String className = persistentClass.getName();
            if (mappings.getClass(className) == null) {
                LOG.debug("binding persistent class " + className);
                final Document doc4j = createDocument(persistentClass);
                final org.w3c.dom.Document docW3c = createW3cDoc(doc4j);
                cfg.addDocument(docW3c);
                writeMappingDoc(doc4j, className);
            } else {
                LOG.info("class [" + className + "] is already mapped, skipping.. ");
            }
        }
    }

    public void createMappingFiles() {
        for (final Iterator<PersistentSpecification> iter = persistentClasses.getPersistentClasses(); iter.hasNext();) {
            final PersistentSpecification persistentClass = iter.next();
            final String className = persistentClass.getName();
            LOG.debug("create mapping for persistent class " + className);
            final Document doc4j = createDocument(persistentClass);
            writeMappingDoc(doc4j, className);
        }
    }

    protected org.w3c.dom.Document createW3cDoc(final Document doc4j) {
        final DOMWriter writer = new DOMWriter();
        try {
            return writer.write(doc4j);
        } catch (final DocumentException e) {
            throw new IsisException(e);
        }
    }

    private Element addColumnAttribute(final Element element, final String name) {
        return element.addAttribute("column", deconflictColumnName(name));
    }

    private Element addTableAttribute(final Element element, final String name) {
        return element.addAttribute("table", deconflictTableName(name));
    }

    private void bindTitle(
            final Element classElement,
            final PersistentSpecification persistent[[NAME]]Class,
            final boolean valueFieldAccess) {
        final Element title = addColumnAttribute(classElement.addElement("property").addAttribute("name", "title"), "title");
        addTitleType(persistent[[NAME]]Class.getSpecification(), title, "type", true, valueFieldAccess);
    }

    private String deconflictName(final String name, final int type) {
        String validName = name;
        while (HibernateUtil.isDatabaseKeyword(validName)) {
            validName = validName + reservedNameSuffixes[type];
        }
        if (!name.equals(validName)) {
            LOG.warn("name: " + name + " is a database keyword, replacing with: " + validName);
        }
        return validName;
    }

    private String deconflictTableName(final String name) {
        return deconflictName(name, tabType);
    }

    private String deconflictColumnName(final String name) {
        return deconflictName(name, colType);
    }

    protected Document createDocument(final PersistentSpecification persistent[[NAME]]Class) {
        LOG.info("creating hbm.xml for class " + persistent[[NAME]]Class.getName());
        final Document document = DocumentHelper.createDocument();
        document.addDocType("hibernate-mapping", "-//Hibernate/Hibernate Mapping DTD 3.0//EN",
                "http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd");
        final Element root = document.addElement("hibernate-mapping");
        if (persistent[[NAME]]Class.isDuplicateUnqualifiedClassName()) {
            root.addAttribute("auto-import", "false");
        }

        final boolean associationFieldAccess = defaultAssociationFieldAccess;
        final boolean valueFieldAccess = defaultValueFieldAccess;

        Element classElement;

        if (persistent[[NAME]]Class.getParent().isRoot()) {
            classElement = bindRootClass(persistent[[NAME]]Class, root, valueFieldAccess);
            bindVersion(classElement, valueFieldAccess);
            bindTitle(classElement, persistent[[NAME]]Class, valueFieldAccess);
        } else {
            classElement = bindSubClass(persistent[[NAME]]Class, root);
        }
        if (persistent[[NAME]]Class.isAbstract()) {
            classElement.addAttribute("abstract", "true");
        }

        final String versionId = versionProperty == null ? null : versionProperty.toLowerCase();
        final String modifiedById = modifiedByProperty == null ? null : modifiedByProperty.toLowerCase();
        final String modifiedOnId = modifiedOnProperty == null ? null : modifiedOnProperty.toLowerCase();
        // bind all fields
        final ObjectAssociation[] allFields = persistent[[NAME]]Class.getUniqueFields();
        for (int i = 0; i < allFields.length; i++) {
            final ObjectAssociation field = allFields[i];
            if (field.isNotPersisted()) {
                continue;
            }
            if (field.isOneToManyAssociation()) {
                bindCollection(classElement, persistent[[NAME]]Class, field, associationFieldAccess);
            } else if (field.isOneToOneAssociation()) {
                if (field.getSpecification().isValueOrIsAggregated()) {
                    final String fieldId = field.getId();
                    if ((fieldId.equals("id") && field.getId().equals("id")) || fieldId.equals(versionId)
                            || fieldId.equals(modifiedById) || fieldId.equals(modifiedOnId) || (fieldId.equals("title"))) {
                        continue;
                    } else {
                        bindProperty(field, classElement, valueFieldAccess);
                    }
                } else {

                    LOG.debug("Binding persistent association [" + field.getId() + "]");
                    Element assnElement;
                    if (persistentClasses.isPersistentClass(field.getSpecification().getFullName())) {
                        assnElement = bindAssociation(classElement, persistent[[NAME]]Class, field, associationFieldAccess);
                    } else if (persistentClasses.isPersistentInterface(field.getSpecification().getFullName())
                            || field.getSpecification().getFullName().equals("java.lang.Object")) {
                        warnAnyAssociation(persistent[[NAME]]Class, field);
                        assnElement = classElement.addElement("any").addAttribute("name",
                                getPropertyName(field, associationFieldAccess));
                        bindAnyAssociation(assnElement, field);
                    } else {
                        throw new IsisException("Un-mapped class/interface: "
                                + field.getSpecification().getFullName());
                    }
                    if (associationFieldAccess) {
                        assnElement.addAttribute("access", "field");
                    }
                    // .addAttribute("cascade", "save-update,lock");
                }
            } else {
                throw new UnknownTypeException(field);
            }
        }
        return document;
    }

    /**
     * Log a warning if we map to a Hibernate Any association.
     */
    private void warnAnyAssociation(final PersistentSpecification persistent[[NAME]]Class, final ObjectAssociation field) {
        LOG.info("Binding persistent association as an ANY association! [class=" + persistent[[NAME]]Class.getName() + ", field="
                + field.getId() + "]");
    }

    private void bindProperty(final ObjectAssociation field, final Element classElement, final boolean valueFieldAccess) {
        LOG.debug("Binding persistent property [" + field.getId() + "]");
        final Element property = classElement.addElement("property");
        setType(field, property, "type", true, valueFieldAccess);
        final Attribute access = property.attribute("access");
        final boolean fieldAccess = access != null && access.getStringValue().equals("field");
        property.addAttribute("name", getPropertyName(field, fieldAccess));
        final org.hibernate.type.Type type = TypeFactory.heuristicType(property.attribute("type").getValue(), null);
        if (type instanceof CompositeCustomType) {
            final String[] names = ((CompositeCustomType) type).getPropertyNames();
            for (int i = 0; i < names.length; i++) {
                final String compositeColumnName = deconflictColumnName(columnName(field.getId()) + "_" + names[i]);
                property.addElement("column").addAttribute("name", compositeColumnName);
            }
        } else {
            addColumnAttribute(property, columnName(field.getId()));
        }
    }

    private String columnName(final String column) {
        return columnNames.getString(COLUMN_PROPERTY_PREFIX + column, column);
    }

    /**
     * Bind a root class (i.e. top of hierarchy)
     */
    private Element bindRootClass(
            final PersistentSpecification persistent[[NAME]]Class,
            final Element root,
            final boolean valueFieldAccess) {
        // TODO - changed to lazy = false as lazy loading doesn't work at moment
        // - need to investigate and change back !

        final String tableName = deconflictTableName(persistent[[NAME]]Class.getTableName());

        final Element classElement = root.addElement("class").addAttribute("name", persistent[[NAME]]Class.getName()).addAttribute(
                "table", tableName).addAttribute("lazy", lazyLoadObjects ? "true" : "false");

        final Element id = addColumnAttribute(classElement.addElement("id").addAttribute("name", "id"), PRIMARY_KEY_PREFIX
                + tableName.toLowerCase() + PRIMARY_KEY_SUFFIX);

        addIdType(persistent[[NAME]]Class.getSpecification(), id, "type", true, valueFieldAccess);
        id.addElement("generator").addAttribute("class", "native");

        if (persistent[[NAME]]Class.hasSubClasses()) {
            classElement.addElement("discriminator").addAttribute("column", "discriminator").addAttribute("type", "string");
            if (!persistent[[NAME]]Class.isAbstract()) {
                classElement.addAttribute("discriminator-value", persistent[[NAME]]Class.getName());
            }
        }
        return classElement;
    }

    private Element bindSubClass(final PersistentSpecification persistent[[NAME]]Class, final Element root) {
        final Element classElement = root.addElement("subclass").addAttribute("name", persistent[[NAME]]Class.getName())
                .addAttribute("extends", persistent[[NAME]]Class.getParent().getName());
        if (!persistent[[NAME]]Class.isAbstract()) {
            classElement.addAttribute("discriminator-value", persistent[[NAME]]Class.getName());
        }
        return classElement;
    }

    private void addIdType(
            final ObjectSpecification spec,
            final Element id,
            final String attributeName,
            final boolean propertyAccessor,
            final boolean valueFieldAccess) {
        try {
            final ObjectAssociation idField = spec.getAssociation("id");
            if (idField != null && idField.getId().equals("id")) {
                setType(idField, id, attributeName, propertyAccessor, valueFieldAccess);
                return;
            }
        } catch (final ObjectSpecificationException ignore) {}
        // field does not exist, but id property might not be exposed to [[NAME]]
        // (e.g. private methods) so check class
        final Method getIdMethod = extractGetMethod(spec, "id");
        if (getIdMethod != null) {
            id.addAttribute(attributeName, getIdMethod.getReturnType().getName());
        } else {
            id.addAttribute(attributeName, ID_TYPE);
            if (propertyAccessor) {
                id.addAttribute("access", OidAccessor.class.getName());
            }
        }
    }

    private void addTitleType(
            final ObjectSpecification spec,
            final Element element,
            final String attributeName,
            final boolean propertyAccessor,
            final boolean valueFieldAccess) {
        try {
            final ObjectAssociation titleField = spec.getAssociation("title");
            if (titleField != null && titleField.getId().equals("Title")) {
                setType(titleField, element, attributeName, propertyAccessor, valueFieldAccess);
                return;
            }
        } catch (final ObjectSpecificationException ignore) {}
        // field does not exist, but title property might not be exposed to [[NAME]]
        // (e.g. private methods) so check class
        final Method getTitleMethod = extractGetMethod(spec, "Title");
        if (getTitleMethod != null) {
            element.addAttribute(attributeName, getTitleMethod.getReturnType().getName());
        } else {
            element.addAttribute(attributeName, "string");
            if (propertyAccessor) {
                element.addAttribute("access", TitleAccessor.class.getName());
            }
        }
    }

    /**
     * Get the property name for the field
     * 
     * @param field
     * @param fieldAccess
     */
    private String getPropertyName(final ObjectAssociation field, final boolean fieldAccess) {
        String name = field.getId().replace(" ", "");
        if (fieldAccess) {
            name = fieldPrefix + name;
        }
        // use same method as Hibernate
        return Introspector.decapitalize(name);
    }

    /**
     * Create a simple assocation (to a class/interface)
     * 
     * @param persistent[[NAME]]Class
     * @param associationFieldAccess
     */
    private Element bindAssociation(
            final Element classElement,
            final PersistentSpecification persistent[[NAME]]Class,
            final ObjectAssociation field,
            final boolean associationFieldAccess) {
        final Association assn = persistent[[NAME]]Class.getAssociation(field.getId());

        if (assn != null && assn.getField().isOneToOneAssociation()) {
            if (assn.isInverse()) {
                final String FKColumn = deconflictColumnName(FOREIGN_KEY_PREFIX + field.getId());

                return classElement.addElement("many-to-one")
                        .addAttribute("name", getPropertyName(field, associationFieldAccess)).addAttribute("column", FKColumn)
                        .addAttribute("class", field.getSpecification().getFullName()).addAttribute("unique", "true");
            } else {
                return classElement.addElement("one-to-one").addAttribute("name", getPropertyName(field, associationFieldAccess))
                        .addAttribute("class", field.getSpecification().getFullName()).addAttribute("property-ref",
                                getPropertyName(assn.getField(), associationFieldAccess));
            }
        }
        final String FKColumn = deconflictColumnName(FOREIGN_KEY_PREFIX + field.getId());
        return classElement.addElement("many-to-one").addAttribute("name", getPropertyName(field, associationFieldAccess))
                .addAttribute("column", FKColumn).addAttribute("class", field.getSpecification().getFullName());
    }

    /**
     * Create an assocation using an any mapping.
     */
    private void bindAnyAssociation(final Element anyElement, final ObjectAssociation field) {
        addIdType(field.getSpecification(), anyElement, "id-type", false, false);
        anyElement.addElement("column").addAttribute("name", deconflictColumnName(field.getId() + "type"));
        anyElement.addElement("column").addAttribute("name", deconflictColumnName(field.getId() + PRIMARY_KEY_SUFFIX));
        return;
    }

    private Element bindCollection(
            final Element classElement,
            final PersistentSpecification persistent[[NAME]]Class,
            final ObjectAssociation field,
            final boolean associationFieldAccess) {
        LOG.debug("Binding persistent collection [" + field.getId() + "]");
        final ObjectSpecification spec = persistent[[NAME]]Class.getSpecification();
        final Class<?> returnType = getReturnType(field, spec);
        final String collectionType = getCollectionType(returnType);

        final Element collElement = classElement.addElement(collectionType);
        final boolean fieldAccess = associationFieldAccess;
        collElement.addAttribute("access", "field");
        collElement.addAttribute("name", getPropertyName(field, fieldAccess));
        final Element keyElement = collElement.addElement("key");
        final String tableName = deconflictTableName(persistent[[NAME]]Class.getTableName());

        String associationType = null;
        if (field.getSpecification().isService()) {
            addColumnAttribute(keyElement, FOREIGN_KEY_PREFIX + tableName.toLowerCase());
            associationType = "element";
            addTableAttribute(collElement, tableName + "_" + field.getId().toUpperCase());
        } else {
            final Association assn = persistent[[NAME]]Class.getAssociation(field.getId());
            if (assn != null) {
                final ObjectAssociation associatedField = assn.getField();
                if (associatedField.isOneToOneAssociation()) {
                    associationType = "one-to-many";
                    collElement.addAttribute("inverse", "true");
                    addColumnAttribute(keyElement, FOREIGN_KEY_PREFIX + associatedField.getId());
                } else { // is collection
                    // inverseCollectionType = getCollectionType(getReturnType(inverseAssociation,
                    // associatedClass.getSpecification()));
                    associationType = "many-to-many";
                    addColumnAttribute(keyElement, FOREIGN_KEY_PREFIX + tableName.toLowerCase());
                    final PersistentSpecification associatedClass = assn.getPersistentClass();
                    final String associatedTableName = deconflictTableName(associatedClass.getTableName());
                    if (assn.isInverse()) {
                        addTableAttribute(collElement, associatedTableName + "_" + tableName);
                        collElement.addAttribute("inverse", "true");
                    } else {
                        addTableAttribute(collElement, tableName + "_" + associatedTableName);
                    }
                }
            } else if (persistentClasses.isPersistentClass(field.getSpecification().getFullName())) {
                associationType = collections + "-to-many";
                if ("many".equals(collections)) {
                    addTableAttribute(collElement, tableName + "_" + field.getId().toUpperCase());
                    addColumnAttribute(keyElement, FOREIGN_KEY_PREFIX + tableName.toLowerCase());
                } else {
                    addColumnAttribute(keyElement, FOREIGN_KEY_PREFIX + tableName.toLowerCase() + "_" + field.getId());
                }
            } else {
                warnAnyAssociation(persistent[[NAME]]Class, field);
                associationType = "many-to-any";
                addTableAttribute(collElement, tableName + "_" + field.getId().toUpperCase());
                addColumnAttribute(keyElement, FOREIGN_KEY_PREFIX + tableName.toLowerCase());
                final Element anyElement = collElement.addElement("many-to-any");
                bindAnyAssociation(anyElement, field);
            }
        }
        collElement.addAttribute("lazy", lazyLoadCollections ? "true" : "false");

        if (collectionType.equals("list")) {
            final Element listIndexElement = collElement.addElement("list-index");
            if (associationType.startsWith("one")) {
                // since column is on associated table need to make column
                // name unique in case other relationships are mapped
                addColumnAttribute(listIndexElement, tableName.toLowerCase() + "_" + field.getId() + "_idx");
            } else {
                addColumnAttribute(listIndexElement, "position");
            }
        }

        if (associationType.equals("element")) {
            addColumnAttribute(collElement.addElement("element").addAttribute("type", DomainModelResourceType.class.getName()),
                    field.getId().toLowerCase());
        } else if (!associationType.equals("many-to-any")) {
            final Element assnElement = collElement.addElement(associationType).addAttribute("class",
                    field.getSpecification().getFullName());
            // .addAttribute("unique", "true")
            if (associationType.equals("many-to-many")) {
                final PersistentSpecification associatedClass = persistentClasses.getPersistentClass(field.getSpecification()
                        .getFullName());
                final String associatedTableName = deconflictTableName(associatedClass.getTableName());
                addColumnAttribute(assnElement, FOREIGN_KEY_PREFIX + associatedTableName.toLowerCase());
            }
        }

        return collElement;
    }

    private String getCollectionType(final Class<?> returnType) {
        String type;
        if (returnType.equals(List.class)) {
            type = listType;
        } else if (returnType.equals(Set.class) || returnType.equals(SortedSet.class)) {
            type = "set";
        } else if (returnType.equals(Map.class) || returnType.equals(SortedMap.class)) {
            type = "map";
        } else if (returnType.isArray()) {
            type = listType; // ??
        } else if (returnType.equals(Vector.class)) {
            type = listType; // ??
        } else {
            throw new IsisException("Unsupported collection type " + returnType.getName());
        }
        return type;
    }

    private Class<?> getReturnType(final ObjectAssociation field, final ObjectSpecification spec) {
        final Method getMethod = extractPublicGetMethod(spec, field.getId());
        if (getMethod == null) {
            throw new IsisException("Cannot find get method for collection " + field.getId() + " in spec " + spec);
        }

        return getMethod.getReturnType();
    }

    private Method extractPublicGetMethod(final ObjectSpecification spec, final String name) {
        final String nameWithNoSpaces = name.replace(" ", "");
        Method method = null;
        try {
            final Class<?> clazz = Class.forName(spec.getFullName());
            try {
                method = clazz.getMethod(javaBeanGetterName(nameWithNoSpaces), (Class[]) null);
            } catch (final NoSuchMethodException nsme) {
                try {
                    method = clazz.getMethod(dotNetBeanGetterName(nameWithNoSpaces), (Class[]) null);
                } catch (final NoSuchMethodException nsme2) {}
            }
        } catch (final Exception e) {
            throw new IsisException(e);
        }
        return method;
    }

    /**
     * Method may be public/private/protected
     */
    private Method extractGetMethod(final ObjectSpecification spec, final String name) {
        final String nameWithNoSpaces = name.replace(" ", "");
        try {
            final Class<?> clazz = Class.forName(spec.getFullName());
            return getGetMethod(nameWithNoSpaces, clazz);
        } catch (final Exception e) {
            throw new IsisException(e);
        }
    }

    private Method getGetMethod(final String name, final Class<?> clazz) {
        if (clazz == Object.class || clazz == null) {
            return null;
        }
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(javaBeanGetterName(name), (Class[]) null);
        } catch (final NoSuchMethodException nsme) {
            try {
                method = clazz.getDeclaredMethod(dotNetBeanGetterName(name), (Class[]) null);
            } catch (final NoSuchMethodException nsme2) {}
        }
        if (method == null) {
            if (clazz.isInterface()) {
                final Class<?>[] interfaces = clazz.getInterfaces();
                for (int i = 0; i < interfaces.length; i++) {
                    method = getGetMethod(name, interfaces[i]);
                    if (method != null) {
                        return method;
                    }
                }
            } else {
                return getGetMethod(name, clazz.getSuperclass());
            }
        }
        return method;
    }

    private void setType(
            final ObjectAssociation field,
            final Element property,
            final String attributeName,
            final boolean propertyAccessor,
            final boolean valueFieldAccess) {
        if (field.getSpecification().isService()) {
            property.addAttribute("type", DomainModelResourceType.class.getName());
            if (valueFieldAccess) {
                property.addAttribute("access", "field");
            }
            return;
        }
        final PropertyConverter propertyConverter = converterFactory.getConverter(field);
        if (propertyConverter != null) {
            property.addAttribute("type", propertyConverter.getHibernateType());
            if (propertyAccessor) {
                property.addAttribute("access", ObjectPropertyAccessor.class.getName());
            } else if (valueFieldAccess) {
                property.addAttribute("access", "field");
            }
            return;
        }
        final String fullName = field.getSpecification().getFullName();

        // TODO replace this with a better mechanism - I'd prefer an explicit map - type to custom type
        if (fullName.startsWith(applibPrefix)) {
            final String type = applibReplacementPrefix + fullName.substring(applibPrefix.length()) + "Type";
            property.addAttribute(attributeName, type);
        } else if (fullName.startsWith("java.awt.Image")) {
            final String type = "org.apache.isis.nos.store.hibernate.type.AwtImageType";
            property.addAttribute(attributeName, type);
        } else {
            property.addAttribute(attributeName, fullName);
        }
        if (valueFieldAccess) {
            property.addAttribute("access", "field");
        }
    }

    private void bindVersion(final Element classElement, final boolean valueFieldAccess) {
        // add version, user and timestamp
        final Element version = classElement.addElement("version").addAttribute("name", "adapter_version").addAttribute("type",
                "long").addAttribute("access", VersionAccessor.class.getName());
        setVersionColumnMeta(version, "version", versionProperty, versionAccess, valueFieldAccess);
        final Element user = classElement.addElement("property").addAttribute("name", PropertyHelper.MODIFIED_BY).addAttribute(
                "type", "string").addAttribute("access", UserAccessor.class.getName());
        setVersionColumnMeta(user, "modified_by", modifiedByProperty, modifiedByAccess, valueFieldAccess);
        // note: not used for optimistic locking, so cannot use "timestamp" element
        final Element timestamp = classElement.addElement("property").addAttribute("name", PropertyHelper.MODIFIED_ON)
                .addAttribute("type", "timestamp").addAttribute("access", TimestampAccessor.class.getName());
        setVersionColumnMeta(timestamp, "modified_on", modifiedOnProperty, modifiedOnAccess, valueFieldAccess);
    }

    private void setVersionColumnMeta(
            final Element version,
            final String defaultColumn,
            final String property,
            final String access,
            final boolean valueFieldAccess) {
        if (property == null) {
            addColumnAttribute(version, defaultColumn);
        } else {
            addColumnAttribute(version, property.toLowerCase());
            final String propertyWithPrefix = valueFieldAccess ? fieldPrefix + property : property;
            version.addElement("meta").addAttribute("attribute", PropertyHelper.NAKED_PROPERTY).addText(propertyWithPrefix);
            if (access != null) {
                version.addElement("meta").addAttribute("attribute", PropertyHelper.NAKED_ACCESS).addText(access);
            } else if (valueFieldAccess) {
                version.addElement("meta").addAttribute("attribute", PropertyHelper.NAKED_ACCESS).addText("field");
            }
        }
    }

    /**
     * Export Hibernate mapping files for all [[NAME]] currently in Isis.
     */
    public void exportHbmXml(final String basedir) {
        final OutputFormat outformat = OutputFormat.createPrettyPrint();

        final File basedirFile = new File(basedir);
        for (final Iterator<PersistentSpecification> iter = persistentClasses.getPersistentClasses(); iter.hasNext();) {
            final PersistentSpecification persistentClass = iter.next();
            LOG.debug("exporting hbm.xml for " + persistentClass.getName());
            final Document doc = createDocument(persistentClass);

            final String className = persistentClass.getName();
            String dir = "";
            String file;
            final int pos = className.lastIndexOf(".");
            if (pos < 0) {
                file = className;
            } else {
                file = className.substring(pos + 1);
                dir = className.substring(0, pos).replace(".", FILE_SEPERATOR);
            }
            file += ".hbm.xml";
            final File outputDir = new File(basedirFile, dir);
            outputDir.mkdirs();

            final File outFile = new File(outputDir, file);
            LOG.info("writing mapping file: " + outFile.getAbsolutePath());
            serializeToXML(doc, outFile, outformat);
        }
    }

    private void serializeToXML(final Document doc, final File outFile, final OutputFormat outformat) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outFile);
            final XMLWriter writer = new XMLWriter(fos, outformat);
            writer.write(doc);
            writer.flush();
        } catch (final Exception e) {
            throw new IsisException(e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (final Exception ignore) {}
        }
    }
}
