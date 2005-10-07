<?xml version="1.0"?>


<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  >

  <xsl:output indent="yes"/>

  <xsl:template match="/">
    <html>
    
    <head>
		<!-- this tag is not being copied across with the closing slash ? -->
        <link rel="STYLESHEET" href="./screen.css" media="screen"/>
        <link rel="STYLESHEET" href="./print.css" media="print"/>
        <title>
            <xsl:value-of select="/section/title"/>
        </title>
    </head>
    <body>

      <!-- create Acrobat index and keys -->
      <!--xsl:for-each select="publication|chapter">
        
        <xsl:for-each select="chapter">
          <fox:outline internal-destination="{generate-id(.)}">
            <fox:label><xsl:value-of select="@title"/></fox:label>
            
            <xsl:for-each select="section">
              <fox:outline internal-destination="{generate-id(.)}">
                <fox:label><xsl:value-of select="@title"/></fox:label>
                
                <xsl:for-each select="subsection">
                  <fox:outline internal-destination="{generate-id(.)}">
                    <fox:label><xsl:value-of select="@title"/></fox:label>
                    
                    <xsl:for-each select="section">
                      <fox:outline internal-destination="{generate-id(.)}">
                        <fox:label><xsl:value-of select="@title"/></fox:label>
                      </fox:outline>
                    </xsl:for-each>
                    
                  </fox:outline>
                </xsl:for-each>
                
              </fox:outline>
            </xsl:for-each>
          </fox:outline>
        
        </xsl:for-each>
      </xsl:for-each--> 

     

      <!-- Table of contents -->
      <!--fo:page-sequence initial-page-number="0" master-reference="toc">
        <fo:flow flow-name="xsl-region-body">
          <p>
            Contents
          </p>

          <xsl:for-each select="/publication">
            <fo:block text-align-last="justify" 
              font-size="120%"
              >
              <xsl:value-of select="@title"/>
              <fo:leader leader-pattern="dots"/>
              <fo:page-number-citation ref-id="section"/>
              <xsl:for-each select="chapter">
                <p>
                  <xsl:value-of select="@title"/>
                  <fo:leader leader-pattern="dots"/>
                  <fo:page-number-citation ref-id="section"/>
                </p>
              </xsl:for-each>
            </p>

            </xsl:for-each>

        </fo:flow>
      </fo:page-sequence-->

      <!-- xsl:apply-templates select="/publication/preamble"/-->


      <!-- content -->
      <!--xsl:apply-templates select="/sections"/>
        
        <fo:page-sequence master-reference="regular">
          <fo:static-content flow-name="xsl-region-before">
            <fo:block font-size="8pt" line-height="10pt" space-before="0cm" 
              font-family="sans-serif" 
              text-align="start" text-indent="-3.5cm">
              <xsl:value-of select="/publication/title"/>
            </p>
            <fo:block text-align="end" 
              font-size="8pt" 
              font-family="sans-serif" 
              space-before="-10pt" 
              line-height="14pt" >
              <xsl:value-of select="attribute::title"/>
            </p>
          </fo:static-content>

          <fo:static-content flow-name="xsl-region-after">
            <fo:block font-size="8pt" line-height="10pt" space-before="0cm" 
              font-family="sans-serif" 
              text-align="start" text-indent="-3.5cm">
              Draft 
            </p>
            <fo:block font-size="8pt"
              space-before="-10pt" 
              font-family="sans-serif" 
              text-align="end">
              <fo:page-number></fo:page-number>
            </p>
          </fo:static-content> 
        
          <fo:flow flow-name="xsl-region-body">
            <fo:block font-size="11pt" 
              font-family="serif" 
              line-height="145%"
              text-align="left"

              >
              <xsl:apply-templates select="."/>
            </p>
          </fo:flow>
        </fo:page-sequence>
      </xsl:for-each-->

        <xsl:apply-templates/> 
    </body>
    </html>
  </xsl:template>


  <!--xsl:template match="part">
    <fo:block 
      font-size="22pt"
      line-height="130%"
      font-family="sans-serif"
      space-after=".65cm"
      border-after-style="solid"
      border-width="2mm"
      border-color="black"
      keep-with-next="always"
      id="{generate-id(key('parts', .))}"
      >
      <p>
        <xsl:text>Part </xsl:text>
        <xsl:number level="multiple" count="part" format="I - "/>
        <xsl:value-of select="@title"/>
        <xsl:if test="@draft='yes'">
          <b>
        </xsl:if>
      </p>
    </p>
    <fo:block 
      
      >
      <xsl:apply-templates/> 
    </p>
  </xsl:template-->
        

  <!--xsl:template match="chapter">
    <fo:block 
      font-size="16pt"
      line-height="130%"
      font-family="sans-serif"
      space-after=".65cm"
      border-after-style="solid"
      border-width=".5mm"
      border-color="black"
      keep-with-next="always"
      id="{generate-id(key('chapters', .))}"
      >
      <p>
        <xsl:if test="@numbered='on'">
          <xsl:number level="multiple" count="//chapter[@numbered='on']" format="1. "/>
        </xsl:if>
        <xsl:value-of select="@title"/>
        <xsl:if test="@draft='yes'">
          <b>
        </xsl:if>
      </p>
    </p>
    <fo:block 
      
      >
      <xsl:apply-templates/> 
    </p>
  </xsl:template-->
        

  <xsl:template match ="section/description">
  </xsl:template>

  <xsl:template match ="intro">
    <p class="intro">
      <xsl:apply-templates/> 
    </p>
  </xsl:template>
 
  <xsl:template match ="title">
    <h1>
      <xsl:apply-templates/> 
    </h1>
  </xsl:template>

  <xsl:template match ="heading">
    <h2>
      <xsl:apply-templates/> 
    </h2>
  </xsl:template>

  <xsl:template match ="subheading">
    <h3>
      <xsl:apply-templates/> 
    </h3>
  </xsl:template>


  <xsl:template match ="minorheading">
    <h4>
      <xsl:apply-templates/> 
    </h4>
  </xsl:template>

  <xsl:template match="list">
    <ul>
      <xsl:apply-templates/>
    </ul>
  </xsl:template>

  <xsl:template match="list/para">
    <li>
        <xsl:apply-templates/>
    </li>
  </xsl:template>

  <xsl:template match="list/index|item/index">
  </xsl:template>

  <xsl:template match="item">
    <li>
        <xsl:apply-templates/>
    </li>
  </xsl:template>


  <xsl:template match ="para">
    <p>
      <xsl:apply-templates/> 
    </p>
  </xsl:template>

 <xsl:template match ="inline-graphic">
    <div class="inline">
    <img>
        <xsl:attribute name="src">
            <xsl:value-of select="@fileref"/>
        </xsl:attribute>
        <xsl:apply-templates/> 
    </img>
    </div>
  </xsl:template>

    <xsl:template match ="inline-graphic/description">
        <xsl:value-of select="."/>
    </xsl:template>

  <xsl:template match ="biblio">
    <p>
        [<xsl:value-of select="@name"/><xsl:value-of select="@year"/>]
        <xsl:apply-templates/>
        <xsl:if test="@url != ''">
          <xsl:text>See </xsl:text>
          <xsl:value-of select="@url"/>
        </xsl:if>
    </p>
  </xsl:template>


  <xsl:template match ="comment">
  </xsl:template>

  <xsl:template match ="command-listing|property-listing|program-listing">
    <pre>
        <xsl:attribute name="class">
            <xsl:value-of select="name()"/>
        </xsl:attribute>
        <xsl:apply-templates/> 
    </pre>
  </xsl:template>

  <xsl:template match ="program-listing/listing-note">
    <p class="listing-note">
      <xsl:apply-templates/> 
    </p>
  </xsl:template>

  <xsl:template match ="panel">
    <p>
        <xsl:value-of select="caption"/>
        <xsl:apply-templates/>
    </p>
  </xsl:template>

  <xsl:template match="caption"/>


  <xsl:template match="index">
    <p>
        <xsl:value-of select="term"/><xsl:text>: </xsl:text><xsl:value-of select="usage"/>
    </p>
  </xsl:template>




  <xsl:template match="class|method|variable|object|menu|field|code|path|property">
    <span>
        <xsl:attribute name="class">
            <xsl:value-of select="name()"/>
        </xsl:attribute>
        <xsl:apply-templates/> 
    </span>
  </xsl:template>

  <xsl:template match="ital">
    <strong>
      <xsl:apply-templates/> 
    </strong>
  </xsl:template>

  <xsl:template match="em">
    <em>
      <xsl:apply-templates/> 
    </em>
  </xsl:template>

  <xsl:template match="method/em">
    <b>
      <xsl:apply-templates/> 
    </b>
  </xsl:template>

  <xsl:template match="rephrase">
    <b>
      [<xsl:apply-templates/>
        <b>
          rephrase
          </b>]
    </b>
  </xsl:template>


  <xsl:template match="weblink">
    <xsl:choose>
      <xsl:when test="string-length(.) &gt; 0">
        <a>
            <xsl:apply-templates/> 
            <xsl:value-of select="@address"/>
        </a>
      </xsl:when>
        
      <xsl:otherwise>
        <b>
          <xsl:value-of select="@address"/>
        </b>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>



    <xsl:template match="note">
    <!-- fo:footnote>
      <b>
        <xsl:number level="any" count="weblink|note" format="(1)"></xsl:number>
      </b>
      <fo:footnote-body>
        <p>
          <b>
            <xsl:number level="any" count="weblink|note"  format="(1)"/>
          </b>
          <xsl:apply-templates/>
        </p>
      </fo:footnote-body>
    </fo:footnote-->
  </xsl:template>

  <xsl:template match="ref">
    [<xsl:value-of select="@name"/><xsl:value-of select="@year"/>]
  </xsl:template>

  <xsl:template match="note/weblink">
    <xsl:apply-templates/>
    (<xsl:value-of select="@address"/>)
  </xsl:template>

  <xsl:template match="figure">
      <div class="figure">
          <img>
              <xsl:attribute name="src">
                  <xsl:value-of select="@fileref"/>
              </xsl:attribute>
          </img>
          <p>
              <xsl:value-of select="@label"/>
          </p>
        </div>
  </xsl:template>


