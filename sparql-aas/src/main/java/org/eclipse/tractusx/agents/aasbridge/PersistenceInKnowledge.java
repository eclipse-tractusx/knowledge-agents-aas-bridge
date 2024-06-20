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

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AssetAdministrationShellSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.ConceptDescriptionSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelElementSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.util.QueryModifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implements the FAAAST Persistence API by means of a set of
 * mapping executors which generate AAS objects from the given
 * parameters on the fly.
 */
public class PersistenceInKnowledge implements Persistence<PersistenceInKnowledgeConfig> {

    private static final String MSG_MODIFIER_NOT_NULL = "The message modifier cannot be null";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    PersistenceInKnowledgeConfig persistenceConfig;
    CoreConfig coreConfig;
    ServiceContext serviceContext;
    Environment model;
    MappingExecutor executor;

    public PersistenceInKnowledge() {
    }

    @Override
    public void init(CoreConfig coreConfig, PersistenceInKnowledgeConfig persistenceInKnowledgeConfig, ServiceContext serviceContext) throws ConfigurationInitializationException {
        logger.info("Initializing AAS Bridge Persistence");
        Ensure.requireNonNull(coreConfig, "coreConfig must be non-null");
        Ensure.requireNonNull(persistenceInKnowledgeConfig, "config must be non-null");
        Ensure.requireNonNull(serviceContext, "context must be non-null");
        this.persistenceConfig = persistenceInKnowledgeConfig;
        this.coreConfig = coreConfig;
        this.serviceContext = serviceContext;
        this.executor = new MappingExecutor(
                persistenceConfig.getProviderSparqlEndpoint(),
                persistenceConfig.getCredentials(),
                persistenceConfig.getTimeoutSeconds(),
                persistenceConfig.getThreadPoolSize(),
                persistenceConfig.getMappings(),
                persistenceConfig.isLogResults());
        this.model = new DefaultEnvironment.Builder().build();
    }

    @Override
    public PersistenceInKnowledgeConfig asConfig() {
        return persistenceConfig;
    }

    @Override
    public AssetAdministrationShell getAssetAdministrationShell(String s, QueryModifier queryModifier) throws ResourceNotFoundException {
        return get(s, queryModifier, AssetAdministrationShell.class);
    }

    @Override
    public Page<org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell> findAssetAdministrationShells(AssetAdministrationShellSearchCriteria assetAdministrationShellSearchCriteria, QueryModifier queryModifier, PagingInfo pagingInfo) {
        return preparePagedResult(executor.queryAllShells(assetAdministrationShellSearchCriteria.getIdShort(), assetAdministrationShellSearchCriteria.getAssetIds()).stream(),
                queryModifier, pagingInfo);
    }

    @Override
    public void save(org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell assetAdministrationShell) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAssetAdministrationShell(String s) throws ResourceNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Submodel getSubmodel(String s, QueryModifier queryModifier) throws ResourceNotFoundException {
        return get(s, queryModifier, Submodel.class);
    }

    @Override
    public Page<org.eclipse.digitaltwin.aas4j.v3.model.Submodel> findSubmodels(SubmodelSearchCriteria submodelSearchCriteria, QueryModifier queryModifier, PagingInfo pagingInfo) {
        return preparePagedResult(executor.queryAllSubmodels(submodelSearchCriteria.getIdShort(), submodelSearchCriteria.getSemanticId()).stream(), queryModifier, pagingInfo);
    }

