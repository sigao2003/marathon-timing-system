package com.marathon.controller;

import com.marathon.model.Athlete;
import com.marathon.service.AthleteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/athletes")
public class AthleteController {

    @Autowired
    private AthleteService athleteService;

    @PostMapping
    public ResponseEntity<?> registerAthlete(@RequestBody Athlete athlete) {
        try {
            Athlete savedAthlete = athleteService.registerAthlete(athlete);
            return ResponseEntity.ok(savedAthlete);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error registering athlete: " + e.getMessage());
        }
    }

    @GetMapping
    public List<Athlete> getAllAthletes() {
        return athleteService.getAllAthletes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Athlete> getAthleteById(@PathVariable Long id) {
        Optional<Athlete> athlete = athleteService.getAthleteById(id);
        return athlete.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAthlete(@PathVariable Long id) {
        try {
            athleteService.deleteAthlete(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting athlete: " + e.getMessage());
        }
    }
}