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
package demoapp.dom.types.javaawt.images.holder;

import java.awt.image.BufferedImage;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.PromptStyle;
import org.apache.isis.applib.annotations.SemanticsOf;
import org.apache.isis.applib.annotations.Where;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Action(
        semantics = SemanticsOf.IDEMPOTENT,
        hidden = Where.EVERYWHERE   // TODO: action parameters for images not yet supported.
)
@ActionLayout(
        promptStyle = PromptStyle.INLINE
        , named = "Update"
        , associateWith = "readOnlyProperty"
        , sequence = "1")
@RequiredArgsConstructor
public class JavaAwtBufferedImageHolder_updateReadOnlyProperty {

    private final JavaAwtBufferedImageHolder holder;

    @MemberSupport public JavaAwtBufferedImageHolder act(final BufferedImage newValue) {
        holder.setReadOnlyProperty(newValue);
        return holder;
    }

    @MemberSupport public BufferedImage default0Act() {
        return holder.getReadOnlyProperty();
    }

}
//end::class[]
