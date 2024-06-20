// Copyright (c) 2023 SAP SE 
// Copyright (c) 2023 T-Systems International GmbH 
// Copyright (c) 2023 Contributors to the Eclipse Foundation
//
// See the NOTICE file(s) distributed with this work for additional
// information regarding copyright ownership.
//
// This program and the accompanying materials are made available under the
// terms of the Apache License, Version 2.0 which is available at
// https://www.apache.org/licenses/LICENSE-2.0.
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// License for the specific language governing permissions and limitations
// under the License.
//
// SPDX-License-Identifier: Apache-2.0
package org.eclipse.tractusx.agents.aasbridge;

import java.io.File;
import javax.xml.transform.Transformer;

/**
 * a mapping configuration holds together a (submmodel/aas) template with its
 * queries. It is always located in some domain
 */
public class MappingConfiguration {
    private final Transformer mappingSpecification;
    private final File getOneQueryTemplate;
    private final File getAllQuery;
    private final String semanticId;
    private final String domain;

    /**
     * Creates a new mapping configuration
     *
     * @param domain               the domain identifier
     * @param mappingSpecification the template
     * @param getOneQueryTemplate  the query to obtain one or several twins/submodels
     * @param getAllQuery          the query to get all twins/submodels, may be empty if that would be too expensive
     * @param semanticId           the semantic id associated to the aas/submodel template
     */

    public MappingConfiguration(String domain, Transformer mappingSpecification, File getOneQueryTemplate, File getAllQuery, String semanticId) {
        this.mappingSpecification = mappingSpecification;
        this.getOneQueryTemplate = getOneQueryTemplate;
        this.getAllQuery = getAllQuery;
        this.semanticId = semanticId;
        this.domain = domain;
    }


    public Transformer getMappingSpecification() {
        return mappingSpecification;
    }

    public File getGetOneQueryTemplate() {
        return getOneQueryTemplate;
    }

    public File getGetAllQuery() {
        return getAllQuery;
    }

    public String getSemanticId() {
        return semanticId;
    }

    public String getDomain() {
        return domain;
    }
}
