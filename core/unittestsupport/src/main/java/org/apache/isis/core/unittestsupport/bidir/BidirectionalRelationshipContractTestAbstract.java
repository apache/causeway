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
package org.apache.isis.core.unittestsupport.bidir;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.jdo.annotations.Persistent;

import org.hamcrest.Matchers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.apache.isis.core.unittestsupport.AbstractApplyToAllContractTest;
import org.apache.isis.core.unittestsupport.utils.CollectUtils;
import org.apache.isis.core.unittestsupport.utils.ReflectUtils;
import org.apache.isis.core.unittestsupport.utils.StringUtils;

import static org.apache.isis.core.commons.internal.collections._Collections.toHashSet;
import static org.apache.isis.core.commons.internal.reflection._Reflect.streamAllFields;
import static org.apache.isis.core.commons.internal.reflection._Reflect.streamAllMethods;
import static org.apache.isis.core.commons.internal.reflection._Reflect.withMethodParametersCount;
import static org.apache.isis.core.commons.internal.reflection._Reflect.withName;
import static org.apache.isis.core.unittestsupport.utils.ReflectUtils.withParametersAssignableFrom;

public abstract class BidirectionalRelationshipContractTestAbstract extends AbstractApplyToAllContractTest implements Instantiators {

    private final InstantiatorMap instantiatorMap;

    protected BidirectionalRelationshipContractTestAbstract(
            final String packagePrefix,
            Map<Class<?>, Instantiator> instantiatorsByClass) {
        super(packagePrefix);
        instantiatorMap = new InstantiatorMap(instantiatorsByClass);
    }

    @Override
    protected void applyContractTest(Class<?> entityType) {
        final Set<Field> mappedByFields = streamAllFields(entityType, true)
                .filter(ReflectUtils.persistentMappedBy)
                .collect(toHashSet());

        for (Field mappedByField : mappedByFields) {
            final Parent p = new Parent();
            p.entityType = entityType;
            p.childField = mappedByField;
            try {
                out.println("processing " + p.entityType.getSimpleName() + "#" + p.childField.getName());
                out.incrementIndent();
                process(p);
            } finally {
                out.decrementIndent();
            }
        }
    }

