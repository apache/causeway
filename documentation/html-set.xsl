<?xml version="1.0"?>


<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <xsl:output indent="yes"/>

  <xsl:template match="/">
    <html>
        <head>
            <link rel="STYLESHEET" href="./screen.css" media="screen"/>
            <link rel="STYLESHEET" href="./print.css" media="print" />
            <title> Naked Objects Notebook </title>
        </head>
        <body>
            <h1>Naked Objects Notebook</h1>
            <xsl:apply-templates/>
        </body>
    </html>
  </xsl:template>

    <xsl:template match="section">
        <p class="index">
            <a>
                <xsl:attribute name="href">
                        <xsl:value-of select="substring(@file, 0, string-length(@file) - 2)"/>
                        <xsl:text>html</xsl:text>
                </xsl:attribute>
                <xsl:apply-templates select="document(@file)//title"/>
            </a>
             <xsl:apply-templates select="document(@file)//description"/>
        </p>
    </xsl:template>


    <xsl:template match="/title">
        <xsl:value-of select="."/>
    </xsl:template>
    
    <xsl:template match="/description">
        <xsl:value-of select="."/>
    </xsl:template>

</xsl:stylesheet>