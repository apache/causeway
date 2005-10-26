package org.nakedobjects.object.defaults;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.ActionPeer;
import org.nakedobjects.object.reflect.FieldPeer;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.NameConvertor;
import org.nakedobjects.object.reflect.Reflector;

import org.apache.log4j.Logger;


public class NakedObjectSpecificationImpl extends AbstractNakedObjectSpecification {
    private final static Logger LOG = Logger.getLogger(NakedObjectSpecificationImpl.class);

    private String asString;

    private Reflector reflector;

    public NakedObjectSpecificationImpl() {}

    public void clearDirty(NakedObject object) {
        reflector.clearDirty(object);
    }

    public void deleted(NakedObject object) {
        reflector.destroyed(object);
    }

    protected void finalize() throws Throwable {
        super.finalize();
        LOG.info("finalizing specification " + this);
    }

    public Hint getClassHint() {
        Hint hint = reflector.classHint();
        if (hint == null) {
            hint = super.getClassHint();
        }
        return hint;
    }

    protected String getClassName() {
        return reflector.fullName();
    }

    public Object getExtension(Class cls) {
        return reflector.getExtension(cls);
    }

    public Class[] getExtensions() {
        return reflector.getExtensions();
    }

    /**
     * Returns the name of the NakedClass. This is the fully qualified name of the Class object that this
     * object represents (i.e. it includes the package name).
     */
    public String getFullName() {
        return reflector.fullName();
    }

    /**
     * Returns the short name (with spacing) for this object in a pluralised form. The plural from is obtained
     * from the defining classes pluralName method, if it exists, or by adding 's', 'es', or 'ies dependending
     * of the name's ending.
     */
    public final String getPluralName() {
        String pluralName = reflector.pluralName();

        if (pluralName != null) {
            return pluralName;
        } else {
            return NameConvertor.pluralName(getSingularName());
        }
    }

    /**
     * Returns the class name without the package. Removes the text up to, and including the last period
     * (".").
     */
    public String getShortName() {
        return reflector.shortName();
    }

    /**
     * Returns the short name (with spacing) of this NakedClass object. This is the objects name with package
     * name removed.
     * 
     * <p>
     * Removes the text up to, and including the last period (".").
     * </p>
     */
    public String getSingularName() {
        String singularName = reflector.singularName();

        return singularName != null ? singularName : NameConvertor.naturalName(getShortName());
    }

    public void introspect() {
        if (!(reflector instanceof PrimitiveReflector)) {
            ActionPeer delegates[] = reflector.actionPeers(Reflector.OBJECT);
            String[] order = reflector.actionSortOrder();
            Action[] objectActions = createActions(delegates, order);

            delegates = reflector.actionPeers(Reflector.CLASS);
            order = reflector.classActionSortOrder();
            Action[] classActions = createActions(delegates, order);

            FieldPeer fieldDelegates[] = reflector.fields();
            NakedObjectField[] fieldVector = createFields(fieldDelegates);
            NakedObjectField[] fields = (NakedObjectField[]) orderArray(NakedObjectField.class, fieldVector, reflector
                    .fieldSortOrder());

            String superclass = reflector.getSuperclass();
            String[] interfaces = reflector.getInterfaces();

            init(superclass, interfaces, fields, objectActions, classActions);
        }

        init(reflector.title());
    }

    public boolean isAbstract() {
        return reflector.isAbstract();
    }

    public boolean isCollection() {
        return reflector.isCollection();
    }
    
    public boolean isDirty(NakedObject object) {
        return reflector.isDirty(object);
    }

    public boolean isLookup() {
        return reflector.isLookup();
    }

    public boolean isObject() {
        return reflector.isObject();
    }

    public boolean isValue() {
        return reflector.isValue();
    }

    /**
     * Performance tuning.
     */
    private String lazyToString() {
        StringBuffer s = new StringBuffer();

        s.append("NakedObjectSpecification");
        if (reflector != null) {
            s.append(" [name=");
            s.append(getFullName());
            /*
             * s.append(",fields="); s.append(fields.length); s.append(",object methods=");
             * s.append(objectActions.length); s.append(",class methods="); s.append(classActions.length);
             */
            s.append(",reflector=");
            s.append(reflector);
            s.append("]");
        } else {
            s.append("[no relector set up]");
        }

        s.append("  " + Long.toHexString(super.hashCode()).toUpperCase());

        return s.toString();
    }

    public void markDirty(NakedObject object) {
        reflector.markDirty(object);
    }

    public void nonReflect(String className) {
        reflector = new PrimitiveReflector(className);
        init(reflector.title());
    }

    public Persistable persistable() {
        return reflector.persistable();
    }

    public void reflect(String className, Reflector reflector) {
        LOG.debug("creating reflector for " + className + " using " + reflector.getClass());
        this.reflector = reflector;
        // this.className = className;
    }

    public String toString() {
        if (asString == null) {
            asString = lazyToString();
        }

        return asString;
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