<!--xsl:template match ="package">
    <p class="package">
        Package <xsl:value-of select="name"/>
    </p>
    <xsl:apply-templates/> 
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
  </p>

  <fo:block
    font-size="8pt" 
    font-family="sans-serif" 
    line-height="12pt"
    space-after="3pt"
    width="100%"
     >

    <b>
  </p>
  
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
    <p>
  </p>
</fo:table-cell>
		
<fo:table-cell>
  <p>
    <xsl:value-of select="@returns"/>
  </p>
</fo:table-cell>

<fo:table-cell>
  <p>
    <b>
  </b>(<xsl:apply-templates select="parameter"/>)
  <xsl:apply-templates select="exception"/>

  </p>	<xsl:text> - </xsl:text>

</fo:table-cell>
</fo:table-row> 
</xsl:template>

<xsl:template match ="class/variable">
  <fo:table-row> 
  <fo:table-cell>
    <p>
  </p>
</fo:table-cell>

		
<fo:table-cell>
  <p>
</fo:table-cell>

<fo:table-cell>
  <p>
    <b>
    <b>
  </p>
</fo:table-cell>
</fo:table-row> 
</xsl:template>


<xsl:template match="parameter">
  <xsl:if test="position() &gt; 1"><xsl:text>, </xsl:text></xsl:if>
  <b>
    <xsl:value-of select="@type"/> 
  </b>
  <xsl:text> </xsl:text>
  <b>
    <xsl:value-of select="@name"/> 
  </b>
