package io.github.vinipx.wixy.controller;

import io.github.vinipx.wixy.service.ProxyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for proxy configuration management.
 */
@RestController
@RequestMapping("/wixy/admin/proxy")
@Tag(name = "Proxy Management", description = "Enable/disable/query proxy mode")
public class ProxyController {

    private final ProxyService proxyService;

    public ProxyController(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @GetMapping
    @Operation(summary = "Get current proxy status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(proxyService.getStatus());
    }

    @PostMapping("/enable")
    @Operation(summary = "Enable proxy mode to a target URL")
    public ResponseEntity<Map<String, String>> enable(@RequestBody Map<String, String> body) {
        String targetUrl = body.getOrDefault("targetUrl", "");
        proxyService.enableProxy(targetUrl);
        return ResponseEntity.ok(Map.of("status", "Proxy enabled", "targetUrl", targetUrl));
    }

    @PostMapping("/disable")
    @Operation(summary = "Disable proxy mode")
    public ResponseEntity<Map<String, String>> disable() {
        proxyService.disableProxy();
        return ResponseEntity.ok(Map.of("status", "Proxy disabled"));
    }
}
