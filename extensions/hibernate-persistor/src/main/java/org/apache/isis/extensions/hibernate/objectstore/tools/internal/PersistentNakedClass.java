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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;


/**
 * Details of a persistent class within [[NAME]] to be updated by Hibernate. Holds details of the
 * hierarchy, so it can be mapped to class/subclass within Hibernate.
 */
public class PersistentSpecification {
    private PersistentSpecification parent = null;
    private final List<PersistentSpecification> subClasses = new ArrayList<PersistentSpecification>();
    private final ObjectSpecification spec;
    private final String name;
    private final boolean root;
    private int referenceCount = 0;
    private ObjectAssociation[] uniqueFields = null;
    private String tableName;
    private boolean duplicateUnqualifiedClassName = false;
    private boolean requireVersion = false;
    private final HashMap<String, Association> associations = new HashMap<String, Association>();

    public PersistentSpecification() {
        this.name = "root";
        this.spec = null;
        root = true;
    }

    public PersistentSpecification(final ObjectSpecification spec, final PersistentSpecification parent) {
        this.spec = spec;
        this.name = spec.getFullName();
        root = false;
        if (parent != null) {
            this.parent = parent;
            parent.subClasses.add(this);
        }
    }

    public void addReference() {
        referenceCount++;
    }

    protected void debugString(final StringBuffer sb, final String prefix) {
        sb.append("\n" + prefix + name);
        if (referenceCount > 0) {
            sb.append(" (ref=").append(referenceCount).append(")");
        }
        for (final Iterator<PersistentSpecification> iter = subClasses.iterator(); iter.hasNext();) {
            iter.next().debugString(sb, "  " + prefix);
        }
    }

    private void ensureUniqueFieldsResolved() {
        if (uniqueFields != null) {
            return;
        }
        if (parent.isRoot()) {
            uniqueFields = spec.getAssociations();
            return;
        }
        final ObjectAssociation[] parentFields = parent.getSpecification().getAssociations();
        final HashMap<String, String> parentIds = new HashMap<String, String>();
        for (int i = 0; i < parentFields.length; i++) {
            if (!parentFields[i].isNotPersisted()) {
                parentIds.put(parentFields[i].getId(), "");
            }
        }

        final List<ObjectAssociation> uniqueList = new ArrayList<ObjectAssociation>();
        final ObjectAssociation[] fields = spec.getAssociations();
        for (int i = 0; i < fields.length; i++) {
            if (!parentIds.containsKey(fields[i].getId())) {
                uniqueList.add(fields[i]);
            }
        }
        uniqueFields = uniqueList.toArray(new ObjectAssociation[0]);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PersistentSpecification other = (PersistentSpecification) obj;
        if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    public String getName() {
        return name;
    }

    public PersistentSpecification getParent() {
        return parent;
    }

    public int getReferenceCount() {
        return referenceCount;
    }

    public ObjectSpecification getSpecification() {
        return spec;
    }

    public Iterator<PersistentSpecification> getSubClasses() {
        return subClasses.iterator();
    }

    public PersistentSpecification[] getSubClassesArray() {
        return subClasses.toArray(new PersistentSpecification[0]);
    }

    public String getTableName() {
        return tableName;
    }

    /**
     * Return the one, and only one association from this persistent class to the associated class.
     */
    public ObjectAssociation getUniqueAssociation(final String associatedClassName) {
        ensureUniqueFieldsResolved();
        ObjectAssociation association = null;
        for (int i = 0; i < uniqueFields.length; i++) {
            if (uniqueFields[i].isOneToOneAssociation() || uniqueFields[i].isOneToManyAssociation()) {
                if (associatedClassName.equals(uniqueFields[i].getSpecification().getFullName())) {
                    if (association != null) {
                        return null;
                    }
                    association = uniqueFields[i];
                }
            }
        }
        return association;
    }

    /**
     * Fields unique to this class, i.e. not further up the hierarchy. Note: when this method is called the
     * hierarchy may have been stripped down to remove abstact classes, so won't necessarily be the classes
     * declared fields.
     */
    public ObjectAssociation[] getUniqueFields() {
        ensureUniqueFieldsResolved();
        return uniqueFields;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public boolean hasSubClasses() {
        return subClasses.size() > 0;
    }

    public boolean isAbstract() {
        // return supportsFeature(specification, ObjectSpecification.ABSTRACT);
        return spec.isAbstract();
    }

    public boolean isInterface() {
        try {
            return Class.forName(name).isInterface();
        } catch (final ClassNotFoundException e) {
            throw new IsisException(e);
        }
    }

    public boolean isReferenced() {
        return referenceCount > 0;
    }

    public boolean isRoot() {
        return root;
    }

    /**
     * Return true if there is one, and only one association from this persistent class to the associated
     * class.
     */
    public boolean isUniqueAssociation(final String associatedClassName) {
        return getUniqueAssociation(associatedClassName) != null;
    }

    /**
     * This class is to be removed, so change the hierarchy so the superclass has all the subclasses
     */
    public void removeFromHierarchy() {
        if (hasSubClasses()) {
            for (final Iterator<PersistentSpecification> iter = subClasses.iterator(); iter.hasNext();) {
                iter.next().parent = parent;
            }
            parent.subClasses.addAll(subClasses);
        }
        if (parent != null) {
            parent.subClasses.remove(this);
            parent = null;
        }
    }

    public void setParent(final PersistentSpecification newParent) {
        if (parent != null) {
            parent.subClasses.remove(this);
        }
        this.parent = newParent;
        newParent.subClasses.add(this);
    }

    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        return "PersistentSpecification[name=" + name + (root ? " (root)" : "")
                + (referenceCount > 0 ? "(ref=" + referenceCount + ")" : "") + "]";
    }

    public boolean isDuplicateUnqualifiedClassName() {
        return duplicateUnqualifiedClassName;
    }

    public void setDuplicateUnqualifiedClassName(final boolean duplicateUnqualifiedClassName) {
        this.duplicateUnqualifiedClassName = duplicateUnqualifiedClassName;
    }

    public boolean isRequireVersion() {
        return requireVersion;
    }

    public void setRequireVersion(final boolean requireVersion) {
        this.requireVersion = requireVersion;
    }

    public void addAssociation(final String name, final Association association) {
        associations.put(name, association);
    }

    public Association getAssociation(final String name) {
        return associations.get(name);
    }

    public boolean hasAssociation(final String name) {
        return associations.containsKey(name);
    }
}