</xsl:template>


<xsl:template match="exception">
  <xsl:if test="position() = 1"><xsl:text>throws </xsl:text></xsl:if>
  <xsl:if test="position() &gt; 1"><xsl:text>, </xsl:text></xsl:if>
  <b>
    <xsl:value-of select="@type"/> 
  </b>
</xsl:template>




<xsl:template match="javadoc-element">
   <xsl:variable name="member" select="substring-after(@name, '#')"/>
   <xsl:variable name="class" select="substring-before(@name, '#')"/>

   <xsl:if test="count(document('javadoc.xml')/javadoc/javadoc-class[@name=$class]) = 0">
     <b>
	No class <xsl:value-of select="$class"/> found!</b>
   </xsl:if>

   <xsl:if test="count(document('javadoc.xml')/javadoc/javadoc-class[@name=$class]/javadoc-member[@name=$member]) = 0">
     <b>
	No member <xsl:value-of select="@name"/> found!</b>
   </xsl:if>

   <xsl:apply-templates select="document('javadoc.xml')/javadoc/javadoc-class/javadoc-member[@name=$member]"/> 

</xsl:template>

<xsl:template match="javadoc-group">
   <xsl:variable name="group" select="substring-after(@name, '#')"/>
   <xsl:variable name="class" select="substring-before(@name, '#')"/>

   <xsl:if test="count(document('javadoc.xml')/javadoc/javadoc-class[@name=$class]) = 0">
     <b>
	No class <xsl:value-of select="$class"/> found!</b>
   </xsl:if>

   <xsl:if test="count(document('javadoc.xml')/javadoc/javadoc-class[@name=$class]/javadoc-member[@group=$group]) = 0">
     <b>
	No members for group <xsl:value-of select="@name"/> found!</b>
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
    </p>

    <fo:block
      text-indent="0.0cm"
	margin-left="5mm"
      space-after="9px"
      >
	<xsl:if test="count(javadoc-comment) = 0">
	  <b>
	</xsl:if>
	<xsl:apply-templates select="javadoc-comment"/>
    </p>
</xsl:template>

<xsl:template match="javadoc-method">
    <b>
      <xsl:apply-templates/> 
    </b>
</xsl:template>

<xsl:template match="javadoc-variable">
    <b>
      <xsl:apply-templates/> 
    </b>
</xsl:template>

<xsl:template match="javadoc-class">
    <xsl:apply-templates/> 
</xsl:template-->





</xsl:stylesheet>







