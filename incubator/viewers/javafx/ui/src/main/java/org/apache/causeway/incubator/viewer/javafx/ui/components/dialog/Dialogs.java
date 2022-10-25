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
package org.apache.causeway.incubator.viewer.javafx.ui.components.dialog;

import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Dialogs {

    public static void message(String title, String headerText, String contentText){
        val alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();
    }

    public static void message(String title, String headerText, String contentText, Node contentNode){
        val alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.getDialogPane().setExpandableContent(contentNode);
        alert.getDialogPane().setExpanded(true);

        alert.showAndWait();
    }

    public static void warning(String title, String headerText, String contentText){
        val alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();
    }

    public static void error(String title, String headerText, String contentText, Exception ex){
        val alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        // Create expandable Exception.
        val sw = new StringWriter();
        val pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        val exceptionText = sw.toString();

        val label = new Label("The exception stacktrace was:");

        val textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    /**
     *
     * @param title
     * @param headerText
     * @param contentText
     * @param confirmLabel eg. Yes
     * @param denyLabel eg. No
     * @return whether the dialog's question was confirmed
     */
    public static boolean confirm(
            String title,
            String headerText,
            String contentText,
            String confirmLabel,
            String denyLabel){

        val alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        val buttonTypeConfirm = new ButtonType(confirmLabel);
        val buttonTypeDeny = new ButtonType(denyLabel);

        alert.getButtonTypes().setAll(buttonTypeConfirm, buttonTypeDeny);

        ButtonType result = alert.showAndWait()
                .orElse(null);
        if (result == buttonTypeConfirm){
            // ... user chose OK
            return true;
        } else {
            // ... user chose CANCEL or closed the dialog
            return false;
        }

    }


}
