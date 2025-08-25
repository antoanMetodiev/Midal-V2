package com.example.songsmicroservice.feather.repositories;

import com.example.songsmicroservice.feather.models.domain.YoutubeKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface YoutubeKeyRepository extends JpaRepository<YoutubeKey, UUID> {

    @Query(value = "SELECT * FROM youtube_keys WHERE requests_count > 1 LIMIT 1;", nativeQuery = true)
    Optional<YoutubeKey> findKey();

    @Query(value = "SELECT * FROM youtube_keys WHERE key = :key", nativeQuery = true)
    YoutubeKey findByKey(@Param("key") String key);
}
