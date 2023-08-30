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

import org.eclipse.digitaltwin.aas4j.mapping.model.MappingSpecification;

import java.io.File;

public class MappingConfiguration {
    private MappingSpecification mappingSpecification;
    private File getOneQueryTemplate;
    private File getAllQuery;
    private String semanticId;

    public MappingConfiguration(MappingSpecification mappingSpecification, File getOneQueryTemplate, File getAllQuery, String semanticId) {
        this.mappingSpecification = mappingSpecification;
        this.getOneQueryTemplate = getOneQueryTemplate;
        this.getAllQuery = getAllQuery;
        this.semanticId = semanticId;
    }


    public MappingSpecification getMappingSpecification() {
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
}
