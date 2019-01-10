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
package org.apache.isis.core.metamodel.facets.actions.layout;

import java.util.List;

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.actions.notcontributed.NotContributedFacet;
import org.apache.isis.core.metamodel.facets.actions.position.ActionPositionFacet;
import org.apache.isis.core.metamodel.facets.actions.position.ActionPositionFacetFallback;
import org.apache.isis.core.metamodel.facets.actions.redirect.RedirectFacet;
import org.apache.isis.core.metamodel.facets.actions.redirect.RedirectFacetFallback;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFacet;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class ActionLayoutFacetFactory extends FacetFactoryAbstract implements ContributeeMemberFacetFactory {

    public ActionLayoutFacetFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final FacetHolder holder = processMethodContext.getFacetHolder();

        final List<ActionLayout> actionLayouts = Annotations.getAnnotations(processMethodContext.getMethod(), ActionLayout.class);


        // bookmarkable
        BookmarkPolicyFacet bookmarkableFacet = BookmarkPolicyFacetForActionLayoutAnnotation
                .create(actionLayouts, holder);
        FacetUtil.addFacet(bookmarkableFacet);


        // cssClass
        CssClassFacet cssClassFacet = CssClassFacetForActionLayoutAnnotation.create(actionLayouts, holder);
        FacetUtil.addFacet(cssClassFacet);


        // cssClassFa
        CssClassFaFacet cssClassFaFacet = CssClassFaFacetForActionLayoutAnnotation.create(actionLayouts, holder);
        FacetUtil.addFacet(cssClassFaFacet);


        // describedAs
        DescribedAsFacet describedAsFacet = DescribedAsFacetForActionLayoutAnnotation.create(actionLayouts, holder);
        FacetUtil.addFacet(describedAsFacet);


        // hidden
        HiddenFacet hiddenFacet = HiddenFacetForActionLayoutAnnotation.create(actionLayouts, holder);
        FacetUtil.addFacet(hiddenFacet);


        // named
        NamedFacet namedFacet = NamedFacetForActionLayoutAnnotation.create(actionLayouts, holder);
        FacetUtil.addFacet(namedFacet);

        // promptStyle
        PromptStyleFacet promptStyleFacet = PromptStyleFacetForActionLayoutAnnotation
                .create(actionLayouts, getConfiguration(), holder);

        FacetUtil.addFacet(promptStyleFacet);


        // position
        ActionPositionFacet actionPositionFacet = ActionPositionFacetForActionLayoutAnnotation
                .create(actionLayouts, holder);
        if(actionPositionFacet == null) {
            actionPositionFacet = new ActionPositionFacetFallback(holder);
        }
        FacetUtil.addFacet(actionPositionFacet);


        // redirectPolicy
        RedirectFacet redirectFacet = RedirectFacetFromActionLayoutAnnotation.create(actionLayouts, holder);
        if(redirectFacet == null) {
            redirectFacet = new RedirectFacetFallback(holder);
        }
        FacetUtil.addFacet(redirectFacet);


        // contributing
        if (isContributingServiceOrMixinObject(processMethodContext)) {
            NotContributedFacet notContributedFacet = NotContributedFacetForActionLayoutAnnotation
                    .create(actionLayouts, holder);
            FacetUtil.addFacet(notContributedFacet);
        }
    }

    private boolean isContributingServiceOrMixinObject(final ProcessMethodContext processMethodContext) {
        final Class<?> cls =  processMethodContext.getCls();
        final ObjectSpecification spec = getSpecificationLoader().loadSpecification(cls);

        return isContributingService(spec) || isMixinObject(spec);
    }

    private static boolean isContributingService(final ObjectSpecification spec) {
        final DomainServiceFacet domainServiceFacet = spec.getFacet(DomainServiceFacet.class);
        return domainServiceFacet != null && !domainServiceFacet.isNoop() &&
                domainServiceFacet.getNatureOfService() != NatureOfService.VIEW_MENU_ONLY &&
                domainServiceFacet.getNatureOfService() != NatureOfService.DOMAIN;
    }

    private static boolean isMixinObject(final ObjectSpecification spec) {
        final MixinFacet mixinFacet = spec.getFacet(MixinFacet.class);
        final boolean b = mixinFacet != null && !mixinFacet.isNoop();
        return b;
    }

    // UNUSED
    protected boolean skipContributing(final DomainServiceFacet domainServiceFacet) {
        return domainServiceFacet == null || domainServiceFacet.isNoop() || domainServiceFacet.getNatureOfService() == NatureOfService.VIEW_MENU_ONLY || domainServiceFacet.getNatureOfService() == NatureOfService.DOMAIN;
    }

    @Override
    public void process(ProcessContributeeMemberContext processMemberContext) {
        final FacetHolder holder = processMemberContext.getFacetHolder();

        // cssClass
        CssClassFacet cssClassFacet = null;
        FacetUtil.addFacet(cssClassFacet);

        // cssClassFa
        CssClassFaFacet cssClassFaFacet = null;
        FacetUtil.addFacet(cssClassFaFacet);

        // describedAs
        DescribedAsFacet describedAsFacet = null;
        FacetUtil.addFacet(describedAsFacet);

        // hidden
        HiddenFacet hiddenFacet = null;
        FacetUtil.addFacet(hiddenFacet);

        // named
        NamedFacet namedFacet = null;
        FacetUtil.addFacet(namedFacet);

        // position
        ActionPositionFacet actionPositionFacet = null;
        if(! holder.containsDoOpFacet(ActionPositionFacet.class)) {
            actionPositionFacet = new ActionPositionFacetFallback(holder);
        }
        FacetUtil.addFacet(actionPositionFacet);

    }

}
