package com.example.songsmicroservice.feather.repositories;

import com.example.songsmicroservice.feather.models.domain.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SongRepository extends JpaRepository<Song, UUID> {

    Optional<Song> findByVideoId(String videoId);

    @Query(value = "SELECT * FROM songs WHERE version >= 0 LIMIT 50;", nativeQuery = true)
    List<Song> getRandom50Songs();

    @Query(value = "SELECT * FROM songs WHERE LOWER(title) LIKE LOWER(CONCAT('%', :title, '%')) LIMIT 50;", nativeQuery = true)
    List<Song> findByTitle(@Param("title") String title);
}
