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

import de.fraunhofer.iosb.ilt.faaast.service.model.asset.SpecificAssetIdentification;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.xml.XmlDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
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

    private final String DUMMY_LANDSCAPE = MappingExecutor.DEFAULT_SPARQL_ENDPOINT;
    // private final String LOCAL_LANDSCAPE = "http://localhost:8082/sparql";
    // private final String DEV_LANDSCAPE = "https://knowledge.dev.demo.catena-x.net/oem-provider-agent3/sparql";
    private final String LANDSCAPE = DUMMY_LANDSCAPE;
    private final boolean LOG_RESULTS = false;

    MappingExecutorTest() {
    }

    @Test
    void executePartSiteInformationTest() throws IOException {
        Environment env = getTransformedAasEnv("partSiteInformation", "");
        executeGenericTests(env,1);

        assertEquals(18, env.getSubmodels().size());
        env.getAssetAdministrationShells().forEach(aas ->
                assertTrue(aas.getAssetInformation().getGlobalAssetId().contains("urn:uuid")));
        assertTrue(env.getSubmodels().stream().map(sm -> getProperty(sm, "catenaXId")).anyMatch(p -> p.equals("urn:uuid:aad27ddb-43aa-4e42-98c2-01e529ef127c")));
        assertEquals(8, env.getSubmodels().stream()
                .filter(sm -> getProperty(sm, "catenaXId").equals("urn:uuid:aad27ddb-43aa-4e42-98c2-01e529ef127c"))
                .map(sm -> getSmcValues(sm, "sites")).findFirst().orElse(List.of()).size());
    }

    @Test
    void executePartAsPlannedTest() throws IOException {
        Environment env = getTransformedAasEnv("partAsPlanned","");
        executeGenericTests(env,1);

        assertEquals(28, env.getAssetAdministrationShells().size());
        assertEquals(28, env.getSubmodels().size());
        env.getAssetAdministrationShells().forEach(aas ->
                assertTrue(aas.getAssetInformation().getGlobalAssetId().contains("urn:uuid")));
        assertTrue(env.getSubmodels().stream().map(sm -> getProperty(sm, "catenaXId")).anyMatch(p -> p.equals("urn:uuid:0733946c-59c6-41ae-9570-cb43a6e4c79e")),"Found the last submodel in the result.");
    }

    @Test
    void executePartAsPlanned2Test() throws IOException {
        Environment env = getTransformedAasEnv("partAsPlanned","2");
        executeGenericTests(env,2);

        assertEquals(3, env.getSubmodels().size());
        env.getAssetAdministrationShells().forEach(aas ->
                assertTrue(aas.getAssetInformation().getGlobalAssetId().contains("urn:uuid")));
        assertTrue(env.getSubmodels().stream().map(sm -> getProperty(sm, "catenaXId")).anyMatch(p -> p.equals("urn:uuid:0733946c-59c6-41ae-9570-cb43a6e4c79e")),"Found the last submodel in the result.");
    }

    @Test
    void executeSingleLevelBomAsPlannedTest() throws IOException {
        Environment env = getTransformedAasEnv("singleLevelBomAsPlanned","");
        executeGenericTests(env,1);

        assertEquals(12, env.getSubmodels().size());
        env.getAssetAdministrationShells().forEach(aas ->
                assertTrue(aas.getAssetInformation().getGlobalAssetId().contains("urn:uuid")));
        assertTrue(env.getSubmodels().stream().map(sm -> getProperty(sm, "catenaXId")).anyMatch(p -> p.equals("urn:uuid:e5c96ab5-896a-482c-8761-efd74777ca97")));
        assertEquals(3, env.getSubmodels().stream()
                .filter(sm -> getProperty(sm, "catenaXId").equals("urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b"))
                .map(sm -> getSmcValues(sm, "childItems")).findFirst().orElse(List.of()).size());
        env.getSubmodels().forEach(sm -> assertEquals(3, sm.getId().split("/").length));
    }

    @Test
    void executeSingleLevelUsageAsPlannedTest() throws IOException {
        Environment env = getTransformedAasEnv("singleLevelUsageAsPlanned","");
        executeGenericTests(env,1);

        assertEquals(30, env.getSubmodels().size());
        env.getAssetAdministrationShells().forEach(aas ->
                assertTrue(aas.getAssetInformation().getGlobalAssetId().contains("urn:uuid")));
        assertTrue(env.getSubmodels().stream().map(sm -> getProperty(sm, "catenaXId")).anyMatch(p -> p.equals("urn:uuid:f5efbf45-7d84-4442-b3b8-05cf1c5c5a0b")));
        assertEquals(2, env.getSubmodels().stream()
                .filter(sm -> getProperty(sm, "catenaXId").equals("urn:uuid:bee5614f-9e46-4c98-9209-61a6f2b2a7fc"))
                .map(sm -> getSmcValues(sm, "parentParts")).findFirst().orElse(List.of()).size());
        env.getSubmodels().forEach(sm -> assertEquals(3, sm.getId().split("/").length));
    }

    @ParameterizedTest
    @ValueSource(strings = {"partAsPlanned", "partSiteInformation", "singleLevelBomAsPlanned","singleLevelUsageAsPlanned"})
    void executeQueryTest(String aspectName) throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        try (MockWebServer mockWebServer = instantiateMockServer(aspectName)) {
            MappingExecutor executor = new MappingExecutor(
                    mockWebServer.url(MOCK_URL).toString(),
                    System.getProperty("PROVIDER_CREDENTIAL_BASIC"),
                    3,
                    5,
                    AasUtils.loadConfigsFromResources(),
                    LOG_RESULTS);

            try(InputStream inputStream = new File("resources/traceability/" + aspectName + "-select-all.rq").toURL().openStream()) {
                try (InputStream resultStream = executor.executeQuery(new String(inputStream.readAllBytes())).get()) {
                    String result = new String(resultStream.readAllBytes());
                    assertEquals(result, getMockResponseBody(aspectName));
                }
            }
        }
    }

    /**
     * test access to one shell
     */
    @Test
    void queryOneShell() {
        String sampleId="traceability/urn:uuid:e5c96ab5-896a-482c-8761-efd74777ca97";
        MappingExecutor ex = new MappingExecutor(LANDSCAPE, "ignored", 5, 4, AasUtils.loadConfigsFromResources(),LOG_RESULTS);
        List<AssetAdministrationShell> shells = ex.queryAllShells(
                sampleId,
                List.of(new SpecificAssetIdentification.Builder()
                        .key("ignoredAnyway")
                        .value(sampleId)
                        .build()));
        assertEquals(1,shells.size(),"Found the correct shell");
        assertEquals(1,shells.get(0).getDescription().size(),"Shell has correct descriptions");
        assertEquals("HV Modul",shells.get(0).getDescription().get(0).getText(),"Shell has correct description");
        assertEquals(sampleId,shells.get(0).getIdShort(),"Correct id short");
        assertEquals(sampleId,shells.get(0).getAssetInformation().getGlobalAssetId(),"Correct global asset id");
        assertEquals(4,shells.get(0).getSubmodels().size(),"Correct number of submodels");
        shells.get(0).getSubmodels().forEach( submodel -> {
            String[] components=shells.get(0).getIdShort().split("/");
            assertTrue(submodel.getKeys().get(0).getValue().startsWith(components[0]),"Submodel reference starts with domain of twin id");
            assertTrue(submodel.getKeys().get(0).getValue().endsWith(components[1]),"Submodel reference ends with unique part of twin id");
        });
    }

    /**
     * test access to all shells
     */
    @Test
    void queryAllShells() {
        MappingExecutor ex = new MappingExecutor(LANDSCAPE, "ignored", 5, 4, AasUtils.loadConfigsFromResources(),LOG_RESULTS);
        List<AssetAdministrationShell> shells = ex.queryAllShells(
                null,
                null);
        assertEquals(28,shells.size(),"Found all shells");
        shells.forEach(shell -> {
            assertEquals(1,shell.getDescription().size(),"Shell has correct descriptions");
            assertEquals(shell.getIdShort(),shell.getAssetInformation().getGlobalAssetId(),"Correct global asset id and idshort relation");
            assertTrue(shell.getSubmodels().size() > 0,String.format("Shell %s has at least one submodel",shell.getIdShort()));
            shell.getSubmodels().forEach( submodel -> {
                String[] components=shell.getIdShort().split("/");
                assertTrue(submodel.getKeys().get(0).getValue().startsWith(components[0]),String.format("Submodel reference %s starts with domain of twin id %s",submodel.getKeys().get(0).getValue(),components[0]));
                assertTrue(submodel.getKeys().get(0).getValue().endsWith(components[1]),String.format("Submodel reference %s ends with unique part of twin id %s",submodel.getKeys().get(0).getValue(),components[1]));
            });
        });
    }


    /**
     * test access to one shell
     */
    @Test
    void queryOneSubmodel() {
        String sampleId="urn:uuid:e5c96ab5-896a-482c-8761-efd74777ca97";
        String model="urn:bamm:io.catenax.part_site_information_as_planned:1.0.0#PartSiteInformationAsPlanned";
        MappingExecutor ex = new MappingExecutor(LANDSCAPE, "ignored", 5, 4, AasUtils.loadConfigsFromResources(),LOG_RESULTS);
        String identifier="traceability/"+model+"/"+sampleId;
        Identifiable subModel = ex.queryIdentifiableById(identifier, Submodel.class);
        assertNotNull(subModel, "Found the submodel");
        assertTrue(subModel instanceof Submodel,"Its a real submodel");
        Submodel realSubModel=(Submodel) subModel;
        assertEquals(model,realSubModel.getSemanticId().getKeys().get(0).getValue());
    }

    private static Environment getTransformedAasEnv(String submodelIdShort,String suffix) throws IOException {
        Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(new FileInputStream("resources/traceability/" + submodelIdShort + "-mapping.xslt")));
        } catch (TransformerConfigurationException e) {
            throw new IOException(e);
        }
        XmlDeserializer deserializer = new XmlDeserializer();
        StreamSource inputData = new StreamSource(new FileInputStream("./resources/sparqlResponseXml/" + submodelIdShort + suffix + "-sparql-results.xml"));
        StringWriter writer=new StringWriter();
        try {
            transformer.transform(inputData,new StreamResult(writer));
        } catch (TransformerException e) {
            throw new IOException(e);
        }
        try {
            return deserializer.read(writer.toString());
        } catch (DeserializationException e) {
            throw new IOException(e);
        }
    }

    private static void executeGenericTests(Environment env, int maxNumberSubmodels) {
        // each AAS only holds a single Submodel
        env.getAssetAdministrationShells().forEach(aas -> assertTrue(aas.getSubmodels().size()<=maxNumberSubmodels));

        // each Submodel is referred to by a single AAS only
        env.getSubmodels().forEach(sm -> {
            long aasPerSm = env.getAssetAdministrationShells().stream()
                    .map(AssetAdministrationShell::getSubmodels)
                    .filter(smrefs ->
                            smrefs.stream().anyMatch(smref -> smref.getKeys().get(0).getValue().equals(sm.getId())
                            ))
                    .count();
            assertEquals(1, aasPerSm);
        });

        // check no value remains unmapped
        env.getSubmodels().forEach(sm->recurseSmecAndCheckForNull(sm.getSubmodelElements()));
    }

    private static void recurseSmecAndCheckForNull(Collection<SubmodelElement> smes) {
        smes.stream().filter(sme -> sme.getClass().equals(DefaultProperty.class))
                .map(sme -> (DefaultProperty) sme)
                .forEach(p->{
                    assertNotNull(p.getValue(),String.format("Value of property %s should not be null",p.getIdShort()));
                    assertNotNull(p.getValueType(),String.format("Value type of property %s should not be null",p.getIdShort()));
                    assertNotNull(p.getDescription(),String.format("Description of property %s should not be null",p.getIdShort()));
                    assertNotNull(p.getDisplayName(),String.format("Display name of property %s should not be null",p.getIdShort()));
                    assertNotEquals("", p.getValue(),String.format("Value of property %s should not be empty",p.getIdShort()));
                    assertNotEquals("null", p.getValue(),String.format("Value of property %s should not be \"null\"",p.getIdShort()));
                });
        smes.stream().filter(sme -> sme.getClass().equals(DefaultSubmodelElementCollection.class))
                .map(sme -> (DefaultSubmodelElementCollection) sme)
                .forEach(smec->recurseSmecAndCheckForNull(smec.getValue()));
    }

    private MockWebServer instantiateMockServer(String aspectName) throws IOException {
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
        try(InputStream inputStream = new FileInputStream("resources/sparqlResponseXml/" + aspectName +"-sparql-results.xml")) {
            String mockResponseBody = new String(inputStream
                    .readAllBytes());
            return mockResponseBody;
        }
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
                .findFirst().map(SubmodelElementCollection::getValue)
                .orElseThrow(()-> new RuntimeException("smcNotFound"));

    }
}