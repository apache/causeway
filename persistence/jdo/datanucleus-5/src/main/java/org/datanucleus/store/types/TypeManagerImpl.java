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
package org.datanucleus.store.types;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.NucleusContext;
import org.datanucleus.PropertyNames;
import org.datanucleus.exceptions.ClassNotResolvedException;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.identity.IdentityUtils;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.plugin.ConfigurationElement;
import org.datanucleus.plugin.PluginManager;
import org.datanucleus.state.ObjectProvider;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.types.containers.ArrayHandler;
import org.datanucleus.store.types.containers.ArrayListHandler;
import org.datanucleus.store.types.containers.HashMapHandler;
import org.datanucleus.store.types.containers.HashSetHandler;
import org.datanucleus.store.types.containers.HashtableHandler;
import org.datanucleus.store.types.containers.JDKCollectionHandler;
import org.datanucleus.store.types.containers.LinkedHashMapHandler;
import org.datanucleus.store.types.containers.LinkedHashSetHandler;
import org.datanucleus.store.types.containers.LinkedListHandler;
import org.datanucleus.store.types.containers.OptionalHandler;
import org.datanucleus.store.types.containers.PriorityQueueHandler;
import org.datanucleus.store.types.containers.PropertiesHandler;
import org.datanucleus.store.types.containers.StackHandler;
import org.datanucleus.store.types.containers.TreeMapHandler;
import org.datanucleus.store.types.containers.TreeSetHandler;
import org.datanucleus.store.types.containers.VectorHandler;
import org.datanucleus.store.types.converters.ClassStringConverter;
import org.datanucleus.store.types.converters.TypeConverter;
import org.datanucleus.util.ClassUtils;
import org.datanucleus.util.Localiser;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.StringUtils;

/**
 * TODO remove once fixed upstream!
 * <p>
 * Monkey patched from DN original in support of priority based type registration.
 * While documented, was never implemented.
 * <p>
 * See also
 * https://github.com/datanucleus/datanucleus-core/pull/360
 */
public class TypeManagerImpl implements TypeManager, Serializable
{
    private static final long serialVersionUID = 8217508318434539002L;

    protected NucleusContext nucCtx;

    protected transient ClassLoaderResolver clr;

    /** Map of java types, keyed by the class name. */
    protected Map<String, JavaType> javaTypes = new ConcurrentHashMap<>();
    
    /** Map of java type priorities, keyed by the class name. */
    protected Map<String, Integer> javaTypePriorities = new ConcurrentHashMap<>();

    /** Map of ContainerHandlers, keyed by the container type class name. */
    protected Map<Class, ? super ContainerHandler> containerHandlersByClass = new ConcurrentHashMap<>();

    /** Map of TypeConverter keyed by their symbolic name. */
    protected Map<String, TypeConverter> typeConverterByName = new ConcurrentHashMap<>();

    /** Map of TypeConverter keyed by type name that we should default to for this type (user-defined). */
    protected Map<String, TypeConverter> autoApplyConvertersByType = null;

    /** Map of (Map of TypeConverter keyed by the datastore type), keyed by the member type. */
    protected Map<Class, Map<Class, TypeConverter>> typeConverterMap = new ConcurrentHashMap<>();

    /** Cache of TypeConverter datastore type, keyed by the converter. */
    protected Map<TypeConverter, Class> typeConverterDatastoreTypeByConverter = new ConcurrentHashMap<>();

    /** Cache of TypeConverter member type, keyed by the converter. */
    protected Map<TypeConverter, Class> typeConverterMemberTypeByConverter = new ConcurrentHashMap<>();

    /**
     * Constructor, loading support for type mappings using the plugin mechanism.
     * @param nucCtx NucleusContext
     */
    public TypeManagerImpl(NucleusContext nucCtx)
    {
        this.nucCtx = nucCtx;
        this.clr = nucCtx.getClassLoaderResolver(null);
        loadJavaTypes(nucCtx.getPluginManager());

        // Load converters since if a type is otherwise not persistable but a converter is found, then the type becomes persistable, hence needs enhancing
        loadTypeConverters(nucCtx.getPluginManager());
    }

