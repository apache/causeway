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

package org.apache.isis.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides a mask that a value entry should conform to.
 * 
 * <p>
 * <b>NOTE</b>: this annotation has not been implemented in the Wicket viewer.
 * Since the Wicket viewer is the only currently released viewer, this annotation
 * has been marked as deprecated to flag up this fact.
 * 
 * <p>
 * The characters that can be used are shown in the following table (adapted
 * from masks used by Swing's MaskFormatter, Java's SimpleDateFormat and also
 * Microsoft's MaskedEdit control):
 * 
 * <table border='2'>
 * <tr>
 * <th align='center'>Character</th>
 * <th align='center'>Description</th>
 * <th align='center'>Source</th>
 * </tr>
 * <tr>
 * <td align='center'>#</td>
 * <td align='left'>Digit placeholder.</td>
 * <td align='left'>MS, Swing</td>
 * </tr>
 * <tr>
 * <td align='center'>.</td>
 * <td align='left'>Decimal placeholder. The actual character used is the one
 * specified as the decimal placeholder in your international settings. This
 * character is treated as a literal for masking purposes.</td>
 * <td align='left'>MS</td>
 * </tr>
 * <tr>
 * <td align='center'>,</td>
 * <td align='left'>Thousands separator. The actual character used is the one
 * specified as the thousands separator in your international settings. This
 * character is treated as a literal for masking purposes.</td>
 * <td align='left'>MS</td>
 * </tr>
 * <tr>
 * <td align='center'>:</td>
 * <td align='left'>Time separator. This character is treated as a literal for
 * masking purposes.</td>
 * <td align='left'>MS</td>
 * </tr>
 * <tr>
 * <td align='center'>/</td>
 * <td align='left'>Date separator. This character is treated as a literal for
 * masking purposes.</td>
 * <td align='left'>MS</td>
 * </tr>
 * <tr>
 * <td align='center'>&amp;</td>
 * <td align='left'>Character placeholder. Valid values for this placeholder are
 * ANSI characters in the following ranges: 32-126 and 128-255.</td>
 * <td align='left'>MS</td>
 * </tr>
 * 
 * <tr>
 * <td align='center'>A</td>
 * <td align='left'>Alphanumeric character placeholder (
 * <code>Character.isLetter</code> or <code>Character.isDigit</code>), with
 * entry required. For example: a ~ z, A ~ Z, or 0 ~ 9.</td>
 * <td align='left'>MS</td>
 * </tr>
 * 
 * <tr>
 * <td align='center'>a</td>
 * <td align='left'>Alphanumeric character placeholder (entry optional).</td>
 * <td align='left'>MS</td>
 * </tr>
 * <tr>
 * <td align='center'>9</td>
 * <td align='left'>Digit placeholder (entry optional). For example: 0 ~ 9.</td>
 * <td align='left'>MS</td>
 * </tr>
 * <tr>
 * <td align='center'>?</td>
 * <td align='left'>Letter placeholder (<code>Character.isLetter</code>). For
 * example: a ~ z or A ~ Z.</td>
 * <td align='left'>MS, Swing</td>
 * </tr>
 * <tr>
 * <td align='center'>U</td>
 * <td align='left'>Any character (<code>Character.isLetter</code>). All
 * lowercase letters are mapped to upper case.</td>
 * <td align='left'>Swing</td>
 * </tr>
 * <tr>
 * <td align='center'>L</td>
 * <td align='left'>Any character (<code>Character.isLetter</code>). All
 * lowercase letters are mapped to lower case.</td>
 * <td align='left'>Swing</td>
 * </tr>
 * <tr>
 * <td align='center'>Literal</td>
 * <td align='left'>All other symbols are displayed as literals; that is, as
 * themselves.</td>
 * <td align='left'>MS</td>
 * </tr>
 * </table>
 * 
 * <p>
 * Can also be specified for types that are annotated as <tt>@Value</tt> types.
 * To apply, the value must have string semantics.
 * 
 * <p>
 * Not yet implemented:
 * <table border='2'>
 * <tr>
 * <th align='center'>Character</th>
 * <th align='center'>Description</th>
 * <th align='center'>Source</th>
 * </tr>
 * <tr>
 * <td align='center'>\ or '</td>
 * <td align='left'>Treat the next character in the mask string as a literal.
 * This allows you to include the '#', '&', 'A', and '?' characters in the mask.
 * This character is treated as a literal for masking purposes.</td>
 * <td align='left'>MS (\), Swing (')</td>
 * </tr>
 * <tr>
 * <td align='center'>H</td>
 * <td align='left'>Character.isLetter or Character.isDigit.</td>
 * <td align='left'>Swing</td>
 * </tr>
 * <tr>
 * <td align='center'>yy or yyyy</td>
 * <td align='left'>Year, eg 1996; 96.</td>
 * <td align='left'>DateFormat</td>
 * </tr>
 * <tr>
 * <td align='center'>MM</td>
 * <td align='left'>Two digit representation of month, eg 07 for July.</td>
 * <td align='left'>DateFormat</td>
 * </tr>
 * <tr>
 * <td align='center'>MMM</td>
 * <td align='left'>Three character representation of month, eg <i>Jul</i> for
 * July.</td>
 * <td align='left'>DateFormat</td>
 * </tr>
 * <tr>
 * <td align='center'>d</td>
 * <td align='left'>Day in month, eg 3 or 28.</td>
 * <td align='left'>DateFormat</td>
 * </tr>
 * <tr>
 * <td align='center'>dd</td>
 * <td align='left'>Two digit representation of day in month, eg 03 or 28.</td>
 * <td align='left'>DateFormat</td>
 * </tr>
 * <tr>
 * <td align='center'>HH</td>
 * <td align='left'>Two digit representation of hour in day (24 hour clock), eg
 * 05 or 19.</td>
 * <td align='left'>DateFormat</td>
 * </tr>
 * <tr>
 * <td align='center'>mm</td>
 * <td align='left'>Minute in hour, eg 02 or 47.</td>
 * <td align='left'>DateFormat</td>
 * </tr>
 * <tr>
 * <td align='center'>ss</td>
 * <td align='left'>Second in minute in hour, eg 08 or 35.</td>
 * <td align='left'>DateFormat</td>
 * </tr>
 * </table>
 * 
 * @deprecated - not supported by the Wicket viewer
 */
@Deprecated
@Inherited
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Mask {
    String value();
}
