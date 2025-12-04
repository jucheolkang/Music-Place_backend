package org.musicplace.Youtube.controller;

import org.musicplace.Youtube.dto.YoutubeVidioDto;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * YouTube API Mock Controller
 *
 * 용도: 부하 테스트 시 실제 YouTube API 호출을 방지하고 Mock 데이터 반환
 * 활성화: application-loadtest.yml 또는 --spring.profiles.active=loadtest
 *
 * 실제 YouTube API는 할당량 제한(10,000 units/day)이 있어 부하 테스트 불가능
 * 이 Controller는 loadtest 프로파일에서만 활성화되어 실제 API를 대체합니다.
 */
@RestController
@RequestMapping("/youtube")
@Profile("loadtest")  // loadtest 프로파일에서만 활성화
public class YoutubeMockController {

    private final Random random = new Random();

    /**
     * 키워드 검색 Mock
     * GET /youtube/{keyword}
     */
    @GetMapping("/{keyword}")
    public List<YoutubeVidioDto> searchVideoMock(@PathVariable String keyword) {
        // 응답 시간 시뮬레이션 (50-200ms, 실제 YouTube API와 유사)
        simulateDelay(50, 200);

        List<YoutubeVidioDto> mockResults = new ArrayList<>();
        int resultCount = random.nextInt(6) + 5; // 5-10개 결과

        for (int i = 0; i < resultCount; i++) {
            String id = generateRandomVideoId();

            YoutubeVidioDto video = YoutubeVidioDto.builder()
                    .vidioId(id)
                    .vidioTitle(generateMockTitle(keyword, i))
                    .vidioImage(generateMockThumbnail(id))
                    .build();

            mockResults.add(video);
        }

        return mockResults;
    }

    /**
     * 플레이리스트 조회 Mock
     * GET /youtube/playlist
     */
    @GetMapping("/playlist")
    public List<YoutubeVidioDto> getPlaylistVideosMock() {
        simulateDelay(100, 300);

        List<YoutubeVidioDto> mockPlaylist = new ArrayList<>();
        int videoCount = random.nextInt(11) + 10; // 10-20개 비디오

        for (int i = 0; i < videoCount; i++) {
            String id = generateRandomVideoId();

            YoutubeVidioDto video = YoutubeVidioDto.builder()
                    .vidioId(id)
                    .vidioTitle(generateRandomPlaylistTitle(i))
                    .vidioImage(generateMockThumbnail(id))
                    .build();

            mockPlaylist.add(video);
        }

        return mockPlaylist;
    }

    // ===== Private Helper Methods =====

    /**
     * 랜덤 YouTube Video ID 생성 (11자리)
     */
    private String generateRandomVideoId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-";
        StringBuilder videoId = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            videoId.append(chars.charAt(random.nextInt(chars.length())));
        }
        return videoId.toString();
    }

    /**
     * Mock 비디오 제목 생성
     */
    private String generateMockTitle(String keyword, int index) {
        String[] templates = {
                "%s - Official Music Video",
                "Best of %s 2024",
                "%s Compilation Mix",
                "Top %s Songs",
                "%s Playlist for Study/Work",
                "%s Hits Collection",
                "Amazing %s Performance",
                "%s Live Concert"
        };
        String template = templates[random.nextInt(templates.length)];
        return String.format(template, keyword) + " #" + (index + 1);
    }

    /**
     * Mock 플레이리스트 제목 생성
     */
    private String generateRandomPlaylistTitle(int index) {
        String[] genres = {"Pop", "Rock", "Jazz", "Classical", "Hip Hop", "EDM", "Country", "R&B"};
        String[] types = {"Hits", "Classics", "Mix", "Playlist", "Collection", "Best Of"};

        String genre = genres[random.nextInt(genres.length)];
        String type = types[random.nextInt(types.length)];

        return genre + " " + type + " 2024 #" + (index + 1);
    }

    /**
     * Mock 썸네일 URL 생성
     */
    private String generateMockThumbnail(String videoId) {
        // YouTube 썸네일 URL 형식 (실제와 동일하지만 Mock ID 사용)
        return "https://i.ytimg.com/vi/" + videoId + "/default.jpg";
    }

    /**
     * API 응답 시간 시뮬레이션
     */
    private void simulateDelay(int minMs, int maxMs) {
        try {
            int delay = random.nextInt(maxMs - minMs + 1) + minMs;
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
