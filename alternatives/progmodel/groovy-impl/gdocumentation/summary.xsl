<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  version="1.0">

    <xsl:key name="class" match="/jel/jelclass/methods/method/@type" use="@type" />


    <xsl:output method="html" />
    <xsl:template match="/">
        <html>
            <head>
                <title></title>
                <link type="text/css" rel="stylesheet" href="style.css" media="screen,print"/>
            </head>
            <body>
                <xsl:apply-templates/>
             </body>
       </html> 
   </xsl:template >


    <xsl:template match="part">
        <hr />
        <hr />
        <h1><xsl:value-of select="count(.)"/>. <xsl:value-of select="title"/></h1>
        <xsl:apply-templates />
    </xsl:template>

    <xsl:template match="chapter">
        <hr />
        <h2><xsl:value-of select="count(child::node())"/>. <xsl:value-of select="title"/></h2>
        <xsl:apply-templates />
    </xsl:template>

    <xsl:template match="sect1|section">
        <h3><xsl:value-of select="count(/part)"/>. <xsl:value-of select="title"/></h3>
        <xsl:apply-templates />
    </xsl:template>
<!--
    <xsl:template match="sect2|section/section">
        <h4><xsl:value-of select="count(/part)"/>. <xsl:value-of select="title"/></h4>
        <xsl:apply-templates />
    </xsl:template>

    <xsl:template match="sect3|section/section/section">
        <h5><xsl:value-of select="count(/part)"/>. <xsl:value-of select="title"/></h5>
        <xsl:apply-templates />
    </xsl:template>
    -->

    
    <xsl:template match="@*|node()">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
   
   <xsl:template match="sect1/para[1]|sect2/para[1]|sect3/para[1]|section/para[1]">
    <p>
        <xsl:value-of select="position()"/>. <xsl:value-of select="."/>
    </p>
    </xsl:template>
</xsl:stylesheet>
   
   
