<!--
 * Copyright (c) 2023 SAP SE 
 * Copyright (c) 2023 T-Systems International GmbH 
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
-->

## Tractus-X Knowledge Agents Sparql-To-AAS Bridge (KA-AAS-SPARQL)

The AAS-Bridge exposes information from the Knowledge Graph via the APIs of the Asset Administration Shell. It builds
upon the FAAAST Framework and the AAS4J-Transformation-Library.

### Configuration

By default, the AAS-Bridge scans for "domain" folders (see e.g. the [traceability domain](resources/traceability)) in the "resources" directory 
in which the AAS-Bridge [Java Application](src/main/java/org/eclipse/tractusx/agents/aasbridge/AasBridge.java) has been started. 

#### Domain Folders in the Resource Directory

Each domain describes a set of equally structured digital twins (in above example these are serialized parts along the Catena-X Ontology and its Traceability Semantic Models).
All twins of a domain are hosted in the same graph and they share the same set of submodels. We require that the domain id (folder name) coincides to the 
first part of all asset and submodel ids (separated via "/").

#### Mapping Configuration

The structure (shell) of such a twin as well as the submodels are each described by a mapping configuration which is backed
by a set of files which share a common prefix and end with a suffix which describes their role.

A mapping configuration (for the sample the "partAsPlanned" submodel) consists of three files:
- An [Unbound Query select-all.rq](resources/traceability/partAsPlanned-select-all.rq) is a non-parameterized SPARQL query that when executed against the graph will generated a dataset of part information for all parts appearing in the graph.
- A [Bound Query select-some.rq](resources/traceability/partAsPlanned-select-some.rq) is a parameterized SPARQL query that will be given an argument ("%s") which will be formatted with the set of IRI literals coinciding to the IDs of the selected twins/parts.
- A [Mapping Specification mapping.xslt](resources/traceability/partAsPlanned-mapping.xslt) that hosts a XML stylesheet template which transforms the SPARQL result sets of above queries into an temporary AAS4J environment following this [AAS XML Schema](https://github.com/eclipse-aas4j/aas4j/blob/main/dataformat-xml/src/main/resources/AAS.xsd). This environment will be used to answer the respective endpoint query against the AAS server. After the query, the environment will be freed from memory, again.

Each mapping configuration (mapping.xslt) will introduce the namespace "semanticId". 
If the "semanticId" is "https://w3id.org/catenax/ontology/aas#", then the mapping configuration will be used to build AssetAdministationShells (the actual twin "headers").
Otherwise, the "semanticId" will be used to identifiy the respective submodel (and will be referred to in the [Shell mapping.xslt](resources/traceability/aas-mapping.xslt), the [Shell select-all.rq](resources/traceability/aas-select-all.rq) and [Shell select-some.rq](resources/traceability/aas-select-some.rq))

#### Mapping Specification

The AAS-Bridge makes a couple of assumptions about the content of the MappingSpecification:
1. The xsl:stylesheet namespaces AND the xsl:variable section both hold the semanticId of the Submodel that is to
be transformed
2. IDs of submodel-instances must always start with the domain id (tracability/) followed by the semanticId of the submodel (/urn:bamm:io.catenax.part_as_planned:1.0.1#PartAsPlanned) and the identifier of
the asset ("/urn:uuid:0733946c-59c6-41ae-9570-cb43a6e4c79e" - all separated by "/"). How this can be achieved via a template function is demonstrated in the examples' under `xsl:template name="genSubmodelId"`.
3. The function `AasUtils.loadConfigsFromResources()` will search the AAS-Bridge's working directory "resources"
folder for a set of the necessary data. The folder- and naming-convention must be adhered to strictly.

#### Connection to the Knowledge Graph

Currently, AAS-Bridge only supports a single endpoint which is configured by environment variables
- PROVIDER_SPARQL_ENDPOINT - URL pointing to the SPARQL endpoint
- PROVIDER_CREDENTIAL_BASIC - the value that will be set into the "Authorization" header of outgoing SPARQL requests.

## Building

You could invoke the following command to compile and test the Sparql-To-AAS bridge

```console
mvn -s ../../../settings.xml install
```

## Deployment & Usage

### Containerizing 

You could invoke the following command to build the Sparql-To-AAS bridge

```console
mvn -s ../../../settings.xml install -Pwith-docker-image
```

Alternatively, after a sucessful [build](#building) the docker image of the Sparql-To-AAS bridge is created using

```console
docker build -t tractusx/aas-bridge:0.13.6-SNAPSHOT -f src/main/docker/Dockerfile .
```

To run the docker image against a local knowledge graph, you could invoke this command

```console
docker run -p 8443:8443 \
  -v $(pwd)/resources:/app/resources \
  -e "PROVIDER_SPARQL_ENDPOINT=http://oem-provider-agent:8082/sparql" \
  -e "PROVIDER_CREDENTIAL_BASIC=Basic Zm9vOg==" \
  tractusx/aas-bridge:0.13.6-SNAPSHOT
````

Afterwards, you should be able to access the [local AAS endpoint](https://localhost:8443/) via REST

```console
curl --request GET 'https://localhost:8443/api/v3.0/description'
```

### Notice for Docker Image

DockerHub: https://hub.docker.com/r/tractusx/aas-bridge

Eclipse Tractus-X product(s) installed within the image:
GitHub: https://github.com/eclipse-tractusx/knowledge-agents-aas-bridge/tree/main/sparql-aas
Project home: https://projects.eclipse.org/projects/automotive.tractusx
Dockerfile: https://github.com/eclipse-tractusx/knowledge-agents-aas-bridge/blob/main/sparql-aas/src/main/docker/Dockerfile
Project license: Apache License, Version 2.0


**Used base image**

- [eclipse-temurin:17-jre-alpine](https://github.com/adoptium/containers)
- Official Eclipse Temurin DockerHub page: https://hub.docker.com/_/eclipse-temurin
- Eclipse Temurin Project: https://projects.eclipse.org/projects/adoptium.temurin
- Additional information about the Eclipse Temurin images: https://github.com/docker-library/repo-info/tree/master/repos/eclipse-temurin

As with all Docker images, these likely also contain other software which may be under other licenses (such as Bash, etc from the base distribution, along with any direct or indirect dependencies of the primary software being contained).

As for any pre-built image usage, it is the image user's responsibility to ensure that any use of this image complies with any relevant licenses for all software contained within.

