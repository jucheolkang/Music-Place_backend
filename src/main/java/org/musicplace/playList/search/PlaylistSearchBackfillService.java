package org.musicplace.playList.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.musicplace.playList.domain.PLEntity;
import org.musicplace.playList.repository.PLRepository;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistSearchBackfillService {

    private final PLRepository plRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public int backfillAll() {
        List<PLEntity> playlists = plRepository.findAllActivePublicPlaylists();

        List<PlaylistDocument> documents = playlists.stream()
                .map(PlaylistDocument::from)
                .toList();

        elasticsearchOperations.save(documents);

        log.info("검색 인덱스 백필 완료: {}건", documents.size());
        return documents.size();
    }
}
