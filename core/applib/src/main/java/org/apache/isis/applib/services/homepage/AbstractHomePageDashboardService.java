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
package org.apache.isis.applib.services.homepage;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.ViewModel;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.HomePage;

/**
 * Convenience domain service to create a view model (eg a dashboard) to be rendered automatically
 * on the home page of the application.
 *
 * <p>
 * For example, subclass as follows:
 * 
 * <pre>
 * public class MyAppDashboardService extends AbstractHomePageDashboardService&lt;MyAppDashboard&gt; {
 *     public MyAppDashboardService() { super(MyAppDashboard.class); }
 * }
 * </pre>
 * 
 * <p>
 * then register in <tt>isis.properties</tt> in the usual way.
 * 
 * <p>
 * However, if your application requires more flexibility (for example, returning different view models 
 * for different users), then do <i>not</i> subclass from this service; instead write your own service
 * with a no-arg action with the {@link HomePage} annotation.
 */
@Hidden
public abstract class AbstractHomePageDashboardService<T extends ViewModel> extends AbstractService {
    
    /**
     * The default value to use for the {@link #id}.
     */
    private static final String ID_DEFAULT = "dashboard";
    
    private final Class<T> viewModel;
    /**
     * Both the id of this service, and also the memento of the view model.
     */
    private final String id;

    public AbstractHomePageDashboardService(final Class<T> viewModel) {
        this(viewModel, ID_DEFAULT);
    }
    
    /**
     * @param viewModel - view model to instantiate and return. 
     * @param id- both the id of this service, and also the memento of the view model.
     */
    public AbstractHomePageDashboardService(final Class<T> viewModel, final String id) {
        this.viewModel = viewModel;
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public String iconName() {
        return id;
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @HomePage
    public T lookup() {
        return newViewModelInstance(viewModel, id);
    }

}