    private void process(Parent p) {

        // mappedBy
        final Persistent persistentAnnotation = p.childField.getAnnotation(Persistent.class);
        p.mappedBy = persistentAnnotation.mappedBy();

        // getMethod
        final String getMethod = StringUtils.methodNamed("get", p.childField);
        final Set<Method> getMethods = streamAllMethods(p.entityType, true)
                .filter(withConcreteMethodNamed(getMethod))
                .collect(toHashSet());

        assertThat(p.desc() + ": no unique getXxx() method:" + getMethods , getMethods.size(), is(greaterThan(0)));
        p.getMethod = CollectUtils.firstIn(getMethods);

        final Child c = new Child();

        final Class<?> returnType = p.getMethod.getReturnType();
        if(Collection.class.isAssignableFrom(returnType)) {
            // addToMethod
            final String addToMethod = StringUtils.methodNamed("addTo", p.childField);
            final Set<Method> addToMethods = streamAllMethods(p.entityType, true)
                    .filter(withConcreteMethodNamed(addToMethod))
                    .filter(withMethodParametersCount(1))
                    .filter(ReflectUtils.withEntityParameter())
                    .collect(toHashSet());

            if(addToMethods.size() != 1) {
                // just skip
                out.println("no addToXxx() method in parent");
                return;
            }
            p.addToMethod = CollectUtils.firstIn(addToMethods);

            // removeFromMethod
            final String removeFromMethod = StringUtils.methodNamed("removeFrom", p.childField);
            final Set<Method> removeFromMethods = streamAllMethods(p.entityType, true)
                    .filter(withConcreteMethodNamed(removeFromMethod))
                    .filter(withMethodParametersCount(1))
                    .filter(ReflectUtils.withEntityParameter())
                    .collect(toHashSet());

            if(removeFromMethods.size() != 1) {
                // just skip
                out.println("no removeFromXxx() method in parent");
                return;
            }
            p.removeFromMethod = CollectUtils.firstIn(removeFromMethods);

            // child's entityType
            final Class<?> addToParameterType = p.addToMethod.getParameterTypes()[0];
            final Class<?> removeFromParameterType = p.removeFromMethod.getParameterTypes()[0];

            assertThat(p.desc() + ": " + p.addToMethod.getName() + " and " + p.removeFromMethod.getName() + " should have the same parameter type",
                    addToParameterType == removeFromParameterType, is(true));

            c.entityType = addToParameterType;
        } else {

            // modify
            String modifyMethod = StringUtils.methodNamed("modify", p.childField);
            final Set<Method> modifyMethods = streamAllMethods(p.entityType, true)
                    .filter(withConcreteMethodNamed(modifyMethod))
                    .filter(withMethodParametersCount(1))
                    .filter(ReflectUtils.withEntityParameter())
                    .collect(toHashSet());

            if(modifyMethods.size() != 1) {
                // just skip
                out.println("no modifyXxx() method in parent");
                return;
            }
            p.modifyMethod = CollectUtils.firstIn(modifyMethods);

            // clear
            String clearMethod = StringUtils.methodNamed("clear", p.childField);
            final Set<Method> clearMethods = streamAllMethods(p.entityType, true)
                    .filter(withConcreteMethodNamed(clearMethod))
                    .filter(withMethodParametersCount(0))
                    .collect(toHashSet());

            if(clearMethods.size() != 1) {
                // just skip
                out.println("no clearXxx() method in parent");
                return;
            }
            p.clearMethod = CollectUtils.firstIn(clearMethods);

            // child's entityType
            c.entityType = p.modifyMethod.getParameterTypes()[0];
        }

        final Instantiator parentInstantiator = instantiatorFor(p.entityType);
        if(parentInstantiator == null) {
            out.println("no instantiator for " + p.entityType.getName());
            // just skip
            return;
        }
        final Instantiator childInstantiator = instantiatorFor(c.entityType);
        if(childInstantiator == null) {
            out.println("no instantiator for " + c.entityType.getName());
            // just skip
            return;
        }

        process(p, c);
    }

    private static Predicate<Method> withConcreteMethodNamed(final String methodName) {
        return (Method m) -> {
            if(m==null || !methodName.equals(m.getName())){
                return false;
            }
            return !m.isSynthetic() && !Modifier.isAbstract(m.getModifiers());
        };
    }


    private Instantiator instantiatorFor(final Class<?> cls) {
        Instantiator instantiator = instantiatorMap.get(cls);
        if(instantiator != null) {
            return instantiator;
        }

        instantiator = doInstantiatorFor(cls);

        instantiator = instantiatorMap.put(cls, instantiator);
        return instantiator != Instantiator.NOOP? instantiator: null;
    }


    /**
     * Default just tries to use the {@link InstantiatorSimple};
     * subclasses can override with more sophisticated implementations if required.
     */
    protected Instantiator doInstantiatorFor(final Class<?> cls) {
        return new InstantiatorSimple(cls);
    }

