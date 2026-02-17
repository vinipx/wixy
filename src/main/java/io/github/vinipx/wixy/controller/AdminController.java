package io.github.vinipx.wixy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.github.vinipx.wixy.service.StubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for WireMock stub CRUD operations.
 */
@RestController
@RequestMapping("/wixy/admin/mappings")
@Tag(name = "Stub Management", description = "CRUD operations for WireMock stub mappings")
public class AdminController {

    private final StubService stubService;
    private final ObjectMapper objectMapper;

    public AdminController(StubService stubService, ObjectMapper objectMapper) {
        this.stubService = stubService;
        this.objectMapper = objectMapper;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List all stub mappings")
    public ResponseEntity<Map<String, Object>> listAll() {
        List<StubMapping> mappings = stubService.listAll();
        // Convert to raw maps to avoid Jackson serialization issues with WireMock types
        List<Object> rawMappings = mappings.stream()
                .map(m -> {
                    try {
                        return objectMapper.readTree(m.toString());
                    } catch (Exception e) {
                        return m;
                    }
                })
                .toList();

        return ResponseEntity.ok(Map.of(
                "mappings", rawMappings,
                "meta", Map.of("total", mappings.size())
        ));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new stub mapping")
    public ResponseEntity<Object> create(@RequestBody String json) {
        StubMapping created = stubService.create(json);
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(objectMapper.readTree(created.toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a stub mapping by ID")
    public ResponseEntity<Object> getById(@PathVariable UUID id) {
        StubMapping mapping = stubService.getById(id);
        try {
            return ResponseEntity.ok(objectMapper.readTree(mapping.toString()));
        } catch (Exception e) {
            return ResponseEntity.ok(mapping);
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a stub mapping")
    public ResponseEntity<Object> update(@PathVariable UUID id, @RequestBody String json) {
        StubMapping updated = stubService.update(id, json);
        try {
            return ResponseEntity.ok(objectMapper.readTree(updated.toString()));
        } catch (Exception e) {
            return ResponseEntity.ok(updated);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a stub mapping")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        stubService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset (delete) all stub mappings")
    public ResponseEntity<Map<String, String>> resetAll() {
        stubService.resetAll();
        return ResponseEntity.ok(Map.of("status", "All mappings reset"));
    }

    @PostMapping(value = "/import", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Bulk-import stub mappings from JSON")
    public ResponseEntity<Map<String, Object>> importStubs(@RequestBody String json) {
        int count = stubService.importStubs(json);
        return ResponseEntity.ok(Map.of("imported", count));
    }
}
