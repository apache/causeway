package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.application.Lookup;
import org.nakedobjects.application.NonPersistable;
import org.nakedobjects.application.Title;
import org.nakedobjects.application.collection.InternalCollection;
import org.nakedobjects.application.control.ActionAbout;
import org.nakedobjects.application.control.ClassAbout;
import org.nakedobjects.application.control.FieldAbout;
import org.nakedobjects.application.value.BusinessValue;
import org.nakedobjects.application.valueholder.BusinessValueHolder;
import org.nakedobjects.application.valueholder.Logical;
import org.nakedobjects.application.valueholder.TextString;
import org.nakedobjects.object.Aggregated;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecificationException;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.ActionPeer;
import org.nakedobjects.object.reflect.FieldPeer;
import org.nakedobjects.object.reflect.ObjectTitle;
import org.nakedobjects.object.reflect.ReflectionException;
import org.nakedobjects.object.reflect.Reflector;
import org.nakedobjects.reflector.java.JavaObjectFactory;
import org.nakedobjects.reflector.java.control.SimpleClassAbout;
import org.nakedobjects.reflector.java.value.LogicalValueObjectAdapter;
import org.nakedobjects.reflector.java.value.TextStringAdapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;


public class JavaReflector implements Reflector {
    private static final String ABOUT_FIELD_DEFAULT = "aboutFieldDefault";
    private static final String ABOUT_PREFIX = "about";
    private static final String DERIVE_PREFIX = "derive";
    private static final String GET_PREFIX = "get";
    private final static Logger LOG = Logger.getLogger(JavaReflector.class);
    private static final Object[] NO_PARAMETERS = new Object[0];
    private static final String SET_PREFIX = "set";

    /**
     * Returns the name of a Java entity without any prefix. A prefix is defined
     * as the first set of lowercase letters and the name is characters from,
     * and including, the first upper case letter. If no upper case letter is
     * found then an empty string is returned.
     * 
     * <p>
     * Calling this method with the following Java names will produce these
     * results:
     * 
     * <pre>
     * 
     *  
     *       getCarRegistration        -&gt; CarRegistration
     *       CityMayor -&gt; CityMayor
     *       isReady -&gt; Ready
     *       
     *  
     * </pre>
     *  
     */
    protected static String javaBaseName(String javaName) {
        int pos = 0;

        // find first upper case character
        int len = javaName.length();

        while ((pos < len) && (javaName.charAt(pos) != '_') && Character.isLowerCase(javaName.charAt(pos))) {
            pos++;
        }

        if (pos >= len) {
            return "";
        }

        if (javaName.charAt(pos) == '_') {
            pos++;
        }

        if (pos >= len) {
            return "";
        }

        String baseName = javaName.substring(pos);
        char firstChar = baseName.charAt(0);

        if (Character.isLowerCase(firstChar)) {
            return Character.toUpperCase(firstChar) + baseName.substring(1);
        } else {
            return baseName;
        }
    }

    /**
     * Invokes, by reflection, the Order method prefixed by the specified type
     * name. The returned string is tokenized - broken on the commas - and
     * returned in the array.
     */
    private static String[] readSortOrder(Class aClass, String type) {
        try {
            Method method = aClass.getMethod(type + "Order", new Class[0]);

            if (Modifier.isStatic(method.getModifiers())) {
                String s = (String) method.invoke(null, NO_PARAMETERS);
                if (s.trim().length() > 0) {
                    java.util.StringTokenizer st = new java.util.StringTokenizer(s, ",");
                    String[] a = new String[st.countTokens()];
                    int element = 0;

                    while (st.hasMoreTokens()) {
                        a[element] = st.nextToken().trim();
                        element++;
                    }
                    return a;
                } else {
                    return null;
                }

            } else {
                LOG.warn("Method " + aClass.getName() + "." + type + "Order() must be decared as static");
            }
        } catch (NoSuchMethodException ignore) {} catch (IllegalAccessException ignore) {} catch (InvocationTargetException ignore) {}

        return null;
    }

    /**
     * Returns the short name of the fully qualified name (including the package
     * name) . e.g. for com.xyz.example.Customer returns Customer.
     */
    protected static String shortClassName(String fullyQualifiedClassName) {
        return fullyQualifiedClassName.substring(fullyQualifiedClassName.lastIndexOf('.') + 1);
    }

