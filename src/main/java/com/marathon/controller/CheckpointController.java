package com.marathon.controller;

import com.marathon.model.Checkpoint;
import com.marathon.service.CheckpointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/checkpoints")
public class CheckpointController {

    @Autowired
    private CheckpointService checkpointService;

    @PostMapping
    public ResponseEntity<?> createCheckpoint(@RequestBody Checkpoint checkpoint) {
        try {
            Checkpoint savedCheckpoint = checkpointService.createCheckpoint(checkpoint);
            return ResponseEntity.ok(savedCheckpoint);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating checkpoint: " + e.getMessage());
        }
    }

    @GetMapping
    public List<Checkpoint> getAllCheckpoints() {
        return checkpointService.getAllCheckpoints();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Checkpoint> getCheckpointById(@PathVariable Long id) {
        Optional<Checkpoint> checkpoint = checkpointService.getCheckpointById(id);
        return checkpoint.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCheckpoint(@PathVariable Long id, @RequestBody Checkpoint checkpoint) {
        try {
            Checkpoint updatedCheckpoint = checkpointService.updateCheckpoint(id, checkpoint);
            return ResponseEntity.ok(updatedCheckpoint);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating checkpoint: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCheckpoint(@PathVariable Long id) {
        try {
            checkpointService.deleteCheckpoint(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting checkpoint: " + e.getMessage());
        }
    }

    @GetMapping("/start")
    public ResponseEntity<Checkpoint> getStartPoint() {
        Optional<Checkpoint> startPoint = checkpointService.getStartPoint();
        return startPoint.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/finish")
    public ResponseEntity<Checkpoint> getFinishPoint() {
        Optional<Checkpoint> finishPoint = checkpointService.getFinishPoint();
        return finishPoint.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/midpoints")
    public List<Checkpoint> getMidPoints() {
        return checkpointService.getMidPoints();
    }
}