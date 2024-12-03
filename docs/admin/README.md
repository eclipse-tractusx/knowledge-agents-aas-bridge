<!--
 * Copyright (c) 2022,2024 Contributors to the Eclipse Foundation
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
# Tractus-X Knowledge Agents AAS Bridges (KA-AAS) Administration Guide

## Deployment

Deployment can be done
* via [JAR libraries](https://github.com/orgs/eclipse-tractusx/packages?repo_name=knowledge-agents-aas-bridge&ecosystem=maven) copied into your Java runtime
* via [Docker images](https://hub.docker.com/r/tractusx) 
* via [Helm Charts (Stable Versions)](https://eclipse-tractusx.github.io/charts/stable) or [Helm Charts (Dev Versions)](https://eclipse-tractusx.github.io/charts/stable)


## Helm Chart for Sparql-To-AAS Bridge

A helm chart for deploying the bridge can be found under [this folder](../../charts/aas-bridge).

It can be added to your umbrella chart.yaml by the following snippet

```console
dependencies:
  - name: aas-bridge
    repository: https://eclipse-tractusx.github.io/charts/dev
    version: 1.14.24-SNAPSHOT
    alias: my-aas-bridge
```

and then installed using

```console
helm dependency update
```

In your values.yml, you configure your specific instance of the conforming agent like this

```console
aas-bridge:
  aas:
    persistence:
      # -- The sparql server
      sparql: http://oem-provider-agent:8082/sparql
    endpoints:
      default:
        path: "/"
  ingresses:
  - enabled: true
    hostname: *oemAasHost
    annotations:
      nginx.ingress.kubernetes.io/backend-protocol: "HTTPS"
#      nginx.ingress.kubernetes.io/ssl-passthrough: "true"
#      nginx.ingress.kubernetes.io/ssl-redirect: "true"
    endpoints:
      - default
    tls:
      enabled: true
    certManager:
      clusterIssuer: *clusterIssuer
```



