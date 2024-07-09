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

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.xml.XmlDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)

class AasUtilsTest {

    XmlDeserializer deser = new XmlDeserializer();
    Set<Environment> envs;
    Environment merged;

    @BeforeAll
    void instantiate() throws IOException, DeserializationException {

        envs = new HashSet<>(List.of(
                deser.read(fromResource("materialForRecycling.xml")),
                deser.read(fromResource("partAsPlanned.xml")),
                deser.read(fromResource("partSiteInformation.xml")),
                deser.read(fromResource("singleLevelBomAsPlanned.xml"))
        ));

        merged = AasUtils.mergeAasEnvs(envs);
    }

    @Test
    void noDupeSubmodelIds(){
        List<String> smIds = envs.stream().flatMap(env -> env.getSubmodels().stream().map(Identifiable::getId)).collect(Collectors.toList());
        List<String> distinctSmIds = smIds.stream().distinct().collect(Collectors.toList());
        assertEquals(smIds, distinctSmIds);
    }

    @Test
    void mergePreservesSubmodelIds(){
        Set<String> smIds = envs.stream().flatMap(env -> env.getSubmodels().stream().map(Identifiable::getId)).collect(Collectors.toSet());
        Set<String> mergedSmIds = merged.getSubmodels().stream().map(Identifiable::getId).collect(Collectors.toSet());

        assertEquals(smIds.size(),mergedSmIds.size());
        assertEquals(smIds,mergedSmIds);
    }

    @Test
    void mergePreservesSubmodelIdsInReferences(){
        Set<String> smIds = envs.stream().flatMap(env -> env.getSubmodels().stream().map(Identifiable::getId)).collect(Collectors.toSet());
        Set<String> referenceIds = merged.getAssetAdministrationShells().stream()
                .flatMap(aas -> aas.getSubmodels().stream().map(sm -> sm.getKeys().get(0).getValue())).collect(Collectors.toSet());

        assertEquals(smIds.size(),referenceIds.size());
        assertEquals(smIds,referenceIds);
    }
    @Test
    void mergeDoesNotEqualizeSubmodelIds() {

        // All SubmodelReferences in an AAS hold different targets
        merged.getAssetAdministrationShells().forEach(aas->
        {
            List<String> referredSubmodelIds = aas.getSubmodels().stream().map(sm ->
                    sm.getKeys().get(0).getValue()).collect(Collectors.toList());
            List<String> distinctReferredSubmodelIds = referredSubmodelIds.stream().distinct().collect(Collectors.toList());
            assertEquals(referredSubmodelIds,distinctReferredSubmodelIds);
        });
    }

    @Test
    void mergeReducesAasNumber(){
        assertTrue(merged.getAssetAdministrationShells().size()<=envs.stream().mapToLong(env->env.getAssetAdministrationShells().size()).sum());
    }

    String fromResource(String name) throws IOException {
        try(InputStream stream = getClass().getClassLoader()
                .getResourceAsStream("exampleAasEnvs/"+name)) {
            return new String(stream
                    .readAllBytes());
        }
    }
}