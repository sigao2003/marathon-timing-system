package com.marathon.controller;

import com.marathon.model.RaceRecord;
import com.marathon.service.TimingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timing")
public class TimingController {

    @Autowired
    private TimingService timingService;

    @GetMapping("/records/{athleteId}")
    public ResponseEntity<List<RaceRecord>> getAthleteRecords(@PathVariable Long athleteId) {
        try {
            List<RaceRecord> records = timingService.getAthleteRecords(athleteId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/manual")
    public ResponseEntity<?> createManualRecord(
            @RequestParam String cardId,
            @RequestParam Long checkpointId,
            @RequestParam String timestamp) {
        try {
            timingService.createManualRecord(cardId, checkpointId, timestamp);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating manual record: " + e.getMessage());
        }
    }

    @PostMapping("/recalculate/{athleteId}")
    public ResponseEntity<?> recalculateResult(@PathVariable Long athleteId) {
        try {
            timingService.calculateResult(athleteId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error recalculating result: " + e.getMessage());
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            return ResponseEntity.ok(timingService.getLeaderboard(limit));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error getting leaderboard: " + e.getMessage());
        }
    }
}