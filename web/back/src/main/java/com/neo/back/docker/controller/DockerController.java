package com.neo.back.docker.controller;

import com.neo.back.docker.service.DockerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class DockerController {

    private final DockerService dockerService;

    public DockerController(DockerService dockerService) {
        this.dockerService = dockerService;
    }

    @GetMapping("/api/containers")
    public Mono<String> getContainers() {
        return dockerService.listContainers();
    }
}