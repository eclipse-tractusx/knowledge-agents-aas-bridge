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
                xmlns:semanticid="urn:bamm:io.catenax.part_as_planned:1.0.1#PartAsPlanned"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xs="http://www.w3.org/2001/XMLSchema">
    <xsl:variable name="domain" select="'traceability'"/>
    <xsl:variable name="semanticid"  select="'urn:bamm:io.catenax.part_as_planned:1.0.1#PartAsPlanned'"/>
    <xsl:output method="xml" />
    <xsl:template name="genAssetId">
        <xsl:value-of select="$domain"/>/<xsl:value-of select="./sparql:binding[@name='catenaXId']/sparql:uri"/>
    </xsl:template>
    <xsl:template name="genSubmodelId">
        <xsl:value-of select="$domain"/>/<xsl:value-of select="$semanticid"/>/<xsl:value-of select="./sparql:binding[@name='catenaXId']/sparql:uri"/>
    </xsl:template>
    <xsl:key name="catenax-id" match="//sparql:result" use="sparql:binding[@name='catenaXId']/sparql:uri" />
    <xsl:template name="root" match="/">
        <aas:environment xmlns:aas="https://admin-shell.io/aas/3/0"
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
                            <xsl:for-each select="key('catenax-id',./sparql:binding[@name='catenaXId']/sparql:uri)">
                            <aas:reference>
                                <aas:type>ExternalReference</aas:type>
                                <aas:keys>
                                    <aas:key>
                                        <aas:type>Submodel</aas:type>
                                        <aas:value><xsl:call-template name="genSubmodelId"/></aas:value>
                                    </aas:key>
                                </aas:keys>
                            </aas:reference>
                            </xsl:for-each>
                        </aas:submodels>
                    </aas:assetAdministrationShell>
                </xsl:for-each>
            </aas:assetAdministrationShells>
            <aas:submodels>
                <xsl:for-each select="//sparql:result">
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
                        <aas:idShort>PartAsPlanned</aas:idShort>
                        <aas:description>
                            <aas:langStringTextType>
                                <aas:language>en</aas:language>
                                <aas:text>A Part AsPlanned represents an item in the Catena-X Bill of Material (BOM) in As-Planned lifecycle status. </aas:text>
                            </aas:langStringTextType>
                        </aas:description>
                        <aas:submodelElements>
                            <aas:property>
                                <aas:category>Key</aas:category>
                                <aas:idShort>catenaXId</aas:idShort>
                                <aas:description>
                                    <aas:langStringTextType>
                                        <aas:language>en</aas:language>
                                        <aas:text>The fully anonymous Catena-X ID of the serialized part, valid for the Catena-X dataspace.</aas:text>
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
                                            <aas:value>urn:bamm:io.catenax.part_as_planned:1.0.1#catenaXId</aas:value>
                                        </aas:key>
                                    </aas:keys>
                                </aas:semanticId>
                                <aas:valueType>xs:string</aas:valueType>
                                <aas:value><xsl:value-of select="./sparql:binding[@name='catenaXId']/sparql:uri"/></aas:value>
                            </aas:property>
                            <aas:submodelElementCollection>
                                <aas:idShort>partTypeInformation</aas:idShort>
                                <aas:description>
                                    <aas:langStringTextType>
                                        <aas:language>en</aas:language>
                                        <aas:text>Encapsulation for data related to the part type</aas:text>
                                    </aas:langStringTextType>
                                </aas:description>
                                <aas:displayName>
                                    <aas:langStringNameType>
                                        <aas:language>en</aas:language>
                                        <aas:text>Part Type Information Entity</aas:text>
                                    </aas:langStringNameType>
                                </aas:displayName>
                                <aas:value>
                                    <aas:property>
                                        <aas:category>Key</aas:category>
                                        <aas:idShort>manufacturerPartId</aas:idShort>
                                        <aas:description>
                                            <aas:langStringTextType>
                                                <aas:language>en</aas:language>
                                                <aas:text>Part ID as assigned by the manufacturer of the part. The Part ID identifies the part (as designed) in the manufacturer`s dataspace. The Part ID does not reference a specific instance of a part and thus should not be confused with the serial number.</aas:text>
                                            </aas:langStringTextType>
                                        </aas:description>
                                        <aas:displayName>
                                            <aas:langStringNameType>
                                                <aas:language>en</aas:language>
                                                <aas:text>Manufacturer Part ID</aas:text>
                                            </aas:langStringNameType>
                                        </aas:displayName>
                                        <aas:semanticId>
                                            <aas:type>ModelReference</aas:type>
                                            <aas:keys>
                                                <aas:key>
                                                    <aas:type>ConceptDescription</aas:type>
                                                    <aas:value>urn:bamm:io.catenax.part_as_planned:1.0.1#manufacturerPartId</aas:value>
                                                </aas:key>
                                            </aas:keys>
                                        </aas:semanticId>
                                        <aas:valueType>xs:string</aas:valueType>
                                        <aas:value><xsl:value-of select="./sparql:binding[@name='manufacturerPartId']/sparql:literal"/></aas:value>
                                    </aas:property>
                                    <aas:property>
                                        <aas:category>Value</aas:category>
                                        <aas:idShort>nameAtManufacturer</aas:idShort>
                                        <aas:description>
                                            <aas:langStringTextType>
                                                <aas:language>en</aas:language>
                                                <aas:text>Name of the part as assigned by the manufacturer.</aas:text>
                                            </aas:langStringTextType>
                                        </aas:description>
                                        <aas:displayName>
                                            <aas:langStringNameType>
                                                <aas:language>en</aas:language>
                                                <aas:text>Name at Manufacturer</aas:text>
                                            </aas:langStringNameType>
                                        </aas:displayName>
                                        <aas:semanticId>
                                            <aas:type>ModelReference</aas:type>
                                            <aas:keys>
                                                <aas:key>
                                                    <aas:type>ConceptDescription</aas:type>
                                                    <aas:value>urn:bamm:io.catenax.part_as_planned:1.0.1#nameAtManufacturer</aas:value>
                                                </aas:key>
                                            </aas:keys>
                                        </aas:semanticId>
                                        <aas:valueType>xs:string</aas:valueType>
                                        <aas:value><xsl:value-of select="./sparql:binding[@name='nameAtManufacturer']/sparql:literal"/></aas:value>
                                    </aas:property>
                                    <aas:property>
                                        <aas:category>Enum</aas:category>
                                        <aas:idShort>classification</aas:idShort>
                                        <aas:description>
                                            <aas:langStringTextType>
                                                <aas:language>en</aas:language>
                                                <aas:text>Classification of the part as assigned by the manufacturer.</aas:text>
                                            </aas:langStringTextType>
                                        </aas:description>
                                        <aas:displayName>
                                            <aas:langStringNameType>
                                                <aas:language>en</aas:language>
                                                <aas:text>Product Classification</aas:text>
                                            </aas:langStringNameType>
                                        </aas:displayName>
                                        <aas:semanticId>
                                            <aas:type>ModelReference</aas:type>
                                            <aas:keys>
                                                <aas:key>
                                                    <aas:type>ConceptDescription</aas:type>
                                                    <aas:value>urn:bamm:io.catenax.part_as_planned:1.0.1#classification</aas:value>
                                                </aas:key>
                                            </aas:keys>
                                        </aas:semanticId>
                                        <aas:valueType>xs:string</aas:valueType>
                                        <aas:value><xsl:value-of select="./sparql:binding[@name='classification']/sparql:literal"/></aas:value>
                                    </aas:property>
                                </aas:value>
                            </aas:submodelElementCollection>
                            <aas:submodelElementCollection>
                                <aas:idShort>validityPeriod</aas:idShort>
                                <aas:description>
                                    <aas:langStringTextType>
                                        <aas:language>en</aas:language>
                                        <aas:text>Temporal validity period of the part.</aas:text>
                                    </aas:langStringTextType>
                                </aas:description>
                                <aas:displayName>
                                    <aas:langStringNameType>
                                        <aas:language>en</aas:language>
                                        <aas:text>validityPeriod</aas:text>
                                    </aas:langStringNameType>
                                </aas:displayName>
                                <aas:value>
                                    <aas:property>
                                        <aas:category>Time</aas:category>
                                        <aas:idShort>validFrom</aas:idShort>
                                        <aas:description>
                                            <aas:langStringTextType>
                                                <aas:language>en</aas:language>
                                                <aas:text>Start date of validity period.</aas:text>
                                            </aas:langStringTextType>
                                        </aas:description>
                                        <aas:displayName>
                                            <aas:langStringNameType>
                                                <aas:language>en</aas:language>
                                                <aas:text>Valid From</aas:text>
                                            </aas:langStringNameType>
                                        </aas:displayName>
                                        <aas:semanticId>
                                            <aas:type>ModelReference</aas:type>
                                            <aas:keys>
                                                <aas:key>
                                                    <aas:type>ConceptDescription</aas:type>
                                                    <aas:value>urn:bamm:io.catenax.part_as_planned:1.0.1#validFrom</aas:value>
                                                </aas:key>
                                            </aas:keys>
                                        </aas:semanticId>
                                        <aas:valueType>xs:dateTime</aas:valueType>
                                        <aas:value><xsl:value-of select="./sparql:binding[@name='validFrom']/sparql:literal"/></aas:value>
                                    </aas:property>
                                    <aas:property>
                                        <aas:category>Time</aas:category>
                                        <aas:idShort>validFrom</aas:idShort>
                                        <aas:description>
                                            <aas:langStringTextType>
                                                <aas:language>en</aas:language>
                                                <aas:text>End date of validity period.</aas:text>
                                            </aas:langStringTextType>
                                        </aas:description>
                                        <aas:displayName>
                                            <aas:langStringNameType>
                                                <aas:language>en</aas:language>
                                                <aas:text>Valid To</aas:text>
                                            </aas:langStringNameType>
                                        </aas:displayName>
                                        <aas:semanticId>
                                            <aas:type>ModelReference</aas:type>
                                            <aas:keys>
                                                <aas:key>
                                                    <aas:type>ConceptDescription</aas:type>
                                                    <aas:value>urn:bamm:io.catenax.part_as_planned:1.0.1#validTo</aas:value>
                                                </aas:key>
                                            </aas:keys>
                                        </aas:semanticId>
                                        <aas:valueType>xs:dateTime</aas:valueType>
                                        <aas:value><xsl:value-of select="./sparql:binding[@name='validTo']/sparql:literal"/></aas:value>
                                    </aas:property>
                                </aas:value>
                            </aas:submodelElementCollection>
                        </aas:submodelElements>
                    </aas:submodel>
                </xsl:for-each>
            </aas:submodels>
            <aas:conceptDescriptions>
            </aas:conceptDescriptions>
        </aas:environment>
    </xsl:template>
</xsl:stylesheet>