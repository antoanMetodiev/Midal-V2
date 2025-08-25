package com.example.songsmicroservice.feather.services;

import com.example.songsmicroservice.feather.models.domain.YoutubeKey;
import com.example.songsmicroservice.feather.repositories.YoutubeKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class YoutubeKeyService {

    private final YoutubeKeyRepository youtubeKeyRepository;

    @Autowired
    public YoutubeKeyService(YoutubeKeyRepository youtubeKeyRepository) {
        this.youtubeKeyRepository = youtubeKeyRepository;
    }

    public void saveKey(String youtubeKey) {
        youtubeKeyRepository.save(YoutubeKey.builder()
                .key(youtubeKey).requestsCount(100).build());
    }

    public String findKey() {
        Optional<YoutubeKey> response = youtubeKeyRepository.findKey();
        return response.map(YoutubeKey::getKey).orElse(null);
    }
}
