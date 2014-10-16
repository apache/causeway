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
package org.apache.isis.viewer.wicket.ui.components.actionprompt;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;

public class ActionPromptModalWindow extends ModalWindow implements ActionPrompt {

    private static final long serialVersionUID = 1L;

    public static ActionPromptModalWindow getActionPromptModalWindowIfEnabled(ActionPromptModalWindow modalWindow) {
        return !isActionPromptModalDialogDisabled() ? modalWindow : null;
    }

    public static boolean isActionPromptModalDialogDisabled() {
        return getConfiguration().getBoolean("isis.viewer.wicket.disableModalDialogs", false);
    }

    private static IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }

    public static ActionPromptModalWindow newModalWindow(String id) {
        ActionPromptModalWindow modalWindow = new ActionPromptModalWindow(id);
        modalWindow.setAutoSize(true);
        modalWindow.setResizable(false);
        modalWindow.setCssClassName("w_isis");
        return modalWindow;
    }


    // //////////////////////////////////////
    
    
    public ActionPromptModalWindow(String id) {
        super(id);
        setMaskType(MaskType.SEMI_TRANSPARENT);
        add(new CloseOnEscapeKeyBehavior(this));
    }

    private static class CloseOnEscapeKeyBehavior extends AbstractDefaultAjaxBehavior {
        private static final long serialVersionUID = 1L;
        private final ModalWindow modal;
        public CloseOnEscapeKeyBehavior(ModalWindow modal) {
            this.modal = modal;
        }    
        @Override
        protected void respond(AjaxRequestTarget target) {
            if(modal.isShown()) {
                modal.close(target);
            }
        }    
        @Override
        public void renderHead(Component component, IHeaderResponse response) {
            String javaScript = "" +
                "$(document).ready(function() {\n" +
                "  $(document).bind('keyup', function(evt) {\n" +
                "    if (evt.keyCode == 27) {\n" +
                getCallbackScript() + "\n" +
                "        evt.preventDefault();\n" +
                "    }\n" +
                "  });\n" +
                "});";
            String id ="closeModal";
            response.render(JavaScriptHeaderItem.forScript(javaScript, id));
        }
    }
    
    @Override
    public void setPanel(Component component, AjaxRequestTarget target) {
        setContent(component);
    }

    
    @Override
    public void show(AjaxRequestTarget target) {
        
        // http://stackoverflow.com/questions/8013364/how-to-defeat-browser-dialog-popup-when-calling-wicket-setresponsepage-from-mo/8679946#8679946
        target.prependJavaScript("Wicket.Window.unloadConfirmation = false;");
        
        super.show(target);
        
        StringBuilder builder = new StringBuilder();

        // ISIS-771, WICKET-5101
        builder.append(
                "window.setTimeout(" +
                "function() {\n " +
                "  var ww = Wicket.Window.get();\n " +
                "  ww.autoSizeWindow();\n " +
                // this is a nasty hack to make dialog appear without scrollbars on Chrome (noticed in version 37.0.2062.120 m)
                // really though this ought to be fixed in Wicket.Window.get().autoSizeWindow();
                "  $('.actionPanel .myBlockContainer').css('margin', '19px').css('padding', '19px');\n " +
                "}\n, 0);\n");
        
        target.appendJavaScript(builder.toString());

    }


}
