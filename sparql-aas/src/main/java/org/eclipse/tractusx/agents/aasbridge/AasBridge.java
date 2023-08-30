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
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternalConfig;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShellEnvironment;

import java.net.URI;

public class AasBridge {
    public static void main(String[] args) throws ConfigurationException, AssetConnectionException, MessageBusException, EndpointException, NumberFormatException {

        Service faaast = new Service(ServiceConfig.builder()
                .core(CoreConfig.builder()
                        .requestHandlerThreadPoolSize(5)
                        .build())
                .persistence(PersistenceInKnowledgeConfig.builder()
                        .initialModel(new DefaultAssetAdministrationShellEnvironment.Builder().build())
                        .mappings(AasUtils.loadConfigsFromResources())
                        .threadPoolSize(5)
                        .timeoutSeconds(5)
                        .providerSparqlEndpoint(URI.create(System.getProperty("PROVIDER_SPARQL_ENDPOINT", System.getenv("PROVIDER_SPARQL_ENDPOINT"))))
                        .credentials(System.getProperty("PROVIDER_CREDENTIAL_BASIC", System.getenv("PROVIDER_CREDENTIAL_BASIC")))
                        .build())
                .endpoint(HttpEndpointConfig.builder().cors(true).build())
                .messageBus(MessageBusInternalConfig.builder().build())
                .build());


        faaast.start();
    }

}
