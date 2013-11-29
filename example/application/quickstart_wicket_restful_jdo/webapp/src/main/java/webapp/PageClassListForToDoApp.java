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
package webapp;

import org.apache.wicket.Page;

import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.viewer.registries.pages.PageClassListDefault;

public class PageClassListForToDoApp extends PageClassListDefault {

    @Override
    protected Class<? extends Page> getSignInPageClass() {
        // no override
        return super.getSignInPageClass();
    }
    
    @Override
    protected Class<? extends Page> getHomePageClass() {
        // no override
        return super.getHomePageClass();
    }

    @Override
    protected Class<? extends Page> getAboutPageClass() {
        // no override
        return super.getAboutPageClass();
    }
    
    /**
     * More typically, override using custom {@link ComponentFactory}s.
     */
    @Override
    protected Class<? extends Page> getEntityPageClass() {
        // no override
        return super.getEntityPageClass();
    }
    
    /**
     * More typically, override using custom {@link ComponentFactory}s.
     */
    @Override
    protected Class<? extends Page> getActionPromptPageClass() {
        // no override
        return super.getActionPromptPageClass();
    }
}
