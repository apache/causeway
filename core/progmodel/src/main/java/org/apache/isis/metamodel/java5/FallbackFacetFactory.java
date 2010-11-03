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

package org.apache.isis.metamodel.java5;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.facets.FacetUtil;
import org.apache.isis.metamodel.facets.MethodRemover;
import org.apache.isis.metamodel.facets.actions.choices.ActionChoicesFacetNone;
import org.apache.isis.metamodel.facets.actions.defaults.ActionDefaultsFacetNone;
import org.apache.isis.metamodel.facets.actions.executed.ExecutedFacetAtDefault;
import org.apache.isis.metamodel.facets.help.HelpFacetNone;
import org.apache.isis.metamodel.facets.naming.describedas.DescribedAsFacetNone;
import org.apache.isis.metamodel.facets.naming.named.NamedFacetNone;
import org.apache.isis.metamodel.facets.object.ident.title.TitleFacetNone;
import org.apache.isis.metamodel.facets.object.notpersistable.NotPersistableFacetNull;
import org.apache.isis.metamodel.facets.propparam.multiline.MultiLineFacetNone;
import org.apache.isis.metamodel.facets.propparam.validate.maxlength.MaxLengthFacetUnlimited;
import org.apache.isis.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.metamodel.specloader.internal.peer.JavaObjectActionParamPeer;
import org.apache.isis.metamodel.specloader.internal.peer.JavaObjectActionPeer;
import org.apache.isis.metamodel.specloader.internal.peer.JavaObjectMemberPeer;
import org.apache.isis.metamodel.specloader.internal.peer.JavaOneToOneAssociationPeer;

/**
 * Central point for providing some kind of default for any {@link Facet}s required by the Apache Isis framework itself.
 * 
 */
public class FallbackFacetFactory extends FacetFactoryAbstract {

    @SuppressWarnings("unused")
    private final static Map<Class<?>, Integer> TYPICAL_LENGTHS_BY_CLASS = new HashMap<Class<?>, Integer>() {
        private static final long serialVersionUID = 1L;
        {
            putTypicalLength(byte.class, Byte.class, 3);
            putTypicalLength(short.class, Short.class, 5);
            putTypicalLength(int.class, Integer.class, 10);
            putTypicalLength(long.class, Long.class, 20);
            putTypicalLength(float.class, Float.class, 20);
            putTypicalLength(double.class, Double.class, 20);
            putTypicalLength(char.class, Character.class, 1);
            putTypicalLength(boolean.class, Boolean.class, 1);
        }

        private void putTypicalLength(final Class<?> primitiveClass, final Class<?> wrapperClass, final int length) {
            put(primitiveClass, Integer.valueOf(length));
            put(wrapperClass, Integer.valueOf(length));
        }
    };

    public FallbackFacetFactory() {
        super(ObjectFeatureType.EVERYTHING);
    }

    public boolean recognizes(final Method method) {
        return false;
    }

    @Override
    public boolean process(final Class<?> type, final MethodRemover methodRemover, final FacetHolder holder) {
        return FacetUtil.addFacets(new Facet[] { new DescribedAsFacetNone(holder),
            // commenting these out, think this whole isNoop business is a little bogus
            // new ImmutableFacetNever(holder),
            new NotPersistableFacetNull(holder), new TitleFacetNone(holder), });
    }

    @Override
    public boolean process(Class<?> cls, final Method method, final MethodRemover methodRemover,
        final FacetHolder holder) {
        final List<Facet> facets = new ArrayList<Facet>();

        if (holder instanceof JavaObjectMemberPeer) {
            facets.add(new NamedFacetNone(holder));
            facets.add(new DescribedAsFacetNone(holder));
            facets.add(new HelpFacetNone(holder));
        }

        if (holder instanceof JavaOneToOneAssociationPeer) {
            facets.add(new MaxLengthFacetUnlimited(holder));
            facets.add(new MultiLineFacetNone(true, holder));
        }

        if (holder instanceof JavaObjectActionPeer) {
            facets.add(new ExecutedFacetAtDefault(holder));
            facets.add(new ActionDefaultsFacetNone(holder));
            facets.add(new ActionChoicesFacetNone(holder));
        }

        return FacetUtil.addFacets(facets);
    }

    @Override
    public boolean processParams(final Method method, final int paramNum, final FacetHolder holder) {
        final List<Facet> facets = new ArrayList<Facet>();

        if (holder instanceof JavaObjectActionParamPeer) {

            facets.add(new NamedFacetNone(holder));
            facets.add(new DescribedAsFacetNone(holder));
            facets.add(new HelpFacetNone(holder));
            facets.add(new MultiLineFacetNone(false, holder));

            facets.add(new MaxLengthFacetUnlimited(holder));
        }

        return FacetUtil.addFacets(facets);
    }

}