    @Override
    public void save(org.eclipse.digitaltwin.aas4j.v3.model.Submodel submodel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteSubmodel(String s) throws ResourceNotFoundException {
        throw new UnsupportedOperationException();
    }


    @Override
    public Page<Reference> getSubmodelRefs(String s, PagingInfo pagingInfo) throws ResourceNotFoundException {
        throw new UnsupportedOperationException();
    }


    @Override
    public ConceptDescription getConceptDescription(String s, QueryModifier queryModifier) throws ResourceNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SubmodelElement getSubmodelElement(SubmodelElementIdentifier submodelElementIdentifier, QueryModifier queryModifier) throws ResourceNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public OperationResult getOperationResult(OperationHandle operationHandle) throws ResourceNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Page<org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement> findSubmodelElements(SubmodelElementSearchCriteria submodelElementSearchCriteria, QueryModifier queryModifier, PagingInfo pagingInfo) throws ResourceNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Page<ConceptDescription> findConceptDescriptions(ConceptDescriptionSearchCriteria conceptDescriptionSearchCriteria, QueryModifier queryModifier, PagingInfo pagingInfo) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void save(ConceptDescription conceptDescription) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void insert(SubmodelElementIdentifier submodelElementIdentifier, org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement submodelElement) throws ResourceNotFoundException, ResourceNotAContainerElementException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(SubmodelElementIdentifier submodelElementIdentifier, org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement submodelElement) throws ResourceNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save(OperationHandle operationHandle, OperationResult operationResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteConceptDescription(String s) throws ResourceNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteSubmodelElement(SubmodelElementIdentifier submodelElementIdentifier) throws ResourceNotFoundException {
        throw new UnsupportedOperationException();
    }

    /**
     * gets a particular entity (assetadministrationshell, asset, submodel, conceptdescription)
     *
     * @param id       identifier for the entity
     * @param modifier the modifier for the response
     * @param type     the class that should be returned
     * @param <T>      return type
     * @return instance of the entity (rendered along modifier)
     * @throws ResourceNotFoundException in case the resource cannot be found
     */
    public <T extends Identifiable> T get(String id, QueryModifier modifier, Class<T> type) throws ResourceNotFoundException {
        Ensure.requireNonNull(id, "id must be non-null");
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        Ensure.requireNonNull(type, "type must be non-null");
        Identifiable result = executor.queryIdentifiableById(id, type);
        if (result == null) {
            throw new ResourceNotFoundException(id, type);
        }
        if (!type.isAssignableFrom(result.getClass())) {
            throw new ResourceNotFoundException(String.format("Resource found but does not match expected type (id: %s, expected type: %s, actual type: %s)",
                    id,
                    type,
                    result.getClass()));
        }
        T res = type.cast(result);
        return QueryModifierHelper.applyQueryModifier(
                res,
                modifier);
    }

    private static long readCursor(String cursor) {
        return Long.parseLong(cursor);
    }

    private static String nextCursor(PagingInfo paging, int resultCount) {
        return nextCursor(paging, paging.hasLimit() && (long) resultCount > paging.getLimit());
    }

    private static String writeCursor(long index) {
        return Long.toString(index);
    }

    private static String nextCursor(PagingInfo paging, boolean hasMoreData) {
        if (!hasMoreData) {
            return null;
        } else if (!paging.hasLimit()) {
            throw new IllegalStateException("unable to generate next cursor for paging - there should not be more data available if previous request did not have a limit set");
        } else {
            return Objects.isNull(paging.getCursor()) ? writeCursor(paging.getLimit()) : writeCursor(readCursor(paging.getCursor()) + paging.getLimit());
        }
    }

    private static <T> Page<T> preparePagedResult(Stream<T> input, PagingInfo paging) {
        Stream<T> result = input;
        if (Objects.nonNull(paging.getCursor())) {
            result = input.skip(readCursor(paging.getCursor()));
        }

        if (paging.hasLimit()) {
            result = result.limit(paging.getLimit() + 1L);
        }

        List<T> temp = result.collect(Collectors.toList());
        return (Page<T>) Page.builder()
                .result(temp.stream()
                        .limit(paging.hasLimit() ? paging.getLimit() : (long) temp.size())
                        .collect(Collectors.toList()))
                .metadata(
                        PagingMetadata.builder()
                                .cursor(nextCursor(paging, temp.size()))
                                .build()
                )
                .build();
    }

    private static <T extends Referable> Page<T> preparePagedResult(Stream<T> input, QueryModifier modifier, PagingInfo paging) {
        Page<T> result = preparePagedResult(input, paging);
        result.setContent(QueryModifierHelper.applyQueryModifier(result.getContent().stream().map(DeepCopyHelper::deepCopy).collect(Collectors.toList()), modifier));
        return result;
    }

}
