<?xml version="1.0"?>


<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <xsl:output indent="yes"/>

  <xsl:template match="include">
    <xsl:apply-templates select="document(@file)/section"/>
  </xsl:template>

  <xsl:template match="/">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format"
      xmlns:fox="http://xml.apache.org/fop/extensions">

      <fo:layout-master-set>
        <fo:simple-page-master master-name="regular"
          page-width="210mm"  page-height="297mm"
           margin-top="5mm"  margin-bottom="3mm"
           margin-left="20mm" margin-right="20mm">
          <fo:region-body margin-top="15mm" margin-bottom="15mm"/>
          <fo:region-before extent="15mm"/>
          <fo:region-after extent="10mm"/>
        </fo:simple-page-master>
      </fo:layout-master-set>

 
      <fo:page-sequence master-reference="regular">
          <fo:static-content flow-name="xsl-region-after">
            <fo:block font-size="8pt" line-height="10pt" space-before="0cm" 
              font-family="sans-serif" 
              text-align="start" text-indent="0cm">
              Outline 
            </fo:block>
            <fo:block font-size="8pt"
              space-before="-10pt" 
              font-family="sans-serif" 
              text-align="end">
              <fo:page-number></fo:page-number>
            </fo:block>
          </fo:static-content> 
        
          <fo:flow flow-name="xsl-region-body">
            <xsl:apply-templates/>
          </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>


  <xsl:template match="section">
    <fo:block 
      font-size="16pt"
      line-height="130%"
      font-family="sans-serif"
      padding-left="0.5cm"
      border-style="none"
      border-width="0.1mm"
      >
        <xsl:if test="@numbered='on'">
          <xsl:number level="multiple" count="//section[@numbered='on']" format="1. "/>
        </xsl:if>
        <xsl:value-of select="title"/>
        <xsl:if test="@draft='yes'">
          <fo:inline font-size="60%" font-style="italic"> draft</fo:inline>
        </xsl:if>

        <xsl:apply-templates select="section|heading|subheading|minorheading"/> 
    </fo:block>
  </xsl:template>
        
  <xsl:template match ="title">
  </xsl:template>

  <xsl:template match ="subheading">
    <fo:block
      font-family="sans-serif"
      font-size="14pt"
      text-align="start"
      text-indent="1cm"
      >
      <xsl:number level="multiple" count="//section[@numbered='on']" format="1."/>
      <xsl:number level="multiple" count="//section[@numbered='on']//subheading" format="1. "/>
      <xsl:apply-templates/> 
    </fo:block>
  </xsl:template>
  
  
  <xsl:template match ="minorheading">
    <fo:block
      font-family="sans-serif"
      font-size="14pt"
      text-align="start"
      text-indent="1.5cm"
      >
      <xsl:number level="multiple" count="//section[@numbered='on']" format="1."/>
      <xsl:number level="multiple" count="//section[@numbered='on']//subheading" format="1."/>
      <xsl:number level="multiple" count="//section[@numbered='on']//minorheading" format="1. "/>
      <xsl:apply-templates/> 
    </fo:block>
  </xsl:template>

  <xsl:template match="class|method|variable">
    <fo:inline
      hyphenation-keep="column"
      font-family="monospace"
      >
      <xsl:apply-templates/> 
    </fo:inline>
  </xsl:template>

  <xsl:template match="object|menu|field">
    <fo:inline
      font-family="sans-serif"
      background-color="#EEE"

      >
      <xsl:apply-templates/> 
    </fo:inline>
  </xsl:template>

  <xsl:template match="code|path|property">
    <fo:inline
      font-family="monospace"
      font-weight="bold"
      >
      <xsl:apply-templates/> 
    </fo:inline>
  </xsl:template>

  <xsl:template match="ital">
    <fo:inline
      font-style="italic"
      >
      <xsl:apply-templates/> 
    </fo:inline>
  </xsl:template>

  <xsl:template match="em">
    <fo:inline font-weight="bold">
      <xsl:apply-templates/> 
    </fo:inline>
  </xsl:template>

  <xsl:template match="method/em">
    <fo:inline font-style="italic">
      <xsl:apply-templates/> 
    </fo:inline>
  </xsl:template>

  <xsl:template match="rephrase">
    <fo:inline  color="red">
      [<xsl:apply-templates/>
        <fo:inline font-style="normal" baseline-shift="8pt" font-size="50%">
          rephrase
          </fo:inline>]
    </fo:inline>
  </xsl:template>

  <xsl:template match="weblink">
    <xsl:choose>
      <xsl:when test="string-length(.) &gt; 0">
        <xsl:apply-templates/> 
        
        <fo:footnote>
          <fo:inline baseline-shift="super" font-size="75%">
            <xsl:number level="any" count="weblink|note" format="(1)" />
          </fo:inline>
          <fo:footnote-body>
            <fo:block font-size="8pt">
              <fo:inline baseline-shift="200%" font-size="75%">
                <xsl:number level="any" count="weblink|note"  format="(1) "/>
              </fo:inline>
              <xsl:value-of select="@address"/>
            </fo:block>
          </fo:footnote-body>
        </fo:footnote>
        
      </xsl:when>
        
      <xsl:otherwise>
        <fo:inline color="blue">
          <xsl:value-of select="@address"/>
        </fo:inline>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>




</xsl:stylesheet>







