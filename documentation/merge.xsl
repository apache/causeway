<?xml version="1.0"?>


<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output indent="yes"/>
  
    
    <xsl:template match="include">
        <xsl:variable name="source" select="document(@file)/section"/>
        
        <section>
            <xsl:attribute name="file">
                <xsl:value-of select="@file"/>
            </xsl:attribute>
            <xsl:attribute name="label">
                <xsl:value-of select="$source/@label"/>
            </xsl:attribute>
            <xsl:attribute name="numbered">
                <xsl:value-of select="$source/@numbered"/>
            </xsl:attribute>

            <xsl:apply-templates select="$source/*"/>
        </section>
    </xsl:template>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>