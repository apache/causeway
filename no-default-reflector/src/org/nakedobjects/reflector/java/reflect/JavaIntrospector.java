package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.application.Lookup;
import org.nakedobjects.application.NonPersistable;
import org.nakedobjects.application.Title;
import org.nakedobjects.application.collection.InternalCollection;
import org.nakedobjects.application.control.ActionAbout;
import org.nakedobjects.application.control.ClassAbout;
import org.nakedobjects.application.control.FieldAbout;
import org.nakedobjects.application.value.BusinessValue;
import org.nakedobjects.application.valueholder.BusinessValueHolder;
import org.nakedobjects.object.Action;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationException;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.ActionPeer;
import org.nakedobjects.object.reflect.ActionSet;
import org.nakedobjects.object.reflect.FieldPeer;
import org.nakedobjects.object.reflect.MemberIdentifier;
import org.nakedobjects.object.reflect.MemberIdentifierImpl;
import org.nakedobjects.object.reflect.NameConvertor;
import org.nakedobjects.object.reflect.ObjectTitle;
import org.nakedobjects.object.reflect.OneToManyPeer;
import org.nakedobjects.object.reflect.OneToOnePeer;
import org.nakedobjects.object.reflect.ReflectionException;
import org.nakedobjects.object.reflect.ReflectionPeerBuilder;
import org.nakedobjects.reflector.java.control.SimpleClassAbout;
import org.nakedobjects.utility.NakedObjectRuntimeException;
import org.nakedobjects.utility.UnknownTypeException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;


public class JavaIntrospector {
    private static final String HIDDEN_PREFIX = "Hidden";
    private static final String ABOUT_FIELD_DEFAULT = "aboutFieldDefault";
    private static final String ABOUT_PREFIX = "about";
    public static final boolean CLASS = true;
    private static final String DERIVE_PREFIX = "derive";
    private static final String GET_PREFIX = "get";
    private final static Logger LOG = Logger.getLogger(JavaIntrospector.class);
    private static final Object[] NO_PARAMETERS = new Object[0];
    public static final boolean OBJECT = false;
    private static final String SET_PREFIX = "set";

