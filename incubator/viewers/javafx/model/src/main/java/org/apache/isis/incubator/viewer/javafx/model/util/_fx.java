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
package org.apache.isis.incubator.viewer.javafx.model.util;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

@UtilityClass
public final class _fx {

    // -- OBSERVABLES

    public static ObservableValue<String> newStringReadonly(String value) {
        val string = new ReadOnlyStringWrapper(value);
        return string;
    }

    // -- COMPONENT FACTORIES
    
    public static <T extends Node> T add(Pane container, T component) {
        container.getChildren().add(component);
        return component;
    }

    public static Label newLabel(Pane container, String label) {
        val component = new Label(label);
        container.getChildren().add(component);
        return component;
    }
    
    public static Button newButton(Pane container, String label, EventHandler<ActionEvent> eventHandler) {
        val component = new Button(label);
        container.getChildren().add(component);
        component.setOnAction(eventHandler);
        return component;
    }
    
    public static HBox newHBox(Pane container) {
        val component = new HBox();
        container.getChildren().add(component);
        _fx.padding(component, 4, 8);
        _fx.borderDashed(component, Color.BLUE); // debug
        return component;
    }

    public static VBox newVBox(Pane container) {
        val component = new VBox();
        container.getChildren().add(component);
        _fx.padding(component, 4, 8);
        _fx.borderDashed(component, Color.GREEN); // debug
        return component;
    }
    
    public static VBox newVBox(TitledPane container) {
        val component = new VBox();
        container.setContent(component);
        return component;
    }
    
    public static FlowPane newFlowPane(Pane container) {
        val component = new FlowPane();
        container.getChildren().add(component);
        //component.prefWrapLengthProperty().bind(container.widthProperty());
        _fx.padding(component, 4, 8);
        _fx.borderDashed(component, Color.RED); // debug
        return component;
    }

    public static GridPane newGrid(Pane container) {
        val component = new GridPane();
        container.getChildren().add(component);
        return component;
    }
    
    public static GridPane newGrid(TitledPane container) {
        val component = new GridPane();
        container.setContent(component);
        return component;
    }
    
    public static <T extends Node> T addGridCell(GridPane container, T cellNode, int column, int row) {
        container.add(cellNode, column, row);
        return cellNode;
    }

    public static TabPane newTabGroup(Pane container) {
        val component = new TabPane();
        container.getChildren().add(component);
        return component;
    }

    public static Tab newTab(TabPane container, String label) {
        val component = new Tab(label);
        container.getTabs().add(component);
        return component;
    }
    
    public static Accordion newAccordion(Pane container) {
        val component = new Accordion();
        container.getChildren().add(component);
        return component;
    }

    public static TitledPane newTitledPane(Accordion container, String label) {
        
        val component = new TitledPane();
        container.getPanes().add(component);
        component.setText(label);
        component.setAnimated(true);
        return component;
    }
    

    /**
     * @param <S> The type of the TableView generic type (i.e. S == TableView&lt;S&gt;)
     * @param <T> The type of the content in all cells in this TableColumn.
     * @param tableView
     * @param columnLabel
     * @param columnType
     * @return a new column as added to the {@code tableView}
     */
    public static <S, T> TableColumn<S, T> newColumn(TableView<S> tableView, String columnLabel, Class<T> columnType) {
        val column = new TableColumn<S, T>(columnLabel);
        tableView.getColumns().add(column);
        return column;
    }

    // -- ICONS
    
    public static Image imageFromClassPath(@NonNull Class<?> cls, String resourceName) {
        return new Image(cls.getResourceAsStream(resourceName));
    }
    
    public static ImageView iconForImage(Image image, int width, int height) {
        val icon = new ImageView(image);
        icon.setFitWidth(width);
        icon.setFitHeight(height);
        return icon;
    }
    
    // -- LAYOUTS
    
    public static void padding(Region region, int hPadding, int vPadding) {
        region.setPadding(new Insets(vPadding, hPadding, vPadding, hPadding));
    }
    
//    public static GridPane formLayout(GridPane component) {
//        component.setAlignment(Pos.CENTER);
//        component.setHgap(10);
//        component.setVgap(10);
//        component.setPadding(new Insets(25, 25, 25, 25));    
//        return component;
//    }
    
    public static void toolbarLayout(FlowPane component) {
        component.setPadding(new Insets(15, 12, 15, 12));
        component.setHgap(10);
        component.setVgap(10);
        //component.setSpacing(10);
    }
    
    // -- STYLES
    
    public static <T extends Labeled> T h1(T component) {
        component.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        return component;
    }
    
    public static <T extends Labeled> T h2(T component) {
        component.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        return component;
    }
    
    public static <T extends Labeled> T h3(T component) {
        component.setFont(Font.font("Verdana", FontWeight.NORMAL, 17));
        return component;
    }

    public static <T extends Region> T backround(T region, Color color) {
        region.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
        return region;
    }
    
    public static <T extends Region> T borderDashed(T region, Color color) {
        region.setBorder(new Border(new BorderStroke(
                color, 
                BorderStrokeStyle.DASHED, 
                new CornerRadii(3), 
                new BorderWidths(1))));
        return region;
    }
    

}
