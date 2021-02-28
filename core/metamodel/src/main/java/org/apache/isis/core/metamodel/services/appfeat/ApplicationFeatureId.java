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
package org.apache.isis.core.metamodel.services.appfeat;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.services.appfeat.ApplicationMemberType;
import org.apache.isis.applib.util.Equality;
import org.apache.isis.applib.util.Hashing;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.TitleBuffer;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;

import lombok.NonNull;
import lombok.val;

/**
 * Value type representing a package, class or member.
 * <p>
 * This value is {@link Comparable}, the implementation of which considers 
 * {@link #getType() (feature) type}, {@link #getNamespace() logical package name}, 
 * {@link #getTypeSimpleName() class name} and {@link #getMemberName() member name}.
 */
@Value
public class ApplicationFeatureId 
implements
    Comparable<ApplicationFeatureId>, 
    Serializable {

    private static final long serialVersionUID = 1L;

    // -- CONSTANTS

    public static final ApplicationFeatureId PACKAGE_DEFAULT = 
            new ApplicationFeatureId(ApplicationFeatureType.PACKAGE, "default");

    // -- FACTORY METHODS

    public static ApplicationFeatureId fromIdentifier(final @NonNull Identifier identifier) {
        
        val logicalTypeName = identifier.getTypeIdentifier().getLogicalTypeName();
        
        if(identifier.getType().isClass()) {
            return newClass(logicalTypeName); 
        }
        if(identifier.getType().isPropertyOrCollection()) {
            return newMember(logicalTypeName, identifier.getMemberName());
        }
        // its an action
        return newMember(logicalTypeName, identifier.getMemberNameAndParameterClassNamesIdentityString());
    }
    
    public static ApplicationFeatureId newFeature(
            final ApplicationFeatureType featureType, 
            final String fullyQualifiedName) {
        
        switch (featureType) {
        case PACKAGE:
            return newPackage(fullyQualifiedName);
        case CLASS:
            return newClass(fullyQualifiedName);
        case MEMBER:
            return newMember(fullyQualifiedName);
        }
        throw new IllegalArgumentException("Unknown feature type " + featureType);
    }

    public static ApplicationFeatureId newFeature(
            final String packageFqn, 
            final String className,
            final String memberName) {
        if(className == null) {
            return newPackage(packageFqn);
        }
        final String classFqn = packageFqn + "." + className;
        if(memberName == null) {
            return newClass(classFqn);
        }
        return newMember(classFqn, memberName);
    }

    public static ApplicationFeatureId newPackage(final String packageFqn) {
        final ApplicationFeatureId featureId = new ApplicationFeatureId(ApplicationFeatureType.PACKAGE);
        featureId.setNamespace(packageFqn);
        return featureId;
    }

    public static ApplicationFeatureId newClass(final String classFqn) {
        final ApplicationFeatureId featureId = new ApplicationFeatureId(ApplicationFeatureType.CLASS);
        featureId.type.init(featureId, classFqn);
        return featureId;
    }

    public static ApplicationFeatureId newMember(final String classFqn, final String memberName) {
        final ApplicationFeatureId featureId = new ApplicationFeatureId(ApplicationFeatureType.MEMBER);
        ApplicationFeatureType.CLASS.init(featureId, classFqn);
        featureId.type = ApplicationFeatureType.MEMBER;
        featureId.setMemberName(memberName);
        return featureId;
    }

    public static ApplicationFeatureId newMember(final String fullyQualifiedName) {
        final ApplicationFeatureId featureId = new ApplicationFeatureId(ApplicationFeatureType.MEMBER);
        featureId.type.init(featureId, fullyQualifiedName);
        return featureId;
    }

    /**
     * Round-trip with {@link #asString()}
     */
    public static ApplicationFeatureId parse(final String asString) {
        return new ApplicationFeatureId(asString);
    }

    /**
     * Round-trip with {@link #asEncodedString()}
     */
    public static ApplicationFeatureId parseEncoded(final String encodedString) {
        return new ApplicationFeatureId(_Strings.base64UrlDecode(encodedString));
    }


    // //////////////////////////////////////

    // -- constructor

    private ApplicationFeatureId(final String asString) {
        final Iterator<String> iterator = _Strings.splitThenStream(asString, ":").iterator();
        final ApplicationFeatureType type = ApplicationFeatureType.valueOf(iterator.next());
        type.init(this, iterator.next());
    }

    /**
     * Must be called by {@link ApplicationFeatureType#init(ApplicationFeatureId, String)} immediately afterwards
     * to fully initialize.
     */
    ApplicationFeatureId(final ApplicationFeatureType type) {
        this.type = type;
    }

    public ApplicationFeatureId(final ApplicationFeatureType type, final String fullyQualifiedName) {
        type.init(this, fullyQualifiedName);
    }



    // //////////////////////////////////////

    // -- identification
    /**
     * having a title() method (rather than using @Title annotation) is necessary as a workaround to be able to use
     * wrapperFactory#unwrap(...) method, which is otherwise broken in Isis 1.6.0
     */
    public String title() {
        final TitleBuffer buf = new TitleBuffer();
        buf.append(getFullyQualifiedName());
        return buf.toString();
    }


    // //////////////////////////////////////

    // -- fullyQualifiedName (property)

    @Programmatic
    public String getFullyQualifiedName() {
        final StringBuilder buf = new StringBuilder();
        buf.append(getNamespace());
        if(getTypeSimpleName() != null) {
            buf.append(".").append(getTypeSimpleName());
        }
        if(getMemberName() != null) {
            buf.append("#").append(getMemberName());
        }
        return buf.toString();
    }



    // -- objectSpecId (property)

    @Programmatic
    public ObjectSpecId getObjectSpecId() {
        if (getTypeSimpleName() == null) {
            return null;
        }

        final StringBuilder buf = new StringBuilder();
        if(!_Strings.isNullOrEmpty(getNamespace())) {
            buf.append(getNamespace()).append(".");
        }
        buf.append(getTypeSimpleName());

        return ObjectSpecId.of(buf.toString());
    }



    // //////////////////////////////////////

    // -- type (property)
    ApplicationFeatureType type;

    public ApplicationFeatureType getType() {
        return type;
    }


    // //////////////////////////////////////

    // -- namespace (property)
    private String namespace;

    @Programmatic
    public String getNamespace() {
        return namespace;
    }

    void setNamespace(final String namespace) {
        this.namespace = namespace;
    }


    // //////////////////////////////////////

    // -- className (property, optional)

    private String typeSimpleName;

    @Programmatic
    public String getTypeSimpleName() {
        return typeSimpleName;
    }

    void setTypeSimpleName(final String className) {
        this.typeSimpleName = className;
    }


    // //////////////////////////////////////

    // -- memberName (property, optional)
    private String memberName;

    @Programmatic
    public String getMemberName() {
        return memberName;
    }

    void setMemberName(final String memberName) {
        this.memberName = memberName;
    }


    // //////////////////////////////////////

    // -- Package or Class: getParentPackageId

    /**
     * The {@link ApplicationFeatureId id} of the parent package of this
     * class or package.
     */
    @Programmatic
    public ApplicationFeatureId getParentPackageId() {
        ApplicationFeatureType.ensurePackageOrClass(this);

        if(type == ApplicationFeatureType.CLASS) {
            return ApplicationFeatureId.newPackage(getNamespace());
        } else {
            final String packageName = getNamespace(); // eg aaa.bbb.ccc

            if(!packageName.contains(".")) {
                return null; // parent is root
            }

            final int cutOffPos = packageName.lastIndexOf('.');
            final String parentPackageName = packageName.substring(0, cutOffPos);

            return newPackage(parentPackageName);
        }
    }



    // //////////////////////////////////////

    // -- Member: getParentClassId

    /**
     * The {@link ApplicationFeatureId id} of the member's class.
     */
    public ApplicationFeatureId getParentClassId() {
        ApplicationFeatureType.ensureMember(this);
        final String classFqn = this.getNamespace() + "." + getTypeSimpleName();
        return newClass(classFqn);
    }


    // //////////////////////////////////////

    // -- asString, asEncodedString

    @Programmatic
    public String asString() {
        return type.name() + ":" + getFullyQualifiedName();
    }

    @Programmatic
    public String asEncodedString() {
        return _Strings.base64UrlEncode(asString());
    }
    

    // //////////////////////////////////////

    // -- Functions

    public static class Functions {

        private Functions(){}

        public static final Function<ApplicationFeatureId, String> GET_CLASS_NAME = 
                ApplicationFeatureId::getTypeSimpleName;

        public static final Function<ApplicationFeatureId, String> GET_MEMBER_NAME = 
                ApplicationFeatureId::getMemberName;

    }


    // //////////////////////////////////////

    // -- Predicates

    public static class Predicates {
        private Predicates(){}

        public static Predicate<ApplicationFeatureId> isClassContaining(
                final ApplicationMemberType memberType, final ApplicationFeatureRepositoryDefault applicationFeatures) {
            return new Predicate<ApplicationFeatureId>() {
                @Override
                public boolean test(final ApplicationFeatureId input) {
                    if(input.getType() != ApplicationFeatureType.CLASS) {
                        return false;
                    }
                    final ApplicationFeature feature = applicationFeatures.findFeature(input);
                    if(feature == null) {
                        return false;
                    }
                    return memberType == null || !feature.membersOf(memberType).isEmpty();
                }
            };
        }

        public static Predicate<ApplicationFeatureId> isClassRecursivelyWithin(final ApplicationFeatureId packageId) {
            return (ApplicationFeatureId input) -> input.getParentIds().contains(packageId);
        }
    }


    // //////////////////////////////////////

    // -- Comparators
    public static final class Comparators {
        private Comparators(){}
        public static Comparator<ApplicationFeatureId> natural() {
            return new ApplicationFeatureIdComparator();
        }

        static class ApplicationFeatureIdComparator implements Comparator<ApplicationFeatureId>, Serializable {
            private static final long serialVersionUID = 1L;

            @Override
            public int compare(final ApplicationFeatureId o1, final ApplicationFeatureId o2) {
                return o1.compareTo(o2);
            }
        }
    }


    // //////////////////////////////////////

    // -- pathIds, parentIds

    @Programmatic
    public List<ApplicationFeatureId> getPathIds() {
        return pathIds(this);
    }

    @Programmatic
    public List<ApplicationFeatureId> getParentIds() {
        return pathIds(getParentId());
    }

    private ApplicationFeatureId getParentId() {
        return type == ApplicationFeatureType.MEMBER? getParentClassId(): getParentPackageId();
    }

    private static List<ApplicationFeatureId> pathIds(final ApplicationFeatureId id) {
        final List<ApplicationFeatureId> featureIds = _Lists.newArrayList();
        return Collections.unmodifiableList(appendParents(id, featureIds));
    }

    private static List<ApplicationFeatureId> appendParents(final ApplicationFeatureId featureId, final List<ApplicationFeatureId> parentIds) {
        if(featureId != null) {
            parentIds.add(featureId);
            appendParents(featureId.getParentId(), parentIds);
        }
        return parentIds;
    }


    // //////////////////////////////////////

    // -- equals, hashCode, compareTo, toString

    private static final Comparator<ApplicationFeatureId> byType =
            comparing(ApplicationFeatureId::getType, nullsFirst(naturalOrder()));
    private static final Comparator<ApplicationFeatureId> byPackageName =
            comparing(ApplicationFeatureId::getNamespace, nullsFirst(naturalOrder()));
    private static final Comparator<ApplicationFeatureId> byClassName =
            comparing(ApplicationFeatureId::getTypeSimpleName, nullsFirst(naturalOrder()));
    private static final Comparator<ApplicationFeatureId> byMemberName =
            comparing(ApplicationFeatureId::getMemberName, nullsFirst(naturalOrder()));

    private static final Comparator<ApplicationFeatureId> applicationFeatureIdOrdering =
            Comparator.nullsFirst(byType)
            .thenComparing(byPackageName)
            .thenComparing(byClassName)
            .thenComparing(byMemberName);

    private static final Equality<ApplicationFeatureId> equality =
            ObjectContracts.checkEquals(ApplicationFeatureId::getType)
            .thenCheckEquals(ApplicationFeatureId::getNamespace)
            .thenCheckEquals(ApplicationFeatureId::getTypeSimpleName)
            .thenCheckEquals(ApplicationFeatureId::getMemberName);

    private static final Hashing<ApplicationFeatureId> hashing =
            ObjectContracts.hashing(ApplicationFeatureId::getType)
            .thenHashing(ApplicationFeatureId::getNamespace)
            .thenHashing(ApplicationFeatureId::getTypeSimpleName)
            .thenHashing(ApplicationFeatureId::getMemberName);

    private static final ToString<ApplicationFeatureId> toString =
            ObjectContracts.toString("type", ApplicationFeatureId::getType)
            .thenToString("packageName", ApplicationFeatureId::getNamespace)
            .thenToStringOmitIfAbsent("className", ApplicationFeatureId::getTypeSimpleName)
            .thenToStringOmitIfAbsent("memberName", ApplicationFeatureId::getMemberName);


    @Override
    public int compareTo(final ApplicationFeatureId other) {
        return applicationFeatureIdOrdering.compare(this, other);
    }

    @Override
    public boolean equals(final Object o) {
        return equality.equals(this, o);
    }

    @Override
    public int hashCode() {
        return hashing.hashCode(this);
    }

    @Override
    public String toString() {
        return toString.toString(this);
    }

    // -- WITHERS
    
    /**
     * Returns a new instance that is a clone of this, except for the namespace which is taken from the argument. 
     * @param namespace
     */
    public ApplicationFeatureId withNamespace(final @NonNull String namespace) {
        return newFeature(namespace, this.getTypeSimpleName(), this.getMemberName()); 
    }


}
