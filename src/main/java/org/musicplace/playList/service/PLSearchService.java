package org.musicplace.playList.service;

import lombok.RequiredArgsConstructor;
import org.musicplace.playList.domain.SortType;
import org.musicplace.playList.dto.PlaylistSearchProjection;
import org.musicplace.playList.dto.ResponseSearchDto;
import org.musicplace.playList.repository.PLRepository;
import org.musicplace.playList.util.SearchHighlightUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PLSearchService {

    private final PLRepository plRepository;

    public Page<ResponseSearchDto> search(String keyword, SortType sortType, Pageable pageable) {
        Page<PlaylistSearchProjection> results = switch (sortType) {
            case LATEST -> plRepository.searchByLatest(keyword, pageable);
            case RELEVANCE -> plRepository.searchByRelevance(keyword, pageable);
        };

        return results.map(r -> ResponseSearchDto.builder()
                .playlistId(r.getPlaylistId())
                .title(SearchHighlightUtil.highlight(r.getTitle(), keyword))
                .nickname(r.getNickname())
                .coverImg(r.getCoverImg())
                .onOff(r.getOnoff())
                .comment(SearchHighlightUtil.highlight(r.getComment(), keyword))
                .memberId(r.getMemberId())
                .build());
    }
}