    private void process(Parent p, Child c) {

        // mappedBy field
        final Set<Field> parentFields = streamAllFields(c.entityType, true)
                .filter(withName(p.mappedBy))
                .filter(ReflectUtils.withTypeAssignableFrom(p.entityType))
                .collect(toHashSet());

        assertThat(c.entityType.getName()+  ": could not locate '" + p.mappedBy + "' field, returning supertype of " + p.entityType.getSimpleName() +", (as per @Persistent(mappedBy=...) in parent "+ p.entityType.getSimpleName()+")", parentFields.size(), is(1));
        c.parentField = CollectUtils.firstIn(parentFields);

        // getter
        String getterMethod = StringUtils.methodNamed("get", c.parentField);
        final Set<Method> getterMethods = streamAllMethods(c.entityType, true)
                .filter(withConcreteMethodNamed(getterMethod))
                .filter(withMethodParametersCount(0))
                .filter(ReflectUtils.withReturnTypeAssignableFrom(p.entityType))
                .collect(toHashSet());

        assertThat(p.descRel(c) +": could not locate getter " + getterMethod + "() returning supertype of " + p.entityType.getSimpleName(), getterMethods.size(), is(1));
        c.getMethod = CollectUtils.firstIn(getterMethods);

        // modify
        String modifyMethod = StringUtils.methodNamed("modify", c.parentField);
        final Set<Method> modifyMethods = streamAllMethods(c.entityType, true)
                .filter(withConcreteMethodNamed(modifyMethod))
                .filter(withMethodParametersCount(1))
                .filter(withParametersAssignableFrom(p.entityType))
                .collect(toHashSet());

        if(modifyMethods.size() != 1) {
            // just skip
            out.println("no modifyXxx() method in child");
            return;
        }
        c.modifyMethod = CollectUtils.firstIn(modifyMethods);

        // clear
        String clearMethod = StringUtils.methodNamed("clear", c.parentField);
        final Set<Method> clearMethods = streamAllMethods(c.entityType, true)
                .filter(withConcreteMethodNamed(clearMethod))
                .filter(withMethodParametersCount(0))
                .collect(toHashSet());

        if(clearMethods.size() != 1) {
            // just skip
            out.println("no clearXxx() method in child");
            return;
        }
        c.clearMethod = CollectUtils.firstIn(clearMethods);

        exercise(p, c);
    }

    @Override
    public Object newInstance(final Class<?> entityType) {
        final Instantiator instantiator = instantiatorFor(entityType);
        return instantiator.instantiate();
    }

    private static String assertDesc(Parent p, Child c, String methodDesc, String testDesc) {
        return p.descRel(c) +": " + methodDesc + ": " + testDesc;
    }

    private void exercise(Parent p, Child c) {
        out.println("exercising " + p.descRel(c));
        out.incrementIndent();
        try {
            if(p.addToMethod != null) {
                // 1:m

                // add
                oneToManyParentAddTo(p, c);
                oneToManyParentAddToWhenAlreadyChild(p, c);
                oneToManyParentAddToWhenNull(p, c);
                oneToManyChildModify(p, c);
                oneToManyChildModifyWhenAlreadyParent(p, c);
                oneToManyChildModifyWhenNull(p, c);

                // move (update)
                oneToManyChildModifyToNewParent(p, c);
                oneToManyChildModifyToExistingParent(p, c);

                // delete
                oneToManyParentRemoveFrom(p, c);
                oneToManyParentRemoveFromWhenNotAssociated(p, c);
                oneToManyParentRemoveFromWhenNull(p, c);
                oneToManyChildClear(p, c);
                oneToManyChildClearWhenNotAssociated(p, c);
            } else {
                // 1:1

                // add
                oneToOneParentModify(p, c);
                oneToOneParentModifyWhenAlreadyChild(p, c);
                oneToOneParentModifyWhenNull(p, c);
                oneToOneChildModify(p, c);
                oneToOneChildModifyWhenAlreadyParent(p, c);
                oneToOneChildModifyWhenNull(p, c);

                // move (update)
                oneToOneChildModifyToNewParent(p, c);
                oneToOneChildModifyToExistingParent(p, c);

                // delete
                oneToOneParentClear(p, c);
                oneToOneChildClear(p, c);
                oneToOneChildClearWhenNotAssociated(p, c);
            }

        } finally {
            out.decrementIndent();
        }

    }

    ////////////////
    // 1:m
    ////////////////

    private void oneToManyParentAddTo(Parent p, Child c) {

        final String methodDesc = "oneToManyParentAddTo";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);
        Object child1 = c.newChild(this);

