package org.musicplace.playList.search;

import co.elastic.clients.elasticsearch._types.SortOrder;
import lombok.RequiredArgsConstructor;
import org.musicplace.playList.domain.SortType;
import org.musicplace.playList.dto.ResponseSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PLElasticSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public Page<ResponseSearchDto> search(String keyword, SortType sortType, Pageable pageable) {

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .multiMatch(m -> m
                                .query(keyword)
                                .fields("title^3", "comment^2", "searchText^1")
                                .fuzziness("AUTO")
                        )
                )
                .withSort(s -> sortType == SortType.LATEST
                        ? s.field(f -> f.field("registerDate").order(SortOrder.Desc))
                        : s.score(sc -> sc.order(SortOrder.Desc))
                )
                .withPageable(pageable)
                .build();

        SearchHits<PlaylistDocument> hits = elasticsearchOperations.search(query, PlaylistDocument.class);

        List<ResponseSearchDto> content = hits.getSearchHits().stream()
                .map(this::toDto)
                .toList();

        return new PageImpl<>(content, pageable, hits.getTotalHits());
    }

    private ResponseSearchDto toDto(SearchHit<PlaylistDocument> hit) {
        PlaylistDocument doc = hit.getContent();
        return ResponseSearchDto.builder()
                .playlistId(Long.valueOf(doc.getPlaylistId()))
                .title(doc.getTitle())
                .nickname(doc.getNickname())
                .comment(doc.getComment())
                .memberId(doc.getMemberId())
                .build();
    }
}
