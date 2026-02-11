package io.github.vinipx.wixy.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.github.vinipx.wixy.exception.InvalidStubDefinitionException;
import io.github.vinipx.wixy.exception.StubNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service layer for WireMock stub CRUD operations.
 */
@Service
public class StubService {

    private static final Logger log = LoggerFactory.getLogger(StubService.class);

    private final WireMockServer wireMockServer;

    public StubService(WireMockServer wireMockServer) {
        this.wireMockServer = wireMockServer;
    }

    /**
     * List all active stub mappings.
     */
    public List<StubMapping> listAll() {
        return wireMockServer.getStubMappings();
    }

    /**
     * Get a single stub mapping by its UUID.
     */
    public StubMapping getById(UUID id) {
        StubMapping mapping = wireMockServer.getSingleStubMapping(id);
        if (mapping == null) {
            throw new StubNotFoundException(id.toString());
        }
        return mapping;
    }

    /**
     * Create a new stub mapping from a WireMock JSON definition.
     *
     * @param json WireMock stub mapping JSON string
     * @return the created StubMapping
     */
    public StubMapping create(String json) {
        try {
            StubMapping mapping = StubMapping.buildFrom(json);
            wireMockServer.addStubMapping(mapping);
            log.info("Created stub mapping: {} → {}", mapping.getRequest(), mapping.getId());
            return mapping;
        } catch (Exception e) {
            throw new InvalidStubDefinitionException("Failed to parse stub definition: " + e.getMessage(), e);
        }
    }

    /**
     * Update an existing stub mapping.
     *
     * @param id   the UUID of the stub to update
     * @param json the new stub mapping JSON
     * @return the updated StubMapping
     */
    public StubMapping update(UUID id, String json) {
        // Verify it exists
        getById(id);

        try {
            StubMapping mapping = StubMapping.buildFrom(json);
            mapping.setId(id);
            wireMockServer.editStubMapping(mapping);
            log.info("Updated stub mapping: {}", id);
            return mapping;
        } catch (StubNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidStubDefinitionException("Failed to parse stub definition: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a stub mapping by UUID.
     */
    public void delete(UUID id) {
        // Verify it exists
        getById(id);
        wireMockServer.removeStubMapping(wireMockServer.getSingleStubMapping(id));
        log.info("Deleted stub mapping: {}", id);
    }

    /**
     * Remove all stub mappings.
     */
    public void resetAll() {
        wireMockServer.resetMappings();
        log.info("All stub mappings have been reset");
    }

    /**
     * Bulk-import stub mappings from a JSON array/object string.
     *
     * @param json JSON containing stub mappings (WireMock import format)
     * @return number of imported mappings
     */
    public int importStubs(String json) {
        try {
            // WireMock expects the mappings in its standard import format
            List<StubMapping> mappings = StubMapping.buildFrom(json) != null
                    ? List.of(StubMapping.buildFrom(json))
                    : List.of();

            for (StubMapping mapping : mappings) {
                wireMockServer.addStubMapping(mapping);
            }
            log.info("Imported {} stub mapping(s)", mappings.size());
            return mappings.size();
        } catch (Exception e) {
            throw new InvalidStubDefinitionException("Failed to import stubs: " + e.getMessage(), e);
        }
    }
}
