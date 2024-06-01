<?xml version="1.0" encoding="UTF-8"?>
<!--
 * Copyright (c) 2024 T-Systems International GmbH
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:sparql="http://www.w3.org/2005/sparql-results#"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:semanticid="urn:bamm:io.catenax.part_site_information_as_planned:1.0.0#PartSiteInformationAsPlanned"
                xmlns:xs="http://www.w3.org/2001/XMLSchema">
    <xsl:variable name="domain" select="'traceability'"/>
    <xsl:variable name="semanticid"
                  select="'urn:bamm:io.catenax.part_site_information_as_planned:1.0.0#PartSiteInformationAsPlanned'"/>
    <xsl:output method="xml"/>
    <xsl:template name="genAssetId">
        <xsl:value-of select="$domain"/>/<xsl:value-of select="./sparql:binding[@name='catenaXId']/sparql:uri"/>
    </xsl:template>
    <xsl:template name="genSubmodelId">
        <xsl:value-of select="$domain"/>/<xsl:value-of select="$semanticid"/>/<xsl:value-of
            select="./sparql:binding[@name='catenaXId']/sparql:uri"/>
    </xsl:template>
    <xsl:key name="catenax-id" match="//sparql:result" use="sparql:binding[@name='catenaXId']/sparql:uri"/>
    <xsl:template name="substring-after-last">
        <xsl:param name="input"/>
        <xsl:param name="substr"/>

        <!-- Extract the string which comes after the first occurence -->
        <xsl:variable name="temp" select="substring-after($input,$substr)"/>

        <xsl:choose>
            <!-- If it still contains the search string the recursively process -->
            <xsl:when test="$substr and contains($temp,$substr)">
                <xsl:call-template name="substring-after-last">
                    <xsl:with-param name="input" select="$temp"/>
                    <xsl:with-param name="substr" select="$substr"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$temp"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="root" match="/">
        <aas:environment xmlns:aas="https://admin-shell.io/aas/3/0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="">
            <aas:assetAdministrationShells>
                <xsl:for-each
                        select="//sparql:result[count(. | key('catenax-id', ./sparql:binding[@name='catenaXId']/sparql:uri)[1]) = 1]">
                    <aas:assetAdministrationShell>
                        <aas:idShort>
                            <xsl:call-template name="genAssetId"/>
                        </aas:idShort>
                        <aas:id>
                            <xsl:call-template name="genAssetId"/>
                        </aas:id>
                        <aas:assetInformation>
                            <aas:assetKind>Instance</aas:assetKind>
                            <aas:globalAssetId>
                                <xsl:call-template name="genAssetId"/>
                            </aas:globalAssetId>
                        </aas:assetInformation>
                        <aas:submodels>
                            <aas:reference>
                                <aas:type>ExternalReference</aas:type>
                                <aas:keys>
                                    <aas:key>
                                        <aas:type>Submodel</aas:type>
                                        <aas:value>
                                            <xsl:call-template name="genSubmodelId"/>
                                        </aas:value>
                                    </aas:key>
                                </aas:keys>
                            </aas:reference>
                        </aas:submodels>
                    </aas:assetAdministrationShell>
                </xsl:for-each>
            </aas:assetAdministrationShells>
            <aas:submodels>
                <xsl:for-each
                        select="//sparql:result[count(. | key('catenax-id', ./sparql:binding[@name='catenaXId']/sparql:uri)[1]) = 1]">
                    <aas:submodel>
                        <aas:kind>Instance</aas:kind>
                        <aas:semanticId>
                            <aas:type>ModelReference</aas:type>
                            <aas:keys>
                                <aas:key>
                                    <aas:type>ConceptDescription</aas:type>
                                    <aas:value>
                                        <xsl:value-of select="$semanticid"/>
                                    </aas:value>
                                </aas:key>
                            </aas:keys>
                        </aas:semanticId>
                        <aas:id>
                            <xsl:call-template name="genSubmodelId"/>
                        </aas:id>
                        <aas:idShort>PartSiteInformationAsPlanned</aas:idShort>
                        <aas:description>
                            <aas:langStringTextType>
                                <aas:language>en</aas:language>
                                <aas:text>The aspect provides site related information for a given as planned item (i.e.
                                    a part type or part instance that is uniquely identifiable within Catena-X via its
                                    Catena-X ID). A site is a delimited geographical area where a legal entity does
                                    business. In the "as planned" lifecycle context all potentially related sites are
                                    listed including all sites where e.g. production of this part (type) is planned.
                                </aas:text>
                            </aas:langStringTextType>
                        </aas:description>
                        <aas:submodelElements>
                            <aas:property>
                                <aas:category>Key</aas:category>
                                <aas:idShort>catenaXId</aas:idShort>
                                <aas:description>
                                    <aas:langStringTextType>
                                        <aas:language>en</aas:language>
                                        <aas:text>The Catena-X ID of the given item (i.e. a part type or part instance),
                                            valid for the Catena-X dataspace.
                                        </aas:text>
                                    </aas:langStringTextType>
                                </aas:description>
                                <aas:displayName>
                                    <aas:langStringNameType>
                                        <aas:language>en</aas:language>
                                        <aas:text>Catena-X Identifier</aas:text>
                                    </aas:langStringNameType>
                                </aas:displayName>
                                <aas:semanticId>
                                    <aas:type>ModelReference</aas:type>
                                    <aas:keys>
                                        <aas:key>
                                            <aas:type>ConceptDescription</aas:type>
                                            <aas:value>
                                                urn:bamm:io.catenax.part_site_information_as_planned:1.0.0#catenaXId
                                            </aas:value>
                                        </aas:key>
                                    </aas:keys>
                                </aas:semanticId>
                                <aas:valueType>xs:string</aas:valueType>
                                <aas:value>
                                    <xsl:value-of select="./sparql:binding[@name='catenaXId']/sparql:uri"/>
                                </aas:value>
                            </aas:property>
                            <aas:submodelElementCollection>
                                <aas:idShort>sites</aas:idShort>
                                <aas:description>
                                    <aas:langStringTextType>
                                        <aas:language>en</aas:language>
                                        <aas:text>A site is a delimited geographical area where a legal entity does
                                            business (geographical address with geo coordinates).A site always has a
                                            primary physical address. It is possible that further physical addresses are
                                            specified for the site. P.O. box details are only possible in addition to
                                            the physical address. A site has a 1:n relation to addresses, means at least
                                            1 address is necessary and multiple addresses are possible.
                                        </aas:text>
                                    </aas:langStringTextType>
                                </aas:description>
                                <aas:displayName>
                                    <aas:langStringNameType>
                                        <aas:language>en</aas:language>
                                        <aas:text>Sites</aas:text>
                                    </aas:langStringNameType>
                                </aas:displayName>
                                <aas:value>
                                    <xsl:for-each
                                            select="key('catenax-id',./sparql:binding[@name='catenaXId']/sparql:uri)">
                                        <aas:submodelElementCollection>
                                            <aas:idShort>SiteEntity/<xsl:value-of
                                                    select="./sparql:binding[@name='childCatenaXId']/sparql:uri"/>
                                            </aas:idShort>
                                            <aas:description>
                                                <aas:langStringTextType>
                                                    <aas:language>en</aas:language>
                                                    <aas:text>Catena-X Site ID and meta data of the site entity.
                                                    </aas:text>
                                                </aas:langStringTextType>
                                            </aas:description>
                                            <aas:displayName>
                                                <aas:langStringNameType>
                                                    <aas:language>en</aas:language>
                                                    <aas:text>Site Entity</aas:text>
                                                </aas:langStringNameType>
                                            </aas:displayName>
                                            <aas:value>
                                                <aas:property>
                                                    <aas:category>Key</aas:category>
                                                    <aas:idShort>catenaXsiteId</aas:idShort>
                                                    <aas:description>
                                                        <aas:langStringTextType>
                                                            <aas:language>en</aas:language>
                                                            <aas:text>The identifier of the site according to Catena-X
                                                                BPDM. The catenaXsiteId must be a valid Catena-X BPN.
                                                                The BPN is a unique, unchangeable identifier for
                                                                Business Partners / company locations from foundation to
                                                                closure, regardless of the different business
                                                                relationships / structures between or within the
                                                                Business Partners or company locations.
                                                            </aas:text>
                                                        </aas:langStringTextType>
                                                    </aas:description>
                                                    <aas:displayName>
                                                        <aas:langStringNameType>
                                                            <aas:language>en</aas:language>
                                                            <aas:text>Catena-X Site Identitfier</aas:text>
                                                        </aas:langStringNameType>
                                                    </aas:displayName>
                                                    <aas:semanticId>
                                                        <aas:type>ModelReference</aas:type>
                                                        <aas:keys>
                                                            <aas:key>
                                                                <aas:type>ConceptDescription</aas:type>
                                                                <aas:value>
                                                                    urn:bamm:io.catenax.part_site_information_as_planned:1.0.0#catenaXsiteId
                                                                </aas:value>
                                                            </aas:key>
                                                        </aas:keys>
                                                    </aas:semanticId>
                                                    <aas:valueType>xs:string</aas:valueType>
                                                    <aas:value>
                                                        <xsl:call-template name="substring-after-last">
                                                            <xsl:with-param name="input"
                                                                            select="./sparql:binding[@name='site']/sparql:uri"/>
                                                            <xsl:with-param name="substr" select="':'"/>
                                                        </xsl:call-template>
                                                    </aas:value>
                                                </aas:property>
                                                <aas:property>
                                                    <aas:category>Key</aas:category>
                                                    <aas:idShort>function</aas:idShort>
                                                    <aas:description>
                                                        <aas:langStringTextType>
                                                            <aas:language>en</aas:language>
                                                            <aas:text>The function of the site in relation to the part
                                                                (i.e. the activity within the value chain of the part
                                                                that is performed at the site)
                                                            </aas:text>
                                                        </aas:langStringTextType>
                                                    </aas:description>
                                                    <aas:displayName>
                                                        <aas:langStringNameType>
                                                            <aas:language>en</aas:language>
                                                            <aas:text>Catena-X Site Identitfier</aas:text>
                                                        </aas:langStringNameType>
                                                    </aas:displayName>
                                                    <aas:semanticId>
                                                        <aas:type>ModelReference</aas:type>
                                                        <aas:keys>
                                                            <aas:key>
                                                                <aas:type>ConceptDescription</aas:type>
                                                                <aas:value>
                                                                    urn:bamm:io.catenax.part_site_information_as_planned:1.0.0#function
                                                                </aas:value>
                                                            </aas:key>
                                                        </aas:keys>
                                                    </aas:semanticId>
                                                    <aas:valueType>xs:string</aas:valueType>
                                                    <aas:value>
                                                        <xsl:value-of
                                                                select="./sparql:binding[@name='function']/sparql:literal"/>
                                                    </aas:value>
                                                </aas:property>
                                                <aas:property>
                                                    <aas:category>Time</aas:category>
                                                    <aas:idShort>functionValidFrom</aas:idShort>
                                                    <aas:description>
                                                        <aas:langStringTextType>
                                                            <aas:language>en</aas:language>
                                                            <aas:text>Timestamp, from when the site has the specified
                                                                function for the given part
                                                            </aas:text>
                                                        </aas:langStringTextType>
                                                    </aas:description>
                                                    <aas:displayName>
                                                        <aas:langStringNameType>
                                                            <aas:language>en</aas:language>
                                                            <aas:text>Function valid from</aas:text>
                                                        </aas:langStringNameType>
                                                    </aas:displayName>
                                                    <aas:semanticId>
                                                        <aas:type>ModelReference</aas:type>
                                                        <aas:keys>
                                                            <aas:key>
                                                                <aas:type>ConceptDescription</aas:type>
                                                                <aas:value>
                                                                    urn:bamm:io.catenax.part_site_information_as_planned:1.0.0#functionValidFrom
                                                                </aas:value>
                                                            </aas:key>
                                                        </aas:keys>
                                                    </aas:semanticId>
                                                    <aas:valueType>xs:dateTime</aas:valueType>
                                                    <aas:value>
                                                        <xsl:value-of
                                                                select="./sparql:binding[@name='roleValidFrom']/sparql:literal"/>
                                                    </aas:value>
                                                </aas:property>
                                                <aas:property>
                                                    <aas:category>Time</aas:category>
                                                    <aas:idShort>functionValidUntil</aas:idShort>
                                                    <aas:description>
                                                        <aas:langStringTextType>
                                                            <aas:language>en</aas:language>
                                                            <aas:text>Timestamp, until when the site has the specified
                                                                function for the given part
                                                            </aas:text>
                                                        </aas:langStringTextType>
                                                    </aas:description>
                                                    <aas:displayName>
                                                        <aas:langStringNameType>
                                                            <aas:language>en</aas:language>
                                                            <aas:text>Function Valid Until</aas:text>
                                                        </aas:langStringNameType>
                                                    </aas:displayName>
                                                    <aas:semanticId>
                                                        <aas:type>ModelReference</aas:type>
                                                        <aas:keys>
                                                            <aas:key>
                                                                <aas:type>ConceptDescription</aas:type>
                                                                <aas:value>
                                                                    urn:bamm:io.catenax.part_site_information_as_planned:1.0.0#functionValidUntil
                                                                </aas:value>
                                                            </aas:key>
                                                        </aas:keys>
                                                    </aas:semanticId>
                                                    <aas:valueType>xs:dateTime</aas:valueType>
                                                    <aas:value>
                                                        <xsl:value-of
                                                                select="./sparql:binding[@name='roleValidTo']/sparql:literal"/>
                                                    </aas:value>
                                                </aas:property>
                                            </aas:value>
                                        </aas:submodelElementCollection>
                                    </xsl:for-each>
                                </aas:value>
                            </aas:submodelElementCollection>
                        </aas:submodelElements>
                    </aas:submodel>
                </xsl:for-each>
            </aas:submodels>
        </aas:environment>
    </xsl:template>
</xsl:stylesheet>