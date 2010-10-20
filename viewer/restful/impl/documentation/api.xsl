<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  version="2.0">

    <xsl:key name="class" match="/jel/jelclass/methods/method/@type" use="@type" />


    <xsl:output method="html" />
    <xsl:template match="/">
        <html>
            <head>
                <title>Restful Objects API</title>
                <link type="text/css" rel="stylesheet" href="style.css" media="screen,print"/>
            </head>
            <body>
                
                <div class="packages">    
                <table>
                <tr><th>package</th><th>classes</th><th>interfaces</th><th>total</th></tr>
                <xsl:for-each select="/jel/jelclass[not(@package = preceding-sibling::jelclass/@package)]">
                    <xsl:sort select="@package" />
                    <tr>
                        <td>
                            <xsl:value-of select="@package" />
                        </td>
                        <td>
                            <xsl:value-of select="count(/jel/jelclass[@package = current()/@package and not(@interface = 'true')])"/>
                        </td>
                        <td>
                            <xsl:value-of select="count(/jel/jelclass[@package = current()/@package and @interface = 'true'])"/>
                        </td>
                        <td>
                            <xsl:value-of select="count(/jel/jelclass[@package = current()/@package])"/>
                        </td>
                    </tr>
                </xsl:for-each>
                </table>
                </div>

                <div class="packages">    
                <xsl:for-each select="/jel/jelclass[not(@package = preceding-sibling::jelclass/@package)]">
                    <xsl:sort select="@package" />
                    <div class="package">    
                    <p class="title">
                        <xsl:value-of select="@package" />
                    </p>
                
                    <p class="classes">
                    <xsl:for-each select="/jel/jelclass[@package = current()/@package]">
                        <xsl:sort select="@type" />
                        <span>
                            <xsl:if test="@interface='true'">
                                <xsl:attribute name="class">interface</xsl:attribute>
                            </xsl:if>
                            <xsl:value-of select="@type" />
                        </span><br/>
                    </xsl:for-each>
                    </p>
                    </div>
                </xsl:for-each>
                </div>

                <xsl:for-each select="/jel/jelclass">
                    <xsl:sort select="@type" />
                    <div class="spec">
                    <p class="title">
                        <i><xsl:value-of select="@package" /></i>
                        <span class="class">
                            <xsl:if test="@interface='true'">
                                <xsl:attribute name="class">interface</xsl:attribute>
                            </xsl:if>
                            <xsl:value-of select="@type" />
                        </span>
                    </p>
                    
                    <div class="comment">
						<p>
                        <!--<xsl:value-of select="comment/description" />-->
                        <!-- TODO add see annotations -->
                        
                        <xsl:call-template name="globalReplace">
                            <xsl:with-param name="outputString" select="comment/description"/>
                            <xsl:with-param name="target" select="'>'"/>
                            <xsl:with-param name="replacement" select="'>'"/>
                        </xsl:call-template>

                    </p>
						</div>
                    
                    <p class="class">
                        <xsl:value-of select="@visibility" /><xsl:text> </xsl:text>
                        <xsl:if test="@abstract = 'true' and not(@interface = 'true')"><xsl:text> </xsl:text>abstract </xsl:if>
                        <xsl:choose>
                            <xsl:when test="@interface='true'">interface<xsl:text> </xsl:text>
                                <span class="name"><xsl:value-of select="@type" /></span>
                                <xsl:if test="count(implements/interface)>0">
                                    <xsl:text> extends </xsl:text>
                                    <xsl:for-each select="implements/interface">
                                        <xsl:if test="position()>1"><xsl:text>, </xsl:text></xsl:if>
                                        <span class="type"><xsl:value-of select="@type" /></span>
                                    </xsl:for-each>
                                </xsl:if>
                            </xsl:when>
                            <xsl:otherwise>class<xsl:text> </xsl:text>
                                <span class="name"><xsl:value-of select="@type" /></span>
                                <xsl:text> extends </xsl:text><xsl:value-of select="@superclass" />
                                <xsl:if test="count(implements/interface)>0">
                                    <xsl:text> implements </xsl:text>
                                    <xsl:for-each select="implements/interface">
                                        <xsl:if test="position()>1"><xsl:text>, </xsl:text></xsl:if>
                                        <span class="type"><xsl:value-of select="@type" /></span>
                                    </xsl:for-each>
                                </xsl:if>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:text> {</xsl:text>
                        <xsl:if test="comment/attribute/@name='@deprecated'"> <span class="deprecated">//deprecated</span></xsl:if>
                    </p>
                        
                    <xsl:if test="count(fields/field[@static='true'])>0">
                        <xsl:for-each select="fields/field[@static='true']">
                            <xsl:sort select="@name" />
                            <xsl:apply-templates select="."/>
                        </xsl:for-each>
                    </xsl:if>
                    
                    <xsl:if test="count(fields/field[not(@static='true')])>0">
                        <xsl:for-each select="fields/field[not(@static='true')]">
                            <xsl:sort select="@name" />
                            <xsl:apply-templates select="."/>
                        </xsl:for-each>
                    </xsl:if>
                    
                    <xsl:if test="count(methods/constructor)>0">
                        <xsl:apply-templates select="methods/constructor" />
                    </xsl:if>
                    
                    <xsl:if test="count(methods/method[@static='true'])>0">
                        <xsl:for-each select="methods/method[@static='true']">
                            <xsl:sort select="@name" />
                            <xsl:apply-templates select="."/>
                        </xsl:for-each>
                    </xsl:if>
                    
                    <xsl:if test="count(methods/method[not(@static='true')])>0">
                        <xsl:for-each select="methods/method[not(@static='true')]">
                            <xsl:sort select="@name" />
                            <xsl:apply-templates select="."/>
                        </xsl:for-each>
                    </xsl:if>
                    <p class="class">}</p>
                                        
                    <xsl:variable name="cls" select="@type"/>
                    
                    <xsl:if test="count(/jel/jelclass[@superclass=$cls]) > 0">
                        <p class="related"><span class="header">Extended by: </span> 
                        <xsl:for-each select="/jel/jelclass[@superclass=$cls]">
                            <xsl:if test="position()>1"><xsl:text>, </xsl:text></xsl:if>
                            <xsl:apply-templates select="@type"/>
                        </xsl:for-each>
                        </p>
                    </xsl:if>
     
                    <xsl:if test="count(/jel/jelclass/implements/interface[@type=$cls]) > 0">
                        <p class="related"><span class="header">Implemented by: </span>
                        <xsl:for-each select="/jel/jelclass/implements/interface[@type=$cls]">
                            <xsl:if test="position()>1"><xsl:text>, </xsl:text></xsl:if>
                            <xsl:apply-templates select="../../@type"/>
                        </xsl:for-each>
                        </p>
                    </xsl:if>
                    
                    <xsl:if test="count(/jel/jelclass/methods/method[@type=$cls]) > 0">
                        <p class="related"><span class="header">Returned by: </span>
                        <xsl:for-each select="/jel/jelclass/methods/method[@type=$cls]">
                            <xsl:if test="position()>1"><xsl:text>, </xsl:text></xsl:if>
                            <xsl:apply-templates select="../../@type"/>.<xsl:apply-templates select="@name"/><xsl:text>()</xsl:text>
                        </xsl:for-each>
                        </p>
                    </xsl:if>
     
                    <xsl:if test="count(/jel/jelclass/methods/method/params/param[@type=$cls]) > 0">
                        <p class="related"><span class="header">Passed to: </span>
                        <xsl:for-each select="/jel/jelclass/methods/method/params/param[@type=$cls]">
                            <xsl:if test="position()>1"><xsl:text>, </xsl:text></xsl:if>
                            <xsl:apply-templates select="../../../../@type"/>.<xsl:apply-templates select="../../@name"/><xsl:text>()</xsl:text>
                        </xsl:for-each>
                        </p>
                    </xsl:if>
                    
                    <xsl:if test="@superclass">
                        <p class="related"><span class="header">Hierarchy: </span>
                            <xsl:apply-templates select="."/>
                        </p>
                    </xsl:if>
                    
                    </div>
                </xsl:for-each>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template match="jelclass">
        <xsl:variable name="superclass" select="@superclass"/>
        <xsl:choose>
            <xsl:when test="$superclass='Object'">
                <xsl:text>Object &#x21e8; </xsl:text>
            </xsl:when>
            <xsl:when test="not(/jel/jelclass[@type=$superclass])">
                <xsl:value-of select="$superclass"/><xsl:text> &#x21e8; </xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="/jel/jelclass[@type=$superclass]"/>
                <xsl:text> &#x21e8; </xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        
        

        <xsl:value-of select="@type" />
        <xsl:if test="count(implements/interface)>0">
            <xsl:text>(</xsl:text>
            <xsl:for-each select="implements/interface">
                <xsl:if test="position()>1">, </xsl:if>
                <xsl:value-of select="@type"/>
            </xsl:for-each>
            <xsl:text>)</xsl:text>
        </xsl:if>
    </xsl:template>
         
    <xsl:template match="field">
        <p class="field">
        <xsl:value-of select="@visibility" />
        <xsl:text> </xsl:text>
        <xsl:if test="@static='true'">static </xsl:if>
        <xsl:if test="@final='true'">final </xsl:if>
        <span class="type"><xsl:value-of select="@type" /></span>
        <xsl:text> </xsl:text>
        <span class="name"><xsl:value-of select="@name" /></span>
        <xsl:text>;</xsl:text>
        <xsl:if test="comment/attribute/@name='@deprecated'"> <span class="deprecated"> // deprecated</span></xsl:if>
        </p>
    </xsl:template>
    
    <xsl:template match="method">
        <p class="method">
        <xsl:value-of select="@visibility" />
        <xsl:text> </xsl:text>
        <xsl:if test="@static='true'">static </xsl:if>
        <xsl:if test="@final='true'">final </xsl:if>
        <span class="type"><xsl:value-of select="@type" /></span>
        <xsl:text> </xsl:text>
        <span class="name"><xsl:value-of select="@name" /></span>(<xsl:apply-templates select="params" />
        <xsl:text>);</xsl:text>
        <xsl:if test="comment/attribute/@name='@deprecated'"> <span class="deprecated"> // deprecated</span></xsl:if>
        </p>
    </xsl:template>
    
    <xsl:template match="constructor">
        <p class="constructor">
        <xsl:value-of select="@visibility" />
        <xsl:if test="@final='true'">final </xsl:if>
        <xsl:text> </xsl:text>
        <span class="name"><xsl:value-of select="@name" /></span>(<xsl:apply-templates select="params" />
        <xsl:text>);</xsl:text>
        <xsl:if test="comment/attribute/@name='@deprecated'"> <span class="deprecated"> // deprecated</span></xsl:if>
        </p>
    </xsl:template>
    
    <xsl:template match="params">
        <xsl:for-each select="param">
            <xsl:if test="position()>1">, </xsl:if>
            <xsl:value-of select="@type" />
            <xsl:text> </xsl:text>
            <span class="type"><xsl:value-of select="@name" /></span>
        </xsl:for-each>
    </xsl:template>
    
    
    <!-- see http://www.xml.com/pub/a/2002/06/05/transforming.html -->
    <xsl:template name="globalReplace">
      <xsl:param name="outputString"/>
      <xsl:param name="target"/>
      <xsl:param name="replacement"/>
      <xsl:choose>
        <xsl:when test="contains($outputString,$target)">
       
          <xsl:value-of select=
            "concat(substring-before($outputString,$target),
                   '>')"/>
          <xsl:call-template name="globalReplace">
            <xsl:with-param name="outputString" 
                 select="substring-after($outputString,$target)"/>
            <xsl:with-param name="target" select="$target"/>
            <xsl:with-param name="replacement" 
                 select="$replacement"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$outputString"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
