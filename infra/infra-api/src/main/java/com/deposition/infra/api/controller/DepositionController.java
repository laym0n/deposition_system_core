package com.deposition.infra.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class DepositionController {

    @PostMapping("/depone")
    public ResponseEntity<String> depone(@RequestBody String request) {
        return ResponseEntity.ok("Deposition request received: " + request);
    }
}
