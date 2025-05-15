package com.limed_backend.security.controller;

import com.limed_backend.security.dto.Sanction.ActiveSanctionResponse;
import com.limed_backend.security.dto.Sanction.InactiveSanctionResponse;
import com.limed_backend.security.service.SanctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sanction")
@RequiredArgsConstructor
public class SanctionController {

    private final SanctionService sanctionService;

    /** Получение активных блокировок по 20 шт*/
    @GetMapping("/active/page={page}")
    public ResponseEntity<List<ActiveSanctionResponse>> getAllActiveSanctions(@PathVariable int page){
        List<ActiveSanctionResponse> activeSanctions= sanctionService.getAllActiveSanctions(page);
        return ResponseEntity.ok(activeSanctions);
    }

    /** Получение истекших либо отозванных блокировок*/
    @GetMapping("/inactive/page={page}")
    public ResponseEntity<List<InactiveSanctionResponse>> getAllInactiveSanctions(@PathVariable int page){
        List<InactiveSanctionResponse> inactiveSanctions= sanctionService.getAllInactiveSanctions(page);
        return ResponseEntity.ok(inactiveSanctions);
    }
}