    /**
     * Returns the name of a Java entity without any prefix. A prefix is defined as the first set of lowercase
     * letters and the name is characters from, and including, the first upper case letter. If no upper case
     * letter is found then an empty string is returned.
     * 
     * <p>
     * Calling this method with the following Java names will produce these results:
     * 
     * <pre>
     *             getCarRegistration        -&gt; CarRegistration
     *             CityMayor -&gt; CityMayor
     *             isReady -&gt; Ready
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
     * Invokes, by reflection, the Order method prefixed by the specified type name. The returned string is
     * tokenized - broken on the commas - and returned in the array.
     */
    private String[] readSortOrder(Class aClass, String type) {
        Method method = findMethod(true, type + "Order", String.class, null);
        if (method == null) {
            return null;
        } else {
            try {
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
                    LOG.warn("method " + aClass.getName() + "." + type + "Order() must be decared as static");
                }
            } catch (IllegalAccessException ignore) {} catch (InvocationTargetException ignore) {}

            return null;
        }
    }

    /**
     * Invokes, by reflection, the Order method prefixed by the specified type name. The returned string is
     * tokenized - broken on the commas - and returned in the array.
     */
    private Set readSortOrder2(Class aClass, String type) {
        Method method = findMethod(true, type + "Order", String.class, null);
        if (method == null) {
            return null;
        }

        String s;
        try {
            if (Modifier.isStatic(method.getModifiers())) {
                s = (String) method.invoke(null, NO_PARAMETERS);
                if (s.trim().length() == 0) {
                    return null;
                }
            } else {
                LOG.warn("method " + aClass.getName() + "." + type + "Order() must be decared as static");
                return null;
            }
        } catch (IllegalAccessException ignore) {
            LOG.warn("method " + aClass.getName() + "." + type + "Order() must be decared as public");
            return null;
        } catch (InvocationTargetException e) {
            throw new ReflectionException(e);
        }

        return extractOrder(s);
    }

    private Set extractOrder(String s) {
        Set set = new Set();
        StringTokenizer st = new StringTokenizer(s, ",");
        while (st.hasMoreTokens()) {
            String element = st.nextToken().trim();
            
            boolean ends;
            if(ends = element.endsWith(")")) {
                element = element.substring(0, element.length() - 1).trim();
            }
            
            if(element.startsWith("(")) {
                int colon = element.indexOf(':');
                String groupName = element.substring(1, colon).trim();
                element = element.substring(colon + 1).trim();
                set = new Set(set, groupName, element);
            } else {
                set.add(element);
            }
            
            if(ends) {
                set = set.getParent();
            }
        }
        return set;
    }
    
    private static class Set {
        final Vector elements = new Vector();
        final Set parent;
        final String groupName;
        
        public Set() {
            parent = null;
            groupName = "";
        }

        public Set(Set set, String groupName, String element) {
            parent = set;
            parent. elements.addElement(this);
            this.groupName = groupName;
            add(element);
        }

        Set getParent() {
            return parent;
        }

        void add(String element) {
            elements.addElement(element);
        }
        
    }

    /**
     * Returns the short name of the fully qualified name (including the package name) . e.g. for
     * com.xyz.example.Customer returns Customer.
     */
    protected static String shortClassName(String fullyQualifiedClassName) {
        return fullyQualifiedClassName.substring(fullyQualifiedClassName.lastIndexOf('.') + 1);
    }

    private final ReflectionPeerBuilder builder;
    private Action[] classActions;
    private String className;
    private Method clearDirtyMethod;
    private Class cls;
    private Method defaultAboutFieldMethod;
    private NakedObjectField[] fields;
    private Method isDirtyMethod;
    private Method markDirtyMethod;
    private Method methods[];
    private Action[] objectActions;

    public JavaIntrospector(Class cls, ReflectionPeerBuilder builder) {
        LOG.debug("creating JavaIntrospector for " + cls);
        this.builder = builder;

        if (!Modifier.isPublic(cls.getModifiers())) {
            throw new NakedObjectSpecificationException("A NakedObject class must be marked as public.  Error in " + cls);
        }
        this.cls = cls;
        methods = cls.getMethods();

        for (int i = 0; i < methods.length; i++) {
            LOG.debug("  " + methods[i]);

        }
        LOG.debug("");

        isDirtyMethod = findMethod(false, "isDirty", boolean.class, new Class[0]);
        clearDirtyMethod = findMethod(false, "clearDirty", void.class, new Class[0]);
        markDirtyMethod = findMethod(false, "markDirty", void.class, new Class[0]);

        className = cls.getName();
    }

    private ActionPeer[] actionPeers(boolean forClass) {
        LOG.debug("  looking for action methods");
        Method defaultAboutMethod = findMethod(forClass, "aboutActionDefault", null, new Class[] { ActionAbout.class });
        LOG.debug(defaultAboutMethod == null ? "  no default about method for actions" : defaultAboutMethod.toString());

        Vector actions = new Vector();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i] == null) {
                continue;
            }
            Method method = methods[i];

            if (Modifier.isStatic(method.getModifiers()) != forClass) {
                continue;
            }

            String fullMethodName = method.getName();
            String[] prefixes = { "action", "explorationAction", "debugAction" };
            int actionPrefix = -1;
            for (int j = 0; j < prefixes.length; j++) {
                if (fullMethodName.startsWith(prefixes[j])) {
                    actionPrefix = j;
                    break;
                }
            }

            if (actionPrefix == -1) {
                continue;
            }

            Class[] params = method.getParameterTypes();

            LOG.debug("  identified action " + method);
            methods[i] = null;

            String actionName = fullMethodName.substring(prefixes[actionPrefix].length());

            Action.Target target = Action.DEFAULT;
            if (actionName.startsWith("Local")) {
                target = Action.LOCAL;
                actionName = actionName.substring(5);
            } else if (actionName.startsWith("Remote")) {
                target = Action.REMOTE;
                actionName = actionName.substring(6);
            }

            Class[] longParams = new Class[params.length + 1];
            longParams[0] = ActionAbout.class;
            System.arraycopy(params, 0, longParams, 1, params.length);
            String aboutName = "about" + fullMethodName.substring(0, 1).toUpperCase() + fullMethodName.substring(1);
            Method aboutMethod = findMethod(forClass, aboutName, null, longParams);
            if (aboutMethod == null) {
                aboutMethod = defaultAboutMethod;
            } else {
                LOG.debug("  with about method " + aboutMethod);
            }

            Action.Type action;
            action = new Action.Type[] { Action.USER, Action.EXPLORATION, Action.DEBUG }[actionPrefix];
            ActionPeer local = createAction(method, actionName, aboutMethod, action, target);
            actions.addElement(local);
        }

        return convertToArray(actions);
    }

    private Set actionSortOrder() {
        LOG.debug("  looking for action sort order");
        return readSortOrder2(cls, "action");
    }

    private Set classActionSortOrder() {
        LOG.debug("  looking for class action sort order");
        return readSortOrder2(cls, "classAction");
    }

    public Hint classHint() {
        LOG.debug("  looking for class about");
        try {
            SimpleClassAbout about = new SimpleClassAbout(null, null);
            String className = shortName();
            Method aboutMethod = getClassAboutMethod(className);
            aboutMethod.invoke(null, new Object[] { about });
            return about;
        } catch (NoSuchMethodException ignore) {} catch (IllegalAccessException ignore) {} catch (InvocationTargetException ignore) {}

        return null;
    }

    private String className() {
        return cls.getName();
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

    private ActionPeer createAction(Method method, String name, Method aboutMethod, Action.Type action, Action.Target target) {
        Class[] cls = method.getParameterTypes();
        NakedObjectSpecification[] parameters = new NakedObjectSpecification[cls.length];
        for (int i = 0; i < cls.length; i++) {
            parameters[i] = specification(cls[i]);
        }

        MemberIdentifier identifier = new MemberIdentifierImpl(className, name, parameters);
        return new JavaAction(identifier, action, parameters, target, method, aboutMethod);
    }

    private NakedObjectSpecification specification(Class returnType) {
        return NakedObjects.getSpecificationLoader().loadSpecification(returnType.getName());
    }

    private Action[] createActions(ReflectionPeerBuilder builder, ActionPeer[] delegates, Set order) {
        Action actions[] = new Action[delegates.length];
        for (int i = 0; i < delegates.length; i++) {
            actions[i] = builder.createAction(className, delegates[i]);
        }
        
        Action[] orderedActions = extractedOrderedActions(actions, order);
        
        Vector completeList = new Vector();
        for (int i = 0; i < orderedActions.length; i++) {
            completeList.addElement(orderedActions[i]);
        }
        for (int i = 0; i < actions.length; i++) {
            if(actions[i] != null) {
                completeList.addElement(actions[i]);
            }
        }
        
        Action[] completeActions = new Action[completeList.size()];
        completeList.copyInto(completeActions);
        return completeActions;
    }

    private NakedObjectField[] createFields(ReflectionPeerBuilder builder, FieldPeer fieldPeers[]) {
        NakedObjectField[] fields = new NakedObjectField[fieldPeers.length];

        for (int i = 0; i < fieldPeers.length; i++) {

            Object object = fieldPeers[i];

            if (object instanceof OneToOnePeer) {
                fields[i] = builder.createField(className, (OneToOnePeer) object);

            } else if (object instanceof OneToManyPeer) {
                fields[i] = builder.createField(className, (OneToManyPeer) object);

            } else {
                throw new NakedObjectRuntimeException();
            }
        }

        return fields;
    }

    private void derivedFields(Vector fields) {
        Vector v = findPrefixedMethods(OBJECT, DERIVE_PREFIX, null, 0);

        // create vector of derived values from all derive methods
        Enumeration e = v.elements();

        while (e.hasMoreElements()) {
            Method method = (Method) e.nextElement();
            LOG.debug("  identified derived value method " + method);
            String name = javaBaseName(method.getName());

            Method aboutMethod = findMethod(OBJECT, ABOUT_PREFIX + name, null, new Class[] { FieldAbout.class });
            if (aboutMethod == null) {
                aboutMethod = defaultAboutFieldMethod;
            }

            MemberIdentifier identifier = new MemberIdentifierImpl(className, name);
            JavaOneToOneAssociation association = new JavaOneToOneAssociation(false, identifier, method.getReturnType(), method,
                    null, null, null, aboutMethod, false, true);
            fields.addElement(association);

        }
    }

    private FieldPeer[] fields() {
        if (cls.getName().startsWith("java.") || BusinessValueHolder.class.isAssignableFrom(cls)) {
            return new FieldPeer[0];
        }

        removeMethod(false, "getClass", Class.class, null);

        LOG.debug("  looking for fields for " + cls);
        Vector elements = new Vector();
        defaultAboutFieldMethod = findMethod(OBJECT, ABOUT_FIELD_DEFAULT, null, new Class[] { FieldAbout.class });
        valueFields(elements, BusinessValueHolder.class);

        valueFields(elements, BusinessValue.class);
        valueFields(elements, String.class);
        valueFields(elements, Date.class);
        valueFields(elements, boolean.class);
        valueFields(elements, char.class);
        valueFields(elements, byte.class);
        valueFields(elements, short.class);
        valueFields(elements, int.class);
        valueFields(elements, long.class);
        valueFields(elements, float.class);
        valueFields(elements, double.class);

        derivedFields(elements);
        oneToManyAssociationFieldsVector(elements);
        oneToManyAssociationFieldsInternalCollection(elements);
        oneToManyAssociationFieldsArray(elements);
        // need to find one-many first, so they are not mistaken as one-one
        // associations
        oneToOneAssociationFields(elements);

        FieldPeer[] results = new FieldPeer[elements.size()];
        elements.copyInto(results);
        return results;
    }

    private String[] fieldSortOrder() {
        return readSortOrder(cls, "field");
    }

    protected void finalize() throws Throwable {
        super.finalize();
        LOG.debug("finalizing reflector " + this);
    }

    /**
     * Returns a specific public methods that: have the specified prefix; have the specified return type, or
     * void, if canBeVoid is true; and has the specified number of parameters. If the returnType is specified
     * as null then the return type is ignored.
     * 
     * @param forClass
     * @param name
     * @param returnType
     * @param paramTypes
     *            the set of parameters the method should have, if null then is ignored
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
     * Returns a Vector of public methods that: have the specified prefix; have the specified return type, or
     * void, if canBeVoid is true; and has the specified number of parameters. If the returnType is specified
     * as null then the return type is ignored.
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

    private Method getClassAboutMethod(String className) throws NoSuchMethodException {
        Method method = cls.getMethod(ABOUT_PREFIX + className, new Class[] { ClassAbout.class });
        if (method == null) {
            return getClassAboutMethod(getSuperclass());
        }
        return method;
    }

    public Action[] getClassActions() {
        return classActions;
    }

    public Method getClearDirtyMethod() {
        return clearDirtyMethod;
    }

    public NakedObjectField[] getFields() {
        return fields;
    }

    public String getFullName() {
        return cls.getName();
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

    public Method getIsDirtyMethod() {
        return isDirtyMethod;
    }

    public Method getMarkDirtyMethod() {
        return markDirtyMethod;
    }

    public Action[] getObjectActions() {
        return objectActions;
    }

    public String getSuperclass() {
        Class superclass = cls.getSuperclass();

        if (superclass == null) {
            return null;
        }
        return superclass.getName();
    }

    protected void introspect() {
        LOG.info("introspecting " + cls.getName());

        ActionPeer delegates[] = actionPeers(OBJECT);
        Set order = actionSortOrder();
        objectActions = createActions(builder, delegates, order);

        delegates = actionPeers(CLASS);
        order = classActionSortOrder();
        classActions = createActions(builder, delegates, order);

        FieldPeer fieldDelegates[] = fields();
        fieldDelegates = orderArray(fieldDelegates, fieldSortOrder());
        fields = createFields(builder, fieldDelegates);
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(cls.getModifiers());
    }

    public boolean isCollection() {
        return Vector.class.isAssignableFrom(cls) || InternalCollection.class.isAssignableFrom(cls);
    }

    public boolean isLookup() {
        return Lookup.class.isAssignableFrom(cls);
    }

    public boolean isObject() {
        return !isValue() && !isCollection();
    }

    public boolean isValue() {
        return BusinessValueHolder.class.isAssignableFrom(cls) || BusinessValue.class.isAssignableFrom(cls)
                || (cls.getName().startsWith("java.") && !cls.isAssignableFrom(Vector.class));
    }

    private void oneToManyAssociationFieldsArray(Vector associations) {
        Vector v = findPrefixedMethods(OBJECT, GET_PREFIX, Object[].class, 0);

        // create vector of multiRoles from all get methods
        Enumeration e = v.elements();

        while (e.hasMoreElements()) {
            Method getMethod = (Method) e.nextElement();
            LOG.debug("  identified 1-many association method " + getMethod);
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
             * The type of element can be ascertained if there is an add/associate method, otherwise it can
             * not be determined until runtime.
             */
            Class elementType = getMethod.getReturnType().getComponentType();

            if (((aboutType != null) && (aboutType != elementType)) || ((addType != null) && (addType != elementType))
                    || ((removeType != null) && (removeType != elementType))) {
                LOG.error("The add/remove/associate/dissociate/about methods in " + className() + " must "
                        + "all deal with same type of object.  There are at least two different " + "types");
            }

            boolean isHidden = false;
            if (name.startsWith(HIDDEN_PREFIX)) {
                isHidden = true;
                name = name.substring(HIDDEN_PREFIX.length());
            }

            MemberIdentifier identifier = new MemberIdentifierImpl(className, name);
            associations.addElement(new JavaOneToManyAssociation(identifier, elementType, getMethod, addMethod, removeMethod,
                    aboutMethod, isHidden));
        }
    }

    private void oneToManyAssociationFieldsInternalCollection(Vector associations) {
        Vector v = findPrefixedMethods(OBJECT, GET_PREFIX, InternalCollection.class, 0);

        // create vector of multiRoles from all get methods
        Enumeration e = v.elements();

        while (e.hasMoreElements()) {
            Method getMethod = (Method) e.nextElement();
            LOG.debug("  identified 1-many association method " + getMethod);
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
             * The type of element can be ascertained if there is an add/associate method, otherwise it can
             * not be determined until runtime.
             */
            Class elementType = (aboutType == null) ? null : aboutType;
            elementType = (addType == null) ? elementType : addType;
            elementType = (removeType == null) ? elementType : removeType;

            if (((aboutType != null) && (aboutType != elementType)) || ((addType != null) && (addType != elementType))
                    || ((removeType != null) && (removeType != elementType))) {
                LOG.error("the add/remove/associate/dissociate/about methods in " + className() + " must "
                        + "all deal with same type of object.  There are at least two different " + "types");
            }

            boolean isHidden = false;
            if (name.startsWith(HIDDEN_PREFIX)) {
                isHidden = true;
                name = name.substring(HIDDEN_PREFIX.length());
            }

            MemberIdentifier identifier = new MemberIdentifierImpl(className, name);
            associations.addElement(new JavaInternalCollection(identifier, elementType, getMethod, addMethod, removeMethod,
                    aboutMethod, isHidden));
        }
    }

    /**
     * Returns the details about the basic accessor/mutator methods. Based on each suitable get... method a
     * vector of OneToManyAssociation objects are returned.
     * 
     */
    // TODO merge this method and next
    private void oneToManyAssociationFieldsVector(Vector associations) {
        Vector v = findPrefixedMethods(OBJECT, GET_PREFIX, Vector.class, 0);

        // create vector of multiRoles from all get methods
        Enumeration e = v.elements();

        while (e.hasMoreElements()) {
            Method getMethod = (Method) e.nextElement();
            LOG.debug("  identified 1-many association method " + getMethod);
            String name = javaBaseName(getMethod.getName());

            Method aboutMethod = findMethod(OBJECT, ABOUT_PREFIX + name, null, new Class[] { FieldAbout.class, null,
                    boolean.class });
            Class aboutType = (aboutMethod == null) ? null : aboutMethod.getParameterTypes()[1];
            if (aboutMethod == null) {
                aboutMethod = defaultAboutFieldMethod;
            }

            Class[] params = new Class[] { null };
            // look for corresponding add and remove methods
            Method addMethod = findMethod(OBJECT, "addTo" + name, void.class, params);
            if (addMethod == null) {
                addMethod = findMethod(OBJECT, "add" + name, void.class, params);
            }
            if (addMethod == null) {
                addMethod = findMethod(OBJECT, "associate" + name, void.class, params);
            }

            Method removeMethod = findMethod(OBJECT, "removeFrom" + name, void.class, params);
            if (removeMethod == null) {
                removeMethod = findMethod(OBJECT, "remove" + name, void.class, params);
            }
            if (removeMethod == null) {
                removeMethod = findMethod(OBJECT, "dissociate" + name, void.class, params);
            }

            if (addMethod == null || removeMethod == null) {
                LOG.error("there must be both add and remove methods for " + name + " in " + className());
                return;
            }

            Class removeType = (removeMethod == null) ? null : removeMethod.getParameterTypes()[0];
            Class addType = (addMethod == null) ? null : addMethod.getParameterTypes()[0];

            /*
             * The type of element can be ascertained if there is an add/associate method, otherwise it can
             * not be determined until runtime.
             */
            Class elementType = (aboutType == null) ? null : aboutType;
            elementType = (addType == null) ? elementType : addType;
            elementType = (removeType == null) ? elementType : removeType;

            if (elementType == null) {
                LOG.warn("cannot determine a type for the collection " + name + "; not added as a field");
                return;
            }

            if (((aboutType != null) && (aboutType != elementType)) || ((addType != null) && (addType != elementType))
                    || ((removeType != null) && (removeType != elementType))) {
                LOG.error("the add/remove/associate/dissociate/about methods in " + className() + " must "
                        + "all deal with same type of object.  There are at least two different " + "types");
            }

            boolean isHidden = false;
            if (name.startsWith(HIDDEN_PREFIX)) {
                isHidden = true;
                name = name.substring(HIDDEN_PREFIX.length());
            }

            MemberIdentifier identifier = new MemberIdentifierImpl(className, name);
            associations.addElement(new JavaOneToManyAssociation(identifier, elementType, getMethod, addMethod, removeMethod,
                    aboutMethod, isHidden));
        }
    }

    /**
     * Returns a vector of Association fields for all the get methods that use NakedObjects.
     * 
     * @throws ReflectionException
     */
    private void oneToOneAssociationFields(Vector associations) throws ReflectionException {
        Vector v = findPrefixedMethods(OBJECT, GET_PREFIX, Object.class, 0);

        // create vector of roles from all get methods
        Enumeration e = v.elements();

        while (e.hasMoreElements()) {
            Method getMethod = (Method) e.nextElement();
            LOG.debug("  identified 1-1 association method " + getMethod);

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

            boolean isHidden = false;
            if (name.startsWith(HIDDEN_PREFIX)) {
                isHidden = true;
                name = name.substring(HIDDEN_PREFIX.length());
            }

            LOG.debug("one-to-one association " + name + " ->" + addMethod);
            MemberIdentifier identifier = new MemberIdentifierImpl(className, name);
            JavaOneToOneAssociation association = new JavaOneToOneAssociation(true, identifier, getMethod.getReturnType(),
                    getMethod, setMethod, addMethod, removeMethod, aboutMethod, isHidden, setMethod == null);
            associations.addElement(association);
        }
    }

    protected FieldPeer[] orderArray(FieldPeer[] original, String[] order) {
        if (order == null) {
            return original;
    
        } else {
            for (int i = 0; i < order.length; i++) {
                order[i] = NameConvertor.simpleName(order[i]);
            }
    
            FieldPeer[] ordered = new FieldPeer[original.length];
    
            // work through each order element and find, if there is one, a
            // matching member.
            int orderedIndex = 0;
            ordering: for (int orderIndex = 0; orderIndex < order.length; orderIndex++) {
                for (int memberIndex = 0; memberIndex < original.length; memberIndex++) {
                    FieldPeer member = original[memberIndex];
                    if (member == null) {
                        continue;
                    }
                    if (member.getIdentifier().getName().equalsIgnoreCase(order[orderIndex])) {
                        ordered[orderedIndex++] = original[memberIndex];
                        original[memberIndex] = null;
    
                        continue ordering;
                    }
                }
    
                if (!order[orderIndex].trim().equals("")) {
                    LOG.warn("invalid ordering element '" + order[orderIndex] + "' in " + className);
                }
            }
    
            FieldPeer[] results = new FieldPeer[original.length];
            int index = 0;
            for (int i = 0; i < ordered.length; i++) {
                FieldPeer member = ordered[i];
                if (member != null) {
                    results[index++] = member;
                }
            }
            for (int i = 0; i < original.length; i++) {
                FieldPeer member = original[i];
                if (member != null) {
                    results[index++] = member;
                }
            }
    
            return results;
        }
    }

    private Action getAction(Action[] actions, String name) {
        for (int i = 0; i < actions.length; i++) {
            Action action = actions[i];
            if(action != null && action.getId().equals(name)) {
                actions[i] = null;
                return action;
            }
        }
        
        throw new NakedObjectRuntimeException("No field " + name);
    }
    
    protected Action[] extractedOrderedActions(Action[] original, Set order) {
        if (order == null) {
            return original;
        }
        
        Action[] actions = new Action[order.elements.size()];
        
        Enumeration elements = order.elements.elements();
        int i = 0;
        while (elements.hasMoreElements()) {
            Object name =  elements.nextElement();
            if(name instanceof String) {
                actions[i++] = getAction(original, NameConvertor.simpleName(name.toString()));
            } else if (name instanceof Set) {
                actions[i++] = new ActionSet("", ((Set) name).groupName, extractedOrderedActions(original, (Set) name));
            } else {
                throw new UnknownTypeException(name);
            }
        }
                
        return actions;
    }

    public Persistable persistable() {
        if (NonPersistable.class.isAssignableFrom(cls)) {
            return Persistable.TRANSIENT;
            /*
             * } else if(Immutable.class.isAssignableFrom(cls)) { return Persistable.IMMUTABLE; } else
             * if(ProgramPersistable.class.isAssignableFrom(cls)) { return Persistable.PROGRAM_PERSISTABLE;
             */} else {
            return Persistable.USER_PERSISTABLE;
        }
    }

    public String pluralName() {
        try {
            return (String) cls.getMethod("pluralName", new Class[0]).invoke(null, NO_PARAMETERS);
        } catch (NoSuchMethodException ignore) {} catch (IllegalAccessException ignore) {} catch (InvocationTargetException ignore) {}
        return NameConvertor.pluralName(singularName());
    }

    private void removeMethod(boolean forClass, String name, Class returnType, Class[] paramTypes) {
        findMethod(forClass, name, returnType, paramTypes);
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

        return NameConvertor.naturalName(shortName());
    }

    public ObjectTitle title() {
        Method titleMethod = findMethod(OBJECT, "title", Title.class, null);

        if (titleMethod == null) {
            titleMethod = findMethod(OBJECT, "title", String.class, null);
        }

        if (titleMethod == null) {
            return new ObjectTitle() {
                public String title(NakedObject object) {
                    return null;
                }
            };
        } else {
            return new JavaObjectTitle(titleMethod);
        }
    }

    private Vector valueFields(Vector fields, Class type) {
        Vector v = findPrefixedMethods(OBJECT, GET_PREFIX, type, 0);

        // create vector of attributes from all get methods
        for (Enumeration e = v.elements(); e.hasMoreElements();) {
            Method getMethod = (Method) e.nextElement();
            Class returnType = getMethod.getReturnType();
            boolean valueHolder = BusinessValueHolder.class.isAssignableFrom(returnType);
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
                LOG.error("the method set" + name + " is not needed for the NakedValue class " + className());
            }

            if (findMethod(OBJECT, "associate" + name, void.class, params) != null) {
                LOG.error("the method associate" + name + " is not needed for the NakedValue class " + className());
            }

            boolean isHidden = false;
            if (name.startsWith(HIDDEN_PREFIX)) {
                isHidden = true;
                name = name.substring(HIDDEN_PREFIX.length());
            }

            // create Field
            LOG.debug("  identified value " + name + " -> " + getMethod);
            MemberIdentifier identifier = new MemberIdentifierImpl(className, name);
            JavaOneToOneAssociation association = new JavaOneToOneAssociation(false, identifier, getMethod.getReturnType(),
                    getMethod, setMethod, null, null, aboutMethod, isHidden, setMethod == null && !valueHolder);
            fields.addElement(association);
        }

        return fields;
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */