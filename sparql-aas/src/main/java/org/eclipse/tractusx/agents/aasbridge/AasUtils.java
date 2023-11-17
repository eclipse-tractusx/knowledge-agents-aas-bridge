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

import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShellEnvironment;
import org.eclipse.digitaltwin.aas4j.mapping.MappingSpecificationParser;
import org.eclipse.digitaltwin.aas4j.mapping.model.MappingSpecification;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Helper logic to setup the configuration
 */
public class AasUtils {

    public static Logger logger = LoggerFactory.getLogger(AasUtils.class);

    /**
     * investigates the filesystems resources folder to find mappings
     *
     * @return a domain of mapping configurations
     */
    public static Map<String, List<MappingConfiguration>> loadConfigsFromResources() {

        logger.info("About to load mapping configurations.");

        File searchPath = new File("resources");

        ConfigurationBuilder builder = new ConfigurationBuilder();
        try {
            builder = builder.addUrls(searchPath.toURL());
        } catch (MalformedURLException e) {
            logger.warn("Could not build url.", e);
        }
        Configuration config = builder.setScanners(Scanners.Resources);
        Reflections reflections = new Reflections(config);
        Set<String> files = reflections.getResources(Pattern.compile(".*-mapping\\.json"));

        logger.info("Scanning for *-mapping.json in resources folder found {}", files);

        return files.stream()
                .map(relativePath -> {
                    String[] components = relativePath.split("/");
                    String mappingPath = searchPath.getPath() + "/" + relativePath;
                    try {
                        MappingSpecification spec = new MappingSpecificationParser().loadMappingSpecification(mappingPath);
                        String semanticId = spec.getHeader().getNamespaces().get("semanticId");
                        if (semanticId == null) {
                            logger.warn("Mapping {} has no namespace called 'semanticId'. So it will not be accessible.", mappingPath);
                        }
                        File selectSomeFile = new File(mappingPath.split("-")[0] + "-select-some.rq");
                        File selectAllFile = new File(mappingPath.split("-")[0] + "-select-all.rq");
                        if (!selectSomeFile.exists() || !selectSomeFile.isFile()) {
                            logger.warn("Bound select for mapping {} is not a valid file {}. Ignoring.", mappingPath, selectSomeFile);
                            selectSomeFile = null;
                        }
                        if (!selectAllFile.exists() || !selectAllFile.isFile()) {
                            logger.warn("Unbound select for mapping {} is not a valid file {}. Ignoring.", mappingPath, selectAllFile);
                            selectAllFile = null;
                        }
                        return new MappingConfiguration(
                                components[0],
                                spec,
                                selectSomeFile,
                                selectAllFile,
                                semanticId
                        );
                    } catch (IOException e) {
                        logger.warn("Could not read mapping specification in {} because of {}. Ignoring.", mappingPath, e);
                        return null;
                    }
                })
                .filter(conf -> conf != null)
                .collect(Collectors.groupingBy(MappingConfiguration::getDomain));
    }

    public static AssetAdministrationShellEnvironment mergeAasEnvs(Set<AssetAdministrationShellEnvironment> aasEnvs) {
        Set<AssetAdministrationShell> collect = aasEnvs.stream()
                .flatMap(env -> env.getAssetAdministrationShells().stream())
                .collect(Collectors.toSet());
        Map<String, List<AssetAdministrationShell>> collect1 = collect.stream()
                .collect(Collectors.groupingBy(aas ->
                        // TODO: if gaid not available, match for any said-k-v-pair
                        aas.getAssetInformation().getGlobalAssetId().getKeys().get(0).getValue()));
        List<AssetAdministrationShell> mergedShells = collect1.values().stream().map(group ->
                group.stream().reduce((aas1, aas2) -> {
                    aas1.getSubmodels().addAll(aas2.getSubmodels());
                    return aas1;
                }).get()).collect(Collectors.toList());

        return new DefaultAssetAdministrationShellEnvironment.Builder()
                .assetAdministrationShells(mergedShells)
                .submodels(aasEnvs.stream().flatMap(env -> env.getSubmodels().stream()).collect(Collectors.toList()))
                .conceptDescriptions(aasEnvs.stream().flatMap(env -> env.getConceptDescriptions().stream()).collect(Collectors.toList()))
                .build();
    }
}
