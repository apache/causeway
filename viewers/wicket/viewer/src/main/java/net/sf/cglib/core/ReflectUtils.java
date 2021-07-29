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
package net.sf.cglib.core;

import java.beans.*;
import java.lang.reflect.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.*;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Type;

import lombok.extern.log4j.Log4j2;

/**
 * forked from https://github.com/cglib/cglib
 * provided as monkey-patch
 * <p>
 * credits to https://github.com/rototor/cglib/commit/cc8632ed0930879b929b0d07e4ede5ac15103e9f
 * @deprecated as of 2021 Cglib seems inactive - while yet Wicket 8. and 9. rely on it
 */
@Deprecated
@Log4j2
public class ReflectUtils {
    private ReflectUtils() { }

    private static final Map primitives = new HashMap(8);
    private static final Map transforms = new HashMap(8);
    private static final ClassLoader defaultLoader = ReflectUtils.class.getClassLoader();
    private static Method DEFINE_CLASS, DEFINE_CLASS_UNSAFE, DEFINE_CLASS_LOOKUP;
    private static Class mh;
    private static Object lookupObject;
    private static final ProtectionDomain PROTECTION_DOMAIN;
    private static final Object UNSAFE;
    private static final Throwable THROWABLE;

    private static final List<Method> OBJECT_METHODS = new ArrayList<Method>();

