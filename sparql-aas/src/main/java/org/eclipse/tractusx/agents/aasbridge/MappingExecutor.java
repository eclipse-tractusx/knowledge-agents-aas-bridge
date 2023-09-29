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

import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.SpecificAssetIdentification;
import io.adminshell.aas.v3.model.Asset;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.ConceptDescription;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.exceptions.TransformationException;
import org.eclipse.digitaltwin.aas4j.mapping.model.MappingSpecification;
import org.eclipse.digitaltwin.aas4j.transform.GenericDocumentTransformer;
import org.eclipse.rdf4j.query.resultio.helpers.QueryResultCollector;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLParser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * Executes mappings which are combinations of statements and their transformation into aas objects.
 */
public class MappingExecutor {
    //Client config

    private final GenericDocumentTransformer transformer;
    private final URI sparqlEndpoint;

    private final String credentials;
    private final int timeoutSeconds;
    private final HttpClient client;

    private final Map<String, List<MappingConfiguration>> mappings;

    public MappingExecutor(URI sparqlEndpoint, String credentials, int timeoutSeconds, int fixedThreadPoolSize, Map<String, List<MappingConfiguration>> mappings) {
        this.mappings = mappings;
        this.transformer = new GenericDocumentTransformer();
        this.sparqlEndpoint = URI.create(sparqlEndpoint.toString());
        this.credentials = credentials;
        this.timeoutSeconds = timeoutSeconds;
        this.client = HttpClient.newBuilder().executor(Executors.newFixedThreadPool(fixedThreadPoolSize)).build();
    }

