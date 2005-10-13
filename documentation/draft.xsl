<?xml version="1.0"?>


<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <xsl:output indent="yes"/>

  <xsl:key name="parts" match="/publication/section" use="."/>
  <xsl:key name="chapters" match="/publication/section//section" use="."/>

  <xsl:template match="include">
    <xsl:apply-templates select="document(@file)/section"/>
  </xsl:template>

  <xsl:template match="/">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format"
      xmlns:fox="http://xml.apache.org/fop/extensions">

      <fo:layout-master-set>
         <fo:simple-page-master master-name="title"
          page-width="210mm"  page-height="297mm"
           margin-top="5mm"  margin-bottom="3mm"
           margin-left="20mm" margin-right="20mm">
          <fo:region-body margin-top="15mm" margin-bottom="15mm"/>
          <fo:region-before extent="15mm"/>
          <fo:region-after extent="10mm"/>
        </fo:simple-page-master>

         <fo:simple-page-master master-name="toc"
          page-width="210mm"  page-height="297mm"
           margin-top="5mm"  margin-bottom="3mm"
           margin-left="20mm" margin-right="20mm">
          <fo:region-body margin-top="15mm" margin-bottom="15mm"/>
          <fo:region-before extent="15mm"/>
          <fo:region-after extent="10mm"/>
        </fo:simple-page-master>

        <fo:simple-page-master master-name="first"
          page-width="210mm"  page-height="297mm"
           margin-top="5mm"  margin-bottom="3mm"
           margin-left="50mm" margin-right="20mm">
          <fo:region-body margin-top="15mm" margin-bottom="15mm"/>
          <fo:region-before extent="15mm"/>
          <fo:region-after extent="10mm"/>
        </fo:simple-page-master>

        <fo:simple-page-master master-name="regular"
          page-width="210mm"  page-height="297mm"
           margin-top="5mm"  margin-bottom="3mm"
           margin-left="50mm" margin-right="20mm">
          <fo:region-body margin-top="15mm" margin-bottom="15mm"/>
          <fo:region-before extent="15mm"/>
          <fo:region-after extent="10mm"/>
        </fo:simple-page-master>
    </fo:layout-master-set>


      <!-- create Acrobat index and keys -->
      <xsl:for-each select="publication|section">
        
        <xsl:for-each select="section">
          <fox:outline internal-destination="{generate-id(.)}">
            <fox:label><xsl:value-of select="title"/></fox:label>
            
            <xsl:for-each select="section">
              <fox:outline internal-destination="{generate-id(.)}">
                <fox:label><xsl:value-of select="title"/></fox:label>
                
                <xsl:for-each select="section">
                  <fox:outline internal-destination="{generate-id(.)}">
                    <fox:label><xsl:value-of select="title"/></fox:label>
                    
                    <xsl:for-each select="section">
                      <fox:outline internal-destination="{generate-id(.)}">
                        <fox:label><xsl:value-of select="title"/></fox:label>
                      </fox:outline>
                    </xsl:for-each>
                    
                  </fox:outline>
                </xsl:for-each>
                
              </fox:outline>
            </xsl:for-each>
          </fox:outline>
        
        </xsl:for-each>
      </xsl:for-each> 

      <!-- Title page -->
      <fo:page-sequence master-reference="title">
        <fo:flow flow-name="xsl-region-body">
          <fo:block text-align="center"
            space-before="12cm"
            font-size="30pt" 
            font-family="sans-serif">
            <xsl:value-of select="/publication/title"/>
          </fo:block>
          <fo:block text-align="center" 
            font-size="20pt" 
            font-family="sans-serif" >
            <xsl:value-of select="/publication/subtitle"/>
          </fo:block>
          <fo:block text-align="right" 
            space-before="2cm"
            font-size="16pt" 
            font-family="serif" >
            <xsl:value-of select="/publication/author"/>
          </fo:block>
        </fo:flow>
      </fo:page-sequence>
      

      <!-- Copyright page -->
      <fo:page-sequence master-reference="title">
        <fo:flow flow-name="xsl-region-body">
          <fo:block text-align="left"
            space-before="24cm"
            font-size="10pt" 
            font-family="sans-serif">
            &#xA9;
            <xsl:text> </xsl:text>
            Copyright
            <xsl:value-of select="/publication/copyright/year"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="/publication/copyright/holder"/>

          </fo:block>

          <xsl:for-each select="/publication/copyright/license/para">
            <fo:block text-align="left" 
              space-before="6px"
              font-size="10pt" 
              font-family="sans-serif" >
              <xsl:apply-templates select="."/>
            </fo:block>
          </xsl:for-each>
        </fo:flow>
      </fo:page-sequence>
      

      <!-- Table of contents -->
      <fo:page-sequence initial-page-number="0" master-reference="toc">
        <fo:flow flow-name="xsl-region-body">
          <fo:block text-align="center" font-size="20pt" space-after="1cm">
            Contents
          </fo:block>

          <xsl:for-each select="/publication/section">
            <fo:block text-align-last="justify" 
              font-size="120%"
              background-color="grey"
              > 
              
              <xsl:text>Part </xsl:text>
              <xsl:number level="multiple" count="/publication/section" format="I - "/>  
              <xsl:value-of select="title"/>
              <fo:leader leader-pattern="dots"/>
              <fo:page-number-citation ref-id="/publication/section"/>
              
              <xsl:for-each select="section">
                <fo:block font-size="90%" 
                    text-align-last="justify" 
                    background-color="white"
                    text-align="start" 
                    text-indent="1.5cm">
                  <xsl:value-of select="title"/>
                  <fo:leader leader-pattern="dots"/>
                  <fo:page-number-citation ref-id="section"/>
                </fo:block>
              </xsl:for-each>
            </fo:block>
          </xsl:for-each>

        </fo:flow>
      </fo:page-sequence>

      <!-- xsl:apply-templates select="/publication/preamble"/-->


      <!-- main content -->
      <xsl:for-each select="/publication/section">
        <fo:page-sequence master-reference="first">
          <fo:static-content flow-name="xsl-region-after">
            <fo:block font-size="8pt" line-height="10pt" space-before="0cm" 
              font-family="sans-serif" 
              text-align="start" text-indent="-3.5cm">
              Draft 
            </fo:block>
            <fo:block font-size="8pt"
              space-before="-10pt" 
              font-family="sans-serif" 
              text-align="end">
              <fo:page-number></fo:page-number>
            </fo:block>
          </fo:static-content> 
        
          <fo:flow flow-name="xsl-region-body">
              <fo:block 
                font-size="46pt"
                space-before="8cm"
                font-family="sans-serif"
                space-after=".65cm"
                id="{generate-id(key('parts', .))}"
              >
                <xsl:text>Part </xsl:text>
                <xsl:number level="multiple" count="/publication/section" format="I"/>
             </fo:block>
             
             <fo:block 
                font-size="23pt"
                line-height="130%"
                font-family="sans-serif"
                space-after=".65cm"
              >
                <xsl:value-of select="title"/>
                <xsl:if test="@draft='yes'">
                  <fo:inline font-size="60%" font-style="italic"> draft</fo:inline>
                </xsl:if>
             </fo:block>
          </fo:flow>
        </fo:page-sequence>
 

        <xsl:for-each select="section">
     
        <fo:page-sequence master-reference="regular">
          <fo:static-content flow-name="xsl-region-before">
            <fo:block font-size="8pt" line-height="10pt" space-before="0cm" 
              font-family="sans-serif" 
              text-align="start" text-indent="-3.5cm">
              <xsl:value-of select="title"/>
            </fo:block>
            <fo:block text-align="end" 
              font-size="8pt" 
              font-family="sans-serif" 
              space-before="-10pt" 
              line-height="14pt" >
              <xsl:value-of select="attribute::title"/>
            </fo:block>
          </fo:static-content>
    
          <fo:static-content flow-name="xsl-region-after">
            <fo:block font-size="8pt" line-height="10pt" space-before="0cm" 
              font-family="sans-serif" 
              text-align="start" text-indent="-3.5cm">
              Draft 
            </fo:block>
            <fo:block font-size="8pt"
              space-before="-10pt" 
              font-family="sans-serif" 
              text-align="end">
              <fo:page-number></fo:page-number>
            </fo:block>
          </fo:static-content> 
        
          <fo:flow flow-name="xsl-region-body">
                <xsl:apply-templates select="."/>
                <block/>
           </fo:flow>
        </fo:page-sequence>
         </xsl:for-each>

    </xsl:for-each>

    </fo:root>
  </xsl:template>


  <xsl:template match="section">
    <fo:block 
      font-size="16pt"
      line-height="130%"
      font-family="sans-serif"
      space-after=".65cm"
      border-after-style="solid"
      border-width="2mm"
      border-color="black"
      keep-with-next="always"
      id="{generate-id(key('chapters', .))}"
      >
      <fo:block>
        <xsl:if test="@numbered='on'">
          <xsl:number level="multiple" count="//section[@numbered='on']" format="1. "/>
        </xsl:if>
        <xsl:value-of select="title"/>
        <xsl:if test="@draft='yes'">
          <fo:inline font-size="60%" font-style="italic"> draft</fo:inline>
        </xsl:if>
      </fo:block>
    </fo:block>
    
    <fo:block>
      <xsl:apply-templates/> 
    </fo:block>
  </xsl:template>
        


  <xsl:template match ="intro">
    <fo:block 
      font-style="italic"
      space-after=".35cm"
      keep-with-next="always"
      >
      <xsl:apply-templates/> 
    </fo:block>
  </xsl:template>


  <xsl:template match ="subheading">
    <fo:block
      font-family="sans-serif"
      font-size="14pt"
      text-align="start"
      space-before=".2cm"
      space-after=".2cm"
      border-after-style="solid"
      border-width="1mm"
      border-color="black"
      keep-with-next="always"
      >
      <xsl:apply-templates/> 
    </fo:block>
  </xsl:template>
  
  
  <xsl:template match ="minorheading">
    <fo:block
      font-family="sans-serif"
      font-size="14pt"
      text-align="start"
      space-before=".2cm"
      space-after=".2cm"
      border-after-style="solid"
      border-width="0.5mm"
      border-color="black"
      keep-with-next="always"
      >
      <xsl:apply-templates/> 
    </fo:block>
  </xsl:template>


  <xsl:template match="list">
    <fo:list-block  start-indent="5mm" provisional-distance-between-starts="5mm">
      <xsl:apply-templates/>
    </fo:list-block>
  </xsl:template>

  <xsl:template match="list/para">
    <fo:list-item>
      <fo:list-item-label end-indent="label-end()">
        <fo:block font-size="140%">&#x2022;</fo:block>
      </fo:list-item-label>
      
      <fo:list-item-body start-indent="body-start()">
        <fo:block
          space-after="6px">
          <xsl:apply-templates/>
        </fo:block>
      </fo:list-item-body>
    </fo:list-item>
  </xsl:template>

  <xsl:template match="list/index|item/index">
  </xsl:template>

  <xsl:template match="item">
    <fo:list-item>
      <fo:list-item-label end-indent="label-end()">
        <fo:block font-size="140%">&#x2022;</fo:block>
      </fo:list-item-label>
      
      <fo:list-item-body start-indent="body-start()">
        <xsl:apply-templates/>
      </fo:list-item-body>
    </fo:list-item>
  </xsl:template>



  <xsl:template match ="para">
    <fo:block
      text-indent="0.0cm"
      space-after="9px"
      >
      <xsl:apply-templates/> 
    </fo:block>
  </xsl:template>

  <xsl:template match ="biblio">
    <fo:block
      space-after="6px">
      [<xsl:value-of select="@name"/><xsl:value-of select="@year"/>]
    <xsl:apply-templates/>
    <xsl:if test="@url != ''">
      <xsl:text>See </xsl:text>
      <xsl:value-of select="@url"/>
    </xsl:if>
  </fo:block>
  </xsl:template>


  <xsl:template match ="para/comment">
    <fo:inline
      color="#609">
      [Note: <xsl:apply-templates/>]
    </fo:inline>
  </xsl:template>



  <xsl:template match ="program-listing">
    <fo:block font-weight="normal"
      font-size="9pt"
      font-family="monospace"
      color="green"
      text-align="start"
      start-indent="0cm"
      padding-before="10pt"
      padding-after="10pt"
      keep-together="always"
      white-space-collapse="false"
      space-after="9px"
      >
      <xsl:apply-templates/> 
    </fo:block>
  </xsl:template>


  <xsl:template match ="program-listing/listing-note">
    <fo:block font-size="9pt" 
      color="black"
      font-family="serif"
      width="2.4in"
      font-style="italic"
      line-height="14pt"
      white-space-collapse="true"
      text-align="start"
      margin-left=".5in"
    >
      <xsl:apply-templates/> 
    </fo:block>
  </xsl:template>


  <xsl:template match ="command-listing|property-listing">
    <fo:block font-size="9pt"
      font-weight="bold"
      font-family="monospace"
      color="blue"
      text-align="start"
      start-indent="0cm"
      white-space-collapse="false"
      space-after="9px"
      >
      <xsl:apply-templates/> 
    </fo:block>
  </xsl:template>

  <xsl:template match ="comment">
    <fo:block font-size="10pt" 
      font-family="serif" 
      text-align="start"
      space-after="10px"
      color="#609"
      white-space-collapse="true">
      <fo:inline font-weight="bold">Author's comment</fo:inline>
      <xsl:text>: </xsl:text>
      <xsl:apply-templates/> 
    </fo:block>
  </xsl:template>




  <xsl:template match ="panel">
    <fo:block
      space-before="0px"
      space-after="10px"
      padding="20px"
      border-style="solid"
      >
      <fo:block 
        font-size="130%"
        font-style="italic"
        >
        <xsl:value-of select="caption"/>
      </fo:block>
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>

  <xsl:template match="caption"/>


  <xsl:template match ="figure">
    <fo:block
      space-before="0px"
      space-after="10px"
      width="10cm"
      padding="20px"
      border-style="solid"
      keep-with-next="always"
      >
      <fo:block 
        font-size="80%"
        font-style="italic"
        keep-with-next="always"
        >
        <xsl:value-of select="@label"/>
      </fo:block>
      <fo:block keep-with-next="always">
          <fo:external-graphic scaling="uniform" width="250px">
            <xsl:attribute name="src">src/<xsl:value-of select="@fileref"/></xsl:attribute>
            <!--xsl:attribute name="content-height"><xsl:value-of select="@width"/></xsl:attribute-->
          </fo:external-graphic>
      </fo:block>
      <fo:block
        color="gray"
        font-size="8pt"
        line-height="11pt"
        space-after="8px"
        keep-with-next="always"
        >
        <xsl:value-of select="description"/>
      </fo:block>
    </fo:block>
  </xsl:template>
  

  <xsl:template match ="inline-graphic">
    <fo:block
      space-before="0px"
      space-after="10px"
      >
      <fo:external-graphic
        background-color="white"
        >
        <xsl:attribute name="src"><xsl:value-of select="@fileref"/></xsl:attribute>
        <xsl:attribute name="width">127mm</xsl:attribute>
        <!--xsl:if test="@width='80%'">
          <xsl:attribute name="width">127mm</xsl:attribute>
        </xsl:if-->
      </fo:external-graphic>
    </fo:block>
    <fo:block
      color="gray"
      font-size="8pt"
      line-height="11pt"
      space-after="18px"
     >
      [<xsl:value-of select="@width"/> IMAGE <xsl:value-of select="@fileref"/>: <xsl:value-of select="description"/>]
      
  </fo:block>
  </xsl:template>

  <xsl:template match="index">
    <fo:block
      color="green"
      font-family="sans-serif"
      font-size="75%"
      text-align="right"
      ><xsl:value-of select="term"/><xsl:text>: </xsl:text><xsl:value-of select="usage"/>
    </fo:block>
  </xsl:template>




  <xsl:template match="class|method|variable">
    <fo:inline
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



    <xsl:template match="note">
    <fo:footnote>
      <fo:inline baseline-shift="super" font-size="75%">
        <xsl:number level="any" count="weblink|note" format="(1)"></xsl:number>
      </fo:inline>
      <fo:footnote-body>
        <fo:block font-size="8pt" line-height="11pt">
          <fo:inline baseline-shift="super" font-size="75%">
            <xsl:number level="any" count="weblink|note"  format="(1)"/>
          </fo:inline>
          <xsl:apply-templates/>
        </fo:block>
      </fo:footnote-body>
    </fo:footnote>
  </xsl:template>

  <xsl:template match="ref">
    [<xsl:value-of select="@name"/><xsl:value-of select="@year"/>]
  </xsl:template>

  <xsl:template match="note/weblink">
    <xsl:apply-templates/>
    (<xsl:value-of select="@address"/>)
  </xsl:template>



