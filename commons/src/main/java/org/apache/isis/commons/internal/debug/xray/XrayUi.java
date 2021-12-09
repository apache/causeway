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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.debug.xray.XrayModel.HasIdAndLabel;
import org.apache.isis.commons.internal.debug.xray.XrayModel.Stickiness;

import lombok.RequiredArgsConstructor;
import lombok.val;

public class XrayUi extends JFrame {

    private static final long serialVersionUID = 1L;
    private final JTree tree;
    private final DefaultMutableTreeNode root;
    private final XrayModel xrayModel;

    private static XrayUi INSTANCE;

    private static AtomicBoolean startRequested = new AtomicBoolean();
    private static CountDownLatch latch = null;

    public static void start(final int defaultCloseOperation) {
        val alreadyRequested = startRequested.getAndSet(true);
        if(!alreadyRequested) {
            latch = new CountDownLatch(1);
            SwingUtilities.invokeLater(()->new XrayUi(defaultCloseOperation));
        }
    }

    public static void updateModel(final Consumer<XrayModel> consumer) {
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

    public static boolean isXrayEnabled() {
        return startRequested.get();
    }

    protected XrayUi(final int defaultCloseOperation) {

        //create the root node
        root = new DefaultMutableTreeNode("X-ray");

        xrayModel = new XrayModelSimple(root);

        //create the tree by passing in the root node
        tree = new JTree(root);

        tree.setShowsRootHandles(false);

        val detailPanel = layoutUIAndGetDetailPanel(tree);

        tree.getSelectionModel().addTreeSelectionListener((final TreeSelectionEvent e) -> {

            val selPath = e.getNewLeadSelectionPath();
            if(selPath==null) {
                return; // ignore event
            }
            val selectedNode = (DefaultMutableTreeNode) selPath.getLastPathComponent();
            val userObject = selectedNode.getUserObject();

            //detailPanel.removeAll();

            if(userObject instanceof XrayDataModel) {
                ((XrayDataModel) userObject).render(detailPanel);
            } else {
                val infoPanel = new JPanel();
                infoPanel.add(new JLabel("Details"));
                detailPanel.setViewportView(infoPanel);
            }

            detailPanel.revalidate();
            detailPanel.repaint();

            //System.out.println("selected: " + selectedNode.toString());
        });

        val popupMenu = new JPopupMenu();

        val clearThreadsAction = popupMenu.add(new JMenuItem("Clear Threads"));
        clearThreadsAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                doClearThreads();
            }
        });

        val callStackMergeAction = popupMenu.add(new JMenuItem("Merge Logged Call-Stack"));
        callStackMergeAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                doMergeCallStacksOnSelectedNodes();
            }
        });

        val deleteAction = popupMenu.add(new JMenuItem("Delete"));
        deleteAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                doRemoveSelectedNodes();
            }
        });

        tree.setCellRenderer(new XrayTreeCellRenderer(
                (DefaultTreeCellRenderer) tree.getCellRenderer(),
                iconCache));

        tree.addMouseListener(new MouseListener() {

            @Override public void mouseReleased(final MouseEvent e) {}
            @Override public void mousePressed(final MouseEvent e) {}
            @Override public void mouseExited(final MouseEvent e) {}
            @Override public void mouseEntered(final MouseEvent e) {}

            @Override
            public void mouseClicked(final MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        tree.addKeyListener(new KeyListener() {

            @Override public void keyReleased(final KeyEvent e) {}
            @Override public void keyTyped(final KeyEvent e) {}

            @Override
            public void keyPressed(final KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_DELETE) {
                    doRemoveSelectedNodes();
                    return;
                }
                if(e.getKeyCode() == KeyEvent.VK_F5) {
                    doClearThreads();
                    return;
                }
            }

        });

        // report key bindings to the UI
        {
            val root = xrayModel.getRootNode();
            val env = xrayModel.addDataNode(root,
                    new XrayDataModel.KeyValue("isis-xray-keys", "X-ray Keybindings", Stickiness.CANNOT_DELETE_NODE));
            env.getData().put("F5", "Clear Threads");
            env.getData().put("DELETE", "Delete Selected Nodes");
        }


        this.setDefaultCloseOperation(defaultCloseOperation);
        this.setTitle("X-ray Viewer (Apache Isis™)");
        this.pack();
        this.setSize(800, 600);

        this.setLocationRelativeTo(null);
        this.setVisible(true);

        INSTANCE = this;

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                latch.countDown();
            }
        });
    }

    private Stream<DefaultMutableTreeNode> streamSelectedNodes() {
        return Can.ofArray(tree.getSelectionModel().getSelectionPaths())
                .stream()
                .map(path->(DefaultMutableTreeNode)path.getLastPathComponent());
    }

    private Stream<DefaultMutableTreeNode> streamChildrenOf(final DefaultMutableTreeNode node) {
        return IntStream.range(0, node.getChildCount())
        .mapToObj(root::getChildAt)
        .map(DefaultMutableTreeNode.class::cast);
    }

    private Optional<HasIdAndLabel> extractUserObject(final DefaultMutableTreeNode node) {
        return _Casts.castTo(HasIdAndLabel.class, node.getUserObject());
    }

    private boolean canRemoveNode(final DefaultMutableTreeNode node) {
        if(node.getParent()==null) {
            return false; // don't remove root
        }
        return extractUserObject(node)
        .map(HasIdAndLabel::getStickiness)
        .map(stickiness->stickiness.isCanDeleteNode())
        .orElse(true); // default: allow removal
    }

    private void removeNode(final DefaultMutableTreeNode nodeToBeRemoved) {
        if(canRemoveNode(nodeToBeRemoved)) {
            ((DefaultTreeModel)tree.getModel()).removeNodeFromParent(nodeToBeRemoved);
            xrayModel.remove(nodeToBeRemoved);
        }
    }

    private void doClearThreads(){
        val root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        val threadNodes = streamChildrenOf(root)
        .filter(node->extractUserObject(node)
                .map(HasIdAndLabel::getId)
                .map(id->id.startsWith("thread-"))
                .orElse(false))
        .collect(Can.toCan()); // collect into can, before processing (otherwise concurrent modification)

        threadNodes.forEach(this::removeNode);
    }

    private void doRemoveSelectedNodes() {
        streamSelectedNodes().forEach(this::removeNode);
    }

    private void doMergeCallStacksOnSelectedNodes() {
        val logEntries = streamSelectedNodes()
        .filter(node->node.getUserObject() instanceof XrayDataModel.LogEntry)
        .map(node->(XrayDataModel.LogEntry)node.getUserObject())
        .collect(Can.toCan());

        if(!logEntries.getCardinality().isMultiple()) {
            System.err.println("must select at least 2 logs for merging");
            return;
        }

        val callStackMerger = new _CallStackMerger(logEntries);

        JFrame frame = new JFrame("Merged Log View");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(true);
//        val canvas = _SwingUtil.canvas(g->{
//            g.setColor(Color.GRAY);
//            g.fill(g.getClip());
//            callStackMerger.render(g);
//        });
//        JScrollPane scroller = new JScrollPane(canvas);

        //Create a text area.
        JTextArea textArea = new JTextArea("no content");
        textArea.setFont(new Font("Serif", Font.PLAIN, 16));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scroller = new JScrollPane(textArea);
        callStackMerger.render(textArea);

        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scroller);
        frame.getContentPane().add(BorderLayout.CENTER, panel);
        frame.setPreferredSize(new Dimension(800, 600));
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        frame.setResizable(true);
        frame.setVisible(true);
    }

    private JScrollPane layoutUIAndGetDetailPanel(final JTree masterTree) {

        JScrollPane masterScrollPane = new JScrollPane(masterTree);
        JScrollPane detailScrollPane = new JScrollPane();

        //Create a split pane with the two scroll panes in it.
        val splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                   masterScrollPane, detailScrollPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(260);

        //Provide minimum sizes for the two components in the split pane.
        Dimension minimumSize = new Dimension(100, 50);
        masterScrollPane.setMinimumSize(minimumSize);
        detailScrollPane.setMinimumSize(minimumSize);

        detailScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        detailScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        detailScrollPane.getVerticalScrollBar().setUnitIncrement(8);

        //Provide a preferred size for the split pane.
        splitPane.setPreferredSize(new Dimension(800, 600));

        getContentPane().add(splitPane);

        return detailScrollPane;
    }

    // -- CUSTOM TREE NODE ICONS

    private final Map<String, Optional<ImageIcon>> iconCache = _Maps.newConcurrentHashMap();

    @RequiredArgsConstructor
    class XrayTreeCellRenderer implements TreeCellRenderer {

        final DefaultTreeCellRenderer delegate;
        final Map<String, Optional<ImageIcon>> iconCache;

        @Override
        public Component getTreeCellRendererComponent(
                final JTree tree,
                final Object value,
                final boolean selected,
                final boolean expanded,
                final boolean leaf,
                final int row,
                final boolean hasFocus) {

            val label = (DefaultTreeCellRenderer)
                    delegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            Object o = ((DefaultMutableTreeNode) value).getUserObject();
            if (o instanceof XrayDataModel) {
                XrayDataModel dataModel = (XrayDataModel) o;
                val imageIcon = iconCache.computeIfAbsent(dataModel.getIconResource(), iconResource->{
                    URL imageUrl = getClass().getResource(dataModel.getIconResource());
                    return Optional.ofNullable(imageUrl)
                            .map(ImageIcon::new);
                });
                imageIcon.ifPresent(label::setIcon);
                label.setText(dataModel.getLabel());
            }
            return label;
        }
    }



}