    public static String parametrizeQuery(File queryTemplate, Object... parameters) {
        Object[] render = new Object[parameters.length];
        for (int count = 0; count < parameters.length; count++) {
            if (parameters[count] instanceof Iterable) {
                render[count] = "";
                for (var parameter : ((Iterable<Object>) parameters[count])) {
                    render[count] += "<" + String.valueOf(parameter) + "> ";
                }
            } else {
                render[count] = "<" + String.valueOf(parameters[count]) + ">";
            }
        }
        try {
            return String.format(Files.readString(queryTemplate.toPath()), render);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean queryResultEmpty(String result) {
        SPARQLResultsXMLParser parser = new SPARQLResultsXMLParser();
        QueryResultCollector handler = new QueryResultCollector();
        parser.setTupleQueryResultHandler(handler);
        try {
            parser.parseQueryResult(new ByteArrayInputStream(result.getBytes()));
            return handler.getBindingSets().isEmpty();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't parse the query result provided", e);
        }
    }

    public AssetAdministrationShellEnvironment executeMapping(String query, MappingSpecification specification) {
        try {
            InputStream queryResult = executeQuery(query).get();
            return transformer.execute(queryResult, specification);
        } catch (URISyntaxException | InterruptedException | ExecutionException | IOException |
                 TransformationException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * executes the given query
     *
     * @param query the string containing the (if necessary parametrized) query, probably loaded from resources
     * @return xml structure of the query response
     * @throws URISyntaxException if target url is not correctly specified
     * @throws IOException        if query target cannot be correctly interfaced
     */
    protected CompletableFuture<InputStream> executeQuery(String query) throws URISyntaxException, IOException {

        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(query);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(sparqlEndpoint)
                .POST(bodyPublisher)
                .header("Content-Type", "application/sparql-query")
                .header("Accept", "application/xml")
                .timeout(Duration.of(timeoutSeconds, SECONDS));

        if (credentials != null && !credentials.isEmpty()) {
            requestBuilder = requestBuilder.header("Authorization", credentials);
        }

        HttpRequest request = requestBuilder.build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(res -> {
            if (res.statusCode() >= 200 && res.statusCode() < 300) {
                return res.body();
            } else {
                throw new RuntimeException("Sparql-Request failed with " + res.statusCode() + res.body());
            }
        }).thenApply(body -> new ByteArrayInputStream(body.getBytes()));
    }

    /**
     * Implements access to assets, submodels, conceptdescriptions (and assets)
     *
     * @param identifier identifies the asset, the submodel or the conceptdescription
     * @param type       class of the instance that should be returned
     * @return the found entity, should be null if not found
     */
    public Identifiable queryIdentifiableById(Identifier identifier, Class<? extends Identifiable> type) {
        if (type.isAssignableFrom(Submodel.class)) {
            // maybe separate by cd, sm, aas later
            String[] components = identifier.getIdentifier().split("/");
            String domain = components[0];
            String semanticId = components[1];
            MappingConfiguration mapping = mappings.get(domain).stream()
                    .filter(m -> m.getSemanticId().equals(semanticId))
                    .findFirst().orElseThrow();
            String parametrized = parametrizeQuery(mapping.getGetOneQueryTemplate(), components[2]);
            return executeMapping(parametrized, mapping.getMappingSpecification())
                    .getSubmodels()
                    .get(0); //should only be one
        } else if (type.isAssignableFrom(AssetAdministrationShell.class)) {
            return queryAllShells(identifier.getIdentifier(), List.of(new SpecificAssetIdentification.Builder()
                    .key("ignoredAnyway")
                    .value(identifier.getIdentifier())
                    .build())).get(0);
            // check for existence of submodels
            // create new AAS maybe (maybe even here)
        } else if (type.isAssignableFrom(ConceptDescription.class)) {
            // execute all conceptDescriptionMappings on startup
            // keep in memory, never update, just query
        } else if (type.isAssignableFrom(Asset.class)) {
            // so what?
        } else {
            throw new RuntimeException(String.format("Identifiable %s is neither AAS, Submodel or CD", identifier.getIdentifier()));
        }

        return null;
    }


    // may return an aas with assetId global even when queried as specific
    public List<AssetAdministrationShell> queryAllShells(String idShort, List<AssetIdentification> assetIds) {
        if (assetIds == null && idShort == null) {
            return mappings.values().stream()
                    .flatMap(mappings -> mappings.stream().flatMap(mapping -> {
                        if (mapping.getSemanticId().equals("https://w3id.org/catenax/ontology/aas#")) {
                            File template = mapping.getGetAllQuery();
                            String query = parametrizeQuery(template);
                            try {
                                InputStream in = executeQuery(query).get();
                                String result = new String(in.readAllBytes());
                                if (queryResultEmpty(result)) {
                                    return null;
                                }
                                AssetAdministrationShellEnvironment transformedEnv = transformer.execute(new ByteArrayInputStream(result.getBytes()),
                                        mapping.getMappingSpecification());
                                return transformedEnv.getAssetAdministrationShells().stream();
                            } catch (URISyntaxException | IOException | ExecutionException | InterruptedException |
                                     TransformationException e) {
                                throw new RuntimeException(e);
                            }

                        } else {
                            return Stream.of();
                        }
                    })).collect(Collectors.toList());
        } else {
            if (assetIds == null) {
                assetIds = List.of(new SpecificAssetIdentification.Builder().value(idShort).build());
            }
            Map<String, List<Map.Entry<String, String>>> domainIds = assetIds.stream().map(id -> {
                String idString;
                String domain;
                if (id.getClass().isAssignableFrom(GlobalAssetIdentification.class)) {
                    GlobalAssetIdentification gaid = (GlobalAssetIdentification) id;
                    idString = gaid.getReference().getKeys().get(0).getValue();
                } else if (id.getClass().isAssignableFrom(SpecificAssetIdentification.class)) {
                    SpecificAssetIdentification said = (SpecificAssetIdentification) id;
                    idString = said.getValue();
                } else {
                    idString = String.valueOf(id);
                }
                String[] components = idString.split("/");
                return new AbstractMap.SimpleEntry(components[0], components[1]);
            }).collect(Collectors.groupingBy(Map.Entry<String, String>::getKey));

            return domainIds.entrySet().stream().flatMap(
                    idsPerDomain -> {
                        return mappings.get(idsPerDomain.getKey()).stream().filter(mapping -> mapping.getSemanticId().equals("https://w3id.org/catenax/ontology/aas#")).flatMap(
                                aasMapping -> {
                                    List<String> candidates = idsPerDomain.getValue().stream().map(Map.Entry<String, String>::getValue).collect(Collectors.toList());
                                    File template = aasMapping.getGetOneQueryTemplate();
                                    String query = parametrizeQuery(template, candidates);
                                    try {
                                        InputStream in = executeQuery(query).get();
                                        String result = new String(in.readAllBytes());
                                        if (queryResultEmpty(result)) {
                                            return null;
                                        }
                                        AssetAdministrationShellEnvironment transformedEnv = transformer.execute(new ByteArrayInputStream(result.getBytes()),
                                                aasMapping.getMappingSpecification());
                                        return transformedEnv.getAssetAdministrationShells().stream();
                                    } catch (URISyntaxException | IOException | ExecutionException |
                                             InterruptedException |
                                             TransformationException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        );
                    }
            ).collect(Collectors.toList());
        }
    }


    public List<Submodel> queryAllSubmodels(String idShort, Reference semanticId) {
        if (semanticId == null) {
            return mappings.values().stream().flatMap(domainMappings -> domainMappings.stream().flatMap(
                    mapping -> {
                        if (!mapping.getSemanticId().equals("https://w3id.org/catenax/ontology/aas#")) {
                            try {
                                String query = Files.readString(mapping.getGetAllQuery().toPath());
                                return executeMapping(query, mapping.getMappingSpecification()).getSubmodels().stream();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            return Stream.of();
                        }
                    })).collect(Collectors.toList());
        } else {
            return mappings.values().stream().flatMap(domainMappings -> domainMappings.stream().flatMap(
                    mapping -> {
                        if (mapping.getSemanticId().equals(semanticId.getKeys().get(0).getValue())) {
                            try {
                                String query = Files.readString(mapping.getGetAllQuery().toPath());
                                return executeMapping(query, mapping.getMappingSpecification()).getSubmodels().stream();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            return Stream.of();
                        }
                    })).collect(Collectors.toList());
        }
    }

}