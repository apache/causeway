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
package org.apache.isis.applib.services.appfeat;

import java.io.Serializable;
import java.util.Comparator;
import java.util.function.Consumer;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import javax.annotation.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.util.Equality;
import org.apache.isis.applib.util.Hashing;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.TitleBuffer;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.Getter;
import lombok.NonNull;
import lombok.Synchronized;
import lombok.val;

/**
 * Value type representing a namespace, type or member.
 * <p>
 * This value is {@link Comparable}, the implementation of which considers 
 * {@link #getSort() (feature) sort}, {@link #getNamespace() namespace}, 
 * {@link #getTypeSimpleName() type simple name} and {@link #getMemberName() member name}.
 * 
 * @since 1.x revised for 2.0 {@index}
 */
@Value
public class ApplicationFeatureId 
implements
    Comparable<ApplicationFeatureId>, 
    Serializable {

    private static final long serialVersionUID = 1L;

    // -- CONSTANTS

    public static final ApplicationFeatureId NAMESPACE_DEFAULT = newNamespace("default");

    // -- FACTORY METHODS

    public static ApplicationFeatureId fromIdentifier(final @NonNull Identifier identifier) {
        
        val logicalTypeName = identifier.getLogicalTypeName();
        
        if(identifier.getType().isClass()) {
            return newType(logicalTypeName); 
        }
        if(identifier.getType().isPropertyOrCollection()) {
            return newMember(logicalTypeName, identifier.getMemberName());
        }
        // its an action
        return newMember(logicalTypeName, identifier.getMemberNameAndParameterClassNamesIdentityString());
    }
    
    public static ApplicationFeatureId newFeature(
            final @NonNull ApplicationFeatureSort featureSort, 
            final @NonNull String qualifiedName) {
        
        switch (featureSort) {
        case NAMESPACE:
            return newNamespace(qualifiedName);
        case TYPE:
            return newType(qualifiedName);
        case MEMBER:
            return newMember(qualifiedName);
        }
        throw _Exceptions.illegalArgument("Unknown feature sort '%s'", featureSort);
    }

    public static ApplicationFeatureId newFeature(
            final @NonNull  String namespace, 
            final @Nullable String logicalTypeSimpleName,
            final @Nullable String memberName) {
        
        if(logicalTypeSimpleName == null) {
            return newNamespace(namespace);
        }
        val logicalTypeName = namespace + "." + logicalTypeSimpleName;
        if(memberName == null) {
            return newType(logicalTypeName);
        }
        return newMember(logicalTypeName, memberName);
    }

    public static ApplicationFeatureId newNamespace(final String namespace) {
        val featureId = new ApplicationFeatureId(ApplicationFeatureSort.NAMESPACE);
        featureId.namespace = namespace;
        featureId.typeSimpleName = null;
        featureId.memberName = null;
        return featureId;
    }

    public static ApplicationFeatureId newType(final String logicalTypeName) {
        val featureId = new ApplicationFeatureId(ApplicationFeatureSort.TYPE);
        initType(featureId, logicalTypeName);
        return featureId;
    }

    public static ApplicationFeatureId newMember(final String logicalTypeName, final String memberName) {
        val featureId = new ApplicationFeatureId(ApplicationFeatureSort.MEMBER);
        initType(featureId, logicalTypeName);
        featureId.memberName = memberName;
        return featureId;
    }

    public static ApplicationFeatureId newMember(String fullyQualifiedName) {
        val featureId = new ApplicationFeatureId(ApplicationFeatureSort.MEMBER);
        final int i = fullyQualifiedName.lastIndexOf("#");
        if(i == -1) {
            throw new IllegalArgumentException("Malformed, expected a '#': " + fullyQualifiedName);
        }
        val logicalTypeName = fullyQualifiedName.substring(0, i);
        val memberName = fullyQualifiedName.substring(i + 1);
        initType(featureId, logicalTypeName);
        featureId.memberName = memberName;
        return featureId;
    }
    
    // -- FACTORY HELPERS
    
    private static void initType(final ApplicationFeatureId featureId, final String fullyQualifiedName) {
        final int i = fullyQualifiedName.lastIndexOf(".");
        if(i != -1) {
            featureId.namespace = fullyQualifiedName.substring(0, i);
            featureId.typeSimpleName = fullyQualifiedName.substring(i+1);
        } else {
            featureId.namespace = "";
            featureId.typeSimpleName = fullyQualifiedName;
        }
        featureId.memberName = null;
    }
    
    // -- CONSTRUCTOR

    private ApplicationFeatureId(final ApplicationFeatureSort sort) {
        this.sort = sort;
    }

    // -- TITLE
    
    /**
     * having a title() method (rather than using @Title annotation) is necessary as a workaround to be able to use
     * wrapperFactory#unwrap(...) method, which is otherwise broken in Isis 1.6.0
     */
    public String title() {
        final TitleBuffer buf = new TitleBuffer();
        buf.append(getFullyQualifiedName());
        return buf.toString();
    }

    // -- PROPERTIES
    
    @Getter final @NonNull ApplicationFeatureSort sort;
    
    /**
     * The {@link ApplicationFeatureId id} of the member's logical type.
     */
    public ApplicationFeatureId getParentTypeFeatureId() {
        _Assert.assertTrue(sort.isMember());
        final String logicalTypeName = this.getNamespace() + "." + getTypeSimpleName();
        return newType(logicalTypeName);
    }
    
    // -- PROPERTIES - NON UI
    
    @Programmatic 
    @Getter private String namespace;

    @Programmatic 
    @Getter private String typeSimpleName;

    @Programmatic 
    @Getter private String memberName;

    @Programmatic
    public String getFullyQualifiedName() {
        val buf = new StringBuilder();
        buf.append(getNamespace());
        if(getTypeSimpleName() != null) {
            buf.append(".").append(getTypeSimpleName());
        }
        if(getMemberName() != null) {
            buf.append("#").append(getMemberName());
        }
        return buf.toString();
    }

    @Programmatic
    public String getLogicalTypeName() {
        if (getTypeSimpleName() == null) {
            return null;
        }
        val buf = new StringBuilder();
        if(!_Strings.isNullOrEmpty(getNamespace())) {
            buf.append(getNamespace()).append(".");
        }
        buf.append(getTypeSimpleName());

        return buf.toString();
    }

    /**
     * The {@link ApplicationFeatureId id} of the parent package of this
     * class or package.
     */
    @Programmatic
    public ApplicationFeatureId getParentNamespaceFeatureId() {
        
        _Assert.assertFalse(sort.isMember());

        if(sort.isType()) {
            return ApplicationFeatureId.newNamespace(getNamespace());
        } else {
            val namespace = getNamespace(); // eg aaa.bbb.ccc
            if(!namespace.contains(".")) {
                return null; // parent is root
            }
            final int cutOffPos = namespace.lastIndexOf('.');
            final String parentPackageName = namespace.substring(0, cutOffPos);
            return newNamespace(parentPackageName);
        }
    }

    // -- ENCODING / DECODING

    @Programmatic
    public String stringify() {
        return sort.name() + ":" + getFullyQualifiedName();
    }

    /**
     * Round-trip with {@link #stringify()}
     */
    public static ApplicationFeatureId parse(final String stringified) {
        return _Strings.splitThenApplyRequireNonEmpty(stringified, ":", (sort, fqn)->
            newFeature(ApplicationFeatureSort.valueOf(sort), fqn))
        .orElseThrow(()->_Exceptions.illegalArgument("cannot parse feature-id '%s'", stringified));
    }
    
    @Programmatic
    public String asEncodedString() {
        return _Strings.base64UrlEncode(stringify());
    }

    /**
     * Round-trip with {@link #asEncodedString()}
     */
    public static ApplicationFeatureId parseEncoded(final String encodedString) {
        return parse(_Strings.base64UrlDecode(encodedString));
    }

    // -- pathIds, parentIds

    private transient Can<ApplicationFeatureId> pathIds;
    
    @Programmatic @Synchronized
    public Can<ApplicationFeatureId> getPathIds() {
        if(pathIds==null) {
            pathIds = pathIds(this);
        }
        return pathIds;
    }

    @Programmatic
    public Can<ApplicationFeatureId> getParentFeatureIds() {
        val parent = getParentFeatureId();
        return parent!=null
                ? getParentFeatureId().getPathIds()
                : Can.empty();
    }

    private ApplicationFeatureId getParentFeatureId() {
        return sort.isMember()
                ? getParentTypeFeatureId()
                : getParentNamespaceFeatureId();
    }

    private static Can<ApplicationFeatureId> pathIds(final ApplicationFeatureId featureId) {
        val featureIds = _Lists.<ApplicationFeatureId>newArrayList();
        visitSelfAndParents(featureId, featureIds::add);
        return Can.ofCollection(featureIds);
    }

    private static void visitSelfAndParents(
            final ApplicationFeatureId featureId, 
            final Consumer<ApplicationFeatureId> onNext) {
        if(featureId != null) {
            onNext.accept(featureId);
            visitSelfAndParents(featureId.getParentFeatureId(), onNext);
        }
    }

    // -- OBJECT CONTRACT

    private static final Comparator<ApplicationFeatureId> bySort =
            comparing(ApplicationFeatureId::getSort, nullsFirst(naturalOrder()));
    private static final Comparator<ApplicationFeatureId> byNamespace =
            comparing(ApplicationFeatureId::getNamespace, nullsFirst(naturalOrder()));
    private static final Comparator<ApplicationFeatureId> byTypeSimpleName =
            comparing(ApplicationFeatureId::getTypeSimpleName, nullsFirst(naturalOrder()));
    private static final Comparator<ApplicationFeatureId> byMemberName =
            comparing(ApplicationFeatureId::getMemberName, nullsFirst(naturalOrder()));

    private static final Comparator<ApplicationFeatureId> comparator =
            Comparator.nullsFirst(bySort)
            .thenComparing(byNamespace)
            .thenComparing(byTypeSimpleName)
            .thenComparing(byMemberName);

    private static final Equality<ApplicationFeatureId> equality =
            ObjectContracts.checkEquals(ApplicationFeatureId::getSort)
            .thenCheckEquals(ApplicationFeatureId::getNamespace)
            .thenCheckEquals(ApplicationFeatureId::getTypeSimpleName)
            .thenCheckEquals(ApplicationFeatureId::getMemberName);

    private static final Hashing<ApplicationFeatureId> hashing =
            ObjectContracts.hashing(ApplicationFeatureId::getSort)
            .thenHashing(ApplicationFeatureId::getNamespace)
            .thenHashing(ApplicationFeatureId::getTypeSimpleName)
            .thenHashing(ApplicationFeatureId::getMemberName);

    private static final ToString<ApplicationFeatureId> toString =
            ObjectContracts.toString("sort", ApplicationFeatureId::getSort)
            .thenToString("namespace", ApplicationFeatureId::getNamespace)
            .thenToStringOmitIfAbsent("typeSimpleName", ApplicationFeatureId::getTypeSimpleName)
            .thenToStringOmitIfAbsent("memberName", ApplicationFeatureId::getMemberName);


    @Override
    public int compareTo(final ApplicationFeatureId other) {
        return comparator.compare(this, other);
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
     * Returns a new instance that is a clone of this, except for the namespace,
     * which is taken from the argument. 
     * @param namespace
     */
    public ApplicationFeatureId withNamespace(final @NonNull String namespace) {
        return newFeature(namespace, this.getTypeSimpleName(), this.getMemberName()); 
    }




}
