/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.ui.components.widgets.zclip;

import com.google.common.io.Resources;

import org.apache.wicket.SharedResources;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.util.resource.UrlResourceStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.ResourceStreamResource;
import org.apache.wicket.request.resource.SharedResourceReference;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

public final class ZeroClipboardLink extends AjaxLink<ObjectAdapter> {

    /**
     * @see #addSharedResourceTo(SharedResources)
     */
    private static final String SHARED_RESOURCE_NAME = ZeroClipboardLink.class.getName();
    /**
     * Relative to this class.
     */
    private static final String FILE_NAME_SWF = "ZeroClipboard.swf";

    private final String linkJQuerySelector;
    
    private final CharSequence zeroClipboardSwfUrl;
    private final String baseUrl;
    private static final long serialVersionUID = 1L;

    public static void addSharedResourceTo(SharedResources sharedResources) {
        sharedResources.add(ZeroClipboardLink.SHARED_RESOURCE_NAME, ZeroClipboardLink.newSwfFileResource());
    }

    private static ResourceStreamResource newSwfFileResource() {
        return new ResourceStreamResource(
                new UrlResourceStream(
                        Resources.getResource(ZeroClipboardLink.class, FILE_NAME_SWF)));
    }

    // //////////////////////////////////////

    public ZeroClipboardLink(String id, AbstractLink linkComponent) {
        this(id, "#"+linkComponent.getMarkupId());
    }

    public ZeroClipboardLink(String id, String linkJQuerySelector) {
        super(id);
        this.linkJQuerySelector = linkJQuerySelector;
        this.zeroClipboardSwfUrl = getRequestCycle().getUrlRenderer().renderFullUrl(Url.parse(urlFor(new SharedResourceReference(SHARED_RESOURCE_NAME), null)));
        this.baseUrl = getRequestCycle().getUrlRenderer().renderFullUrl(Url.parse("."));
    }
    
    @Override
    public void onClick(AjaxRequestTarget target) {
        // (nothing to do)
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        final StringBuilder buf = new StringBuilder();
        //buf.append("var url = " + quote(baseUrl) + "+" + jQueryLinkSelector() + ".attr(\"href\");");
        
        buf.append(jQuerySelectorOf("#"+getMarkupId()) + ".zclip({");
        buf.append("    path:'" + zeroClipboardSwfUrl + "'");
        //buf.append("   ,copy: url");
        buf.append("   ,copy: function(){ return " + quote(baseUrl) + "+" + jQueryLinkSelector() + ".attr(\"href\"); }");
        buf.append("});");
        buf.append(jQuerySelectorOf("#"+getMarkupId()) + ".zclip('show')");

        String js=buf.toString();
        
        response.render(OnDomReadyHeaderItem.forScript(js));
    }

    private String jQueryLinkSelector() {
        return jQuerySelectorOf(linkJQuerySelector);
    }

    private static String jQuerySelectorOf(String selector) {
        return "$('" + selector + "')";
    }

    private static String quote(final String baseUrl) {
        return "\"" + baseUrl +"\"";
    }


}