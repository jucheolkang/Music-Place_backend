package org.musicplace.playList.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.musicplace.playList.repository.PLRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchTextBackfillService {

    private final PLRepository plRepository;
    private final MusicService musicService;

    public int backfillAll() {
        List<Long> playlistIds = plRepository.findAllActivePlaylistIds();

        for (Long playlistId : playlistIds) {
            musicService.refreshSearchTextPublic(playlistId); // 아래 3번 참고
        }

        log.info("search_text 백필 완료: {}건", playlistIds.size());
        return playlistIds.size();
    }
}
