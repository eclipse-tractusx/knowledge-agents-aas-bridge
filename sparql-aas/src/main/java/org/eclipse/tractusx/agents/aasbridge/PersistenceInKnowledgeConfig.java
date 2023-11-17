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

import de.fraunhofer.iosb.ilt.faaast.service.persistence.PersistenceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Configuration of the knowledge-backed persistence implementation.
 */
public class PersistenceInKnowledgeConfig extends PersistenceConfig<PersistenceInKnowledge> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceInKnowledgeConfig.class);
    private Map<String, List<MappingConfiguration>> mappings; // query to mappingspecification
    private String providerSparqlEndpoint;
    private String credentials;
    private int threadPoolSize;
    private int timeoutSeconds;

    private boolean logResults;


    public Map<String, List<MappingConfiguration>> getMappings() {
        return mappings;
    }

    public void setMappings(Map<String, List<MappingConfiguration>> mappings) {
        this.mappings = mappings;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getProviderSparqlEndpoint() {
        return providerSparqlEndpoint;
    }

    public String getCredentials() {
        return credentials;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setProviderSparqlEndpoint(String providerSparqlEndpoint) {
        this.providerSparqlEndpoint = providerSparqlEndpoint;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isLogResults() {
        return logResults;
    }

    public void setLogResults(boolean logResults) {
        this.logResults = logResults;
    }

    private abstract static class AbstractBuilder<T extends PersistenceInKnowledgeConfig, B extends AbstractBuilder<T, B>> extends PersistenceConfig.AbstractBuilder<PersistenceInKnowledge, T, B> {
        public B mappings(Map<String, List<MappingConfiguration>> value) {
            getBuildingInstance().setMappings(value);
            return getSelf();
        }

        public B providerSparqlEndpoint(String value) {
            getBuildingInstance().setProviderSparqlEndpoint(value);
            return getSelf();
        }

        public B credentials(String value) {
            getBuildingInstance().setCredentials(value);
            return getSelf();
        }

        public B threadPoolSize(int value) {
            getBuildingInstance().setThreadPoolSize(value);
            return getSelf();
        }

        public B timeoutSeconds(int value) {
            getBuildingInstance().setTimeoutSeconds(value);
            return getSelf();
        }

        public B logResults(boolean value) {
            getBuildingInstance().setLogResults(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<PersistenceInKnowledgeConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected PersistenceInKnowledgeConfig newBuildingInstance() {
            return new PersistenceInKnowledgeConfig();
        }
    }

}
