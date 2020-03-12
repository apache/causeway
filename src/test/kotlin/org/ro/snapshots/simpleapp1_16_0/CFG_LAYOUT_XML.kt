package org.ro.snapshots.simpleapp1_16_0

import org.ro.snapshots.Response

object CFG_LAYOUT_XML: Response() {
    override val url= "http://localhost:8080/restful/objects/isisApplib.ConfigurationProperty/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI_Pgo8Y29uZmlndXJhdGlvblByb3BlcnR5PgogICAgPGtleT5pc2lzLmFwcE1hbmlmZXN0PC9rZXk-CiAgICA8dmFsdWU-ZG9tYWluYXBwLmFwcGxpY2F0aW9uLm1hbmlmZXN0LkRvbWFpbkFwcEFwcE1hbmlmZXN0PC92YWx1ZT4KPC9jb25maWd1cmF0aW9uUHJvcGVydHk-Cg==/object-layout"
    override val str = """
<bs3:grid xmlns:cpt="http://isis.apache.org/applib/layout/component" xmlns:lnk="http://isis.apache.org/applib/layout/links" xmlns:bs3="http://isis.apache.org/applib/layout/grid/bootstrap3">
    <bs3:row>
        <bs3:col span="12" unreferencedActions="true">
            <cpt:domainObject>
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/element</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>http://localhost:8080/restful/objects/isisApplib.ConfigurationProperty/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI_Pgo8Y29uZmlndXJhdGlvblByb3BlcnR5PgogICAgPGtleT5pc2lzLmFwcE1hbmlmZXN0PC9rZXk-CiAgICA8dmFsdWU-ZG9tYWluYXBwLmFwcGxpY2F0aW9uLm1hbmlmZXN0LkRvbWFpbkFwcEFwcE1hbmlmZXN0PC92YWx1ZT4KPC9jb25maWd1cmF0aW9uUHJvcGVydHk-Cg==</lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object"</lnk:type>
                </cpt:link>
            </cpt:domainObject>
            <cpt:action hidden="EVERYWHERE" id="clearHints">
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>http://localhost:8080/restful/objects/isisApplib.ConfigurationProperty/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI_Pgo8Y29uZmlndXJhdGlvblByb3BlcnR5PgogICAgPGtleT5pc2lzLmFwcE1hbmlmZXN0PC9rZXk-CiAgICA8dmFsdWU-ZG9tYWluYXBwLmFwcGxpY2F0aW9uLm1hbmlmZXN0LkRvbWFpbkFwcEFwcE1hbmlmZXN0PC92YWx1ZT4KPC9jb25maWd1cmF0aW9uUHJvcGVydHk-Cg==/actions/clearHints</lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                </cpt:link>
            </cpt:action>
            <cpt:action id="openRestApi">
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>http://localhost:8080/restful/objects/isisApplib.ConfigurationProperty/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI_Pgo8Y29uZmlndXJhdGlvblByb3BlcnR5PgogICAgPGtleT5pc2lzLmFwcE1hbmlmZXN0PC9rZXk-CiAgICA8dmFsdWU-ZG9tYWluYXBwLmFwcGxpY2F0aW9uLm1hbmlmZXN0LkRvbWFpbkFwcEFwcE1hbmlmZXN0PC92YWx1ZT4KPC9jb25maWd1cmF0aW9uUHJvcGVydHk-Cg==/actions/openRestApi</lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                </cpt:link>
            </cpt:action>
        </bs3:col>
    </bs3:row>
    <bs3:row>
        <bs3:col span="12">
            <bs3:tabGroup>
                <bs3:tab name="General">
                    <bs3:row>
                        <bs3:col span="8">
                            <cpt:fieldSet name="Property" id="key">
<cpt:action id="downloadLayoutXml" position="PANEL_DROPDOWN">
    <cpt:link>
        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
        <lnk:method>GET</lnk:method>
        <lnk:href>http://localhost:8080/restful/objects/isisApplib.ConfigurationProperty/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI_Pgo8Y29uZmlndXJhdGlvblByb3BlcnR5PgogICAgPGtleT5pc2lzLmFwcE1hbmlmZXN0PC9rZXk-CiAgICA8dmFsdWU-ZG9tYWluYXBwLmFwcGxpY2F0aW9uLm1hbmlmZXN0LkRvbWFpbkFwcEFwcE1hbmlmZXN0PC92YWx1ZT4KPC9jb25maWd1cmF0aW9uUHJvcGVydHk-Cg==/actions/downloadLayoutXml</lnk:href>
        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
    </cpt:link>
</cpt:action>
<cpt:action id="rebuildMetamodel" position="PANEL_DROPDOWN">
    <cpt:link>
        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
        <lnk:method>GET</lnk:method>
        <lnk:href>http://localhost:8080/restful/objects/isisApplib.ConfigurationProperty/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI_Pgo8Y29uZmlndXJhdGlvblByb3BlcnR5PgogICAgPGtleT5pc2lzLmFwcE1hbmlmZXN0PC9rZXk-CiAgICA8dmFsdWU-ZG9tYWluYXBwLmFwcGxpY2F0aW9uLm1hbmlmZXN0LkRvbWFpbkFwcEFwcE1hbmlmZXN0PC92YWx1ZT4KPC9jb25maWd1cmF0aW9uUHJvcGVydHk-Cg==/actions/rebuildMetamodel</lnk:href>
        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
    </cpt:link>
</cpt:action>
<cpt:property id="key">
    <cpt:link>
        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
        <lnk:method>GET</lnk:method>
        <lnk:href>http://localhost:8080/restful/objects/isisApplib.ConfigurationProperty/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI_Pgo8Y29uZmlndXJhdGlvblByb3BlcnR5PgogICAgPGtleT5pc2lzLmFwcE1hbmlmZXN0PC9rZXk-CiAgICA8dmFsdWU-ZG9tYWluYXBwLmFwcGxpY2F0aW9uLm1hbmlmZXN0LkRvbWFpbkFwcEFwcE1hbmlmZXN0PC92YWx1ZT4KPC9jb25maWd1cmF0aW9uUHJvcGVydHk-Cg==/properties/key</lnk:href>
        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"</lnk:type>
    </cpt:link>
</cpt:property>
<cpt:property id="value" multiLine="5">
    <cpt:link>
        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
        <lnk:method>GET</lnk:method>
        <lnk:href>http://localhost:8080/restful/objects/isisApplib.ConfigurationProperty/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI_Pgo8Y29uZmlndXJhdGlvblByb3BlcnR5PgogICAgPGtleT5pc2lzLmFwcE1hbmlmZXN0PC9rZXk-CiAgICA8dmFsdWU-ZG9tYWluYXBwLmFwcGxpY2F0aW9uLm1hbmlmZXN0LkRvbWFpbkFwcEFwcE1hbmlmZXN0PC92YWx1ZT4KPC9jb25maWd1cmF0aW9uUHJvcGVydHk-Cg==/properties/value</lnk:href>
        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"</lnk:type>
    </cpt:link>
</cpt:property>
                            </cpt:fieldSet>
                        </bs3:col>
                    </bs3:row>
                </bs3:tab>
                <bs3:tab name="Other">
                    <bs3:row>
                        <bs3:col span="12">
                            <cpt:fieldSet name="Other" id="other" unreferencedProperties="true"/>
                        </bs3:col>
                    </bs3:row>
                </bs3:tab>
            </bs3:tabGroup>
        </bs3:col>
    </bs3:row>
    <bs3:row>
        <bs3:col span="12">
            <bs3:tabGroup unreferencedCollections="true"/>
        </bs3:col>
    </bs3:row>
</bs3:grid> 
"""
}
