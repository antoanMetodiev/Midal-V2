package com.example.songsmicroservice.feather.services;

import com.example.songsmicroservice.feather.models.domain.Song;
import com.example.songsmicroservice.feather.models.domain.YoutubeKey;
import com.example.songsmicroservice.feather.repositories.SongRepository;
import com.example.songsmicroservice.feather.repositories.YoutubeKeyRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SongsService {

    private final YoutubeKeyService youtubeKeyService;

    private final SongRepository songRepository;
    private final YoutubeKeyRepository youtubeKeyRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public SongsService(YoutubeKeyService youtubeKeyService,
                        YoutubeKeyRepository youtubeKeyRepository,
                        SongRepository songRepository,
                        RestTemplate restTemplate) {

        this.youtubeKeyService = youtubeKeyService;
        this.youtubeKeyRepository = youtubeKeyRepository;
        this.songRepository = songRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void createSong(String title) {
        final String YOUTUBE_API_URL = System.getenv("YOUTUBE_API_URL");
        final String YOUTUBE_KEY = youtubeKeyService.findKey();

        final String URL = YOUTUBE_API_URL
                + "?part=snippet"
                + "&type=video"
                + "&q=" + title
                + "&maxResults=50"
                + "&order=relevance"
                + "&key=" + YOUTUBE_KEY;

        String jsonResponse = restTemplate.getForObject(URL, String.class);
        if (jsonResponse == null) {
            System.out.println("===== No response from YouTube API!");
            return;
        }

        JsonObject responseObj = JsonParser.parseString(jsonResponse).getAsJsonObject();
        JsonArray items = responseObj.getAsJsonArray("items");
        if (items.isEmpty()) return;

        // Check 1:
        List<Song> retrievedSongsBySpecialWords = retrieveOfficialSongsBySpecialWords(items);

        // Check 2:
        // This method here return all valid songs:
        List<Song> retrievedValidSongs = retrieveOfficialSongsByInvalidDuration(retrievedSongsBySpecialWords, YOUTUBE_KEY);
        if (retrievedValidSongs.isEmpty()) return; // its mean here don't have songs which finish - Check 2!

        YoutubeKey key = youtubeKeyRepository.findByKey(YOUTUBE_KEY);
        key.setRequestsCount(key.getRequestsCount() - 2);
        youtubeKeyRepository.save(key);

        saveSongs(retrievedValidSongs);
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void saveSongs(List<Song> retrievedValidSongs) {
        for (Song song : retrievedValidSongs) {

            try {
                if (isExists(song.getVideoId())) continue;
                songRepository.save(song);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.printf("=============>>>>>>>>>>>>>>>  Error saving song...!!!!  -  %s%n", song.getTitle());
            }
        }
    }

    private List<Song> retrieveOfficialSongsBySpecialWords(JsonArray items) {
        List<Song> validOfficialSongs = new ArrayList<>();

        String[] excludeWords = {"remix", "live", "cover", "acoustic", "instrumental",
                "karaoke", "edit", "version", "bootleg", "fan made",
                "mashup", "tribute", "lyric", "lyrics"};

        for (JsonElement itemEl : items) {
            JsonObject item = itemEl.getAsJsonObject();
            JsonObject snippet = item.getAsJsonObject("snippet");
            String videoTitleLower = snippet.get("title").getAsString().toLowerCase();

            boolean isContain = false;
            for (String word : excludeWords) {
                if (videoTitleLower.contains(word)) {
                    isContain = true;
                    break;
                }
            }

            // Ако се съдържа и 1 думичка от тях - не го запазвам!
            if (isContain) continue;

            JsonObject thumbnails = snippet.getAsJsonObject("thumbnails");

            // Тъмбнейли!
            JsonObject mediumThumb = thumbnails.getAsJsonObject("medium");
            JsonObject highThumb = thumbnails.getAsJsonObject("high");

            String videoId = item.getAsJsonObject("id").get("videoId").getAsString();

            Song song = Song.builder()
                    .videoId(videoId)
                    .title(snippet.get("title").getAsString())
                    .description(snippet.get("description").getAsString())
                    .publishedAt(snippet.get("publishedAt").getAsString())
                    .channelTitle(snippet.get("channelTitle").getAsString())
                    .mediumThumbnailUrl(mediumThumb.get("url").getAsString())
                    .highThumbnailUrl(highThumb.get("url").getAsString())
                    .build();

            // Добавям записа, само когато е валиден Song!
            validOfficialSongs.add(song);
        }

        return validOfficialSongs;
    }

    private List<Song> retrieveOfficialSongsByInvalidDuration(List<Song> songs, String YOUTUBE_KEY) {
        List<String> videoIds = songs.stream()
                .map(Song::getVideoId).toList();

        String idsParam = String.join(",", videoIds);
        String videosUrl = "https://www.googleapis.com/youtube/v3/videos"
                + "?part=contentDetails"
                + "&id=" + idsParam
                + "&key=" + YOUTUBE_KEY; // <- API key в URL-а

        ResponseEntity<String> response = restTemplate.exchange(
                videosUrl,
                HttpMethod.GET,
                null,
                String.class
        );

        String videosJson = response.getBody();
        JsonArray videosItems = JsonParser.parseString(videosJson)
                .getAsJsonObject()
                .getAsJsonArray("items");

        Set<String> invalidVideoIds = new HashSet<>();
        for (JsonElement videoEl : videosItems) {
            JsonObject video = videoEl.getAsJsonObject();

            String videoId = video.get("id").getAsString();
            String durationStr = video.getAsJsonObject("contentDetails")
                    .get("duration").getAsString();

            int totalSeconds = parseDuration(durationStr);
            int minutes = totalSeconds / 60;

            if (minutes < 2 || minutes > 7) {
                invalidVideoIds.add(videoId);
            }
        }

        // Филтрираме песните само веднъж
        return songs.stream()
                .filter(song -> !invalidVideoIds.contains(song.getVideoId()))
                .toList();
    }

    public List<Song> getRandom50Songs() {
        return songRepository.getRandom50Songs();
    }

    private boolean isExists(String videoId) {
        return songRepository.findByVideoId(videoId).isPresent();
    }

    // Helper: конвертира ISO 8601 PT3M45S към секунди
    private int parseDuration(String isoDuration) {
        // ISO 8601 формат: PT#H#M#S
        Pattern pattern = Pattern.compile("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?");
        Matcher matcher = pattern.matcher(isoDuration);

        if (!matcher.matches()) {
            return 0; // или хвърли IllegalArgumentException
        }

        int hours = matcher.group(1) != null ? Integer.parseInt(matcher.group(1)) : 0;
        int minutes = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
        int seconds = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;

        return hours * 3600 + minutes * 60 + seconds;
    }

    public List<Song> findByTitle(String title) {
        return songRepository.findByTitle(title);
    }

//    public void saveAccessToken(String code) {
//        final String YOUTUBE_CLIENT_ID = System.getenv("YOUTUBE_CLIENT_ID");
//        final String YOUTUBE_CLIENT_SECRET = System.getenv("YOUTUBE_CLIENT_SECRET");
//        String redirectUri = "http://localhost:3000/youtube-auth";
//
//        // Подготвяме POST request към Google token endpoint
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//        body.add("code", code);
//        body.add("client_id", YOUTUBE_CLIENT_ID);
//        body.add("client_secret", YOUTUBE_CLIENT_SECRET);
//        body.add("redirect_uri", redirectUri);
//        body.add("grant_type", "authorization_code");
//
//        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
//
//        ResponseEntity<Map> response = restTemplate.postForEntity(
//                "https://oauth2.googleapis.com/token",
//                request,
//                Map.class
//        );
//
//        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
//            // Връща JSON с access_token, expires_in, refresh_token, token_type
//            String accessToken = (String) response.getBody().get("access_token");
//            String refreshToken = (String) response.getBody().get("refresh_token");
//
//            YoutubeKey token = YoutubeKey.builder()
//                    .key(accessToken)
//                    .build();
//
//            youtubeKeyRepository.save(token);
//
//            // Запази accessToken/refreshToken където искаш (база, memory, config)
//            System.out.println("Access Token: " + accessToken);
//            System.out.println("Refresh Token: " + refreshToken);
//        } else {
//            throw new RuntimeException("Failed to exchange code for access token");
//        }
//    }
}
