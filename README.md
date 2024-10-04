<!--
 * Copyright (c) 2023,2024 T-Systems International GmbH 
 * Copyright (c) 2023 SAP SE 
 * Copyright (c) 2023,2024 Contributors to the Eclipse Foundation
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

# Tractus-X Knowledge Agents AAS Bridges (KA-AAS)

![GitHub contributors](https://img.shields.io/github/contributors/eclipse-tractusx/knowledge-agents-aas-bridge)
![GitHub Org's stars](https://img.shields.io/github/stars/eclipse-tractusx)
![GitHub](https://img.shields.io/github/license/eclipse-tractusx/knowledge-agents-aas-bridge)
![GitHub all releases](https://img.shields.io/github/downloads/eclipse-tractusx/knowledge-agents-aas-bridge/total)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=eclipse-tractusx_knowledge-agents-aas-bridge&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=eclipse-tractusx_knowledge-agents-aas-bridge)

Tractus-X Knowledge Agents AAS Bridges (KA-AAS) is a product of the [Catena-X Knowledge Agents Kit (about to move to: Tractus-X Knowledge Agents Kit)](https://bit.ly/tractusx-agents). It implements bridging components between the [Catena-X Association's](http://catena-x.net) CX-0084 (Federated Queries in Dataspaces) and [Industrial Digitial Twin Association's](https://industrialdigitaltwin.org/) AAS Part 1 & 2 (Asset Administration Sheel) standards. 

* See the [Authors](AUTHORS.md)
* See the [Changelog](CHANGELOG.md)
* See the [Code of Conduct](CODE_OF_CONDUCT.md)
* See the [Contribution Guidelines](CONTRIBUTING.md)
* See the [License](LICENSE)
* See the [Notice on 3rd Party Software](NOTICE.md)
* See this [Readme](README.md)
* See the [Security Notice](Security.md)

## About the Project

This repository provides FOSS implementations for so-called bridging components between 'Knowledge Agents' and 'Asset Administration Shells'.

According to CX-0084, an "Agent" is a component that allows to represent and query a backend system ('source') using [Semantic Web](https://www.w3.org/2001/sw/wiki/Main_Page) protocols (such as [SPARQL](https://www.w3.org/2001/sw/wiki/SPARQL)).

According to AAS Part 1 & 2, an "Asset Administration Shell" (Server, Registry) is a component that allows to represent and query a source using a predefined REST Api.

Both Knowledge Agents as well as AAS components are expected to be compatible to the [Tractus-X EDC](https://github.com/eclipse-tractusx/tractusx-edc) connector.

Currently we provide the following bridge components: 

- [Knowledge Agents Sparql-To-AAS Bridge (KA-AAS-SPARQL)](sparql-aas) Implements an AAS server/AAS registry interface that is backed by a one or several agents. Using a templating approach, queries to the AAS API are translated into SPARQL queries. Changes in the (virtual) knowledge graph should be automatically reflected in changes to the resulting (virtual) AAS tree. 

Included in this repository are ready-made [Helm charts](charts). 

They can be installed from the [Tractus-X Helm Repository (Stable Versions)](https://eclipse-tractusx.github.io/charts/stable) or [Tractus-X Helm Repository (Dev Versions)](https://eclipse-tractusx.github.io/charts/dev).

## Getting Started

### Build

To compile, package and containerize the binary artifacts (includes running the unit tests)

```shell
./mvnw package -Pwith-docker-image
```

To publish the binary artifacts (environment variables GITHUB_ACTOR and GITHUB_TOKEN must be set)

```shell
./mvnw -s settings.xml publish
```

To update the [DEPENDENCIES](./DEPENDENCIES) declarations

```shell
./mvnw org.eclipse.dash:license-tool-plugin:license-check 
```

### Deployment

Deployment can be done
* via [JAR libraries](https://github.com/orgs/eclipse-tractusx/packages?repo_name=knowledge-agents-aas-bridge&ecosystem=maven) copied into your Java runtime
* via [Docker images](https://hub.docker.com/r/tractusx) 
* via [Helm Charts (Stable Versions)](https://eclipse-tractusx.github.io/charts/stable) or [Helm Charts (Dev Versions)](https://eclipse-tractusx.github.io/charts/stable)

See the individual bridge documentations for more detailed deployment information
* [Knowledge Agents Sparql-To-AAS Bridge (KA-AAS-SPARQL)](sparql-aas/README.md)

#### Setup using Helm/Kind

In order to run KA-RI applications via helm on your local machine, please make sure the following
preconditions are met.

- Have a local Kubernetes runtime ready. We've tested this setup with [KinD](https://kind.sigs.k8s.io/), but other
  runtimes such
  as [Minikube](https://minikube.sigs.k8s.io/docs/start/) may work as well, we just haven't tested them. All following
  instructions will assume KinD.

For the most bare-bones installation of the dataspace, execute the following commands in a shell:

```shell
kind create cluster -n ka --config kind.config.yaml
# the next step is specific to KinD and will be different for other Kubernetes runtimes!
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
# wait until the ingress controller is ready
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=90s
# transfer images
kind load docker-image docker.io/tractusx/aas-bridge:1.14.24-SNAPSHOT --name ka
# run container test
ct install --charts charts/aas-bridge
```


### Notice for Docker Images

* [Notice for Knowledge Agents Sparql-To-AAS Bridge (KA-AAS-SPARQL)](sparql-aas/README.md#notice-for-docker-images)

