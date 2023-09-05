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
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AasUtils {

    public static List<MappingConfiguration> loadConfigsFromResources() {

        ConfigurationBuilder builder=new ConfigurationBuilder();
        try {
            builder=builder.addUrls(new File("resources").toURL());
        } catch(MalformedURLException e) {
        }
        Configuration config= builder.setScanners(Scanners.Resources);
        Reflections reflections = new Reflections(config);
        Set<String> files = reflections.getResources(Pattern.compile(".*-select\\.rq"));
        return files.stream()
                .map(Path::of)
                    .map(getOnePath -> {
                        String nameInclSelect = getOnePath.getFileName().toString();
                        String mappingFileFolder = "resources/mappingSpecifications/";
                        String mappingFileName = nameInclSelect.split("-")[0] + "-mapping.json";
                        MappingSpecification spec =
                                null;
                        try {
                            spec = new MappingSpecificationParser().loadMappingSpecification(mappingFileFolder + mappingFileName);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        String getAllPath = "resources/selectQueries/"+nameInclSelect;
                        return new MappingConfiguration(
                                spec,
                                new File("resources/"+getOnePath.toString()),
                                new File(getAllPath),
                                spec.getHeader().getNamespaces().get("semanticId")
                        );
                    })
                    .collect(Collectors.toList());


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
