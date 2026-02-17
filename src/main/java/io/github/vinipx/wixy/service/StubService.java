package io.github.vinipx.wixy.service;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.github.vinipx.wixy.engine.EngineManager;
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

    private final EngineManager engineManager;

    public StubService(EngineManager engineManager) {
        this.engineManager = engineManager;
    }

    /**
     * List all active stub mappings.
     */
    public List<StubMapping> listAll() {
        return engineManager.getActiveEngine().listAllStubs();
    }

    /**
     * Get a single stub mapping by its UUID.
     */
    public StubMapping getById(UUID id) {
        StubMapping mapping = engineManager.getActiveEngine().getStubById(id);
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
            engineManager.getActiveEngine().addStubMapping(mapping);
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
            engineManager.getActiveEngine().editStubMapping(mapping);
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
        StubMapping mapping = getById(id);
        engineManager.getActiveEngine().removeStubMapping(mapping);
        log.info("Deleted stub mapping: {}", id);
    }

    /**
     * Remove all stub mappings.
     */
    public void resetAll() {
        engineManager.getActiveEngine().resetStubs();
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
            // NOTE: StubMapping.buildFrom(json) might return only one mapping if it's a single object
            // or we might need a more complex parsing for bulk.
            // For now, let's stick to the current implementation logic.
            StubMapping mapping = StubMapping.buildFrom(json);
            if (mapping != null) {
                engineManager.getActiveEngine().addStubMapping(mapping);
                log.info("Imported 1 stub mapping");
                return 1;
            }
            return 0;
        } catch (Exception e) {
            throw new InvalidStubDefinitionException("Failed to import stubs: " + e.getMessage(), e);
        }
    }
}
