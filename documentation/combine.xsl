<?xml version="1.0"?>


<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output indent="yes"/>
    
    <xsl:template match="/">
    <publication>
        <title> Naked Objects Notebook </title>

        <chapter>
        
        <xsl:apply-templates/>
    </chapter>
    </publication>
    </xsl:template>
    
    <xsl:template match="/content/section">
        <xsl:apply-templates select="document(@file)/section" mode="content"/>
    </xsl:template>
    
    
    <xsl:template match="section" mode="content">
        <xsl:copy-of select="."/>
    </xsl:template>

</xsl:stylesheet>