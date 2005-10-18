<?xml version="1.0"?>


<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output indent="yes"/>
  
 
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="include">
        <xsl:apply-templates select="document(@file)/section"/>
    </xsl:template>
  

</xsl:stylesheet>