<xsl:template match ="package">
  <fo:block 
    font-size="14pt" 
    font-family="sans-serif" 
    font-weight="bold" 
    line-height="18pt"
    space-before.optimum="15pt"
    space-after="15pt"
    text-align="start"
    >
    Package <xsl:value-of select="name"/>
</fo:block>
<xsl:apply-templates/> 
<fo:block break-after="page"/>
</xsl:template>

<xsl:template match ="javadoc">
  <xsl:for-each select="class|interface">
    <xsl:sort select="@name"/>

  <fo:block
    font-weight="bold"
    font-size="10pt" 
    font-family="sans-serif" 
    line-height="14pt"
    space-before.optimum="15pt"
    space-after="3pt"
    text-align="start" >
    <xsl:value-of select="@package"/>.<xsl:value-of select="@name"/>
  </fo:block>

  <fo:block
    font-size="8pt" 
    font-family="sans-serif" 
    line-height="12pt"
    space-after="3pt"
    width="100%"
     >

    <fo:inline text-align="end" font-style="italic"><xsl:value-of select="@interfaces"/></fo:inline>
  </fo:block>
  
  <fo:table table-layout="fixed">
    <fo:table-column column-width="22mm"/>
    <fo:table-column column-width="17mm"/>
    <fo:table-column column-width="110mm"/>
    <fo:table-body 
      line-height="12pt"
      font-family="sans-serif"
      font-size="8pt">
      <xsl:apply-templates/> 
    </fo:table-body>
  </fo:table>
