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
package org.apache.causeway.applib.layout.menubars.bootstrap;

import java.util.List;
import java.util.function.Consumer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.layout.component.ServiceActionLayoutData;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

/**
 * Describes the collection of domain services into menubars, broadly corresponding to the aggregation of information within {@link org.apache.causeway.applib.annotation.DomainServiceLayout}.
 *
 * @since 1.x {@index}
 */
@XmlRootElement(
        name = "menuBars"
        )
@XmlType(
        name = "menuBars"
        , propOrder = {
                "primary",
                "secondary",
                "tertiary",
                "metadataError"
        }
        )
public class BSMenuBars extends org.apache.causeway.applib.layout.menubars.MenuBarsAbstract {

    private static final long serialVersionUID = 1L;

    public BSMenuBars() {
    }

    private BSMenuBar primary = new BSMenuBar();

    public BSMenuBar getPrimary() {
        return primary;
    }

    public void setPrimary(final BSMenuBar primary) {
        this.primary = primary;
    }

    private BSMenuBar secondary = new BSMenuBar();

    public BSMenuBar getSecondary() {
        return secondary;
    }

    public void setSecondary(final BSMenuBar secondary) {
        this.secondary = secondary;
    }

    private BSMenuBar tertiary = new BSMenuBar();

    public BSMenuBar getTertiary() {
        return tertiary;
    }

    public void setTertiary(final BSMenuBar tertiary) {
        this.tertiary = tertiary;
    }

    @Override
    public BSMenuBar menuBarFor(final DomainServiceLayout.MenuBar menuBar) {
        switch (menuBar) {
        case PRIMARY:
            return getPrimary();
        case SECONDARY:
            return getSecondary();
        case TERTIARY:
            return getTertiary();
        case NOT_SPECIFIED:
            break;
        default:
            throw _Exceptions.unmatchedCase(menuBar);
        }
        return null;
    }

    public interface Visitor extends Consumer<ServiceActionLayoutData> {
        void preVisit(final BSMenuBar menuBar);
        void visit(final BSMenuBar menuBar);
        void postVisit(final BSMenuBar menuBar);

        void preVisit(final BSMenu menu);
        void visit(final BSMenu menu);
        void postVisit(final BSMenu menu);

        void preVisit(final BSMenuSection menuSection);
        void visit(final BSMenuSection section);
        void postVisit(final BSMenuSection menuSection);
    }

    public static class VisitorAdapter implements BSMenuBars.Visitor {
        @Override public void preVisit(final BSMenuBar menuBar) { }
        @Override public void visit(final BSMenuBar menuBar) { }
        @Override public void postVisit(final BSMenuBar menuBar) { }

        @Override public void preVisit(final BSMenu menu) { }
        @Override public void visit(final BSMenu menu) { }
        @Override public void postVisit(final BSMenu menu) { }

        @Override public void preVisit(final BSMenuSection menuSection) { }
        @Override public void visit(final BSMenuSection section) { }
        @Override public void postVisit(final BSMenuSection menuSection) { }

        @Override public void accept(final ServiceActionLayoutData serviceActionLayoutData) { }

        // -- PREDEFINED SHORTCUTS

        public static VisitorAdapter visitingMenuSections(Consumer<BSMenuSection> onVisit) {
            return new VisitorAdapter() {
                @Override public void visit(final BSMenuSection section) {
                    onVisit.accept(section);
                }
            };
        }

        public static VisitorAdapter visitingMenus(Consumer<BSMenu> onVisit) {
            return new VisitorAdapter() {
                @Override public void visit(final BSMenu menu) {
                    onVisit.accept(menu);
                }
            };
        }

    }

    @Override
    public void visit(final Consumer<ServiceActionLayoutData> visitor) {
        traverseMenuBar(getPrimary(), visitor);
        traverseMenuBar(getSecondary(), visitor);
        traverseMenuBar(getTertiary(), visitor);
    }

    private void traverseMenuBar(final BSMenuBar menuBar, final Consumer<ServiceActionLayoutData> visitor) {

        final Visitor bsVisitor = visitor instanceof Visitor ? (Visitor) visitor : null;

        if(bsVisitor != null) {
            bsVisitor.preVisit(menuBar);
            bsVisitor.visit(menuBar);
        }

        for (BSMenu menu : menuBar.getMenus()) {
            traverseMenu(menu, visitor);
        }

        if(bsVisitor != null) {
            bsVisitor.postVisit(menuBar);
        }
    }

    private void traverseMenu(final BSMenu menu, final Consumer<ServiceActionLayoutData> visitor) {

        final Visitor bsVisitor = visitor instanceof Visitor ? (Visitor) visitor : null;

        if(bsVisitor != null) {
            bsVisitor.preVisit(menu);
            bsVisitor.visit(menu);
        }

        final List<BSMenuSection> sections = menu.getSections();
        for (BSMenuSection section : sections) {
            traverseSection(section, visitor);
        }

        if(bsVisitor != null) {
            bsVisitor.postVisit(menu);
        }
    }

    private void traverseSection(final BSMenuSection section, final Consumer<ServiceActionLayoutData> visitor) {

        final Visitor bsVisitor = visitor instanceof Visitor ? (Visitor) visitor : null;
        if(bsVisitor != null) {
            bsVisitor.preVisit(section);
            bsVisitor.visit(section);
        }

        final List<ServiceActionLayoutData> actions = section.getServiceActions();
        for (ServiceActionLayoutData action : actions) {
            visitor.accept(action);
        }

        if(bsVisitor != null) {
            bsVisitor.postVisit(section);
        }
    }

    private String metadataError;

    /**
     * For diagnostics; populated by the framework if and only if a metadata error.
     */
    @XmlElement(required = false)
    public String getMetadataError() {
        return metadataError;
    }

    public void setMetadataError(final String metadataError) {
        this.metadataError = metadataError;
    }

}
