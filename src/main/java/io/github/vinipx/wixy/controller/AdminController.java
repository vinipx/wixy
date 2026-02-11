package io.github.vinipx.wixy.controller;

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

    public AdminController(StubService stubService) {
        this.stubService = stubService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List all stub mappings")
    public ResponseEntity<Map<String, Object>> listAll() {
        List<StubMapping> mappings = stubService.listAll();
        return ResponseEntity.ok(Map.of(
                "mappings", mappings.stream().map(StubMapping::toString).toList(),
                "meta", Map.of("total", mappings.size())
        ));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new stub mapping")
    public ResponseEntity<String> create(@RequestBody String json) {
        StubMapping created = stubService.create(json);
        return ResponseEntity.status(HttpStatus.CREATED).body(created.toString());
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a stub mapping by ID")
    public ResponseEntity<String> getById(@PathVariable UUID id) {
        StubMapping mapping = stubService.getById(id);
        return ResponseEntity.ok(mapping.toString());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a stub mapping")
    public ResponseEntity<String> update(@PathVariable UUID id, @RequestBody String json) {
        StubMapping updated = stubService.update(id, json);
        return ResponseEntity.ok(updated.toString());
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