</xsl:for-each>

</xsl:template>

<xsl:template match ="class/method|class/constructor|interface/method">
  <fo:table-row> 
  <fo:table-cell>
    <fo:block><xsl:value-of select="@modifiers"/>
  </fo:block>
</fo:table-cell>
		
<fo:table-cell>
  <fo:block text-align="end">
    <xsl:value-of select="@returns"/>
  </fo:block>
</fo:table-cell>

<fo:table-cell>
  <fo:block start-indent="5mm">
    <fo:inline font-weight="bold"><xsl:value-of select="@name"/>
  </fo:inline>(<xsl:apply-templates select="parameter"/>)
  <xsl:apply-templates select="exception"/>

  </fo:block>	<xsl:text> - </xsl:text>

</fo:table-cell>
</fo:table-row> 
</xsl:template>

<xsl:template match ="class/variable">
  <fo:table-row> 
  <fo:table-cell>
    <fo:block><xsl:value-of select="modifiers"/>
  </fo:block>
</fo:table-cell>

		
<fo:table-cell>
  <fo:block text-align="end"><xsl:value-of select="type"/></fo:block>
</fo:table-cell>

<fo:table-cell>
  <fo:block start-indent="5mm">
    <fo:inline font-weight="bold"><xsl:value-of select="name"/></fo:inline>
    <fo:inline font-style="italic"> = <xsl:value-of select="value"/></fo:inline>
  </fo:block>
