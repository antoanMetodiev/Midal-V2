package com.example.songsmicroservice.feather.controllers;

import com.example.songsmicroservice.feather.services.YoutubeKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class YoutubeKeyController {

    private final YoutubeKeyService youtubeKeyService;

    @Autowired
    public YoutubeKeyController(YoutubeKeyService youtubeKeyService) {
        this.youtubeKeyService = youtubeKeyService;
    }

    @PostMapping("/save-key/{youtubeKey}")
    public ResponseEntity<Void> saveKey(@PathVariable(name = "youtubeKey") String youtubeKey) {
        youtubeKeyService.saveKey(youtubeKey);
        return ResponseEntity.accepted().build();
    }
}
