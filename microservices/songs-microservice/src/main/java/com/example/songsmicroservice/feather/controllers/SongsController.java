package com.example.songsmicroservice.feather.controllers;

import com.example.songsmicroservice.feather.models.domain.Song;
import com.example.songsmicroservice.feather.services.SongsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/songs")
public class SongsController {

    private final SongsService songsService;

    @Autowired
    public SongsController(SongsService songsService) {
        this.songsService = songsService;
    }

    @GetMapping("/findByTitle/{title}")
    public ResponseEntity<List<Song>> findByTitle(@PathVariable(name = "title") String title) {
        return ResponseEntity.ok().body(songsService.findByTitle(title));
    }

    @GetMapping("/get-random-50-songs")
    public ResponseEntity<List<Song>> getRandom50Songs() {
        return ResponseEntity.ok().body(songsService.getRandom50Songs());
    }

    @PostMapping("/create/{title}")
    public ResponseEntity<Void> createSong(@PathVariable(name = "title") String title) {
        songsService.createSong(title);
        return ResponseEntity.ok().build();
    }
}
