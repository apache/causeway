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
package app;

import java.util.List;

import org.apache.isis.applib.AbstractViewModel;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Render;
import org.apache.isis.applib.annotation.Render.Type;

public class ToDoAppDashboard extends AbstractViewModel {

    public String title() {
        return "Dashboard";
    }
    
    public String iconName() {
        return "Dashboard";
    }
    
    // //////////////////////////////////////
    // ViewModel contract
    // //////////////////////////////////////

    private String memento;
    
    @Override
    public String viewModelMemento() {
        return memento;
    }

    @Override
    public void viewModelInit(String memento) {
        this.memento = memento;
    }


    // //////////////////////////////////////
    // getAnalysisByCategory
    // //////////////////////////////////////

    @Named("By Category")
    @Render(Type.EAGERLY)
    @Disabled
    public List<ToDoItemsByCategoryViewModel> getAnalysisByCategory() {
        return toDoItemAnalysis.toDoItemsByCategory();
    }
    
    // //////////////////////////////////////
    // getAnalysisByDateRange
    // //////////////////////////////////////
    
    @Named("By Date Range")
    @Render(Type.EAGERLY)
    @Disabled
    public List<ToDoItemsByDateRangeViewModel> getAnalysisByDateRange() {
        return toDoItemAnalysis.toDoItemsByDateRange();
    }
    

    // //////////////////////////////////////
    // injected services
    // //////////////////////////////////////
    
    @javax.inject.Inject
    private ToDoItemAnalysis toDoItemAnalysis;

}
