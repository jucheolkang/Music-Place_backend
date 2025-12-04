package org.musicplace.Youtube.controller;

import org.musicplace.Youtube.dto.YoutubeVidioDto;
import org.musicplace.Youtube.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/youtube")
@Profile("!loadtest")
public class YoutubeController {

    @Autowired
    private YoutubeService youtubeService;

    @GetMapping("/{keyword}")
    public ResponseEntity<List<YoutubeVidioDto>> searchVideo(@PathVariable String keyword) throws IOException {
        List<YoutubeVidioDto> result = youtubeService.searchVideo(keyword);
        return ResponseEntity.ok(result);

    }

    @GetMapping("/playlist")
    public ResponseEntity<List<YoutubeVidioDto>> getPlaylistVideos() throws IOException {
        List<YoutubeVidioDto> result = youtubeService.getPlaylistVideos();
        return ResponseEntity.ok(result);
    }
}