        // when
        p.addToChildren(parent1, child1);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent contains child"), p.getChildren(parent1), Matchers.containsInAnyOrder(child1));
        assertThat(assertDesc(p,c,methodDesc,"child references parent"), c.getParent(child1), is(parent1));
    }

    private void oneToManyParentAddToWhenAlreadyChild(Parent p, Child c) {

        final String methodDesc = "oneToManyParentAddToWhenAlreadyChild";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);
        Object child1 = c.newChild(this);

        // given
        p.addToChildren(parent1, child1);

        // when
        p.addToChildren(parent1, child1);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent still contains child"), p.getChildren(parent1), Matchers.containsInAnyOrder(child1));
        assertThat(assertDesc(p,c,methodDesc,"child still references parent"), c.getParent(child1), is(parent1));
    }

    private void oneToManyParentAddToWhenNull(Parent p, Child c) {

        final String methodDesc = "oneToManyParentAddToWhenNull";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);

        // when
        p.addToChildren(parent1, null);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent does not have any children"), p.getChildren(parent1).isEmpty(), is(true));
    }

    private void oneToManyChildModify(Parent p, Child c) {

        final String methodDesc = "oneToManyChildModify";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);
        Object child1 = c.newChild(this);

        // when
        c.modifyParent(child1, parent1);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent contains child"), p.getChildren(parent1), Matchers.containsInAnyOrder(child1));
        assertThat(assertDesc(p,c,methodDesc,"child references parent"), c.getParent(child1), is(parent1));
    }

    private void oneToManyChildModifyWhenAlreadyParent(Parent p, Child c) {

        final String methodDesc = "oneToManyChildModifyWhenAlreadyParent";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);
        Object child1 = c.newChild(this);

        c.modifyParent(child1, parent1);

        // when
        c.modifyParent(child1, parent1);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent still contains child"), p.getChildren(parent1), Matchers.containsInAnyOrder(child1));
        assertThat(assertDesc(p,c,methodDesc,"child still references parent"), c.getParent(child1), is(parent1));
    }


    private void oneToManyChildModifyWhenNull(Parent p, Child c) {

        final String methodDesc = "oneToManyChildModifyWhenNull";
        out.println(methodDesc);

        // given
        Object child1 = c.newChild(this);

        // when
        c.modifyParent(child1, null);

        // then
        assertThat(assertDesc(p,c,methodDesc,"child does not reference any parent"), c.getParent(child1), is(nullValue()));
    }


    private void oneToManyChildModifyToNewParent(Parent p, Child c) {

        final String methodDesc = "oneToManyChildModifyToNewParent";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);
        Object parent2 = p.newParent(this);
        Object child1 = c.newChild(this);
        Object child2 = c.newChild(this);

        p.addToChildren(parent1, child1);
        p.addToChildren(parent2, child2);

        // when
        c.modifyParent(child1, parent2);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent 1 no longer has any children"), p.getChildren(parent1).isEmpty(), is(true));
        assertThat(assertDesc(p,c,methodDesc,"parent 2 now has both children"), p.getChildren(parent2), Matchers.containsInAnyOrder(child1, child2));
        assertThat(assertDesc(p,c,methodDesc,"child 1 now references parent 2"), c.getParent(child1), is(parent2));
        assertThat(assertDesc(p,c,methodDesc,"child 2 still references parent 2"), c.getParent(child2), is(parent2));
    }

    private void oneToManyChildModifyToExistingParent(Parent p, Child c) {

        final String methodDesc = "oneToManyChildModifyToExistingParent";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);
        Object parent2 = p.newParent(this);
        Object child1 = c.newChild(this);
        Object child2 = c.newChild(this);

        p.addToChildren(parent1, child1);
        p.addToChildren(parent2, child2);

        // when
        c.modifyParent(child1, parent1);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent 1 still contains child 1"), p.getChildren(parent1), Matchers.containsInAnyOrder(child1));
        assertThat(assertDesc(p,c,methodDesc,"parent 2 still contains child 2"), p.getChildren(parent2), Matchers.containsInAnyOrder(child2));
        assertThat(assertDesc(p,c,methodDesc,"child 1 still references parent 1"), c.getParent(child1), is(parent1));
        assertThat(assertDesc(p,c,methodDesc,"child 2 still references parent 2"), c.getParent(child2), is(parent2));
    }

    private void oneToManyParentRemoveFrom(Parent p, Child c) {

        final String methodDesc = "oneToManyParentRemoveFrom";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);
        Object child1 = c.newChild(this);

        p.addToChildren(parent1, child1);

        // when
        p.removeFromChildren(parent1, child1);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent no longer contains child"), p.getChildren(parent1).isEmpty(), is(true));
        assertThat(assertDesc(p,c,methodDesc,"child no longer references parent"), c.getParent(child1), is(nullValue()));
    }

    private void oneToManyParentRemoveFromWhenNull(Parent p, Child c) {

        final String methodDesc = "oneToManyParentRemoveFromWhenNull";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);
        Object child1 = c.newChild(this);

        p.addToChildren(parent1, child1);

        // when
        p.removeFromChildren(parent1, null);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent still contains child"), p.getChildren(parent1), Matchers.containsInAnyOrder(child1));
        assertThat(assertDesc(p,c,methodDesc,"child still references parent"), c.getParent(child1), is(parent1));
    }

    private void oneToManyParentRemoveFromWhenNotAssociated(Parent p, Child c) {

        final String methodDesc = "oneToManyParentRemoveFromWhenNotAssociated";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);
        Object child1 = c.newChild(this);
        Object child2 = c.newChild(this);

        p.addToChildren(parent1, child1);

        // when
        p.removeFromChildren(parent1, child2);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent still contains child"), p.getChildren(parent1), Matchers.containsInAnyOrder(child1));
        assertThat(assertDesc(p,c,methodDesc,"child still references parent"), c.getParent(child1), is(parent1));
    }

    private void oneToManyChildClear(Parent p, Child c) {

        final String methodDesc = "oneToManyChildClear";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);
        Object child1 = c.newChild(this);

        p.addToChildren(parent1, child1);

        // when
        c.clearParent(child1);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent no longer contains child"), p.getChildren(parent1).isEmpty(), is(true));
        assertThat(assertDesc(p,c,methodDesc,"child no longer references parent"), c.getParent(child1), is(nullValue()));
    }

    private void oneToManyChildClearWhenNotAssociated(Parent p, Child c) {

        final String methodDesc = "oneToManyChildClearWhenNotAssociated";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);
        Object child1 = c.newChild(this);

        // when
        c.clearParent(child1);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent still does not reference child"), p.getChildren(parent1).isEmpty(), is(true));
        assertThat(assertDesc(p,c,methodDesc,"child still does not reference parent"), c.getParent(child1), is(nullValue()));
    }



    ////////////////
    // 1:1
    ////////////////

    private void oneToOneParentModify(Parent p, Child c) {

        final String methodDesc = "oneToOneParentModify";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);
        Object child1 = c.newChild(this);

        // when
        p.modifyChild(parent1, child1);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent references child"),p.getChild(parent1), is(child1));
        assertThat(assertDesc(p,c,methodDesc,"child references parent"), c.getParent(child1), is(parent1));
    }

    private void oneToOneParentModifyWhenAlreadyChild(Parent p, Child c) {

        final String methodDesc = "oneToOneParentModifyWhenAlreadyChild";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);
        Object child1 = c.newChild(this);

        p.modifyChild(parent1, child1);

        // when
        p.modifyChild(parent1, child1);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent still references child"), p.getChild(parent1), is(child1));
        assertThat(assertDesc(p,c,methodDesc,"child still references parent"), c.getParent(child1), is(parent1));
    }

    private void oneToOneParentModifyWhenNull(Parent p, Child c) {

        final String methodDesc = "oneToOneParentModifyWhenNull";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);

        // when
        p.modifyChild(parent1, null);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent still references child"), p.getChild(parent1), is(nullValue()));
    }

    private void oneToOneChildModify(Parent p, Child c) {

        final String methodDesc = "oneToOneChildModify";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);
        Object child1 = c.newChild(this);

        // when
        c.modifyParent(child1, parent1);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent references child"), p.getChild(parent1), is(child1));
        assertThat(assertDesc(p,c,methodDesc,"child references parent"), c.getParent(child1), is(parent1));
    }

    private void oneToOneChildModifyWhenAlreadyParent(Parent p, Child c) {

        final String methodDesc = "oneToOneChildModifyWhenAlreadyParent";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);
        Object child1 = c.newChild(this);

        c.modifyParent(child1, parent1);

        // when
        c.modifyParent(child1, parent1);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent still references child"), p.getChild(parent1), is(child1));
        assertThat(assertDesc(p,c,methodDesc,"child still references parent"), c.getParent(child1), is(parent1));
    }


    private void oneToOneChildModifyWhenNull(Parent p, Child c) {

        final String methodDesc = "oneToOneChildModifyWhenNull";
        out.println(methodDesc);

        // given
        Object child1 = c.newChild(this);

        // when
        c.modifyParent(child1, null);

        // then
        assertThat(assertDesc(p,c,methodDesc,"child still has no parent"), c.getParent(child1), is(nullValue()));
    }


    private void oneToOneChildModifyToNewParent(Parent p, Child c) {

        final String methodDesc = "oneToOneChildModifyToNewParent";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);
        Object parent2 = p.newParent(this);
        Object child1 = c.newChild(this);
        Object child2 = c.newChild(this);

        p.modifyChild(parent1, child1);
        p.modifyChild(parent2, child2);

        // when
        c.modifyParent(child1, parent2);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent 1 no longer references child 1"), p.getChild(parent1), is(nullValue()));
        assertThat(assertDesc(p,c,methodDesc,"parent 2 now references child 1"), p.getChild(parent2), is(child1));
        assertThat(assertDesc(p,c,methodDesc,"child 1 now references parent 2"), c.getParent(child1), is(parent2));
        assertThat(assertDesc(p,c,methodDesc,"child 2, as a side-effect, no longer references parent 2"), c.getParent(child2), is(nullValue()));
    }

    private void oneToOneChildModifyToExistingParent(Parent p, Child c) {

        final String methodDesc = "oneToOneChildModifyToExistingParent";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);
        Object parent2 = p.newParent(this);
        Object child1 = c.newChild(this);
        Object child2 = c.newChild(this);

        p.modifyChild(parent1, child1);
        p.modifyChild(parent2, child2);

        // when
        c.modifyParent(child1, parent1);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent 1 still references child 1"), p.getChild(parent1), is(child1));
        assertThat(assertDesc(p,c,methodDesc,"parent 2 still references child 2"), p.getChild(parent2), is(child2));
        assertThat(assertDesc(p,c,methodDesc,"child 1 still references parent 1"), c.getParent(child1), is(parent1));
        assertThat(assertDesc(p,c,methodDesc,"child 2 still references parent 2"), c.getParent(child2), is(parent2));
    }

    private void oneToOneParentClear(Parent p, Child c) {

        final String methodDesc = "oneToOneParentClear";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);
        Object child1 = c.newChild(this);

        p.modifyChild(parent1, child1);

        // when
        p.clearChild(parent1);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent no longer references child"), p.getChild(parent1), is(nullValue()));
        assertThat(assertDesc(p,c,methodDesc,"child no longer references parent"), c.getParent(child1), is(nullValue()));
    }


    private void oneToOneChildClear(Parent p, Child c) {

        final String methodDesc = "oneToOneChildClear";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);
        Object child1 = c.newChild(this);

        p.modifyChild(parent1, child1);

        // when
        c.clearParent(child1);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent no longer references child"), p.getChild(parent1), is(nullValue()));
        assertThat(assertDesc(p,c,methodDesc,"child no longer references parent"), c.getParent(child1), is(nullValue()));
    }

    private void oneToOneChildClearWhenNotAssociated(Parent p, Child c) {

        final String methodDesc = "oneToOneChildClearWhenNotAssociated";
        out.println(methodDesc);

        // given
        Object parent1 = p.newParent(this);
        Object child1 = c.newChild(this);

        // when
        c.clearParent(child1);

        // then
        assertThat(assertDesc(p,c,methodDesc,"parent still does not reference child"), p.getChild(parent1), is(nullValue()));
        assertThat(assertDesc(p,c,methodDesc,"child still does not reference parent"), c.getParent(child1), is(nullValue()));
    }

}
