# Copyright (c) 2023,2024 T-Systems International GmbH
# Copyright (c) 2023 SAP SE
# Copyright (c) 2023,2024 Contributors to the Eclipse Foundation
#
# See the NOTICE file(s) distributed with this work for additional
# information regarding copyright ownership.
#
# This program and the accompanying materials are made available under the
# terms of the Apache License, Version 2.0 which is available at
# https://www.apache.org/licenses/LICENSE-2.0.
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations
# under the License.
#
# SPDX-License-Identifier: Apache-2.0


FROM alpine:3.19.0 AS otel

ENV OTEL_AGENT_LOCATION "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.27.0/opentelemetry-javaagent.jar"

HEALTHCHECK NONE

RUN apk update && apk add curl=8.5.0-r0 --no-cache
RUN curl -L --proto "=https" -sSf ${OTEL_AGENT_LOCATION} --output /tmp/opentelemetry-javaagent.jar

FROM eclipse-temurin:22_36-jre-alpine

ARG APP_USER=faaast
ARG APP_UID=10100
ARG APP_GID=30000

ARG PROVIDER_SPARQL_ENDPOINT_ARG
ARG PROVIDER_CREDENTIAL_BASIC_ARG

ENV PROVIDER_SPARQL_ENDPOINT ${PROVIDER_SPARQL_ENDPOINT_ARG}
ENV PROVIDER_CREDENTIAL_BASIC ${PROVIDER_CREDENTIAL_BASIC_ARG}
ENV TIMEOUT_SECONDS 10

RUN addgroup --gid "$APP_GID" --system "$APP_USER"

RUN adduser \
     --shell /sbin/nologin \
     --disabled-password \
     --gecos "" \
     --ingroup "$APP_USER" \
     --no-create-home \
     --uid "$APP_UID" \
     "$APP_USER"

RUN mkdir /app \
    && chgrp -R 0 /app \
    && chmod -R g=u /app \
    # Create directories to which FA³ST needs permissions
    && mkdir /app/resources /app/logs /app/PKI /app/USERS_PKI \
    # Grant read and write permissions on created directories
    && chmod -R ugo+rw /app/resources /app/PKI /app/USERS_PKI /app/logs
    
USER "$APP_USER"
WORKDIR /app

COPY --from=otel /tmp/opentelemetry-javaagent.jar .
COPY target/aas-bridge.jar aas-bridge.jar
COPY target/lib ./lib/
COPY resources ./resources/

EXPOSE 8443

HEALTHCHECK NONE

CMD [ "java", \
     "-javaagent:/app/opentelemetry-javaagent.jar", \
     "-Dotel.javaagent.configuration-file=/app/opentelemetry.properties", \
     "-Dotel.metrics.exporter=prometheus", \
     "-Dotel.exporter.prometheus.port=9090", \
     "-Djava.security.edg=file:/dev/.urandom", \
     "-jar", \
     "aas-bridge.jar"]
