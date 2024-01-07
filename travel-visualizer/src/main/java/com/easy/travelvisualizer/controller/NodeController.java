package com.easy.travelvisualizer.controller;

import com.easy.travelvisualizer.dto.NodeRequest;
import com.easy.travelvisualizer.dto.NodeResponse;
import com.easy.travelvisualizer.dto.UpdateNodeRequest;
import com.easy.travelvisualizer.service.NodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/nodes")
public class NodeController {

    private final NodeService nodeService;

    @GetMapping
    public List<NodeResponse> getAll() {
        return nodeService.findAll();
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody NodeRequest newNode) {
        nodeService.create(newNode);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping
    public ResponseEntity<Void> update(@RequestBody UpdateNodeRequest updateNode) {
        nodeService.update(updateNode);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