    private Method clearDirtyMethod;

    private Class cls;
    private Method defaultAboutFieldMethod;
    private Method isDirtyMethod;
    private Method markDirtyMethod;
    private Method methods[];
    private final JavaObjectFactory objectFactory;

    public JavaReflector(String name, JavaObjectFactory objectFactory) throws ReflectionException {
        this.objectFactory = objectFactory;
        Class cls;

        try {
            cls = Class.forName(name);

        } catch (ClassNotFoundException e) {
            throw new ReflectionException("Could not load class " + name, e);
        }

        if (!Modifier.isPublic(cls.getModifiers())) {
            throw new NakedObjectSpecificationException("A NakedObject class must be marked as public.  Error in " + cls);
        }
        this.cls = cls;
        methods = cls.getMethods();

        isDirtyMethod = findMethod(false, "isDirty", boolean.class, new Class[0]);
        clearDirtyMethod = findMethod(false, "clearDirty", void.class, new Class[0]);
        markDirtyMethod = findMethod(false, "markDirty", void.class, new Class[0]);
    }

    public Naked acquireInstance() {
        if (Modifier.isAbstract(cls.getModifiers())) {
            throw new IllegalStateException("Handling of abstract naked classes is not yet supported: " + cls);
        }

        Object object = objectFactory.createObject(cls);

        // TODO this code is duplicated in JavaReflectorFactory
        if (object instanceof TextString) {
            return new TextStringAdapter((TextString) object);
        } else if (object instanceof Logical) {
            return new LogicalValueObjectAdapter((Logical) object);
        } else {
            return NakedObjects.getPojoAdapterFactory().createAdapter(objectFactory.createObject(cls));
        }
    }