</fo:table-cell>
</fo:table-row> 
</xsl:template>


<xsl:template match="parameter">
  <xsl:if test="position() &gt; 1"><xsl:text>, </xsl:text></xsl:if>
  <fo:inline color="green"    >
    <xsl:value-of select="@type"/> 
  </fo:inline>
  <xsl:text> </xsl:text>
  <fo:inline color="blue"    >
    <xsl:value-of select="@name"/> 
  </fo:inline>
</xsl:template>


<xsl:template match="exception">
  <xsl:if test="position() = 1"><xsl:text>throws </xsl:text></xsl:if>
  <xsl:if test="position() &gt; 1"><xsl:text>, </xsl:text></xsl:if>
  <fo:inline color="red"    >
    <xsl:value-of select="@type"/> 
  </fo:inline>
</xsl:template>




<xsl:template match="javadoc-element">
   <xsl:variable name="member" select="substring-after(@name, '#')"/>
   <xsl:variable name="class" select="substring-before(@name, '#')"/>

   <xsl:if test="count(document('javadoc.xml')/javadoc/javadoc-class[@name=$class]) = 0">
     <fo:inline color="red" font-style="italic">
	No class <xsl:value-of select="$class"/> found!</fo:inline>
   </xsl:if>

   <xsl:if test="count(document('javadoc.xml')/javadoc/javadoc-class[@name=$class]/javadoc-member[@name=$member]) = 0">
     <fo:inline color="red" font-style="italic">
	No member <xsl:value-of select="@name"/> found!</fo:inline>
   </xsl:if>

   <xsl:apply-templates select="document('javadoc.xml')/javadoc/javadoc-class/javadoc-member[@name=$member]"/> 

