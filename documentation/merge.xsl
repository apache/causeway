<?xml version="1.0"?>


<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output indent="yes"/>
  
    <xsl:variable name="javadoc" select="document('build/javadoc.xml')/javadoc"/>
        
    
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
   
   
    <xsl:template match="javadoc">
        <xsl:variable name="class" select="@class"/>
        <javadoc>
            <xsl:apply-templates select="$javadoc/package/interface[@name=$class]"/>
            <xsl:apply-templates select="$javadoc/package/class[@name=$class]"/>
        </javadoc>
    </xsl:template>
    
    <xsl:template match="javadoc/package/interface|javadoc/package/class">
        <xsl:variable name="package" select="../@name"/>
        <package>
            <xsl:attribute name="name">
                <xsl:value-of select="$package"/>
            </xsl:attribute>
                
            <xsl:copy>
                <xsl:apply-templates select="@*|node()"/>
            </xsl:copy>
        </package>
    </xsl:template>
   
</xsl:stylesheet>