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
package org.apache.causeway.extensions.tabular.pdf.factory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class FontUtils {

    private record FontMetrics(
            float height,
            float ascent,
            float descent) {
    }

    /**
     * {@link HashMap} for caching {@link FontMetrics} for designated
     * {@link PDFont} because {@link FontUtils#getHeight(PDFont, float)} is
     * expensive to calculate and the results are only approximate.
     */
    private static final Map<String, FontMetrics> fontMetrics = new HashMap<>();

    private static final Map<String, PDFont> defaultFonts = new HashMap<>();

    /**
     * Loads the {@link PDType0Font} to be embedded in the specified
     * {@link PDDocument}.
     * @param document
     *            {@link PDDocument} where fonts will be loaded
     * @param fontPath
     *            font path which will be loaded
     * @return The read {@link PDType0Font}
     */
    public static final PDType0Font loadFont(final PDDocument document, final String fontPath) {
        try {
            return PDType0Font.load(document, FontUtils.class.getClassLoader().getResourceAsStream(fontPath));
        } catch (IOException e) {
            log.warn("Cannot load given external font", e);
            return null;
        }
    }

    /**
     * Retrieving {@link String} width depending on current font size. The width
     * of the string in 1/1000 units of text space.
     * @param font
     *            The font of text whose width will be retrieved
     * @param text
     *            The text whose width will be retrieved
     * @param fontSize
     *            The font size of text whose width will be retrieved
     * @return text width
     */
    public static float getStringWidth(final PDFont font, final String text, final float fontSize) {
        try {
            return font.getStringWidth(text) / 1000 * fontSize;
        } catch (final IOException e) {
            // turn into runtime exception
            throw new IllegalStateException("Unable to determine text width", e);
        }
    }

    /**
     * Calculate the font ascent distance.
     * @param font
     *            The font from which calculation will be applied
     * @param fontSize
     *            The font size from which calculation will be applied
     * @return Positive font ascent distance
     */
    public static float getAscent(final PDFont font, final float fontSize) {
        final String fontName = font.getName();
        if (!fontMetrics.containsKey(fontName)) {
            createFontMetrics(font);
        }

        return fontMetrics.get(fontName).ascent * fontSize;
    }

    /**
     * Calculate the font descent distance.
     * @param font
     *            The font from which calculation will be applied
     * @param fontSize
     *            The font size from which calculation will be applied
     * @return Negative font descent distance
     */
    public static float getDescent(final PDFont font, final float fontSize) {
        final String fontName = font.getName();
        if (!fontMetrics.containsKey(fontName)) {
            createFontMetrics(font);
        }

        return fontMetrics.get(fontName).descent * fontSize;
    }

    /**
     * Calculate the font height.
     * @param font
     *            {@link PDFont} from which the height will be calculated.
     * @param fontSize
     *            font size for current {@link PDFont}.
     * @return {@link PDFont}'s height
     */
    public static float getHeight(final PDFont font, final float fontSize) {
        final String fontName = font.getName();
        if (!fontMetrics.containsKey(fontName)) {
            createFontMetrics(font);
        }

        return fontMetrics.get(fontName).height * fontSize;
    }

    /**
     * Create basic {@link FontMetrics} for current font.
     * @param font
     *            The font from which calculation will be applied <<<<<<< HEAD
     * @throws IOException
     *             If reading the font file fails ======= >>>>>>> using FreeSans
     *             as default font and added new free fonts
     */
    private static void createFontMetrics(final PDFont font) {
        final float base = font.getFontDescriptor().getXHeight() / 1000;
        final float ascent = font.getFontDescriptor().getAscent() / 1000 - base;
        final float descent = font.getFontDescriptor().getDescent() / 1000;
        fontMetrics.put(font.getName(), new FontMetrics(base + ascent - descent, ascent, descent));
    }

    public static void addDefaultFonts(final PDFont font, final PDFont fontBold, final PDFont fontItalic,
            final PDFont fontBoldItalic) {
        defaultFonts.put("font", font);
        defaultFonts.put("fontBold", fontBold);
        defaultFonts.put("fontItalic", fontItalic);
        defaultFonts.put("fontBoldItalic", fontBoldItalic);
    }

    public static Map<String, PDFont> getDefaultfonts() {
        return defaultFonts;
    }

    public static void setSansFontsAsDefault(final PDDocument document) {
        defaultFonts.put("font", loadFont(document, "fonts/FreeSans.ttf"));
        defaultFonts.put("fontBold", loadFont(document, "fonts/FreeSansBold.ttf"));
        defaultFonts.put("fontItalic", loadFont(document, "fonts/FreeSansOblique.ttf"));
        defaultFonts.put("fontBoldItalic", loadFont(document, "fonts/FreeSansBoldOblique.ttf"));
    }
}
