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

import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.EndpointException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorageConfig;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.memory.FileStorageInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternalConfig;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AasBridge {

    /**
     * Main entry into the aas bridge
     *
     * @param args command line arguments
     * @throws ConfigurationException   if faaast cannot be configured
     * @throws AssetConnectionException if faaast cannot connect to an asset
     * @throws MessageBusException      if faaast message bus cannot be initialitzed
     * @throws EndpointException        if faaast endpoint cannot be published
     * @throws NumberFormatException    if faaast cannot parse a number
     */
    public static void main(String[] args) throws ConfigurationException, AssetConnectionException, MessageBusException, EndpointException, NumberFormatException {

        Logger mainLogger = LoggerFactory.getLogger(AasBridge.class);

        mainLogger.info("Building AAS Bridge");

        CoreConfig coreConfig = CoreConfig.builder()
                .requestHandlerThreadPoolSize(5)
                .build();

        mainLogger.debug("Built coreConfig {}", coreConfig);

        PersistenceInKnowledgeConfig persistenceConfig = PersistenceInKnowledgeConfig.builder()
                .initialModel(new DefaultEnvironment.Builder().build())
                .mappings(AasUtils.loadConfigsFromResources())
                .threadPoolSize(5)
                .timeoutSeconds(5)
                .providerSparqlEndpoint(System.getProperty("PROVIDER_SPARQL_ENDPOINT", System.getenv("PROVIDER_SPARQL_ENDPOINT")))
                .credentials(System.getProperty("PROVIDER_CREDENTIAL_BASIC", System.getenv("PROVIDER_CREDENTIAL_BASIC")))
                .logResults(Boolean.parseBoolean(System.getProperty("PROVIDER_LOG_RESULTS", System.getenv().getOrDefault("PROVIDER_LOG_RESULTS", "false"))))
                .build();

        mainLogger.debug("Built persistenceConfig {}", persistenceConfig);

        HttpEndpointConfig httpConfig = HttpEndpointConfig
                .builder()
                .cors(true)
                .port(8443)
                .sni(false)
                .build();

        mainLogger.debug("Built httpConfig {}", httpConfig);

        MessageBusInternalConfig busConfig = MessageBusInternalConfig.builder().build();

        mainLogger.debug("Built busConfig {}", busConfig);

        FileStorageConfig fsConfig = FileStorageInMemoryConfig.builder().build();

        mainLogger.debug("Built fsConfig {}", fsConfig);

        ServiceConfig serviceConfig = ServiceConfig.builder()
                .core(coreConfig)
                .persistence(persistenceConfig)
                .fileStorage(fsConfig)
                .endpoint(httpConfig)
                .messageBus(busConfig)
                .build();

        mainLogger.debug("Built serviceConfig {}", serviceConfig);

        Service faaast = new Service(serviceConfig);

        mainLogger.info("Starting AAS Bridge {}", faaast);

        faaast.start();
    }

}