    static {

        log.warn("initializing patched, yet unrealeased Cglib");

        ProtectionDomain protectionDomain;
        Method defineClass, defineClassUnsafe;
        Object unsafe;
        Throwable throwable = null;
        try {
            protectionDomain = getProtectionDomain(ReflectUtils.class);
            try {
                defineClass = (Method) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    @Override
                    public Object run() throws Exception {
                            Class loader = Class.forName("java.lang.ClassLoader"); // JVM crash w/o this
                            Method defineClass = loader.getDeclaredMethod("defineClass",
                                                                    new Class[]{ String.class,
                                                                                 byte[].class,
                                                                                 Integer.TYPE,
                                                                                 Integer.TYPE,
                                                                                 ProtectionDomain.class });
                            defineClass.setAccessible(true);
                            return defineClass;
                    }
                });
                defineClassUnsafe = null;
                unsafe = null;
            } catch (Throwable t) {
                // Fallback on Jigsaw where this method is not available.
                throwable = t;
                defineClass = null;
                try {
                    unsafe = AccessController.doPrivileged(new PrivilegedExceptionAction() {
                        @Override
                        public Object run() throws Exception {
                            Class u = Class.forName("sun.misc.Unsafe");
                            Field theUnsafe = u.getDeclaredField("theUnsafe");
                            theUnsafe.setAccessible(true);
                            return theUnsafe.get(null);
                        }
                    });
                    Class u = Class.forName("sun.misc.Unsafe");
                    defineClassUnsafe = u.getMethod("defineClass",
                                            new Class[]{ String.class,
                                                        byte[].class,
                                                        Integer.TYPE,
                                                        Integer.TYPE,
                                                        ClassLoader.class,
                                                        ProtectionDomain.class });
                } catch(Throwable e) {
                    defineClassUnsafe = null;
                    unsafe = null;
                    mh = Class.forName("java.lang.invoke.MethodHandles");
                    lookupObject = mh.getMethod("lookup").invoke(null);
                    DEFINE_CLASS_LOOKUP = lookupObject.getClass().getDeclaredMethod("defineClass", byte[].class);
                }
            }
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                @Override
                public Object run() throws Exception {
                    Method[] methods = Object.class.getDeclaredMethods();
                    for (Method method : methods) {
                        if ("finalize".equals(method.getName())
                                || (method.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) > 0) {
                            continue;
                        }
                        OBJECT_METHODS.add(method);
                    }
                    return null;
                }
            });
        } catch (Throwable t) {
            if (throwable == null) {
                throwable = t;
            }
            protectionDomain = null;
            defineClass = null;
            defineClassUnsafe = null;
            unsafe = null;
        }
        PROTECTION_DOMAIN = protectionDomain;
        DEFINE_CLASS = defineClass;
        DEFINE_CLASS_UNSAFE = defineClassUnsafe;
        UNSAFE = unsafe;
        THROWABLE = throwable;
    }

    private static final String[] CGLIB_PACKAGES = {
        "java.lang",
    };

    static {
        primitives.put("byte", Byte.TYPE);
        primitives.put("char", Character.TYPE);
        primitives.put("double", Double.TYPE);
        primitives.put("float", Float.TYPE);
        primitives.put("int", Integer.TYPE);
        primitives.put("long", Long.TYPE);
        primitives.put("short", Short.TYPE);
        primitives.put("boolean", Boolean.TYPE);

        transforms.put("byte", "B");
        transforms.put("char", "C");
        transforms.put("double", "D");
        transforms.put("float", "F");
        transforms.put("int", "I");
        transforms.put("long", "J");
        transforms.put("short", "S");
        transforms.put("boolean", "Z");
    }

    public static ProtectionDomain getProtectionDomain(final Class source) {
        if(source == null) {
            return null;
        }
        return (ProtectionDomain)AccessController.doPrivileged(new PrivilegedAction() {
            @Override
            public Object run() {
                return source.getProtectionDomain();
            }
        });
    }

    public static Type[] getExceptionTypes(final Member member) {
        if (member instanceof Method) {
            return TypeUtils.getTypes(((Method)member).getExceptionTypes());
        } else if (member instanceof Constructor) {
            return TypeUtils.getTypes(((Constructor)member).getExceptionTypes());
        } else {
            throw new IllegalArgumentException("Cannot get exception types of a field");
        }
    }

    public static Signature getSignature(final Member member) {
        if (member instanceof Method) {
            return new Signature(member.getName(), Type.getMethodDescriptor((Method)member));
        } else if (member instanceof Constructor) {
            Type[] types = TypeUtils.getTypes(((Constructor)member).getParameterTypes());
            return new Signature(Constants.CONSTRUCTOR_NAME,
                                 Type.getMethodDescriptor(Type.VOID_TYPE, types));

        } else {
            throw new IllegalArgumentException("Cannot get signature of a field");
        }
    }

    public static Constructor findConstructor(final String desc) {
        return findConstructor(desc, defaultLoader);
    }

    public static Constructor findConstructor(final String desc, final ClassLoader loader) {
        try {
            int lparen = desc.indexOf('(');
            String className = desc.substring(0, lparen).trim();
            return getClass(className, loader).getConstructor(parseTypes(desc, loader));
        } catch (ClassNotFoundException e) {
            throw new CodeGenerationException(e);
        } catch (NoSuchMethodException e) {
            throw new CodeGenerationException(e);
        }
    }

    public static Method findMethod(final String desc) {
        return findMethod(desc, defaultLoader);
    }

    public static Method findMethod(final String desc, final ClassLoader loader) {
        try {
            int lparen = desc.indexOf('(');
            int dot = desc.lastIndexOf('.', lparen);
            String className = desc.substring(0, dot).trim();
            String methodName = desc.substring(dot + 1, lparen).trim();
            return getClass(className, loader).getDeclaredMethod(methodName, parseTypes(desc, loader));
        } catch (ClassNotFoundException e) {
            throw new CodeGenerationException(e);
        } catch (NoSuchMethodException e) {
            throw new CodeGenerationException(e);
        }
    }

    private static Class[] parseTypes(final String desc, final ClassLoader loader) throws ClassNotFoundException {
        int lparen = desc.indexOf('(');
        int rparen = desc.indexOf(')', lparen);
        List params = new ArrayList();
        int start = lparen + 1;
        for (;;) {
            int comma = desc.indexOf(',', start);
            if (comma < 0) {
                break;
            }
            params.add(desc.substring(start, comma).trim());
            start = comma + 1;
        }
        if (start < rparen) {
            params.add(desc.substring(start, rparen).trim());
        }
        Class[] types = new Class[params.size()];
        for (int i = 0; i < types.length; i++) {
            types[i] = getClass((String)params.get(i), loader);
        }
        return types;
    }

    private static Class getClass(final String className, final ClassLoader loader) throws ClassNotFoundException {
        return getClass(className, loader, CGLIB_PACKAGES);
    }

    private static Class getClass(String className, final ClassLoader loader, final String[] packages) throws ClassNotFoundException {
        String save = className;
        int dimensions = 0;
        int index = 0;
        while ((index = className.indexOf("[]", index) + 1) > 0) {
            dimensions++;
        }
        StringBuffer brackets = new StringBuffer(className.length() - dimensions);
        for (int i = 0; i < dimensions; i++) {
            brackets.append('[');
        }
        className = className.substring(0, className.length() - 2 * dimensions);

        String prefix = (dimensions > 0) ? brackets + "L" : "";
        String suffix = (dimensions > 0) ? ";" : "";
        try {
            return Class.forName(prefix + className + suffix, false, loader);
        } catch (ClassNotFoundException ignore) { }
        for (int i = 0; i < packages.length; i++) {
            try {
                return Class.forName(prefix + packages[i] + '.' + className + suffix, false, loader);
            } catch (ClassNotFoundException ignore) { }
        }
        if (dimensions == 0) {
            Class c = (Class)primitives.get(className);
            if (c != null) {
                return c;
            }
        } else {
            String transform = (String)transforms.get(className);
            if (transform != null) {
                try {
                    return Class.forName(brackets + transform, false, loader);
                } catch (ClassNotFoundException ignore) { }
            }
        }
        throw new ClassNotFoundException(save);
    }


    public static Object newInstance(final Class type) {
        return newInstance(type, Constants.EMPTY_CLASS_ARRAY, null);
    }

    public static Object newInstance(final Class type, final Class[] parameterTypes, final Object[] args) {
        return newInstance(getConstructor(type, parameterTypes), args);
    }

    public static Object newInstance(final Constructor cstruct, final Object[] args) {

        boolean flag = cstruct.isAccessible();
        try {
            if (!flag) {
                cstruct.setAccessible(true);
            }
            Object result = cstruct.newInstance(args);
            return result;
        } catch (InstantiationException e) {
            throw new CodeGenerationException(e);
        } catch (IllegalAccessException e) {
            throw new CodeGenerationException(e);
        } catch (InvocationTargetException e) {
            throw new CodeGenerationException(e.getTargetException());
        } finally {
            if (!flag) {
                cstruct.setAccessible(flag);
            }
        }

    }

    public static Constructor getConstructor(final Class type, final Class[] parameterTypes) {
        try {
            Constructor constructor = type.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new CodeGenerationException(e);
        }
    }

    public static String[] getNames(final Class[] classes)
    {
        if (classes == null)
            return null;
        String[] names = new String[classes.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = classes[i].getName();
        }
        return names;
    }

    public static Class[] getClasses(final Object[] objects) {
        Class[] classes = new Class[objects.length];
        for (int i = 0; i < objects.length; i++) {
            classes[i] = objects[i].getClass();
        }
        return classes;
    }

    public static Method findNewInstance(final Class iface) {
        Method m = findInterfaceMethod(iface);
        if (!m.getName().equals("newInstance")) {
            throw new IllegalArgumentException(iface + " missing newInstance method");
        }
        return m;
    }

    public static Method[] getPropertyMethods(final PropertyDescriptor[] properties, final boolean read, final boolean write) {
        Set methods = new HashSet();
        for (int i = 0; i < properties.length; i++) {
            PropertyDescriptor pd = properties[i];
            if (read) {
                methods.add(pd.getReadMethod());
            }
            if (write) {
                methods.add(pd.getWriteMethod());
            }
        }
        methods.remove(null);
        return (Method[])methods.toArray(new Method[methods.size()]);
    }

    public static PropertyDescriptor[] getBeanProperties(final Class type) {
        return getPropertiesHelper(type, true, true);
    }

    public static PropertyDescriptor[] getBeanGetters(final Class type) {
        return getPropertiesHelper(type, true, false);
    }

    public static PropertyDescriptor[] getBeanSetters(final Class type) {
        return getPropertiesHelper(type, false, true);
    }

    private static PropertyDescriptor[] getPropertiesHelper(final Class type, final boolean read, final boolean write) {
        try {
            BeanInfo info = Introspector.getBeanInfo(type, Object.class);
            PropertyDescriptor[] all = info.getPropertyDescriptors();
            if (read && write) {
                return all;
            }
            List properties = new ArrayList(all.length);
            for (int i = 0; i < all.length; i++) {
                PropertyDescriptor pd = all[i];
                if ((read && pd.getReadMethod() != null) ||
                    (write && pd.getWriteMethod() != null)) {
                    properties.add(pd);
                }
            }
            return (PropertyDescriptor[])properties.toArray(new PropertyDescriptor[properties.size()]);
        } catch (IntrospectionException e) {
            throw new CodeGenerationException(e);
        }
    }



    public static Method findDeclaredMethod(final Class type,
                                            final String methodName, final Class[] parameterTypes)
    throws NoSuchMethodException {

        Class cl = type;
        while (cl != null) {
            try {
                return cl.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                cl = cl.getSuperclass();
            }
        }
        throw new NoSuchMethodException(methodName);

    }

    public static List addAllMethods(final Class type, final List list) {


        if (type == Object.class) {
            list.addAll(OBJECT_METHODS);
        } else
            list.addAll(java.util.Arrays.asList(type.getDeclaredMethods()));

        Class superclass = type.getSuperclass();
        if (superclass != null) {
            addAllMethods(superclass, list);
        }
        Class[] interfaces = type.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            addAllMethods(interfaces[i], list);
        }

        return list;
    }

    public static List addAllInterfaces(final Class type, final List list) {
        Class superclass = type.getSuperclass();
        if (superclass != null) {
            list.addAll(Arrays.asList(type.getInterfaces()));
            addAllInterfaces(superclass, list);
        }
        return list;
    }


    public static Method findInterfaceMethod(final Class iface) {
        if (!iface.isInterface()) {
            throw new IllegalArgumentException(iface + " is not an interface");
        }
        Method[] methods = iface.getDeclaredMethods();
        if (methods.length != 1) {
            throw new IllegalArgumentException("expecting exactly 1 method in " + iface);
        }
        return methods[0];
    }

    public static Class defineClass(final String className, final byte[] b, final ClassLoader loader) throws Exception {
        return defineClass(className, b, loader, PROTECTION_DOMAIN);
    }

    public static Class defineClass(final String className, final byte[] b, final ClassLoader loader, final ProtectionDomain protectionDomain) throws Exception {
        Class c;
        if (DEFINE_CLASS != null) {
            Object[] args = new Object[]{className, b, new Integer(0), new Integer(b.length), protectionDomain };
            c = (Class)DEFINE_CLASS.invoke(loader, args);
        } else if (DEFINE_CLASS_UNSAFE != null) {
            Object[] args = new Object[]{className, b, new Integer(0), new Integer(b.length), loader, protectionDomain };
            c = (Class)DEFINE_CLASS_UNSAFE.invoke(UNSAFE, args);
        } else if (DEFINE_CLASS_LOOKUP != null) {
            Object[] args = new Object[]{b};
            try {
                org.objectweb.asm.ClassReader reader = new org.objectweb.asm.ClassReader(b);
                String clsName = reader.getClassName().replace("/",".");
                Class packageClass = loader.loadClass(clsName.substring(0,clsName.indexOf("$$")).replace("Wicket_Proxy_",""));
            Object privateLookup = mh.getMethod("privateLookupIn", Class.class, lookupObject.getClass()).invoke(null, new Object[]{packageClass,lookupObject});
            c = (Class)DEFINE_CLASS_LOOKUP.invoke(privateLookup, args);
            }catch(Throwable t) {
                t.printStackTrace();
                throw new CodeGenerationException(t);
            }
        } else {
            throw new CodeGenerationException(THROWABLE);
        }
        // Force static initializers to run.
        Class.forName(className, true, loader);
        return c;
    }

    public static int findPackageProtected(final Class[] classes) {
        for (int i = 0; i < classes.length; i++) {
            if (!Modifier.isPublic(classes[i].getModifiers())) {
                return i;
            }
        }
        return 0;
    }

    public static MethodInfo getMethodInfo(final Member member, final int modifiers) {
        final Signature sig = getSignature(member);
        return new MethodInfo() {
            private ClassInfo ci;
            @Override
            public ClassInfo getClassInfo() {
                if (ci == null)
                    ci = ReflectUtils.getClassInfo(member.getDeclaringClass());
                return ci;
            }
            @Override
            public int getModifiers() {
                return modifiers;
            }
            @Override
            public Signature getSignature() {
                return sig;
            }
            @Override
            public Type[] getExceptionTypes() {
                return ReflectUtils.getExceptionTypes(member);
            }
            public Attribute getAttribute() {
                return null;
            }
        };
    }

    public static MethodInfo getMethodInfo(final Member member) {
        return getMethodInfo(member, member.getModifiers());
    }

    public static ClassInfo getClassInfo(final Class clazz) {
        final Type type = Type.getType(clazz);
        final Type sc = (clazz.getSuperclass() == null) ? null : Type.getType(clazz.getSuperclass());
        return new ClassInfo() {
            @Override
            public Type getType() {
                return type;
            }
            @Override
            public Type getSuperType() {
                return sc;
            }
            @Override
            public Type[] getInterfaces() {
                return TypeUtils.getTypes(clazz.getInterfaces());
            }
            @Override
            public int getModifiers() {
                return clazz.getModifiers();
            }
        };
    }

    // used by MethodInterceptorGenerated generated code
    public static Method[] findMethods(final String[] namesAndDescriptors, final Method[] methods)
    {
        Map map = new HashMap();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            map.put(method.getName() + Type.getMethodDescriptor(method), method);
        }
        Method[] result = new Method[namesAndDescriptors.length / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = (Method)map.get(namesAndDescriptors[i * 2] + namesAndDescriptors[i * 2 + 1]);
            if (result[i] == null) {
                // TODO: error?
            }
        }
        return result;
    }
}
