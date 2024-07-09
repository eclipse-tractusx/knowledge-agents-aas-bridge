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
                xmlns:semanticid="urn:bamm:io.catenax.single_level_usage_as_planned:1.1.0#SingleLevelUsageAsPlanned"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xs="http://www.w3.org/2001/XMLSchema">
    <xsl:variable name="domain" select="'traceability'"/>
    <xsl:variable name="semanticid"  select="'urn:bamm:io.catenax.single_level_usage_as_planned:1.1.0#SingleLevelUsageAsPlanned'"/>
    <xsl:output method="xml" />
    <xsl:template name="genAssetId">
        <xsl:value-of select="$domain"/>/<xsl:value-of select="./sparql:binding[@name='catenaXId']/sparql:uri"/>
    </xsl:template>
    <xsl:template name="genSubmodelId">
        <xsl:value-of select="$domain"/>/<xsl:value-of select="$semanticid"/>/<xsl:value-of select="./sparql:binding[@name='catenaXId']/sparql:uri"/>
    </xsl:template>
    <xsl:key name="catenax-id" match="//sparql:result" use="sparql:binding[@name='catenaXId']/sparql:uri" />
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
                <xsl:for-each select="//sparql:result[count(. | key('catenax-id', ./sparql:binding[@name='catenaXId']/sparql:uri)[1]) = 1]">
                    <aas:assetAdministrationShell>
                        <aas:idShort><xsl:call-template name="genAssetId"/></aas:idShort>
                        <aas:id><xsl:call-template name="genAssetId"/></aas:id>
                        <aas:assetInformation>
                            <aas:assetKind>Instance</aas:assetKind>
                            <aas:globalAssetId><xsl:call-template name="genAssetId"/></aas:globalAssetId>
                        </aas:assetInformation>
                        <aas:submodels>
                            <aas:reference>
                                <aas:type>ExternalReference</aas:type>
                                <aas:keys>
                                    <aas:key>
                                        <aas:type>Submodel</aas:type>
                                        <aas:value><xsl:call-template name="genSubmodelId"/></aas:value>
                                    </aas:key>
                                </aas:keys>
                            </aas:reference>
                        </aas:submodels>
                    </aas:assetAdministrationShell>
                </xsl:for-each>
            </aas:assetAdministrationShells>
            <aas:submodels>
                <xsl:for-each select="//sparql:result[count(. | key('catenax-id', ./sparql:binding[@name='catenaXId']/sparql:uri)[1]) = 1]">
                    <aas:submodel>
                    <aas:kind>Instance</aas:kind>
                    <aas:semanticId>
                        <aas:type>ModelReference</aas:type>
                        <aas:keys>
                            <aas:key>
                                <aas:type>ConceptDescription</aas:type>
                                <aas:value><xsl:value-of select="$semanticid"/></aas:value>
                            </aas:key>
                        </aas:keys>
                    </aas:semanticId>
                    <aas:id><xsl:call-template name="genSubmodelId"/></aas:id>
                    <aas:idShort>SingleLevelUsageAsPlanned</aas:idShort>
                    <aas:description>
                        <aas:langStringTextType>
                            <aas:language>en</aas:language>
                            <aas:text>The single-level Bill of Material represents one level of assemblies and does not include any super-assemblies. In As-Planned lifecycle state all variants are covered ("120% BoM").</aas:text>
                        </aas:langStringTextType>
                    </aas:description>
                    <aas:submodelElements>
                        <aas:property>
                            <aas:category>Key</aas:category>
                            <aas:idShort>catenaXId</aas:idShort>
                            <aas:description>
                                <aas:langStringTextType>
                                    <aas:language>en</aas:language>
                                    <aas:text>The Catena-X ID of the given part (e.g. the component), valid for the Catena-X dataspace.</aas:text>
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
                                        <aas:value>urn:bamm:io.catenax.single_level_usage_as_planned:1.1.0#catenaXId</aas:value>
                                    </aas:key>
                                </aas:keys>
                            </aas:semanticId>
                            <aas:valueType>xs:string</aas:valueType>
                            <aas:value><xsl:value-of select="./sparql:binding[@name='catenaXId']/sparql:uri"/></aas:value>
                        </aas:property>
                        <aas:submodelElementCollection>
                        <aas:idShort>parentParts</aas:idShort>
                        <aas:description>
                            <aas:langStringTextType>
                                <aas:language>en</aas:language>
                                <aas:text>Set of parent parts in As-Planned lifecycle phase, into which the given child object is assembled into (one structural level up).</aas:text>
                            </aas:langStringTextType>
                        </aas:description>
                        <aas:displayName>
                            <aas:langStringNameType>
                                <aas:language>en</aas:language>
                                <aas:text>Parent Parts</aas:text>
                            </aas:langStringNameType>
                        </aas:displayName>
                        <aas:value>
                        <xsl:for-each select="key('catenax-id',./sparql:binding[@name='catenaXId']/sparql:uri)">
                            <aas:submodelElementCollection>
                                <aas:idShort>ParentPart/<xsl:value-of select="./sparql:binding[@name='childCatenaXId']/sparql:uri"/></aas:idShort>
                                <aas:description>
                                    <aas:langStringTextType>
                                        <aas:language>en</aas:language>
                                        <aas:text>Catena-X ID and meta data of the parent item.</aas:text>
                                    </aas:langStringTextType>
                                </aas:description>
                                <aas:displayName>
                                    <aas:langStringNameType>
                                        <aas:language>en</aas:language>
                                        <aas:text>Parent Item</aas:text>
                                    </aas:langStringNameType>
                                </aas:displayName>
                                <aas:value>
                                <aas:property>
                                    <aas:category>Key</aas:category>
                                    <aas:idShort>parentCatenaXId</aas:idShort>
                                    <aas:description>
                                        <aas:langStringTextType>
                                            <aas:language>en</aas:language>
                                            <aas:text>The Catena-X ID of the parent object into which the given child part is assembled into.</aas:text>
                                        </aas:langStringTextType>
                                    </aas:description>
                                    <aas:displayName>
                                        <aas:langStringNameType>
                                            <aas:language>en</aas:language>
                                            <aas:text>Catena-X Parent ID</aas:text>
                                        </aas:langStringNameType>
                                    </aas:displayName>
                                    <aas:semanticId>
                                        <aas:type>ModelReference</aas:type>
                                        <aas:keys>
                                            <aas:key>
                                                <aas:type>ConceptDescription</aas:type>
                                                <aas:value>urn:bamm:io.catenax.single_level_usage_as_planned:1.1.0#parentCatenaXId</aas:value>
                                            </aas:key>
                                        </aas:keys>
                                    </aas:semanticId>
                                    <aas:valueType>xs:string</aas:valueType>
                                    <aas:value><xsl:value-of select="./sparql:binding[@name='parentCatenaXId']/sparql:uri"/></aas:value>
                                </aas:property>
                                <aas:property>
                                    <aas:category>Key</aas:category>
                                    <aas:idShort>businessPartner</aas:idShort>
                                    <aas:description>
                                        <aas:langStringTextType>
                                            <aas:language>en</aas:language>
                                            <aas:text>Business Partner Number of the Legal Entity Assemblying the Parent Item.</aas:text>
                                        </aas:langStringTextType>
                                    </aas:description>
                                    <aas:displayName>
                                        <aas:langStringNameType>
                                            <aas:language>en</aas:language>
                                            <aas:text>Catena-X Business Partner</aas:text>
                                        </aas:langStringNameType>
                                    </aas:displayName>
                                    <aas:semanticId>
                                        <aas:type>ModelReference</aas:type>
                                        <aas:keys>
                                            <aas:key>
                                                <aas:type>ConceptDescription</aas:type>
                                                <aas:value>urn:bamm:io.catenax.single_level_usage_as_planned:1.1.0#businessPartner</aas:value>
                                            </aas:key>
                                        </aas:keys>
                                    </aas:semanticId>
                                    <aas:valueType>xs:string</aas:valueType>
                                    <aas:value>
                                        <xsl:call-template name="substring-after-last">
                                            <xsl:with-param name="input" select="./sparql:binding[@name='businessPartner']/sparql:uri" />
                                            <xsl:with-param name="substr" select="':'" />
                                        </xsl:call-template>
                                    </aas:value>
                                </aas:property>
                                <aas:property>
                                    <aas:category>Time</aas:category>
                                    <aas:idShort>createdOn</aas:idShort>
                                    <aas:description>
                                        <aas:langStringTextType>
                                            <aas:language>en</aas:language>
                                            <aas:text>Timestamp when the relation between the parent part and the child item was created.</aas:text>
                                        </aas:langStringTextType>
                                    </aas:description>
                                    <aas:displayName>
                                        <aas:langStringNameType>
                                            <aas:language>en</aas:language>
                                            <aas:text>Created on</aas:text>
                                        </aas:langStringNameType>
                                    </aas:displayName>
                                    <aas:semanticId>
                                        <aas:type>ModelReference</aas:type>
                                        <aas:keys>
                                            <aas:key>
                                                <aas:type>ConceptDescription</aas:type>
                                                <aas:value>urn:bamm:io.catenax.single_level_usage_as_planned:1.1.0#createdOn</aas:value>
                                            </aas:key>
                                        </aas:keys>
                                    </aas:semanticId>
                                    <aas:valueType>xs:dateTime</aas:valueType>
                                    <aas:value><xsl:value-of select="./sparql:binding[@name='productionStartDate']/sparql:literal"/></aas:value>
                                </aas:property>
                                <aas:property>
                                    <aas:category>Time</aas:category>
                                    <aas:idShort>lastModifiedOn</aas:idShort>
                                    <aas:description>
                                        <aas:langStringTextType>
                                            <aas:language>en</aas:language>
                                            <aas:text>Timestamp when the relationship between parent part and child part was last modified.</aas:text>
                                        </aas:langStringTextType>
                                    </aas:description>
                                    <aas:displayName>
                                        <aas:langStringNameType>
                                            <aas:language>en</aas:language>
                                            <aas:text>Last Modified on</aas:text>
                                        </aas:langStringNameType>
                                    </aas:displayName>
                                    <aas:semanticId>
                                        <aas:type>ModelReference</aas:type>
                                        <aas:keys>
                                            <aas:key>
                                                <aas:type>ConceptDescription</aas:type>
                                                <aas:value>urn:bamm:io.catenax.single_level_usage_as_planned:1.1.0#lastModifiedOn</aas:value>
                                            </aas:key>
                                        </aas:keys>
                                    </aas:semanticId>
                                    <aas:valueType>xs:dateTime</aas:valueType>
                                    <aas:value><xsl:value-of select="./sparql:binding[@name='productionEndDate']/sparql:literal"/></aas:value>
                                </aas:property>
                                <aas:submodelElementCollection>
                                    <aas:idShort>Quantity/<xsl:value-of select="./sparql:binding[@name='parentCatenaXId']/sparql:uri"/></aas:idShort>
                                    <aas:description>
                                        <aas:langStringTextType>
                                            <aas:language>en</aas:language>
                                            <aas:text>Comprises the number of objects and the unit of measurement for the respective child objects</aas:text>
                                        </aas:langStringTextType>
                                    </aas:description>
                                    <aas:displayName>
                                        <aas:langStringNameType>
                                            <aas:language>en</aas:language>
                                            <aas:text>Quantity</aas:text>
                                        </aas:langStringNameType>
                                    </aas:displayName>
                                    <aas:value>
                                        <aas:property>
                                            <aas:category>Value</aas:category>
                                            <aas:idShort>quantityNumber</aas:idShort>
                                            <aas:description>
                                                <aas:langStringTextType>
                                                    <aas:language>en</aas:language>
                                                    <aas:text>The number of objects related to the measurement unit.</aas:text>
                                                </aas:langStringTextType>
                                            </aas:description>
                                            <aas:displayName>
                                                <aas:langStringNameType>
                                                    <aas:language>en</aas:language>
                                                    <aas:text>Quantity Number</aas:text>
                                                </aas:langStringNameType>
                                            </aas:displayName>
                                            <aas:semanticId>
                                                <aas:type>ModelReference</aas:type>
                                                <aas:keys>
                                                    <aas:key>
                                                        <aas:type>ConceptDescription</aas:type>
                                                        <aas:value>urn:bamm:io.catenax.single_level_usage_as_planned:1.1.0#quantityNumber</aas:value>
                                                    </aas:key>
                                                </aas:keys>
                                            </aas:semanticId>
                                            <aas:valueType>xs:double</aas:valueType>
                                            <aas:value><xsl:value-of select="./sparql:binding[@name='parentQuantity']/sparql:literal"/></aas:value>
                                        </aas:property>
                                        <aas:property>
                                            <aas:category>Value</aas:category>
                                            <aas:idShort>measurementUnit</aas:idShort>
                                            <aas:description>
                                                <aas:langStringTextType>
                                                    <aas:language>en</aas:language>
                                                    <aas:text>Unit of measurement for the quantity of objects.
                                                        If possible, use units from the aspect meta model unit catalog, which is based on the UNECE Recommendation No. 20 "Codes for Units of Measure used in International Trade".</aas:text>
                                                </aas:langStringTextType>
                                            </aas:description>
                                            <aas:displayName>
                                                <aas:langStringNameType>
                                                    <aas:language>en</aas:language>
                                                    <aas:text>Measurement Unit</aas:text>
                                                </aas:langStringNameType>
                                            </aas:displayName>
                                            <aas:semanticId>
                                                <aas:type>ModelReference</aas:type>
                                                <aas:keys>
                                                    <aas:key>
                                                        <aas:type>ConceptDescription</aas:type>
                                                        <aas:value>urn:bamm:io.catenax.single_level_usage_as_planned:1.1.0#measurementUnit</aas:value>
                                                    </aas:key>
                                                </aas:keys>
                                            </aas:semanticId>
                                            <aas:valueType>xs:string</aas:valueType>
                                            <aas:value><xsl:value-of select="./sparql:binding[@name='billOfMaterialUnit']/sparql:uri"/></aas:value>
                                        </aas:property>
                                    </aas:value>
                                </aas:submodelElementCollection> <!--quantity -->
                        </aas:value>
                        </aas:submodelElementCollection> <!-- parent items -->
                        </xsl:for-each>
                        </aas:value>
                      </aas:submodelElementCollection> <!-- parent parts -->
                    </aas:submodelElements>
                    </aas:submodel>
                </xsl:for-each>
            </aas:submodels>
        </aas:environment>
    </xsl:template>
</xsl:stylesheet>