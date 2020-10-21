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
package org.apache.isis.viewer.wicket.ui.components.scalars.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.apache.wicket.markup.html.image.resource.RenderedDynamicImageResource;

import org.apache.isis.commons.internal.image._Images;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
final class JavaAwtImageDynamicResource extends RenderedDynamicImageResource {
    
    private static final long serialVersionUID = 1L;
    
    private transient BufferedImage bufferedImage = null;
    private byte[] imageData;
    
    public JavaAwtImageDynamicResource(@NonNull BufferedImage image) {
        super(image.getWidth(), image.getHeight());
        
        try {
            this.bufferedImage = image;    
            this.imageData = _Images.toBytes(image);      
        } catch (Exception e) {
            this.bufferedImage = null;
        }
    }

    @Override
    protected boolean render(final Graphics2D graphics, Attributes attributes) {
        graphics.drawImage(getBufferedImage(), 0, 0, null);
        return true;
    }
    
    // -- HELPER
    
    private BufferedImage getBufferedImage() {
        if(bufferedImage == null
                && imageData!=null) {
            
            try {
                bufferedImage = _Images.fromBytes(imageData);    
            } catch (Exception e) {
                log.error("failed to deserialize image from previously serilaized image data", e);
                this.bufferedImage = null;
                this.imageData=null; // so we don't hit this issue again
            }
            
        }
        return bufferedImage;
    }
}
