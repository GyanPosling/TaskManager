package com.bsuir.taskmanager.controller;

import com.bsuir.taskmanager.controller.api.DemoControllerApi;
import com.bsuir.taskmanager.model.dto.response.RaceConditionDemoResponse;
import com.bsuir.taskmanager.service.racecondition.RaceConditionDemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/demo")
public class DemoController implements DemoControllerApi {
    private final RaceConditionDemoService raceConditionDemoService;

    @PostMapping("/race-condition")
    public ResponseEntity<RaceConditionDemoResponse> runRaceConditionDemo()
            throws InterruptedException {
        return ResponseEntity.ok(raceConditionDemoService.runAllDemos());
    }
}
