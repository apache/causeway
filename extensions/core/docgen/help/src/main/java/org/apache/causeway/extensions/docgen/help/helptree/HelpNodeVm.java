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
package org.apache.causeway.extensions.docgen.help.helptree;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Navigable;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.graph.tree.TreeNode;
import org.apache.causeway.applib.graph.tree.TreePath;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.extensions.docgen.help.CausewayModuleExtDocgenHelp;
import org.apache.causeway.extensions.docgen.help.applib.HelpNode;
import org.apache.causeway.extensions.docgen.help.applib.HelpNode.HelpTopic;
import org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Named(CausewayModuleExtDocgenHelp.NAMESPACE + ".HelpNodeVm")
@DomainObject(
        nature=Nature.VIEW_MODEL)
@DomainObjectLayout(
        named = "Application Help")
@Log4j2
public class HelpNodeVm implements ViewModel {

    // no longer required to be URL-safe (but nicer if does not get encoded)
    public final static String PATH_DELIMITER = ".";

    public static HelpNodeVm forRootTopic(final HelpTopic rootTopic) {
        return new HelpNodeVm(rootTopic, rootTopic);
    }
    
    @Inject FactoryService factoryService;

    @Getter @Programmatic
    private final HelpTopic rootTopic;

    @Getter @Programmatic
    private final HelpNode helpNode;

    @Inject
    public HelpNodeVm(final HelpTopic rootTopic, final String rootPathMemento) {
        this(rootTopic, TreePath.parse(rootPathMemento, PATH_DELIMITER));
    }

    HelpNodeVm(final HelpTopic rootTopic, final TreePath treePath) {
        this(rootTopic, rootTopic
                .lookup(treePath)
                .orElseGet(()->{
                    log.warn("could not resolve help node {}", treePath);
                    return rootTopic;
                }));
    }

    HelpNodeVm(final HelpTopic rootTopic, final HelpNode helpNode) {
        this.rootTopic = rootTopic;
        this.helpNode = helpNode;
    }

    @ObjectSupport public String title() {
        return helpNode.getTitle();
    }

    @ObjectSupport public String iconName() {
        var type = helpNode.getHelpNodeType();
        return type!=null ? type.name() : "";
    }

    @Property
    @PropertyLayout(labelPosition = LabelPosition.NONE, fieldSetId = "tree", sequence = "1")
    public TreeNode<HelpNodeVm> getTree() {
        final TreeNode<HelpNodeVm> tree = TreeNode.root(
                HelpNodeVm.forRootTopic(rootTopic), HelpTreeAdapter.class, factoryService);

        // expand the current node
        helpNode.getPath().streamUpTheHierarchyStartingAtSelf()
            .forEach(tree::expand);

        return tree;
    }

    @Property
    @PropertyLayout(navigable=Navigable.PARENT, hidden=Where.EVERYWHERE, fieldSetId = "detail", sequence = "1")
    public HelpNodeVm getParent() {
        return Optional.ofNullable(helpNode.getPath().getParentIfAny())
                .map(parentPath->new HelpNodeVm(rootTopic, parentPath.toString()))
                .orElse(null);
    }

    @Property
    @PropertyLayout(labelPosition = LabelPosition.NONE, fieldSetId = "detail", sequence = "2")
    @Getter(lazy = true)
    private final AsciiDoc helpContent = helpNode.getContent();

    @Override
    public String viewModelMemento() {
        return helpNode.getPath().stringify(PATH_DELIMITER);
    }

}
