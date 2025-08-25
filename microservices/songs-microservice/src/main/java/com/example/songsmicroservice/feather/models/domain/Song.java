package com.example.songsmicroservice.feather.models.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "songs")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Column(name = "video_id", nullable = false, unique = true)
    private String videoId;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(nullable = false)
    private String channelTitle;

    @Column(nullable = false)
    private String publishedAt;

    // Can be null!
    @Column(name = "medium_thumbnail_url")
    private String mediumThumbnailUrl;

    @Column(name = "high_thumbnail_url")
    private String highThumbnailUrl;
}
