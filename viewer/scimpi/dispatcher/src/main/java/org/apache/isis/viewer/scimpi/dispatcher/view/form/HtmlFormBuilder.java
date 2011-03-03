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


package org.apache.isis.viewer.scimpi.dispatcher.view.form;

import org.apache.isis.core.commons.exceptions.UnknownTypeException;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.view.HelpLink;


public class HtmlFormBuilder {

    public static void createForm(
            Request request,
            String action,
            HiddenInputField[] hiddenFields,
            InputField[] fields,
            String className, 
            String id,
            String formTitle,
            String description,
            String helpReference,
            String buttonTitle,
            String errors) {

        String classSegment = " class=\"" + className + (id == null ? "\"" : "\" id=\"" + id + "\"");
        request.appendHtml("<form " + classSegment + " action=\"" + action + "\" method=\"post\" accept-charset=\"ISO-8859-1\">\n");
        if (formTitle != null && formTitle.trim().length() > 0) {
            classSegment = " class=\"title\"";
            request.appendHtml("<div" + classSegment  +">" + formTitle + "</div>\n");
        }
        
        // TODO reinstate fieldsets when we can specify them
        //request.appendHtml("<fieldset>\n");

        String cls = "errors";
        if (errors != null) {
            request.appendHtml("<div class=\"" + cls + "\">" + errors + "</div>");
        }
        for (int i = 0; i < hiddenFields.length; i++) {
            HiddenInputField hiddenField = hiddenFields[i];
            if (hiddenField == null) {
                continue;
            }
            request.appendHtml("  <input type=\"hidden\" name=\"" + hiddenField.getName() + "\" value=\"" + hiddenField.getValue()
                    + "\" />\n");
        }
        request.appendHtml(request.getContext().interactionFields());
        for (int i = 0; i < fields.length; i++) {
            InputField fld = fields[i];
            if (fld.isHidden()) {
                request.appendHtml("  <input type=\"hidden\" name=\"" + fld.getName() + "\" value=\"" + fld.getValue()
                        + "\" />\n");
            } else {
                String errorSegment = fld.getErrorText() == null ? "" : "<span class=\"error\">" + fld.getErrorText() + "</span>";
                String fieldSegment = createField(fld);
                String helpSegment = HelpLink.createHelpSegment(fld.getDescription(), fld.getHelpReference());
                String title = fld.getDescription().equals("") ? "" : " title=\"" + fld.getDescription() + "\"";
                request.appendHtml("  <div class=\"field\"><label" + title + ">" + fld.getLabel() + ":</label>" + fieldSegment
                        + errorSegment + helpSegment + "</div>\n");
            }
        }
        
        request.appendHtml("  <input class=\"button\" type=\"submit\" value=\"" + buttonTitle + "\" name=\"execute\" />\n");
        HelpLink.append(request, description, helpReference);
        // TODO reinstate fieldsets when we can specify them
        //request.appendHtml("</fieldset>\n");
        request.appendHtml("</form>\n");
    }

    private static String createField(InputField field) {
        if (field.isHidden()) {
            if (field.getType() == InputField.REFERENCE) {
                return createObjectField(field, "hidden");
            } else {
                return "";
            }
        } else {
            if (field.getType() == InputField.HTML)  {
                return field.getHtml();
            } else if (field.getOptionsText() != null) {
                return createOptions(field);
            } else if (field.getType() == InputField.REFERENCE) {
                return createObjectField(field, "text");
            } else if (field.getType() == InputField.CHECKBOX) {
                return createCheckbox(field);
            } else if (field.getType() == InputField.PASSWORD) {
                return createPasswordField(field);
            } else if (field.getType() == InputField.TEXT) {
                if (field.getHeight() > 1) {
                    return createTextArea(field);
                } else {
                    return createTextField(field);
                }
            } else {
                throw new UnknownTypeException(field.toString());
            }
        }
    }