    public void close()
    {
        containerHandlersByClass = null;
        javaTypes = null;

        typeConverterByName.clear();
        typeConverterMap.clear();
        typeConverterMemberTypeByConverter.clear();
        typeConverterDatastoreTypeByConverter.clear();

        autoApplyConvertersByType = null;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.TypeManager#getSupportedSecondClassTypes()
     */
    @Override
    public Set<String> getSupportedSecondClassTypes()
    {
        return new HashSet(javaTypes.keySet());
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.TypeManager#isSupportedSecondClassType(java.lang.String)
     */
    @Override
    public boolean isSupportedSecondClassType(String className)
    {
        if (className == null)
        {
            return false;
        }

        JavaType type = javaTypes.get(className);
        if (type == null)
        {
            try
            {
                Class cls = clr.classForName(className);
                type = findJavaTypeForClass(cls);
                return type != null;
            }
            catch (Exception e)
            {
            }
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.TypeManager#filterOutSupportedSecondClassNames(java.lang.String[])
     */
    @Override
    public String[] filterOutSupportedSecondClassNames(String[] inputClassNames)
    {
        // Filter out any "simple" type classes
        int filteredClasses = 0;
        for (int i = 0; i < inputClassNames.length; ++i)
        {
            if (isSupportedSecondClassType(inputClassNames[i]))
            {
                inputClassNames[i] = null;
                ++filteredClasses;
            }
        }
        if (filteredClasses == 0)
        {
            return inputClassNames;
        }
        String[] restClasses = new String[inputClassNames.length - filteredClasses];
        int m = 0;
        for (int i = 0; i < inputClassNames.length; ++i)
        {
            if (inputClassNames[i] != null)
            {
                restClasses[m++] = inputClassNames[i];
            }
        }
        return restClasses;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.TypeManager#isDefaultPersistent(java.lang.Class)
     */
    @Override
    public boolean isDefaultPersistent(Class c)
    {
        if (c == null)
        {
            return false;
        }

        JavaType type = javaTypes.get(c.getName());
        if (type != null)
        {
            return true;
        }

        // Try to find a class that this class extends that is supported
        type = findJavaTypeForClass(c);
        if (type != null)
        {
            return true;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.TypeManager#isDefaultFetchGroup(java.lang.Class)
     */
    @Override
    public boolean isDefaultFetchGroup(Class c)
    {
        if (c == null)
        {
            return false;
        }

        if (nucCtx.getApiAdapter().isPersistable(c))
        {
            // 1-1/N-1 (persistable field), so return what the API default is
            return nucCtx.getApiAdapter().getDefaultDFGForPersistableField();
        }

        JavaType type = javaTypes.get(c.getName());
        if (type != null)
        {
            // Field type defined in plugins, so return the setting
            return type.dfg;
        }

        // Try to find a class that this class extends that is supported
        type = findJavaTypeForClass(c);
        if (type != null)
        {
            return type.dfg;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.TypeManager#isDefaultFetchGroupForCollection(java.lang.Class, java.lang.Class)
     */
    @Override
    public boolean isDefaultFetchGroupForCollection(Class c, Class genericType)
    {
        if (c != null && genericType == null)
        {
            return isDefaultFetchGroup(c);
        }
        else if (c == null)
        {
            return false;
        }

        String name = c.getName() + "<" + genericType.getName() + ">";
        JavaType type = javaTypes.get(name);
        if (type != null)
        {
            return type.dfg;
        }

        // Try to find a class that this class extends that is supported
        type = findJavaTypeForCollectionClass(c, genericType);
        if (type != null)
        {
            return type.dfg;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.TypeManager#isDefaultEmbeddedType(java.lang.Class)
     */
    @Override
    public boolean isDefaultEmbeddedType(Class c)
    {
        if (c == null)
        {
            return false;
        }

        JavaType type = javaTypes.get(c.getName());
        if (type != null)
        {
            return type.embedded;
        }

        // Try to find a class that this class extends that is supported
        type = findJavaTypeForClass(c);
        if (type != null)
        {
            return type.embedded;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.TypeManager#isSecondClassMutableType(java.lang.String)
     */
    @Override
    public boolean isSecondClassMutableType(String className)
    {
        return getWrapperTypeForType(className) != null;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.TypeManager#getWrapperTypeForType(java.lang.String)
     */
    @Override
    public Class getWrapperTypeForType(String className)
    {
        if (className == null)
        {
            return null;
        }

        JavaType type = javaTypes.get(className);
        return type == null ? null : type.wrapperType;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.TypeManager#getWrappedTypeBackedForType(java.lang.String)
     */
    @Override
    public Class getWrappedTypeBackedForType(String className)
    {
        if (className == null)
        {
            return null;
        }

        JavaType type = javaTypes.get(className);
        return type == null ? null : type.wrapperTypeBacked;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.TypeManager#isSecondClassWrapper(java.lang.String)
     */
    @Override
    public boolean isSecondClassWrapper(String className)
    {
        if (className == null)
        {
            return false;
        }

        // Check java types with wrappers
        Iterator iter = javaTypes.values().iterator();
        while (iter.hasNext())
        {
            JavaType type = (JavaType)iter.next();
            if (type.wrapperType != null && type.wrapperType.getName().equals(className))
            {
                return true;
            }
            if (type.wrapperTypeBacked != null && type.wrapperTypeBacked.getName().equals(className))
            {
                return true;
            }
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.TypeManager#createSCOInstance(org.datanucleus.state.ObjectProvider, org.datanucleus.metadata.AbstractMemberMetaData, java.lang.Class, java.lang.Object, boolean)
     */
    @Override
    public SCO createSCOInstance(ObjectProvider ownerOP, AbstractMemberMetaData mmd, Class instantiatedType, Object value, boolean replaceField)
    {
        if (value != null && value instanceof SCO)
        {
            // Passed in value is a wrapper type already, so just return it!
            if (replaceField)
            {
                // Replace the field with this value
                ownerOP.replaceField(mmd.getAbsoluteFieldNumber(), value);
            }
            return (SCO) value;
        }

        // Create new wrapper of the required type
        Class requiredType = value != null ? value.getClass() : instantiatedType; // Default to instantiated type
        if ("declared".equalsIgnoreCase(nucCtx.getConfiguration().getStringProperty(PropertyNames.PROPERTY_TYPE_WRAPPER_BASIS)))
        {
            // Use declared type of the field to define the wrapper type
            requiredType = mmd.getType();
        }
        SCO sco = createSCOInstance(ownerOP, mmd, requiredType);

        if (replaceField)
        {
            // Replace the field in the owner with the wrapper before initialising it
            ownerOP.replaceField(mmd.getAbsoluteFieldNumber(), sco);
        }

        // Initialise the SCO for use
        if (value != null)
        {
            // Apply the existing value
            sco.initialise(value);
        }
        else
        {
            // Just create it empty and load from the datastore
            sco.initialise();
        }

        return sco;
    }

    /**
     * Method to create a new SCO wrapper for the specified field replacing the old value with the new value. 
     * If the member value is a SCO already will just return the (new) value.
     * @param ownerOP The ObjectProvider of the owner object
     * @param memberNumber The member number in the owner
     * @param newValue The value to initialise the wrapper with (if any) for this member
     * @param oldValue The previous value that we are replacing with this value
     * @param replaceFieldIfChanged Whether to replace the member in the object if wrapping the value
     * @return The wrapper (or original value if not wrappable)
     */
    public Object wrapAndReplaceSCOField(ObjectProvider ownerOP, int memberNumber, Object newValue, Object oldValue, boolean replaceFieldIfChanged)
    {
        if (newValue == null || !ownerOP.getClassMetaData().getSCOMutableMemberFlags()[memberNumber])
        {
            // We don't wrap null objects currently
            return newValue;
        }

        if (!(newValue instanceof SCO) || ownerOP.getObject() != ((SCO)newValue).getOwner())
        {
            // Not a SCO wrapper, or is a SCO wrapper but not owned by this object
            AbstractMemberMetaData mmd = ownerOP.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(memberNumber);
            if (replaceFieldIfChanged)
            {
                if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                {
                    NucleusLogger.PERSISTENCE.debug(Localiser.msg("026029", StringUtils.toJVMIDString(ownerOP.getObject()), 
                        ownerOP.getExecutionContext() != null ? IdentityUtils.getPersistableIdentityForId(ownerOP.getInternalObjectId()) : ownerOP.getInternalObjectId(), mmd.getName()));
                }
            }

            if (newValue instanceof SCO)
            {
                // Passed in value is a wrapper type already, so just return it!
                if (replaceFieldIfChanged)
                {
                    // Replace the field with this value
                    ownerOP.replaceField(mmd.getAbsoluteFieldNumber(), newValue);
                }
                return newValue;
            }

            // Create new wrapper of the required type
            Class requiredType = newValue.getClass();
            SCO sco = createSCOInstance(ownerOP, mmd, requiredType);

            if (replaceFieldIfChanged)
            {
                // Replace the field in the owner with the wrapper before initialising it
                ownerOP.replaceField(mmd.getAbsoluteFieldNumber(), sco);
            }

            // Initialise the SCO for use, providing new and old values so the wrapper has the ability to do something intelligent
            sco.initialise(newValue, oldValue);

            return sco;
        }
        return newValue;
    }

    /**
     * Method to create a new SCO wrapper for member type.
     * Will find a wrapper suitable for the instantiated type (if provided), otherwise suitable for the member metadata type.
     * @param ownerOP ObjectProvider for the owning object
     * @param mmd The MetaData for the related member.
     * @param instantiatedType Type to instantiate the member as (if known), otherwise falls back to the type from metadata
     * @return The wrapper object of the required type
     * @throws NucleusUserException if an error occurred when creating the SCO instance
     */
    private SCO createSCOInstance(ObjectProvider ownerOP, AbstractMemberMetaData mmd, Class instantiatedType)
    {
        String typeName = (instantiatedType != null) ? instantiatedType.getName() : mmd.getTypeName();

        // Find the SCO wrapper type most suitable
        StoreManager storeMgr = ownerOP.getExecutionContext().getStoreManager();
        boolean backedWrapper = storeMgr.useBackedSCOWrapperForMember(mmd, ownerOP.getExecutionContext());
        Class wrapperType = null;
        if (mmd.isSerialized())
        {
            // If we have all elements serialised into a column then cannot have backing stores
            backedWrapper = false;
        }

        if (backedWrapper)
        {
            wrapperType = getBackedWrapperTypeForType(mmd.getType(), instantiatedType, typeName);
        }
        else
        {
            wrapperType = getSimpleWrapperTypeForType(mmd.getType(), instantiatedType, typeName);
        }
        if (wrapperType == null)
        {
            throw new NucleusUserException(Localiser.msg("023011", mmd.getTypeName(), typeName, mmd.getFullFieldName()));
        }

        // Create the SCO wrapper
        try
        {
            return (SCO) ClassUtils.newInstance(wrapperType, new Class[]{ObjectProvider.class, AbstractMemberMetaData.class}, new Object[]{ownerOP, mmd});
        }
        catch (UnsupportedOperationException uoe)
        {
            // Can't create backing store? so try simple wrapper
            if (backedWrapper)
            {
                NucleusLogger.PERSISTENCE.warn("Creation of backed wrapper for " + mmd.getFullFieldName() + " unsupported, so trying simple wrapper");
                wrapperType = getSimpleWrapperTypeForType(mmd.getType(), instantiatedType, typeName);
                return (SCO) ClassUtils.newInstance(wrapperType, new Class[]{ObjectProvider.class, AbstractMemberMetaData.class}, new Object[]{ownerOP, mmd});
            }

            throw uoe;
        }
    }

    /**
     * Convenience method to return the backed wrapper type for the field definition. Wrapper is null if no backed wrapper is defined for the type.
     * @param declaredType Declared type of the field
     * @param instantiatedType Instantiated type of the field
     * @param typeName Type name to try first
     * @return The wrapper type
     */
    private Class getBackedWrapperTypeForType(Class declaredType, Class instantiatedType, String typeName)
    {
        Class wrapperType = getWrappedTypeBackedForType(typeName);
        if (wrapperType == null)
        {
            // typeName not supported directly (no SCO wrapper for the precise type)
            if (instantiatedType != null)
            {
                // Try the instantiated type
                wrapperType = getWrappedTypeBackedForType(instantiatedType.getName());
            }
            if (wrapperType == null)
            {
                // Try the declared type
                wrapperType = getWrappedTypeBackedForType(declaredType.getName());
            }
        }
        return wrapperType;
    }

    /**
     * Convenience method to return the simple wrapper type for the field definition. Wrapper is null if no simple wrapper is defined for the type.
     * @param declaredType Declared type of the field
     * @param instantiatedType Instantiated type of the field
     * @param typeName Type name to try first
     * @return The wrapper type
     */
    private Class getSimpleWrapperTypeForType(Class declaredType, Class instantiatedType, String typeName)
    {
        Class wrapperType = getWrapperTypeForType(typeName);
        if (wrapperType == null)
        {
            // typeName not supported directly (no SCO wrapper for the precise type)
            if (instantiatedType != null)
            {
                // Try the instantiated type
                wrapperType = getWrapperTypeForType(instantiatedType.getName());
            }
            if (wrapperType == null)
            {
                // Try the declared type
                wrapperType = getWrapperTypeForType(declaredType.getName());
            }
        }
        return wrapperType;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.TypeManager#getTypeForSecondClassWrapper(java.lang.String)
     */
    @Override
    public Class getTypeForSecondClassWrapper(String className)
    {
        Iterator iter = javaTypes.values().iterator();
        while (iter.hasNext())
        {
            JavaType type = (JavaType)iter.next();
            if (type.wrapperType != null && type.wrapperType.getName().equals(className))
            {
                return type.cls;
            }
            if (type.wrapperTypeBacked != null && type.wrapperTypeBacked.getName().equals(className))
            {
                return type.cls;
            }
        }
        return null;
    }

    @Override
    public ContainerAdapter getContainerAdapter(Object container)
    {
        ContainerHandler containerHandler = getContainerHandler(container.getClass());
        return containerHandler == null ? null : containerHandler.getAdapter(container);
    }
    
    @Override
    public <H extends ContainerHandler> H getContainerHandler(Class containerClass)
    {
        H containerHandler = (H) containerHandlersByClass.get(containerClass);
        if (containerHandler == null)
        {
            // Try to find the container handler using the registered type
            JavaType type = findJavaTypeForClass(containerClass);
            if (type != null && type.containerHandlerType != null)
            {
                Class[] parameterTypes = null;
                Object[] parameters = null;
                
                Class[] classParameterTypes = new Class[]{Class.class};

                // Allow ContainerHandlers that receive the container type on the constructor. e.g. ArrayHandler
                if (ClassUtils.getConstructorWithArguments(type.containerHandlerType, classParameterTypes) != null )
                {
                    parameterTypes = classParameterTypes;
                    parameters = new Object[] {containerClass};
                }
                
                containerHandler = (H) ClassUtils.newInstance(type.containerHandlerType, parameterTypes, parameters);
                
                containerHandlersByClass.put(containerClass, containerHandler);
            }
        }

        return containerHandler;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.TypeManager#getTypeConverterForName(java.lang.String)
     */
    @Override
    public TypeConverter getTypeConverterForName(String converterName)
    {
        return converterName == null ? null : typeConverterByName.get(converterName);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.TypeManager#registerConverter(java.lang.String, org.datanucleus.store.types.converters.TypeConverter, java.lang.Class, java.lang.Class, boolean, java.lang.String)
     */
    @Override
    public void registerConverter(String name, TypeConverter converter, Class memberType, Class dbType, boolean autoApply, String autoApplyType)
    {
        // Add to lookup name -> converter
        if (name != null)
        {
            typeConverterByName.put(name, converter);
        }

        // Add to lookup converter -> memberType
        typeConverterDatastoreTypeByConverter.put(converter, dbType);

        // Add to lookup converter -> dbType
        typeConverterMemberTypeByConverter.put(converter, memberType);

        // Add to lookup by memberType and dbType
        Map<Class, TypeConverter> convertersForMember = typeConverterMap.get(memberType);
        if (convertersForMember == null)
        {
            convertersForMember = new ConcurrentHashMap<Class, TypeConverter>();
            typeConverterMap.put(memberType, convertersForMember);
        }
        // TODO We could already have a converter registered for this datastore type, so this replaces it!
        convertersForMember.put(dbType, converter);

        if (converter instanceof ClassStringConverter)
        {
            // ClassStringConverter is a special case that needs the CLR injecting. TODO Find a general way for converters to use this
            ((ClassStringConverter)converter).setClassLoaderResolver(clr);
        }

        if (autoApply)
        {
            // Register converter to auto-apply
            if (autoApplyConvertersByType == null)
            {
                autoApplyConvertersByType = new ConcurrentHashMap<String, TypeConverter>();
            }
            autoApplyConvertersByType.put(autoApplyType, converter);
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.TypeManager#getAutoApplyTypeConverterForType(java.lang.Class)
     */
    @Override
    public TypeConverter getAutoApplyTypeConverterForType(Class memberType)
    {
        return autoApplyConvertersByType == null ? null : autoApplyConvertersByType.get(memberType.getName());
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.TypeManager#setDefaultTypeConverterForType(java.lang.Class, java.lang.String)
     */
    @Override
    public void setDefaultTypeConverterForType(Class memberType, String converterName)
    {
        JavaType javaType = javaTypes.get(memberType.getName());
        if (javaType == null)
        {
            return;
        }

        String typeConverterName = javaType.typeConverterName;
        if (typeConverterName == null || !typeConverterName.equals(converterName))
        {
            javaType.typeConverterName = converterName;
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.TypeManager#getDefaultTypeConverterForType(java.lang.Class)
     */
    @Override
    public TypeConverter getDefaultTypeConverterForType(Class memberType)
    {
        JavaType javaType = javaTypes.get(memberType.getName());
        if (javaType == null)
        {
            return null;
        }
        String typeConverterName = javaType.typeConverterName;
        if (typeConverterName == null)
        {
            return null;
        }
        return getTypeConverterForName(typeConverterName);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.TypeManager#getTypeConverterForType(java.lang.Class, java.lang.Class)
     */
    @Override
    public TypeConverter getTypeConverterForType(Class memberType, Class datastoreType)
    {
        if (memberType == null)
        {
            return null;
        }

        Map<Class, TypeConverter> convertersForMember = typeConverterMap.get(memberType);
        if (convertersForMember == null)
        {
            return null;
        }
        return convertersForMember.get(datastoreType);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.TypeManager#getTypeConvertersForType(java.lang.Class)
     */
    @Override
    public Collection<TypeConverter> getTypeConvertersForType(Class memberType)
    {
        if (memberType == null)
        {
            return null;
        }

        Map<Class, TypeConverter> convertersForMember = typeConverterMap.get(memberType);
        if (convertersForMember == null)
        {
            return null;
        }
        return convertersForMember.values();
    }

    /**
     * Method to return the datastore type for the specified TypeConverter.
     * @param conv The converter
     * @param memberType The member type
     * @return The datastore type
     */
    public Class getDatastoreTypeForTypeConverter(TypeConverter conv, Class memberType)
    {
        return typeConverterDatastoreTypeByConverter.get(conv);
    }

    /**
     * Method to return the member type for the specified TypeConverter.
     * @param conv The converter
     * @param datastoreType The datastore type for this converter
     * @return The member type
     */
    public Class getMemberTypeForTypeConverter(TypeConverter conv, Class datastoreType)
    {
        return typeConverterMemberTypeByConverter.get(conv);
    }

    /**
     * Convenience method to return the JavaType for the specified class. If this class has a defined
     * JavaType then returns it. If not then tries to find a superclass that is castable to the specified type.
     * @param cls The class required
     * @return The JavaType
     */
    protected JavaType findJavaTypeForClass(Class cls)
    {
        if (cls == null)
        {
            return null;
        }
        JavaType type = javaTypes.get(cls.getName());
        if (type != null)
        {
            return type;
        }

        // Not supported so try to find one that is supported that this class derives from
        Collection supportedTypes = new HashSet(javaTypes.values());
        Iterator iter = supportedTypes.iterator();
        while (iter.hasNext())
        {
            type = (JavaType)iter.next();
            if (type.cls == cls && type.genericType == null)
            {
                return type;
            }
            if (!type.cls.getName().equals("java.lang.Object") && !type.cls.getName().equals("java.io.Serializable"))
            {
                Class componentCls = cls.isArray() ? cls.getComponentType() : null;
                if (componentCls != null)
                {
                    // Array type
                    if (type.cls.isArray() && type.cls.getComponentType().isAssignableFrom(componentCls))
                    {
                        javaTypes.put(cls.getName(), type); // Register this subtype for reference
                        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                        {
                            NucleusLogger.PERSISTENCE.debug(Localiser.msg("016001", cls.getName(), type.cls.getName()));
                        }
                        return type;
                    }
                }
                else
                {
                    // Basic type
                    if (type.cls.isAssignableFrom(cls) && type.genericType == null)
                    {
                        if (type.wrapperType == null || type.wrapperType.isAssignableFrom(cls)) // Dont do this when we have a wrapper type to consider since would lead to ClassCastException
                        {
                            javaTypes.put(cls.getName(), type); // Register this subtype for reference
                            if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                            {
                                NucleusLogger.PERSISTENCE.debug(Localiser.msg("016001", cls.getName(), type.cls.getName()));
                            }
                            return type;
                        }
                    }
                }
            }
        }

        // Not supported
        return null;
    }

    /**
     * Convenience method to return the JavaType for the specified class. If this class has a defined
     * JavaType then returns it. If not then tries to find a superclass that is castable to the specified
     * type.
     * @param cls The class required
     * @param genericType Any generic type specified for the element
     * @return The JavaType
     */
    protected JavaType findJavaTypeForCollectionClass(Class cls, Class genericType)
    {
        if (cls == null)
        {
            return null;
        }
        else if (genericType == null)
        {
            return findJavaTypeForClass(cls);
        }

        String typeName = cls.getName() + "<" + genericType.getName() + ">";
        JavaType type = javaTypes.get(typeName);
        if (type != null)
        {
            return type;
        }

        // Not supported so try to find one that is supported that this class derives from
        Collection supportedTypes = new HashSet(javaTypes.values());
        Iterator iter = supportedTypes.iterator();
        while (iter.hasNext())
        {
            type = (JavaType)iter.next();
            if (type.cls.isAssignableFrom(cls))
            {
                if (type.genericType != null && type.genericType.isAssignableFrom(genericType))
                {
                    javaTypes.put(typeName, type); // Register this subtype for reference
                    return type;
                }
            }
        }

        // Fallback to just matching the collection type and forget the generic detail
        return findJavaTypeForClass(cls);
    }

    static class JavaType implements Serializable
    {
        private static final long serialVersionUID = -811442140006259453L;
        final Class cls;
        final Class genericType;
        final boolean embedded;
        final boolean dfg;
        final Class wrapperType;
        final Class wrapperTypeBacked;
        String typeConverterName;
        final Class containerHandlerType;

        public JavaType(Class cls, Class genericType, boolean embedded, boolean dfg, Class wrapperType, Class wrapperTypeBacked, Class containerHandlerType, String typeConverterName)
        {
            this.cls = cls;
            this.genericType = genericType;
            this.embedded = embedded;
            this.dfg = dfg;
            this.wrapperType = wrapperType;
            this.wrapperTypeBacked = wrapperTypeBacked != null ? wrapperTypeBacked : wrapperType;
            this.containerHandlerType = containerHandlerType;
            this.typeConverterName = typeConverterName;
        }
    }

    /**
     * Method to load the java types that we support out of the box.
     * This includes all built-in types, as well as all types registered via the plugin mechanism.
     * @param mgr the PluginManager
     */
    private void loadJavaTypes(PluginManager mgr)
    {
        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
        {
            NucleusLogger.PERSISTENCE.debug(Localiser.msg("016003"));
        }

        // Load up built-in types
        addJavaType(boolean.class, null, true, true, null, null, null, null);
        addJavaType(byte.class, null, true, true, null, null, null, null);
        addJavaType(char.class, null, true, true, null, null, null, null);
        addJavaType(double.class, null, true, true, null, null, null, null);
        addJavaType(float.class, null, true, true, null, null, null, null);
        addJavaType(int.class, null, true, true, null, null, null, null);
        addJavaType(long.class, null, true, true, null, null, null, null);
        addJavaType(short.class, null, true, true, null, null, null, null);

        addJavaType(Boolean.class, null, true, true, null, null, null, null);
        addJavaType(Byte.class, null, true, true, null, null, null, null);
        addJavaType(Character.class, null, true, true, null, null, null, null);
        addJavaType(Double.class, null, true, true, null, null, null, null);
        addJavaType(Float.class, null, true, true, null, null, null, null);
        addJavaType(Integer.class, null, true, true, null, null, null, null);
        addJavaType(Long.class, null, true, true, null, null, null, null);
        addJavaType(Short.class, null, true, true, null, null, null, null);

        addJavaType(Number.class, null, true, true, null, null, null, null);
        addJavaType(String.class, null, true, true, null, null, null, null);
        addJavaType(Enum.class, null, true, true, null, null, null, null);
        addJavaType(StringBuffer.class, null, true, true, null, null, null, "dn.stringbuffer-string");
        addJavaType(StringBuilder.class, null, true, true, null, null, null, "dn.stringbuilder-string");
        addJavaType(Class.class, null, true, true, null, null, null, "dn.class-string");

        if (ClassUtils.isClassPresent("java.awt.Color", clr)) // Not present in some JDKs
        {
            // java.awt
            addJavaType(java.awt.image.BufferedImage.class, null, true, false, null, null, null, "dn.bufferedimage-bytearray");
            addJavaType(java.awt.Color.class, null, true, true, null, null, null, "dn.color-string");
        }

        addJavaType(java.math.BigDecimal.class, null, true, true, null, null, null, null);
        addJavaType(java.math.BigInteger.class, null, true, true, null, null, null, null);

        // java.net
        addJavaType(java.net.URL.class, null, true, true, null, null, null, "dn.url-string");
        addJavaType(java.net.URI.class, null, true, true, null, null, null, "dn.uri-string");

        // date/time/java.time
        addJavaType(java.sql.Date.class, null, true, true, org.datanucleus.store.types.wrappers.SqlDate.class, null, null, null);
        addJavaType(java.sql.Time.class, null, true, true, org.datanucleus.store.types.wrappers.SqlTime.class, null, null, null);
        addJavaType(java.sql.Timestamp.class, null, true, true, org.datanucleus.store.types.wrappers.SqlTimestamp.class, null, null, null);
        addJavaType(java.util.Date.class, null, true, true, org.datanucleus.store.types.wrappers.Date.class, null, null, null);
        addJavaType(Calendar.class, null, true, true, org.datanucleus.store.types.wrappers.GregorianCalendar.class, null, null, "dn.calendar-string");
        addJavaType(GregorianCalendar.class, null, true, true, org.datanucleus.store.types.wrappers.GregorianCalendar.class, null, null, "dn.calendar-string");

        addJavaType(java.time.LocalDate.class, null, true, true, null, null, null, "dn.localdate-sqldate");
        addJavaType(java.time.LocalDateTime.class, null, true, true, null, null, null, "dn.localdatetime-timestamp");
        addJavaType(java.time.LocalTime.class, null, true, true, null, null, null, "dn.localtime-sqltime");
        addJavaType(java.time.OffsetTime.class, null, true, true, null, null, null, "dn.offsettime-sqltime");
        addJavaType(java.time.OffsetDateTime.class, null, true, true, null, null, null, "dn.offsetdatetime-timestamp");
        addJavaType(java.time.Duration.class, null, true, true, null, null, null, "dn.duration-long");
        addJavaType(java.time.Instant.class, null, true, true, null, null, null, "dn.instant-timestamp");
        addJavaType(java.time.Period.class, null, true, true, null, null, null, "dn.period-string");
        addJavaType(java.time.Year.class, null, true, true, null, null, null, "dn.year-integer");
        addJavaType(java.time.YearMonth.class, null, true, true, null, null, null, "dn.yearmonth-string");
        addJavaType(java.time.MonthDay.class, null, true, true, null, null, null, "dn.monthday-string");
        addJavaType(java.time.ZoneId.class, null, true, true, null, null, null, "dn.zoneid-string");
        addJavaType(java.time.ZoneOffset.class, null, true, true, null, null, null, "dn.zoneoffset-string");
        addJavaType(java.time.ZonedDateTime.class, null, true, true, null, null, null, "dn.zoneddatetime-timestamp");

        // java.util
        addJavaType(Locale.class, null, true, true, null, null, null, "dn.locale-string");
        addJavaType(Currency.class, null, true, true, null, null, null, "dn.currency-string");
        addJavaType(UUID.class, null, true, true, null, null, null, "dn.uuid-string");
        addJavaType(TimeZone.class, null, true, true, null, null, null, "dn.timezone-string");

        addJavaType(ArrayList.class, null, false, false, org.datanucleus.store.types.wrappers.ArrayList.class, 
            org.datanucleus.store.types.wrappers.backed.ArrayList.class, ArrayListHandler.class, null);

        String arrayListInnerType = "java.util.Arrays$ArrayList";
        Class arrayListInnerTypeCls = clr.classForName(arrayListInnerType);
        addJavaType(arrayListInnerTypeCls, null, false, false, org.datanucleus.store.types.wrappers.List.class, org.datanucleus.store.types.wrappers.backed.List.class,
            org.datanucleus.store.types.containers.ArrayListHandler.class, null);

        addJavaType(BitSet.class, null, true, true, org.datanucleus.store.types.wrappers.BitSet.class, null, null, "dn.bitset-string");
        addJavaType(Collection.class, null, false, false, org.datanucleus.store.types.wrappers.Collection.class, org.datanucleus.store.types.wrappers.backed.Collection.class, 
            JDKCollectionHandler.class, null);
        addJavaType(HashMap.class, null, false, false, org.datanucleus.store.types.wrappers.HashMap.class, org.datanucleus.store.types.wrappers.backed.HashMap.class,
            HashMapHandler.class, null);
        addJavaType(HashSet.class, null, false, false, org.datanucleus.store.types.wrappers.HashSet.class, org.datanucleus.store.types.wrappers.backed.HashSet.class,
            HashSetHandler.class, null);
        addJavaType(Hashtable.class, null, false, false, org.datanucleus.store.types.wrappers.Hashtable.class, org.datanucleus.store.types.wrappers.backed.Hashtable.class,
            HashtableHandler.class, null);
        addJavaType(LinkedHashMap.class, null, false, false, org.datanucleus.store.types.wrappers.LinkedHashMap.class, org.datanucleus.store.types.wrappers.backed.LinkedHashMap.class,
            LinkedHashMapHandler.class, null);
        addJavaType(LinkedHashSet.class, null, false, false, org.datanucleus.store.types.wrappers.LinkedHashSet.class, org.datanucleus.store.types.wrappers.backed.LinkedHashSet.class,
            LinkedHashSetHandler.class, null);
        addJavaType(LinkedList.class, null, false, false, org.datanucleus.store.types.wrappers.LinkedList.class, org.datanucleus.store.types.wrappers.backed.LinkedList.class,
            LinkedListHandler.class, null);
        addJavaType(List.class, null, false, false, org.datanucleus.store.types.wrappers.List.class, org.datanucleus.store.types.wrappers.backed.List.class,
            ArrayListHandler.class, null);
        addJavaType(Map.class, null, false, false, org.datanucleus.store.types.wrappers.Map.class, org.datanucleus.store.types.wrappers.backed.Map.class,
            HashMapHandler.class, null);
        addJavaType(PriorityQueue.class, null, false, false, org.datanucleus.store.types.wrappers.PriorityQueue.class, org.datanucleus.store.types.wrappers.backed.PriorityQueue.class,
            PriorityQueueHandler.class, null);
        addJavaType(Properties.class, null, false, false, org.datanucleus.store.types.wrappers.Properties.class, org.datanucleus.store.types.wrappers.backed.Properties.class,
            PropertiesHandler.class, null);
        addJavaType(Queue.class, null, false, false, org.datanucleus.store.types.wrappers.Queue.class, org.datanucleus.store.types.wrappers.backed.Queue.class,
            PriorityQueueHandler.class, null);
        addJavaType(Set.class, null, false, false, org.datanucleus.store.types.wrappers.Set.class, org.datanucleus.store.types.wrappers.backed.Set.class,
            HashSetHandler.class, null);
        addJavaType(SortedMap.class, null, false, false, org.datanucleus.store.types.wrappers.SortedMap.class, org.datanucleus.store.types.wrappers.backed.SortedMap.class,
            TreeMapHandler.class, null);
        addJavaType(SortedSet.class, null, false, false, org.datanucleus.store.types.wrappers.SortedSet.class, org.datanucleus.store.types.wrappers.backed.SortedSet.class,
            TreeSetHandler.class, null);
        addJavaType(Stack.class, null, false, false, org.datanucleus.store.types.wrappers.Stack.class, org.datanucleus.store.types.wrappers.backed.Stack.class,
            StackHandler.class, null);
        addJavaType(TreeMap.class, null, false, false, org.datanucleus.store.types.wrappers.TreeMap.class, org.datanucleus.store.types.wrappers.backed.TreeMap.class,
            TreeMapHandler.class, null);
        addJavaType(TreeSet.class, null, false, false, org.datanucleus.store.types.wrappers.TreeSet.class, org.datanucleus.store.types.wrappers.backed.TreeSet.class,
            TreeSetHandler.class, null);
        addJavaType(Vector.class, null, false, false, org.datanucleus.store.types.wrappers.Vector.class, org.datanucleus.store.types.wrappers.backed.Vector.class,
            VectorHandler.class, null);
        addJavaType(Optional.class, null, false, false, null, null, OptionalHandler.class, null);

        // arrays
        addJavaType(boolean[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(byte[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(char[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(double[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(float[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(int[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(long[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(short[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(Boolean[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(Byte[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(Character[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(Double[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(Float[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(Integer[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(Long[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(Short[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(Number[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(String[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(BigDecimal[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(BigInteger[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(java.util.Date[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(java.util.Locale[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(Enum[].class, null, true, false, null, null, ArrayHandler.class, null);
        addJavaType(Object[].class, null, true, false, null, null, ArrayHandler.class, null);

        // Add on any plugin mechanism types
        ConfigurationElement[] elems = mgr.getConfigurationElementsForExtension("org.datanucleus.java_type", null, null);
        if (elems != null)
        {
            for (int i=0; i<elems.length; i++)
            {
                String javaName = elems[i].getAttribute("name").trim();
                String genericTypeName = elems[i].getAttribute("generic-type");
                String embeddedString = elems[i].getAttribute("embedded");
                String dfgString = elems[i].getAttribute("dfg");
                String priorityString = elems[i].getAttribute("priority");
                String wrapperType = elems[i].getAttribute("wrapper-type");
                String wrapperTypeBacked = elems[i].getAttribute("wrapper-type-backed");
                String typeConverterName = elems[i].getAttribute("converter-name");
                String containerHandlerType = elems[i].getAttribute("container-handler");

                boolean embedded = false;
                if (embeddedString != null && embeddedString.equalsIgnoreCase("true"))
                {
                    embedded = true;
                }
                boolean dfg = false;
                if (dfgString != null && dfgString.equalsIgnoreCase("true"))
                {
                    dfg = true;
                }
                int priority = 0;
                if (priorityString != null && !StringUtils.isWhitespace(priorityString))
                {
                    try {
                        priority = Integer.parseInt(priorityString.trim());
                    } catch (Exception e) {
                        // if cannot parse, silently fallback
                        priority = 0;
                    }
                }
                if (!StringUtils.isWhitespace(wrapperType))
                {
                    wrapperType = wrapperType.trim();
                }
                else
                {
                    wrapperType = null;
                }
                if (!StringUtils.isWhitespace(wrapperTypeBacked))
                {
                    wrapperTypeBacked = wrapperTypeBacked.trim();
                }
                else
                {
                    wrapperTypeBacked = null;
                }
                if (!StringUtils.isWhitespace(containerHandlerType))
                {
                    containerHandlerType = containerHandlerType.trim();
                }
                else
                {
                    containerHandlerType = null;
                }

                try
                {
                    Class cls = clr.classForName(javaName);
                    Class genericType = null;
                    String javaTypeName = cls.getName();
                    if (!StringUtils.isWhitespace(genericTypeName))
                    {
                        genericType = clr.classForName(genericTypeName);
                        javaTypeName += "<" + genericTypeName + ">";
                    }

                    
                    boolean doRegister = !javaTypes.containsKey(javaTypeName);
                    if(!doRegister) {
                        // check whether new priority wins over already registered priority
                        final int priorityToBeat = 
                                Optional.ofNullable(javaTypePriorities.get(javaTypeName)).orElse(0);
                        doRegister = priority > priorityToBeat;
                    }
                    
                    if (doRegister)
                    {
                        // Only add first entry for a java type (ordered by the "priority" flag)

                        Class wrapperClass = loadClass(mgr, elems, i, wrapperType, "016005");
                        Class wrapperClassBacked = loadClass(mgr, elems, i, wrapperTypeBacked, "016005");
                        Class containerHandlerClass = loadClass(mgr, elems, i, containerHandlerType, "016009");

                        String typeName = cls.getName();
                        if (genericType != null)
                        {
                            // "Collection<String>"
                            typeName += "<" + genericType.getName() + ">";
                        }
                        javaTypes.put(typeName, new JavaType(cls, genericType, embedded, dfg, wrapperClass, wrapperClassBacked, containerHandlerClass, typeConverterName));

                        // keep track of registered priority values, 
                        // as an optimization, save heap usage, don't collect priority==0 
                        if(priority>0) {
                            javaTypePriorities.put(javaTypeName, priority);
                        }
                    }
                }
                catch (ClassNotResolvedException cnre)
                {
                    NucleusLogger.PERSISTENCE.debug("Not enabling java type support for " + javaName + " : java type not present in CLASSPATH");
                }
                catch (Exception e)
                {
                    NucleusLogger.PERSISTENCE.debug("Not enabling java type support for " + javaName + " : " + e.getMessage());
                }
            }
        }
        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
        {
            List<String> typesList = new ArrayList<String>(javaTypes.keySet());
            Collections.sort(typesList, ALPHABETICAL_ORDER_STRING);
            NucleusLogger.PERSISTENCE.debug(Localiser.msg("016006", StringUtils.collectionToString(typesList)));
        }
    }

    private void addJavaType(Class cls, Class genericType, boolean embedded, boolean dfg, Class wrapperType, Class wrapperTypeBacked, Class containerHandlerType, String typeConverterName)
    {
        String typeName = cls.getName();
        if (genericType != null)
        {
            // "Collection<String>"
            typeName += "<" + genericType.getName() + ">";
        }
        javaTypes.put(typeName, new JavaType(cls, genericType, embedded, dfg, wrapperType, wrapperTypeBacked, containerHandlerType, typeConverterName));
    }

    private Class loadClass(PluginManager mgr, ConfigurationElement[] elems, int i, String className, String messageKey)
    {
        Class result = null;
        if (className != null)
        {
            try
            {
                result = mgr.loadClass(elems[i].getExtension().getPlugin().getSymbolicName(), className);
            }
            catch (NucleusException jpe)
            {
                // Impossible to load the class implementation from this plugin
                NucleusLogger.PERSISTENCE.error(Localiser.msg(messageKey, className));
                throw new NucleusException(Localiser.msg(messageKey, className));
            }
        }
        return result;
    }

    /**
     * Method to load the java type that are currently registered in the PluginManager.
     * @param mgr the PluginManager
     */
    private void loadTypeConverters(PluginManager mgr)
    {
        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
        {
            NucleusLogger.PERSISTENCE.debug(Localiser.msg("016007"));
        }

        // Load up built-in converters
        registerConverter("dn.boolean-yn", new org.datanucleus.store.types.converters.BooleanYNConverter(), Boolean.class, Character.class, false, null);
        registerConverter("dn.boolean-integer", new org.datanucleus.store.types.converters.BooleanIntegerConverter(), Boolean.class, Integer.class, false, null);
        registerConverter("dn.character-string", new org.datanucleus.store.types.converters.CharacterStringConverter(), Character.class, String.class, false, null);
        registerConverter("dn.bigdecimal-string", new org.datanucleus.store.types.converters.BigDecimalStringConverter(), BigDecimal.class, String.class, false, null);
        registerConverter("dn.bigdecimal-double", new org.datanucleus.store.types.converters.BigDecimalDoubleConverter(), BigDecimal.class, Double.class, false, null);
        registerConverter("dn.biginteger-string", new org.datanucleus.store.types.converters.BigIntegerStringConverter(), BigInteger.class, String.class, false, null);
        registerConverter("dn.biginteger-long", new org.datanucleus.store.types.converters.BigIntegerLongConverter(), BigInteger.class, Long.class, false, null);
        registerConverter("dn.bitset-string", new org.datanucleus.store.types.converters.BitSetStringConverter(), BitSet.class, String.class, false, null);

        if (ClassUtils.isClassPresent("java.awt.Color", clr)) // Not present in some JDKs
        {
            // java.awt
            registerConverter("dn.color-string", new org.datanucleus.store.types.converters.ColorStringConverter(), java.awt.Color.class, String.class, false, null);
            registerConverter("dn.color-components", new org.datanucleus.store.types.converters.ColorComponentsConverter(), java.awt.Color.class, int[].class, false, null);
            registerConverter("dn.bufferedimage-bytearray", new org.datanucleus.store.types.converters.BufferedImageByteArrayConverter(), java.awt.image.BufferedImage.class, byte[].class, false, null);
            registerConverter("dn.bufferedimage-bytebuffer", new org.datanucleus.store.types.converters.BufferedImageByteBufferConverter(), java.awt.image.BufferedImage.class, ByteBuffer.class, false, null);
        }

        registerConverter("dn.class-string", new org.datanucleus.store.types.converters.ClassStringConverter(), Class.class, String.class, false, null);
        registerConverter("dn.integer-string", new org.datanucleus.store.types.converters.IntegerStringConverter(), Integer.class, String.class, false, null);
        registerConverter("dn.long-string", new org.datanucleus.store.types.converters.LongStringConverter(), Long.class, String.class, false, null);
        registerConverter("dn.currency-string", new org.datanucleus.store.types.converters.CurrencyStringConverter(), Currency.class, String.class, false, null);
        registerConverter("dn.locale-string", new org.datanucleus.store.types.converters.LocaleStringConverter(), Locale.class, String.class, false, null);
        registerConverter("dn.stringbuffer-string", new org.datanucleus.store.types.converters.StringBufferStringConverter(), StringBuffer.class, String.class, false, null);
        registerConverter("dn.stringbuilder-string", new org.datanucleus.store.types.converters.StringBuilderStringConverter(), StringBuilder.class, String.class, false, null);
        registerConverter("dn.timezone-string", new org.datanucleus.store.types.converters.TimeZoneStringConverter(), TimeZone.class, String.class, false, null);
        registerConverter("dn.uri-string", new org.datanucleus.store.types.converters.URIStringConverter(), java.net.URI.class, String.class, false, null);
        registerConverter("dn.url-string", new org.datanucleus.store.types.converters.URLStringConverter(), java.net.URL.class, String.class, false, null);
        registerConverter("dn.uuid-string", new org.datanucleus.store.types.converters.UUIDStringConverter(), UUID.class, String.class, false, null);

        // Date/Time
        registerConverter("dn.date-long", new org.datanucleus.store.types.converters.DateLongConverter(), java.util.Date.class, Long.class, false, null);
        registerConverter("dn.date-string", new org.datanucleus.store.types.converters.DateStringConverter(), java.util.Date.class, String.class, false, null);

        registerConverter("dn.sqldate-long", new org.datanucleus.store.types.converters.SqlDateLongConverter(), java.sql.Date.class, Long.class, false, null);
        registerConverter("dn.sqldate-string", new org.datanucleus.store.types.converters.SqlDateStringConverter(), java.sql.Date.class, String.class, false, null);
        registerConverter("dn.sqldate-date", new org.datanucleus.store.types.converters.SqlDateStringConverter(), java.sql.Date.class, java.util.Date.class, false, null);
        registerConverter("dn.sqltime-long", new org.datanucleus.store.types.converters.SqlTimeStringConverter(), java.sql.Time.class, Long.class, false, null);
        registerConverter("dn.sqltime-string", new org.datanucleus.store.types.converters.SqlTimeStringConverter(), java.sql.Time.class, String.class, false, null);
        registerConverter("dn.sqltime-date", new org.datanucleus.store.types.converters.SqlTimeStringConverter(), java.sql.Time.class, java.util.Date.class, false, null);
        registerConverter("dn.sqltimestamp-long", new org.datanucleus.store.types.converters.SqlTimestampStringConverter(), java.sql.Timestamp.class, Long.class, false, null);
        registerConverter("dn.sqltimestamp-date", new org.datanucleus.store.types.converters.SqlTimestampStringConverter(), java.sql.Timestamp.class, java.util.Date.class, false, null);
        registerConverter("dn.sqltimestamp-string", new org.datanucleus.store.types.converters.SqlTimestampStringConverter(), java.sql.Timestamp.class, String.class, false, null);

        registerConverter("dn.calendar-string", new org.datanucleus.store.types.converters.CalendarStringConverter(), Calendar.class, String.class, false, null);
        registerConverter("dn.calendar-date", new org.datanucleus.store.types.converters.CalendarDateConverter(), Calendar.class, java.util.Date.class, false, null);
        registerConverter("dn.calendar-timestamp", new org.datanucleus.store.types.converters.CalendarTimestampConverter(), Calendar.class, java.sql.Timestamp.class, false, null);
        registerConverter("dn.calendar-components", new org.datanucleus.store.types.converters.CalendarComponentsConverter(), Calendar.class, Object[].class, false, null);

        // Serializable
        registerConverter("dn.serializable-string", new org.datanucleus.store.types.converters.SerializableStringConverter(), java.io.Serializable.class, String.class, false, null);
        registerConverter("dn.serializable-bytearray", new org.datanucleus.store.types.converters.SerializableByteArrayConverter(), java.io.Serializable.class, byte[].class, false, null);
        registerConverter("dn.serializable-bytebuffer", new org.datanucleus.store.types.converters.SerializableByteBufferConverter(), java.io.Serializable.class, ByteBuffer.class, false, null);

        // Arrays
        registerConverter("dn.bytearray-bytebuffer", new org.datanucleus.store.types.converters.ByteArrayByteBufferConverter(), byte[].class, ByteBuffer.class, false, null);
        registerConverter("dn.booleanarray-bytebuffer", new org.datanucleus.store.types.converters.BooleanArrayByteBufferConverter(), boolean[].class, ByteBuffer.class, false, null);
        registerConverter("dn.chararray-bytebuffer", new org.datanucleus.store.types.converters.CharArrayByteBufferConverter(), char[].class, ByteBuffer.class, false, null);
        registerConverter("dn.doublearray-bytebuffer", new org.datanucleus.store.types.converters.DoubleArrayByteBufferConverter(), double[].class, ByteBuffer.class, false, null);
        registerConverter("dn.floatarray-bytebuffer", new org.datanucleus.store.types.converters.FloatArrayByteBufferConverter(), float[].class, ByteBuffer.class, false, null);
        registerConverter("dn.intarray-bytebuffer", new org.datanucleus.store.types.converters.IntArrayByteBufferConverter(), int[].class, ByteBuffer.class, false, null);
        registerConverter("dn.longarray-bytebuffer", new org.datanucleus.store.types.converters.LongArrayByteBufferConverter(), long[].class, ByteBuffer.class, false, null);
        registerConverter("dn.shortarray-bytebuffer", new org.datanucleus.store.types.converters.ShortArrayByteBufferConverter(), short[].class, ByteBuffer.class, false, null);
        registerConverter("dn.bigintegerarray-bytebuffer", new org.datanucleus.store.types.converters.BigIntegerArrayByteBufferConverter(), BigInteger[].class, ByteBuffer.class, false, null);
        registerConverter("dn.bigdecimalarray-bytebuffer", new org.datanucleus.store.types.converters.BigDecimalArrayByteBufferConverter(), BigDecimal[].class, ByteBuffer.class, false, null);

        // java.time
        registerConverter("dn.localdate-string", new org.datanucleus.store.types.converters.LocalDateStringConverter(), LocalDate.class, String.class, false, null);
        registerConverter("dn.localdate-sqldate", new org.datanucleus.store.types.converters.LocalDateSqlDateConverter(), LocalDate.class, java.sql.Date.class, false, null);
        registerConverter("dn.localdate-date", new org.datanucleus.store.types.converters.LocalDateDateConverter(), LocalDate.class, java.util.Date.class, false, null);

        registerConverter("dn.localtime-string", new org.datanucleus.store.types.converters.LocalTimeStringConverter(), LocalTime.class, String.class, false, null);
        registerConverter("dn.localtime-sqltime", new org.datanucleus.store.types.converters.LocalTimeSqlTimeConverter(), LocalTime.class, java.sql.Time.class, false, null);
        registerConverter("dn.localtime-date", new org.datanucleus.store.types.converters.LocalTimeDateConverter(), LocalTime.class, java.util.Date.class, false, null);
        registerConverter("dn.localtime-long", new org.datanucleus.store.types.converters.LocalTimeLongConverter(), LocalTime.class, Long.class, false, null);

        registerConverter("dn.localdatetime-string", new org.datanucleus.store.types.converters.LocalDateTimeStringConverter(), LocalDateTime.class, String.class, false, null);
        registerConverter("dn.localdatetime-timestamp", new org.datanucleus.store.types.converters.LocalDateTimeTimestampConverter(), LocalDateTime.class, java.sql.Timestamp.class, false, null);
        registerConverter("dn.localdatetime-date", new org.datanucleus.store.types.converters.LocalDateTimeDateConverter(), LocalDateTime.class, java.util.Date.class, false, null);

        registerConverter("dn.offsettime-string", new org.datanucleus.store.types.converters.OffsetTimeStringConverter(), OffsetTime.class, String.class, false, null);
        registerConverter("dn.offsettime-long", new org.datanucleus.store.types.converters.OffsetTimeLongConverter(), OffsetTime.class, Long.class, false, null);
        registerConverter("dn.offsettime-sqltime", new org.datanucleus.store.types.converters.OffsetTimeSqlTimeConverter(), OffsetTime.class, java.sql.Time.class, false, null);

        registerConverter("dn.offsetdatetime-string", new org.datanucleus.store.types.converters.OffsetDateTimeStringConverter(), OffsetDateTime.class, String.class, false, null);
        registerConverter("dn.offsetdatetime-timestamp", new org.datanucleus.store.types.converters.OffsetDateTimeTimestampConverter(), OffsetDateTime.class, java.sql.Timestamp.class, false, null);
        registerConverter("dn.offsetdatetime-date", new org.datanucleus.store.types.converters.OffsetDateTimeDateConverter(), OffsetDateTime.class, java.util.Date.class, false, null);

        registerConverter("dn.duration-string", new org.datanucleus.store.types.converters.DurationStringConverter(), Duration.class, String.class, false, null);
        registerConverter("dn.duration-long", new org.datanucleus.store.types.converters.DurationLongConverter(), Duration.class, Long.class, false, null);
        registerConverter("dn.duration-double", new org.datanucleus.store.types.converters.DurationDoubleConverter(), Duration.class, Double.class, false, null);

        registerConverter("dn.period-string", new org.datanucleus.store.types.converters.PeriodStringConverter(), Period.class, String.class, false, null);
        registerConverter("dn.period-components", new org.datanucleus.store.types.converters.PeriodComponentsConverter(), Period.class, int[].class, false, null);

        registerConverter("dn.instant-timestamp", new org.datanucleus.store.types.converters.InstantTimestampConverter(), Instant.class, java.sql.Timestamp.class, false, null);
        registerConverter("dn.instant-date", new org.datanucleus.store.types.converters.InstantDateConverter(), Instant.class, java.util.Date.class, false, null);
        registerConverter("dn.instant-string", new org.datanucleus.store.types.converters.InstantStringConverter(), Instant.class, String.class, false, null);
        registerConverter("dn.instant-long", new org.datanucleus.store.types.converters.InstantLongConverter(), Instant.class, Long.class, false, null);

        registerConverter("dn.year-string", new org.datanucleus.store.types.converters.YearStringConverter(), Year.class, String.class, false, null);
        registerConverter("dn.year-integer", new org.datanucleus.store.types.converters.YearIntegerConverter(), Year.class, Integer.class, false, null);

        registerConverter("dn.yearmonth-string", new org.datanucleus.store.types.converters.YearMonthStringConverter(), YearMonth.class, String.class, false, null);
        registerConverter("dn.yearmonth-components", new org.datanucleus.store.types.converters.YearMonthComponentsConverter(), YearMonth.class, int[].class, false, null);
        registerConverter("dn.yearmonth-sqldate", new org.datanucleus.store.types.converters.YearMonthSqlDateConverter(), YearMonth.class, java.sql.Date.class, false, null);
        registerConverter("dn.yearmonth-date", new org.datanucleus.store.types.converters.YearMonthDateConverter(), YearMonth.class, java.util.Date.class, false, null);

        registerConverter("dn.monthday-string", new org.datanucleus.store.types.converters.MonthDayStringConverter(), MonthDay.class, String.class, false, null);
        registerConverter("dn.monthday-components", new org.datanucleus.store.types.converters.MonthDayComponentsConverter(), MonthDay.class, int[].class, false, null);
        registerConverter("dn.monthday-sqldate", new org.datanucleus.store.types.converters.MonthDaySqlDateConverter(), MonthDay.class, java.sql.Date.class, false, null);
        registerConverter("dn.monthday-date", new org.datanucleus.store.types.converters.MonthDayDateConverter(), MonthDay.class, java.util.Date.class, false, null);

        registerConverter("dn.zoneid-string", new org.datanucleus.store.types.converters.ZoneIdStringConverter(), ZoneId.class, String.class, false, null);
        registerConverter("dn.zoneoffset-string", new org.datanucleus.store.types.converters.ZoneOffsetStringConverter(), ZoneOffset.class, String.class, false, null);
        registerConverter("dn.zoneddatetime-string", new org.datanucleus.store.types.converters.ZonedDateTimeStringConverter(), ZonedDateTime.class, String.class, false, null);
        registerConverter("dn.zoneddatetime-timestamp", new org.datanucleus.store.types.converters.ZonedDateTimeTimestampConverter(), ZonedDateTime.class, java.sql.Timestamp.class, false, null);

        // Add on any plugin mechanism types
        ConfigurationElement[] elems = mgr.getConfigurationElementsForExtension("org.datanucleus.type_converter", null, null);
        if (elems != null)
        {
            for (int i=0; i<elems.length; i++)
            {
                String name = elems[i].getAttribute("name").trim();
                String memberTypeName = elems[i].getAttribute("member-type").trim();
                String datastoreTypeName = elems[i].getAttribute("datastore-type").trim();
                String converterClsName = elems[i].getAttribute("converter-class").trim();
                Class memberType = null;
                try
                {
                    // Use plugin manager to instantiate the converter in case its in separate plugin
                    TypeConverter conv = (TypeConverter) mgr.createExecutableExtension("org.datanucleus.type_converter", "name", name, "converter-class", null, null);
                    memberType = clr.classForName(memberTypeName);
                    Class datastoreType = clr.classForName(datastoreTypeName);
                    registerConverter(name, conv, memberType, datastoreType, false, null);
                }
                catch (Exception e)
                {
                    if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                    {
                        if (memberType != null)
                        {
                            NucleusLogger.PERSISTENCE.debug("TypeConverter for " + memberTypeName + "<->" +
                                datastoreTypeName + " using " + converterClsName + " not instantiable (missing dependencies?) so ignoring");
                        }
                        else
                        {
                            NucleusLogger.PERSISTENCE.debug("TypeConverter for " + memberTypeName + "<->" +
                                datastoreTypeName + " ignored since java type not present in CLASSPATH");
                        }
                    }
                }
            }
        }

        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
        {
            NucleusLogger.PERSISTENCE.debug(Localiser.msg("016008"));
            if (typeConverterMap != null)
            {
                List<Class> typesList = new ArrayList<Class>(typeConverterMap.keySet());
                Collections.sort(typesList, ALPHABETICAL_ORDER);
                for (Class javaType : typesList)
                {
                    Set<Class> datastoreTypes = typeConverterMap.get(javaType).keySet();
                    StringBuilder str = new StringBuilder();
                    for (Class datastoreCls : datastoreTypes)
                    {
                        if (str.length() > 0)
                        {
                            str.append(',');
                        }
                        str.append(StringUtils.getNameOfClass(datastoreCls));
                    }
                    NucleusLogger.PERSISTENCE.debug("TypeConverter(s) available for " + StringUtils.getNameOfClass(javaType) + " to : " + str.toString());
                }
            }
        }
    }

    private static Comparator<Class> ALPHABETICAL_ORDER = new Comparator<Class>() 
    {
        public int compare(Class cls1, Class cls2) 
        {
            int res = String.CASE_INSENSITIVE_ORDER.compare(cls1.getName(), cls2.getName());
            if (res == 0) 
            {
                res = cls1.getName().compareTo(cls2.getName());
            }
            return res;
        }
    };

    private static Comparator<String> ALPHABETICAL_ORDER_STRING = new Comparator<String>() 
    {
        public int compare(String cls1, String cls2) 
        {
            int res = String.CASE_INSENSITIVE_ORDER.compare(cls1, cls2);
            if (res == 0) 
            {
                res = cls1.compareTo(cls2);
            }
            return res;
        }
    };
}