    public ActionPeer[] actionPeers(boolean forClass) {
        LOG.debug("looking for action methods");
        Method defaultAboutMethod = findMethod(forClass, "aboutActionDefault", null, new Class[] { ActionAbout.class });
        LOG.debug(defaultAboutMethod == null ? "no default about method for actions" : defaultAboutMethod.toString());

        Vector validMethods = new Vector();
        Vector actions = new Vector();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i] == null) {
                continue;
            }
            Method method = methods[i];

            if (Modifier.isStatic(method.getModifiers()) != forClass) {
                continue;
            }

            String[] prefixes = { "action", "explorationAction", "debugAction" };
            int prefix = -1;
            for (int j = 0; j < prefixes.length; j++) {
                if (method.getName().startsWith(prefixes[j])) {
                    prefix = j;
                    break;
                }
            }

            if (prefix == -1) {
                continue;
            }

            /*
             * Class returnType = method.getReturnType(); boolean returnIsValid =
             * returnType == void.class ||
             * NakedObject.class.isAssignableFrom(returnType);
             * 
             * if(! returnIsValid) { LOG.warn("action method " + method + "
             * ignored as return type is not of type Naked" ); continue; }
             */

            Class[] params = method.getParameterTypes();
            /*
             * boolean paramsAreValid = true; for (int j = 0; j < params.length;
             * j++) { Class param = params[j]; if(!
             * Naked.class.isAssignableFrom(param)) { paramsAreValid = false; } }
             * 
             * if(! paramsAreValid) { LOG.warn("action method " + method + "
             * ignored as not all parameters are of type Naked" ); continue; }
             */
            validMethods.addElement(method);

            LOG.debug("identified action " + method);
            String methodName = method.getName();
            methods[i] = null;

            String name = methodName.substring(prefixes[prefix].length());
            Class[] longParams = new Class[params.length + 1];
            longParams[0] = ActionAbout.class;
            System.arraycopy(params, 0, longParams, 1, params.length);
            String aboutName = "about" + methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
            Method aboutMethod = findMethod(forClass, aboutName, null, longParams);
            if (aboutMethod == null) {
                aboutMethod = defaultAboutMethod;
            } else {
                LOG.debug("  with about method " + aboutMethod);
            }

            Action.Type action;
            action = new Action.Type[] { Action.USER, Action.EXPLORATION, Action.DEBUG }[prefix];
            ActionPeer local = createAction(method, name, aboutMethod, action);
            actions.addElement(local);
        }

        return convertToArray(actions);
    }

    public String[] actionSortOrder() {
        LOG.debug("looking for action sort order");
        return readSortOrder(cls, "action");
    }

    public String[] classActionSortOrder() {
        LOG.debug("looking for class action sort order");
        return readSortOrder(cls, "classAction");
    }

    public Hint classHint() {
        LOG.debug("looking for class about");
        try {
            SimpleClassAbout about = new SimpleClassAbout(null, null);
            String className = shortName();
            Method aboutMethod = getAboutMethod(className);
            aboutMethod.invoke(null, new Object[] { about });
            return about;
        } catch (NoSuchMethodException ignore) {} catch (IllegalAccessException ignore) {} catch (InvocationTargetException ignore) {}

        return null;
    }

    private String className() {
        return cls.getName();
    }

    public void clearDirty(NakedObject object) {
        if (clearDirtyMethod == null) {
            return;
        }

        try {
            clearDirtyMethod.invoke(object.getObject(), NO_PARAMETERS);
        } catch (IllegalArgumentException e) {
            throw new NakedObjectRuntimeException(e);
        } catch (IllegalAccessException e) {
            LOG.error("Illegal access of " + isDirtyMethod, e);
        } catch (InvocationTargetException e) {
            JavaMember.invocationException("Exception executing " + isDirtyMethod, e);
        }
    }

    private ActionPeer[] convertToArray(Vector actions) {
        ActionPeer results[] = new ActionPeer[actions.size()];
        Enumeration actionEnumeration = actions.elements();
        int i = 0;
        while (actionEnumeration.hasMoreElements()) {
            results[i++] = (ActionPeer) actionEnumeration.nextElement();

        }
        return (ActionPeer[]) results;
    }

    ActionPeer createAction(Method method, String name, Method aboutMethod, Action.Type action) {
        return new JavaAction(name, action, method, aboutMethod);
    }

    private void derivedFields(Vector fields) {
        Vector v = findPrefixedMethods(OBJECT, DERIVE_PREFIX, null, 0);

        // create vector of derived values from all derive methods
        Enumeration e = v.elements();

        while (e.hasMoreElements()) {
            Method method = (Method) e.nextElement();
            LOG.debug("identified derived value method " + method);
            String name = javaBaseName(method.getName());

            Method aboutMethod = findMethod(OBJECT, ABOUT_PREFIX + name, null, new Class[] { FieldAbout.class });
            if (aboutMethod == null) {
                aboutMethod = defaultAboutFieldMethod;
            }

            // create Field
            //  JavaValueField attribute = new JavaValueField(name,
            // method.getReturnType(), method, null, aboutMethod, null, true);
            //            fields.addElement(attribute);

            JavaOneToOneAssociation association = new JavaOneToOneAssociation(name, method.getReturnType(), method, null, null,
                    null, aboutMethod);
            fields.addElement(association);

        }
    }

    public FieldPeer[] fields() {
        if (cls.getName().startsWith("java.") || BusinessValueHolder.class.isAssignableFrom(cls)) {
            return new FieldPeer[0];
        }

        LOG.debug("looking for fields for " + cls);
        Vector elements = new Vector();
        defaultAboutFieldMethod = findMethod(OBJECT, ABOUT_FIELD_DEFAULT, null, new Class[] { FieldAbout.class });
        valueFields(elements, BusinessValueHolder.class);

        valueFields(elements, BusinessValue.class);
        valueFields(elements, String.class);
        valueFields(elements, Date.class);
        valueFields(elements, float.class);
        valueFields(elements, int.class);
        valueFields(elements, boolean.class);

        //        primitiveFields(elements);
        derivedFields(elements);
        oneToManyAssociationFields(elements);
        oneToManyAssociationFieldsInternalCollection(elements);
        // need to find one-many first, so they are not mistaken as one-one
        // associations
        oneToOneAssociationFields(elements);

        FieldPeer[] results = new FieldPeer[elements.size()];
        elements.copyInto(results);
        return results;
    }

    public String[] fieldSortOrder() {
        return readSortOrder(cls, "field");
    }

    protected void finalize() throws Throwable {
        super.finalize();
        LOG.info("finalizing reflector " + this);
    }

    /**
     * Returns a specific public methods that: have the specified prefix; have
     * the specified return type, or void, if canBeVoid is true; and has the
     * specified number of parameters. If the returnType is specified as null
     * then the return type is ignored.
     * 
     * @param forClass
     * @param name
     * @param returnType
     * @param paramTypes
     *                       the set of parameters the method should have, if null then is
     *                       ignored
     * @return Method
     */
    private Method findMethod(boolean forClass, String name, Class returnType, Class[] paramTypes) {
        method: for (int i = 0; i < methods.length; i++) {
            if (methods[i] == null) {
                continue;
            }

            Method method = methods[i];

            // check for public modifier
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }

            // check for static modifier
            if (Modifier.isStatic(method.getModifiers()) != forClass) {
                continue;
            }

            // check for name
            if (!method.getName().equals(name)) {
                continue;
            }

            // check for return type
            if (returnType != null && returnType != method.getReturnType()) {
                continue;
            }

            // check params (if required)
            if (paramTypes != null) {
                if (paramTypes.length != method.getParameterTypes().length) {
                    continue;
                }

                for (int c = 0; c < paramTypes.length; c++) {
                    if ((paramTypes[c] != null) && (paramTypes[c] != method.getParameterTypes()[c])) {
                        continue method;
                    }
                }
            }
            methods[i] = null;

            return method;
        }

        return null;
    }

    /**
     * Returns a Vector of public methods that: have the specified prefix; have
     * the specified return type, or void, if canBeVoid is true; and has the
     * specified number of parameters. If the returnType is specified as null
     * then the return type is ignored.
     */
    private Vector findPrefixedMethods(boolean forClass, String prefix, Class returnType, boolean canBeVoid, int paramCount) {
        Vector validMethods = new Vector();

        for (int i = 0; i < methods.length; i++) {
            if (methods[i] == null) {
                continue;
            }

            Method method = methods[i];

            if (Modifier.isStatic(method.getModifiers()) != forClass) {
                continue;
            }

            boolean goodPrefix = method.getName().startsWith(prefix);

            boolean goodCount = method.getParameterTypes().length == paramCount;
            Class type = method.getReturnType();
            boolean goodReturn = (returnType == null) || (canBeVoid && (type == void.class)) || returnType.isAssignableFrom(type);

            if (goodPrefix && goodCount && goodReturn) {
                validMethods.addElement(method);
                methods[i] = null;
            }
        }
        return validMethods;
    }

    private Vector findPrefixedMethods(boolean forClass, String prefix, Class returnType, int paramCount) {
        return findPrefixedMethods(forClass, prefix, returnType, false, paramCount);
    }

    public String fullName() {
        return cls.getName();
    }

    private Method getAboutMethod(String className) throws NoSuchMethodException {
        Method method = cls.getMethod(ABOUT_PREFIX + className, new Class[] { ClassAbout.class });
        if (method == null) {
            return getAboutMethod(getSuperclass());
        }
        return method;
    }

    public Object getExtension(Class cls) {
        return null;
    }

    public String[] getInterfaces() {
        Class[] interfaces = cls.getInterfaces();
        Class[] nakedInterfaces = new Class[interfaces.length];
        int validInterfaces = 0;
        for (int i = 0; i < interfaces.length; i++) {
            nakedInterfaces[validInterfaces++] = interfaces[i];
        }

        String[] interfaceNames = new String[validInterfaces];
        for (int i = 0; i < validInterfaces; i++) {
            interfaceNames[i] = nakedInterfaces[i].getName();
        }

        return interfaceNames;
    }

    public String getSuperclass() {
        Class superclass = cls.getSuperclass();

        if (superclass == null) {
            return null;
        }
        /*
         * String naked = Naked.class.getName(); boolean isInstanceOfNaked =
         * Naked.class.isAssignableFrom(superclass); return isInstanceOfNaked ?
         * superclass.getName() : naked;
         */
        return superclass.getName();
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(cls.getModifiers());
    }

    public boolean isCollection() {
        return Vector.class.isAssignableFrom(cls) || InternalCollection.class.isAssignableFrom(cls);
    }

    public boolean isDirty(NakedObject object) {
        if (isDirtyMethod == null) {
            return false;
        }

        try {
            Boolean isDirty = (Boolean) isDirtyMethod.invoke(object.getObject(), NO_PARAMETERS);
            return isDirty.booleanValue();
        } catch (IllegalArgumentException e) {
            throw new NakedObjectRuntimeException(e);
        } catch (IllegalAccessException e) {
            LOG.error("Illegal access of " + isDirtyMethod, e);
            return false;
        } catch (InvocationTargetException e) {
            JavaMember.invocationException("Exception executing " + isDirtyMethod, e);
            return false;
        }
    }

    public boolean isLookup() {
        return Lookup.class.isAssignableFrom(cls);
    }

    public boolean isObject() {
        return !isValue() && !isCollection();
    }

    public boolean isPartOf() {
        return Aggregated.class.isAssignableFrom(cls);
    }

    public boolean isValue() {
        return BusinessValueHolder.class.isAssignableFrom(cls) || BusinessValue.class.isAssignableFrom(cls);
    }

    public void markDirty(NakedObject object) {
        if (markDirtyMethod == null) {
            return;
        }

        try {
            markDirtyMethod.invoke(object.getObject(), NO_PARAMETERS);
        } catch (IllegalArgumentException e) {
            throw new NakedObjectRuntimeException(e);
        } catch (IllegalAccessException e) {
            LOG.error("Illegal access of " + isDirtyMethod, e);
        } catch (InvocationTargetException e) {
            JavaMember.invocationException("Exception executing " + isDirtyMethod, e);
        }
    }

    String[] names(Vector methods) {
        String[] names = new String[methods.size()];
        Enumeration e = methods.elements();
        int i = 0;

        while (e.hasMoreElements()) {
            Method method = (Method) e.nextElement();

            names[i++] = method.getName();
        }

        return names;
    }

    /**
     * Returns the details about the basic accessor/mutator methods. Based on
     * each suitable get... method a vector of OneToManyAssociation objects are
     * returned.
     *  
     */
    private void oneToManyAssociationFields(Vector associations) {
        Vector v = findPrefixedMethods(OBJECT, GET_PREFIX, Vector.class, 0);

        // create vector of multiRoles from all get methods
        Enumeration e = v.elements();

        while (e.hasMoreElements()) {
            Method getMethod = (Method) e.nextElement();
            LOG.debug("identified 1-many association method " + getMethod);
            String name = javaBaseName(getMethod.getName());

            Method aboutMethod = findMethod(OBJECT, ABOUT_PREFIX + name, null, new Class[] { FieldAbout.class, null,
                    boolean.class });
            Class aboutType = (aboutMethod == null) ? null : aboutMethod.getParameterTypes()[1];
            if (aboutMethod == null) {
                aboutMethod = defaultAboutFieldMethod;
            }

            // look for corresponding add and remove methods
            Method addMethod = findMethod(OBJECT, "addTo" + name, void.class, null);
            if (addMethod == null) {
                addMethod = findMethod(OBJECT, "add" + name, void.class, null);
            }
            if (addMethod == null) {
                addMethod = findMethod(OBJECT, "associate" + name, void.class, null);
            }

            Method removeMethod = findMethod(OBJECT, "removeFrom" + name, void.class, null);
            if (removeMethod == null) {
                removeMethod = findMethod(OBJECT, "remove" + name, void.class, null);
            }
            if (removeMethod == null) {
                removeMethod = findMethod(OBJECT, "dissociate" + name, void.class, null);
            }

            if (addMethod == null || removeMethod == null) {
                LOG.error("There must be both add and remove methods for " + name + " in " + className());
            }

            Class removeType = (removeMethod == null) ? null : removeMethod.getParameterTypes()[0];
            Class addType = (addMethod == null) ? null : addMethod.getParameterTypes()[0];

            /*
             * The type of element can be ascertained if there is an
             * add/associate method, otherwise it can not be determined until
             * runtime.
             */
            Class elementType = (aboutType == null) ? null : aboutType;
            elementType = (addType == null) ? elementType : addType;
            elementType = (removeType == null) ? elementType : removeType;

            if (elementType == null) {
                LOG.warn("Cannot determine a type for the collection " + name + "; not added as a field");
                return;
            }

            if (((aboutType != null) && (aboutType != elementType)) || ((addType != null) && (addType != elementType))
                    || ((removeType != null) && (removeType != elementType))) {
                LOG.error("The add/remove/associate/dissociate/about methods in " + className() + " must "
                        + "all deal with same type of object.  There are at least two different " + "types");
            }

            associations.addElement(new JavaOneToManyAssociation(name, elementType, getMethod, addMethod, removeMethod,
                    aboutMethod));
        }
    }

    private void oneToManyAssociationFieldsInternalCollection(Vector associations) {
        Vector v = findPrefixedMethods(OBJECT, GET_PREFIX, InternalCollection.class, 0);

        // create vector of multiRoles from all get methods
        Enumeration e = v.elements();

        while (e.hasMoreElements()) {
            Method getMethod = (Method) e.nextElement();
            LOG.debug("identified 1-many association method " + getMethod);
            String name = javaBaseName(getMethod.getName());

            Method aboutMethod = findMethod(OBJECT, ABOUT_PREFIX + name, null, new Class[] { FieldAbout.class, null,
                    boolean.class });
            Class aboutType = (aboutMethod == null) ? null : aboutMethod.getParameterTypes()[1];
            if (aboutMethod == null) {
                aboutMethod = defaultAboutFieldMethod;
            }

            // look for corresponding add and remove methods
            Method addMethod = findMethod(OBJECT, "addTo" + name, void.class, null);
            if (addMethod == null) {
                addMethod = findMethod(OBJECT, "add" + name, void.class, null);
            }
            if (addMethod == null) {
                addMethod = findMethod(OBJECT, "associate" + name, void.class, null);
            }

            Method removeMethod = findMethod(OBJECT, "removeFrom" + name, void.class, null);
            if (removeMethod == null) {
                removeMethod = findMethod(OBJECT, "remove" + name, void.class, null);
            }
            if (removeMethod == null) {
                removeMethod = findMethod(OBJECT, "dissociate" + name, void.class, null);
            }

            Class removeType = (removeMethod == null) ? null : removeMethod.getParameterTypes()[0];
            Class addType = (addMethod == null) ? null : addMethod.getParameterTypes()[0];

            /*
             * The type of element can be ascertained if there is an
             * add/associate method, otherwise it can not be determined until
             * runtime.
             */
            Class elementType = (aboutType == null) ? null : aboutType;
            elementType = (addType == null) ? elementType : addType;
            elementType = (removeType == null) ? elementType : removeType;

            if (((aboutType != null) && (aboutType != elementType)) || ((addType != null) && (addType != elementType))
                    || ((removeType != null) && (removeType != elementType))) {
                LOG.error("The add/remove/associate/dissociate/about methods in " + className() + " must "
                        + "all deal with same type of object.  There are at least two different " + "types");
            }

            associations
                    .addElement(new JavaInternalCollection(name, elementType, getMethod, addMethod, removeMethod, aboutMethod));
        }
    }

    /**
     * Returns a vector of Association fields for all the get methods that use
     * NakedObjects.
     * 
     * @throws ReflectionException
     */
    private void oneToOneAssociationFields(Vector associations) throws ReflectionException {
        Vector v = findPrefixedMethods(OBJECT, GET_PREFIX, Object.class, 0);

        // create vector of roles from all get methods
        Enumeration e = v.elements();

        while (e.hasMoreElements()) {
            Method getMethod = (Method) e.nextElement();
            LOG.debug("identified 1-1 association method " + getMethod);

            // ignore the getNakedClass method
            if (getMethod.getName().equals("getNakedClass")) {
                continue;
            }

            //
            String name = javaBaseName(getMethod.getName());
            Class[] params = new Class[] { getMethod.getReturnType() };

            Method aboutMethod = findMethod(OBJECT, ABOUT_PREFIX + name, null, new Class[] { FieldAbout.class,
                    getMethod.getReturnType() });
            if (aboutMethod == null) {
                aboutMethod = defaultAboutFieldMethod;
            }

            // look for associate
            Method addMethod = findMethod(OBJECT, "associate" + name, void.class, params);

            if (addMethod == null) {
                addMethod = findMethod(OBJECT, "add" + name, void.class, params);
            }

            // look for disassociate
            Method removeMethod = findMethod(OBJECT, "dissociate" + name, void.class, null);

            if (removeMethod == null) {
                removeMethod = findMethod(OBJECT, "remove" + name, void.class, null);
            }

            // look for set set method
            Method setMethod = findMethod(OBJECT, SET_PREFIX + name, void.class, params);

            // confirm a set method exists
            if (setMethod == null) {
                continue;
            }

            LOG.info("One-to-one association " + name + " ->" + addMethod);
            JavaOneToOneAssociation association = new JavaOneToOneAssociation(name, getMethod.getReturnType(), getMethod,
                    setMethod, addMethod, removeMethod, aboutMethod);
            associations.addElement(association);
        }
    }

    public Persistable persistable() {
        if (NonPersistable.class.isAssignableFrom(cls)) {
            return Persistable.TRANSIENT;
            /*
             * } else if(Immutable.class.isAssignableFrom(cls)) { return
             * Persistable.IMMUTABLE; } else
             * if(ProgramPersistable.class.isAssignableFrom(cls)) { return
             * Persistable.PROGRAM_PERSISTABLE;
             */} else {
            return Persistable.USER_PERSISTABLE;
        }
    }

    public String pluralName() {
        try {
            return (String) cls.getMethod("pluralName", new Class[0]).invoke(null, NO_PARAMETERS);
        } catch (NoSuchMethodException ignore) {} catch (IllegalAccessException ignore) {} catch (InvocationTargetException ignore) {}

        return null;
    }

    public String shortName() {
        String name = cls.getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    public String singularName() {
        try {
            Method method = cls.getMethod("singularName", new Class[0]);
            return (String) method.invoke(null, NO_PARAMETERS);
        } catch (NoSuchMethodException ignore) {} catch (IllegalAccessException ignore) {} catch (InvocationTargetException ignore) {}

        return null;
    }

    public ObjectTitle title() {
        Method titleMethod = findMethod(OBJECT, "title", Title.class, null);

        if (titleMethod == null) {
            titleMethod = findMethod(OBJECT, "title", String.class, null);
        }

        if (titleMethod == null) {
            return new ObjectTitle() {
                public String title(NakedObject object) {
                    return object.getObject().toString();
                }
            };
        } else {
            return new JavaObjectTitle(titleMethod);
        }
    }

    public String unresolvedTitle(NakedObject pojo) {
        return "no title";
    }

    private Vector valueFields(Vector fields, Class type) {
        Vector v = findPrefixedMethods(OBJECT, GET_PREFIX, type, 0);

        // create vector of attributes from all get methods
        Enumeration e = v.elements();

        while (e.hasMoreElements()) {
            Method getMethod = (Method) e.nextElement();
            Class returnType = getMethod.getReturnType();
            String name = javaBaseName(getMethod.getName());

            Method aboutMethod = findMethod(OBJECT, ABOUT_PREFIX + name, null, new Class[] { FieldAbout.class, returnType });
            if (aboutMethod == null) {
                aboutMethod = defaultAboutFieldMethod;
            }

            Method setMethod = findMethod(OBJECT, SET_PREFIX + name, null, new Class[] { returnType });

            // check for invalid methods
            Class[] params = new Class[] { returnType };

            if ((findMethod(OBJECT, SET_PREFIX + name, void.class, params) != null)
                    || (findMethod(OBJECT, "set_" + name, void.class, params) != null)) {
                LOG.error("The method set" + name + " is not needed for the NakedValue class " + className());
            }

            if (findMethod(OBJECT, "associate" + name, void.class, params) != null) {
                LOG.error("The method associate" + name + " is not needed for the NakedValue class " + className());
            }

            // create Field
            LOG.info("Value " + name + " ->" + getMethod);
            /*
             * ValueField attribute = createValueField(getMethod, setMethod,
             * name, aboutMethod, validMethod); fields.addElement(attribute);
             */

            JavaOneToOneAssociation association = new JavaOneToOneAssociation(name, getMethod.getReturnType(), getMethod,
                    setMethod, null, null, aboutMethod);
            fields.addElement(association);
        }

        return fields;
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */
