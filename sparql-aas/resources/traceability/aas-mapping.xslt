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
                xmlns:semanticid="https://w3id.org/catenax/ontology/aas#"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <xsl:variable name="domain"  select="'traceability'"/>
    <xsl:variable name="semanticid"  select="'https://w3id.org/catenax/ontology/aas#'"/>
    <xsl:output method="xml" />
    <xsl:template name="genAssetId">
      <xsl:value-of select="$domain"/>/<xsl:value-of select="./sparql:binding[@name='id']/sparql:uri"/>
    </xsl:template>
    <xsl:template name="root" match="/">
        <aas:environment xmlns:aas="https://admin-shell.io/aas/3/0"
                         xsi:schemaLocation="">
            <aas:assetAdministrationShells>
                <xsl:for-each select="//sparql:result">
                <aas:assetAdministrationShell>
                    <aas:idShort><xsl:call-template name="genAssetId"/></aas:idShort>
                    <aas:id><xsl:call-template name="genAssetId"/></aas:id>
                    <aas:assetInformation>
                        <aas:assetKind>Instance</aas:assetKind>
                        <aas:globalAssetId><xsl:call-template name="genAssetId"/></aas:globalAssetId>
                    </aas:assetInformation>
                    <aas:description>
                        <aas:langStringTextType>
                            <aas:language>en</aas:language>
                            <aas:text><xsl:value-of select="./sparql:binding[@name='name']/sparql:literal"/></aas:text>
                        </aas:langStringTextType>
                    </aas:description>
                    <aas:submodels>
                        <xsl:for-each select="./sparql:binding[@name != 'id' and @name != 'name']">
                        <aas:reference>
                            <aas:type>ExternalReference</aas:type>
                            <aas:keys>
                                <aas:key>
                                    <aas:type>Submodel</aas:type>
                                    <aas:value><xsl:value-of select="$domain"/>/<xsl:value-of select="./sparql:uri"/>/<xsl:value-of select="../sparql:binding[@name = 'id']/sparql:uri"/></aas:value>
                                </aas:key>
                            </aas:keys>
                        </aas:reference>
                        </xsl:for-each>
                    </aas:submodels>
                </aas:assetAdministrationShell>
                </xsl:for-each>
            </aas:assetAdministrationShells>
        </aas:environment>
    </xsl:template>
</xsl:stylesheet>