</xsl:template>

<xsl:template match="javadoc-group">
   <xsl:variable name="group" select="substring-after(@name, '#')"/>
   <xsl:variable name="class" select="substring-before(@name, '#')"/>

   <xsl:if test="count(document('javadoc.xml')/javadoc/javadoc-class[@name=$class]) = 0">
     <fo:inline color="red" font-style="italic">
	No class <xsl:value-of select="$class"/> found!</fo:inline>
   </xsl:if>

   <xsl:if test="count(document('javadoc.xml')/javadoc/javadoc-class[@name=$class]/javadoc-member[@group=$group]) = 0">
     <fo:inline color="red" font-style="italic">
	No members for group <xsl:value-of select="@name"/> found!</fo:inline>
   </xsl:if>

   <xsl:apply-templates select="document('javadoc.xml')/javadoc/javadoc-class[@name=$class]/javadoc-member[@group=$group]"/> 

</xsl:template>


<xsl:template match="javadoc-member">
    <fo:block
	font-style="italic"
      text-indent="0.0cm"
      space-after="0px"
      >
	  <xsl:apply-templates select="javadoc-declaration"/>
    </fo:block>

    <fo:block
      text-indent="0.0cm"
	margin-left="5mm"
      space-after="9px"
      >
	<xsl:if test="count(javadoc-comment) = 0">
	  <fo:inline color="red" font-style="italic">No comment found</fo:inline>
	</xsl:if>
	<xsl:apply-templates select="javadoc-comment"/>
    </fo:block>
</xsl:template>

<xsl:template match="javadoc-method">
    <fo:inline font-weight="bold">
      <xsl:apply-templates/> 
    </fo:inline>
</xsl:template>

<xsl:template match="javadoc-variable">
    <fo:inline font-weight="bold">
      <xsl:apply-templates/> 
    </fo:inline>
</xsl:template>

<xsl:template match="javadoc-class">
    <xsl:apply-templates/> 
</xsl:template>





</xsl:stylesheet>







