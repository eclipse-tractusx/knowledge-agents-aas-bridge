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

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

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
            builder.addUrls(searchPath.toURL());
            Configuration config = builder.setScanners(Scanners.Resources);
            Reflections reflections = new Reflections(config);

            Pattern filePattern = Pattern.compile("[a-zA-Z/0-9]*-mapping\\.xslt");

            Set<String> files = reflections.getResources(filePattern).stream().filter(file -> filePattern.matcher(file).matches()).collect(Collectors.toSet());

            logger.info("Scanning for {} in resources folder found {}", filePattern, files);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder = dbf.newDocumentBuilder();

            TransformerFactory tf = TransformerFactory.newInstance();

            return files.stream()
                    .map(relativePath -> {
                        String[] components = relativePath.split("/");
                        String mappingPath = searchPath.getPath() + "/" + relativePath;
                        try {
                            Document styleSheet = documentBuilder.parse(new FileInputStream(mappingPath));
                            Transformer transformer = tf.newTransformer(new StreamSource(new FileInputStream(mappingPath)));
                            String semanticId = styleSheet.getDocumentElement().getAttribute("xmlns:semanticid");
                            if (semanticId.isEmpty()) {
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
                                    transformer,
                                    selectSomeFile,
                                    selectAllFile,
                                    semanticId
                            );
                        } catch (IOException | SAXException | IllegalArgumentException |
                                 TransformerConfigurationException e) {
                            e.printStackTrace();
                            logger.warn(String.format("Could not read mapping specification in %s because of %s. Ignoring.", mappingPath, e));
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(MappingConfiguration::getDomain));
        } catch (ParserConfigurationException | IOException e) {
            logger.error("Could not load initial configuration..", e);
            throw new RuntimeException(e);
        }
    }

    public static Environment mergeAasEnvs(Set<Environment> aasEnvs) {
        Set<AssetAdministrationShell> collect = aasEnvs.stream()
                .flatMap(env -> env.getAssetAdministrationShells().stream())
                .collect(Collectors.toSet());
        Map<String, List<AssetAdministrationShell>> collect1 = collect.stream()
                .collect(Collectors.groupingBy(aas ->
                        // TODO: if gaid not available, match for any said-k-v-pair
                        aas.getAssetInformation().getGlobalAssetId()));
        List<AssetAdministrationShell> mergedShells = collect1.values().stream().flatMap(group ->
                group.stream().reduce((aas1, aas2) -> {
                    aas1.getSubmodels().addAll(aas2.getSubmodels());
                    return aas1;
                }).stream()).collect(Collectors.toList());

        return new DefaultEnvironment.Builder()
                .assetAdministrationShells(mergedShells)
                .submodels(aasEnvs.stream().flatMap(env -> env.getSubmodels().stream()).collect(Collectors.toList()))
                .conceptDescriptions(aasEnvs.stream().flatMap(env -> env.getConceptDescriptions().stream()).collect(Collectors.toList()))
                .build();
    }
}