    private static String createObjectField(InputField field, String type) {
        String value = field.getValue();
        String valueSegment = value == null ? "" : " value=\"" + value + "\"";
        return field.getHtml() + "\n  <input type=\"hidden\" name=\"" + field.getName() + "\"" + valueSegment + " />";
    }

    private static String createTextArea(InputField field) {
        String columnsSegment = field.getWidth() == 0 ? "" : " cols=\"" + field.getWidth() / field.getHeight() + "\"";
        String rowsSegment = field.getHeight() == 0 ? "" : " rows=\"" + field.getHeight() + "\"";
        String wrapSegment = !field.isWrapped() ? "" : " wrap=\"off\"";
        String requiredSegment = !field.isRequired() ? "" : " <span class=\"required\">*</span>";
        String disabled = field.isEditable() ? "" : " disabled=\"disabled\"";
        String maxLength = field.getMaxLength() == 0 ? "" : " rows=\"" + field.getMaxLength() + "\"";
        return "<textarea name=\"" + field.getName() + "\"" + columnsSegment + rowsSegment + wrapSegment + maxLength + disabled + ">"
                + field.getValue() + "</textarea>" + requiredSegment;
    }

    private static String createPasswordField(InputField field) {
        return createTextField(field, "password");
    }

    private static String createTextField(InputField field) {
        return createTextField(field, "text");
    }
    
    private static String createTextField(InputField field, String type) {
        String value = field.getValue();
        String valueSegment = value == null ? "" : " value=\"" + value + "\"";
        String lengthSegment = field.getWidth() == 0 ? "" : " size=\"" + field.getWidth() + "\"";
        String maxLengthSegment = field.getMaxLength() == 0 ? "" : " maxlength=\"" + field.getMaxLength() + "\"";
        String requiredSegment = !field.isRequired() ? "" : " <span class=\"required\">*</span>";
        String disabled = field.isEditable() ? "" : " disabled=\"disabled\"";
        return "<input type=\"" + type + "\" name=\"" + field.getName() + "\"" + valueSegment + lengthSegment + 
            maxLengthSegment + disabled + " />" + requiredSegment;
    }

    private static String createCheckbox(InputField field) {
        String entryText = field.getValue();
        String valueSegment = entryText != null && entryText.toLowerCase().equals("true") ? " checked=\"checked\"" : "";
        String disabled = field.isEditable() ? "" : " disabled=\"disabled\"";
        return "<input type=\"checkbox\" name=\"" + field.getName() + "\" value=\"true\" " + valueSegment + disabled + " />";
    }

    private static String createOptions(InputField field) {
        StringBuffer str = new StringBuffer();
        String disabled = field.isEditable() ? "" : " disabled=\"disabled\"";
        str.append("\n  <select name=\"" + field.getName() + "\"" + disabled + ">\n");
        String[] options = field.getOptionsText();
        String[] ids = field.getOptionValues();
        int length = options.length;
        boolean offerOther = false;
        for (int i = 0; i < length; i++) {
            String selectedSegment = field.getValue() == null || options[i].equals(field.getValue()) ? " selected=\"selected\"" : "";
            if (field.getType() == InputField.TEXT &&  options[i].equals("__other")) {
                offerOther = true;
            } else {
                str.append("    <option value=\"" + ids[i] + "\"" + selectedSegment + ">" + options[i] + "</option>\n");
            }
        }
        if (offerOther) {
            str.append("    <option value=\"-OTHER-\">Other:</option>\n");
        }
        str.append("  </select>");
        if (field.getType()  == InputField.TEXT) {
            String lengthSegment = field.getWidth() == 0 ? "" : " size=\"" + field.getWidth() + "\"";
            String hideSegment = " style=\"display: none;\" "; // TODO only hide when JS enabled
            str.append("  <input type=\"text\" name=\"" + field.getName() + "-other\"" + hideSegment + lengthSegment + disabled + " />");
        }
        str.append("\n");
        return str.toString();
    }

}

