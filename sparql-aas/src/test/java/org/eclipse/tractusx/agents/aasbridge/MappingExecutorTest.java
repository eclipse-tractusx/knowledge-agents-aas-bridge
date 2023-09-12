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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.SpecificAssetIdentification;
import io.adminshell.aas.v3.model.*;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.eclipse.digitaltwin.aas4j.exceptions.TransformationException;
import org.eclipse.digitaltwin.aas4j.mapping.MappingSpecificationParser;
import org.eclipse.digitaltwin.aas4j.mapping.model.MappingSpecification;
import org.eclipse.digitaltwin.aas4j.transform.GenericDocumentTransformer;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * tests mapping logic
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MappingExecutorTest {

    private final String MOCK_URL = "/oem-edc-data/BPNL00000003COJN/api/agent" +
            "?OemProviderAgent=" +
            URLEncoder.encode("http://oem-provider-agent:8082/sparql", StandardCharsets.ISO_8859_1);

    private final URI LOCAL_LANDSCAPE = new URI("http://localhost:8082/sparql");
    private final URI DEV_LANDSCAPE = new URI("https://knowledge.dev.demo.catena-x.net/oem-provider-agent3/sparql");

    MappingExecutorTest() throws URISyntaxException {
    }

    @Test
    void executePartSiteInformationTest() throws TransformationException, IOException {
        AssetAdministrationShellEnvironment env = getTransformedAasEnv("partSiteInformation");
        executeGenericTests(env);

        assertEquals(18, env.getSubmodels().size());
        env.getAssetAdministrationShells().forEach(aas ->
                assertTrue(aas.getAssetInformation().getGlobalAssetId().getKeys().get(0).getValue().startsWith("urn:uuid")));
        assertTrue(env.getSubmodels().stream().map(sm -> getProperty(sm, "catenaXId")).anyMatch(p -> p.equals("urn:uuid:aad27ddb-43aa-4e42-98c2-01e529ef127c")));
        assertEquals(7, env.getConceptDescriptions().size());
        assertEquals(8, env.getSubmodels().stream()
                .filter(sm -> getProperty(sm, "catenaXId").equals("urn:uuid:aad27ddb-43aa-4e42-98c2-01e529ef127c"))
                .map(sm -> getSmcValues(sm, "sites")).findFirst().get().size());
    }

    @Test
    void executePartAsPlannedTest() throws TransformationException, IOException {
        AssetAdministrationShellEnvironment env = getTransformedAasEnv("partAsPlanned");
        executeGenericTests(env);

        assertEquals(18, env.getSubmodels().size());
        assertEquals(9, env.getConceptDescriptions().size());
        env.getAssetAdministrationShells().forEach(aas ->
                assertTrue(aas.getAssetInformation().getGlobalAssetId().getKeys().get(0).getValue().startsWith("urn:uuid")));
        assertTrue(env.getSubmodels().stream().map(sm -> getProperty(sm, "catenaXId")).anyMatch(p -> p.equals("urn:uuid:e3e2a4d8-58bc-4ae9-afa2-e8946fda1f77")));
    }

    @Test
    void executeSingleLevelBomAsPlannedTest() throws TransformationException, IOException {
        AssetAdministrationShellEnvironment env = getTransformedAasEnv("singleLevelBomAsPlanned");
        executeGenericTests(env);

        assertEquals(12, env.getSubmodels().size());
        assertEquals(13, env.getConceptDescriptions().size());
        env.getAssetAdministrationShells().forEach(aas ->
                assertTrue(aas.getAssetInformation().getGlobalAssetId().getKeys().get(0).getValue().startsWith("urn:uuid")));
        assertTrue(env.getSubmodels().stream().map(sm -> getProperty(sm, "catenaXId")).anyMatch(p -> p.equals("urn:uuid:e5c96ab5-896a-482c-8761-efd74777ca97")));
        assertEquals(3, env.getSubmodels().stream()
                .filter(sm -> getProperty(sm, "catenaXId").equals("urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b"))
                .map(sm -> getSmcValues(sm, "childItems")).findFirst().get().size());
        env.getSubmodels().forEach(sm -> {
            assertEquals(2, sm.getIdentification().getIdentifier().split("/").length);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"partAsPlanned", "partSiteInformation", "singleLevelBomAsPlanned"})
    void executeQueryTest(String aspectName) throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        MockWebServer mockWebServer = instantiateMockServer(aspectName);
        MappingExecutor executor = new MappingExecutor(
                new URI(mockWebServer.url(MOCK_URL).toString()),
                System.getProperty("PROVIDER_CREDENTIAL_BASIC"),
                3,
                5,
                AasUtils.loadConfigsFromResources());

        InputStream inputStream = executor.executeQuery(
                new String(new File("resources/traceability/" + aspectName + "-select-all.rq").toURL().openStream().readAllBytes())).get();
        String result = new String(inputStream.readAllBytes());
        assertEquals(result, getMockResponseBody(aspectName));
    }

    /**
     * test access to one shell
     * @throws InterruptedException
     */
    @Test
    void queryOneShell() throws InterruptedException {
        String sampleId="urn:uuid:e5c96ab5-896a-482c-8761-efd74777ca97";
        MappingExecutor ex = new MappingExecutor(DEV_LANDSCAPE, "ignored", 5, 4, AasUtils.loadConfigsFromResources());
        List<AssetAdministrationShell> shells = ex.queryAllShells(
                sampleId,
                Arrays.asList(new SpecificAssetIdentification.Builder()
                        .key("ignoredAnyway")
                        .value(sampleId)
                        .build()));
        assertEquals(1,shells.size(),"Found the correct shell");
        assertEquals(1,shells.get(0).getDescriptions().size(),"Shell has correct descriptions");
        assertEquals("HV Modul",shells.get(0).getDescriptions().get(0).getValue(),"Shell has correct description");
        assertEquals(sampleId,shells.get(0).getIdShort(),"Correct id short");
        assertEquals(sampleId,shells.get(0).getAssetInformation().getGlobalAssetId().getKeys().get(0).getValue(),"Correct global asset id");
        assertEquals(3,shells.get(0).getSubmodels().size(),"Correct number of submodels");
        shells.get(0).getSubmodels().forEach( submodel -> {
                    assertTrue(submodel.getKeys().get(0).getValue().endsWith(shells.get(0).getIdShort()),"Submodel reference startswith twin id");
        });
    }

    /**
     * test access to all shells
     * @throws InterruptedException
     */
    @Test
    void queryAllShells() throws InterruptedException {
        MappingExecutor ex = new MappingExecutor(DEV_LANDSCAPE, "ignored", 5, 4, AasUtils.loadConfigsFromResources());
        List<AssetAdministrationShell> shells = ex.queryAllShells(
                null,
                null);
        assertEquals(28,shells.size(),"Found all shells");
        shells.forEach(shell -> {
            assertEquals(1,shell.getDescriptions().size(),"Shell has correct descriptions");
            assertEquals(shell.getIdShort(),shell.getAssetInformation().getGlobalAssetId().getKeys().get(0).getValue(),"Correct global asset id and idshort relation");
            assertTrue(shell.getSubmodels().size() > 0,String.format("Shell %s has at least one submodel",shell.getIdShort()));
            shell.getSubmodels().forEach( submodel -> {
                   assertTrue(submodel.getKeys().get(0).getValue().endsWith(shell.getIdShort()),"Submodel reference starts with shell id");
            });
        });
    }


    /**
     * test access to one shell
     * @throws InterruptedException
     */
    @Test
    void queryOneSubmodel() throws InterruptedException {
        String sampleId="urn:uuid:e5c96ab5-896a-482c-8761-efd74777ca97";
        String model="urn:bamm:io.catenax.part_site_information_as_planned:1.0.0#PartSiteInformationAsPlanned";
        MappingExecutor ex = new MappingExecutor(DEV_LANDSCAPE, "ignored", 5, 4, AasUtils.loadConfigsFromResources());
        Identifier identifier=new DefaultIdentifier();
        identifier.setIdentifier(model+"/"+sampleId);
        Identifiable subModel = ex.queryIdentifiableById(identifier, Submodel.class);
        assertNotNull(subModel, "Found the submodel");
        assertTrue(subModel instanceof Submodel,"Its a real submodel");
        Submodel realSubModel=(Submodel) subModel;
        assertEquals(model,realSubModel.getSemanticId().getKeys().get(0).getValue());
    }

    private static AssetAdministrationShellEnvironment getTransformedAasEnv(String submodelIdShort) throws IOException, TransformationException {
        MappingSpecification mapping = new MappingSpecificationParser().loadMappingSpecification("resources/traceability/" + submodelIdShort + "-mapping.json");
        GenericDocumentTransformer transformer = new GenericDocumentTransformer();
        InputStream instream = MappingExecutorTest.class.getResourceAsStream("/sparqlResponseXml/" + submodelIdShort + "-sparql-results.xml");
        String s = new String(instream.readAllBytes());
        return transformer.execute(new ByteArrayInputStream(s.getBytes()), mapping);
    }

    private static void executeGenericTests(AssetAdministrationShellEnvironment env) {
        // each AAS only holds a single Submodel
        env.getAssetAdministrationShells().forEach(aas -> assertEquals(1, aas.getSubmodels().size()));

        // each Submodel is referred to by a single AAS only
        env.getSubmodels().forEach(sm -> {
            long aasPerSm = env.getAssetAdministrationShells().stream()
                    .map(AssetAdministrationShell::getSubmodels)
                    .filter(smrefs ->
                            smrefs.stream().anyMatch(smref -> smref.getKeys().get(0).getValue().equals(sm.getIdentification().getIdentifier())
                            ))
                    .count();
            assertEquals(1, aasPerSm);
        });

        // check no value remains unmapped
        env.getSubmodels().forEach(sm->recurseSmecAndCheckForNull(sm.getSubmodelElements()));
        ;

    }

    private static void recurseSmecAndCheckForNull(Collection<SubmodelElement> smes) {
        smes.stream().filter(sme -> sme.getClass().equals(DefaultProperty.class))
                .map(sme -> (DefaultProperty) sme)
                .forEach(p->{
                    assertNotNull(p.getValue());
                    assertNotEquals("", p.getValue());
                    assertNotEquals("null", p.getValue(), p.getIdShort());
                });
        smes.stream().filter(sme -> sme.getClass().equals(DefaultSubmodelElementCollection.class))
                .map(sme -> (DefaultSubmodelElementCollection) sme)
                .forEach(smec->recurseSmecAndCheckForNull(smec.getValues()));
    }

    private MockWebServer instantiateMockServer(String aspectName) throws IOException, URISyntaxException {
        MockWebServer mockServer = new MockWebServer();
        String mockResponseBody = getMockResponseBody(aspectName);
        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/xml; charset=utf-8")
                .setBody(mockResponseBody)
                .setResponseCode(200);
        mockServer.url(MOCK_URL).toString();
        mockServer.enqueue(response);

        return mockServer;
    }

    private String getMockResponseBody(String aspectName) throws IOException {
        String mockResponseBody = new String(getClass().getClassLoader()
                .getResourceAsStream("sparqlResponseXml/"+ aspectName +"-sparql-results.xml")
                .readAllBytes());
        return mockResponseBody;
    }

    private String getProperty(Submodel submodel, String propertyIdShort) {
        return submodel.getSubmodelElements().stream()
                .filter(sme->sme.getClass().equals(DefaultProperty.class))
                .map(sme->(Property)sme)
                .filter(p->p.getIdShort().equals(propertyIdShort))
                .findFirst().map(Property::getValue)
                .orElseThrow(()-> new RuntimeException("propertyNotFound"));
    }

    private Collection<SubmodelElement> getSmcValues(Submodel submodel, String idShort) {
        return submodel.getSubmodelElements().stream()
                .filter(sme->sme.getClass().equals(DefaultSubmodelElementCollection.class))
                .map(sme->(SubmodelElementCollection)sme)
                .filter(smc -> smc.getIdShort().equals(idShort))
                .findFirst().map(SubmodelElementCollection::getValues)
                .orElseThrow(()-> new RuntimeException("smcNotFound"));

    }
}