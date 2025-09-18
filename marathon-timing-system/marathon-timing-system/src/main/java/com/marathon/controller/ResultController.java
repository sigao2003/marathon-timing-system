package com.marathon.controller;

import com.marathon.model.Result;
import com.marathon.service.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/results")
public class ResultController {

    @Autowired
    private ResultService resultService;

    @GetMapping
    public List<Result> getResults(
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String ageGroup) {

        if (gender != null && ageGroup != null) {
            String[] ages = ageGroup.split("-");
            int minAge = Integer.parseInt(ages[0]);
            int maxAge = Integer.parseInt(ages[1]);
            return resultService.getResultsByGenderAndAgeGroup(gender, minAge, maxAge);
        } else if (gender != null) {
            return resultService.getResultsByGender(gender);
        } else if (ageGroup != null) {
            String[] ages = ageGroup.split("-");
            int minAge = Integer.parseInt(ages[0]);
            int maxAge = Integer.parseInt(ages[1]);
            return resultService.getResultsByAgeGroup(minAge, maxAge);
        } else {
            return resultService.getAllResults();
        }
    }

    @GetMapping("/export")
    public void exportResults(
            @RequestParam(required = false) String format,
            HttpServletResponse response) throws IOException {

        if ("csv".equalsIgnoreCase(format)) {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=marathon-results.csv");
            resultService.exportResultsToCsv(response.getWriter());
        } else {
            response.setContentType("application/json");
            response.getWriter().write("JSON export not implemented yet");
        }
    }

    @PostMapping("/{athleteId}/send-sms")
    public ResponseEntity<?> sendResultSms(@PathVariable Long athleteId) {
        try {
            boolean sent = resultService.sendResultSms(athleteId);
            if (sent) {
                return ResponseEntity.ok("SMS sent successfully");
            } else {
                return ResponseEntity.badRequest().body("Failed to send SMS");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error sending SMS: " + e.getMessage());
        }
    }
}