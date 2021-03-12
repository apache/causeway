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
package org.apache.isis.commons.internal.debug.xray;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import lombok.val;

public class XrayUi extends JFrame {

    private static final long serialVersionUID = 1L;
    private final JTree tree;
    private final DefaultMutableTreeNode root;
    private final XrayModel xrayModel;

    private static XrayUi INSTANCE;

    private static AtomicBoolean startRequested = new AtomicBoolean();
    private static CountDownLatch latch = null;

    public static void start() {
        val alreadyRequested = startRequested.getAndSet(true);
        if(!alreadyRequested) {
            latch = new CountDownLatch(1);
            SwingUtilities.invokeLater(XrayUi::new);    
        }
    }

    public static void updateModel(Consumer<XrayModel> consumer) {
        if(startRequested.get()) {
            SwingUtilities.invokeLater(()->{
                consumer.accept(INSTANCE.xrayModel);
                ((DefaultTreeModel)INSTANCE.tree.getModel()).reload();
                _SwingUtil.setTreeExpandedState(INSTANCE.tree, true);
            });
        }
    }

    public static void waitForShutdown() {
        if(latch==null
                || INSTANCE == null) {
            return;
        }
        System.err.println("Waiting for XrayUi to shut down...");
        try {
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected XrayUi() {

        //create the root node
        root = new DefaultMutableTreeNode("X-ray");
        
        xrayModel = new XrayModelSimple(root);

        //create the tree by passing in the root node
        tree = new JTree(root);

        tree.setShowsRootHandles(false);
        
        val detailPanel = layoutUIAndGetDetailPanel(tree);
        
        tree.getSelectionModel().addTreeSelectionListener((TreeSelectionEvent e) -> {
            val selectedNode = (DefaultMutableTreeNode) e.getNewLeadSelectionPath().getLastPathComponent();
            val userObject = selectedNode.getUserObject();
            
            detailPanel.removeAll();
            
            if(userObject instanceof XrayDataModel) {
                ((XrayDataModel) userObject).render(detailPanel);
            } else {
                detailPanel.add(new JLabel("Details"));
            }
            
            detailPanel.doLayout();
            detailPanel.repaint();
            
            //System.out.println("selected: " + selectedNode.toString());
        });
        
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setTitle("X-ray Viewer");
        this.pack();
        this.setSize(800, 600);
        
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        INSTANCE = this;

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                latch.countDown();
            }
        });
    }

    private JPanel layoutUIAndGetDetailPanel(JTree masterTree) {
        
        val detailPanel = new JPanel();
        
        JScrollPane masterScrollPane = new JScrollPane(masterTree);
        JScrollPane detailScrollPane = new JScrollPane(detailPanel);
        
        //Create a split pane with the two scroll panes in it.
        val splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                   masterScrollPane, detailScrollPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(150);
 
        //Provide minimum sizes for the two components in the split pane.
        Dimension minimumSize = new Dimension(100, 50);
        masterScrollPane.setMinimumSize(minimumSize);
        detailScrollPane.setMinimumSize(minimumSize);
 
        //Provide a preferred size for the split pane.
        splitPane.setPreferredSize(new Dimension(400, 200));
        
        getContentPane().add(splitPane);
        
        return detailPanel;
    }
    

    
}
