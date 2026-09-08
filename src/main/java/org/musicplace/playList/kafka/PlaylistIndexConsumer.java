package org.musicplace.playList.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.musicplace.playList.domain.PLEntity;
import org.musicplace.playList.domain.OnOff;
import org.musicplace.playList.kafka.event.PlaylistIndexEvent;
import org.musicplace.playList.repository.PLRepository;
import org.musicplace.playList.search.PlaylistDocument;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlaylistIndexConsumer {

    private final PLRepository plRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @KafkaListener(
            topics = "playlist-index-events",
            groupId = "search-index-consumer",
            containerFactory = "searchIndexKafkaListenerContainerFactory"
    )
    public void consume(List<ConsumerRecord<String, PlaylistIndexEvent>> records, Acknowledgment ack) {

        Set<Long> playlistIds = records.stream()
                .map(r -> r.value().playlistId())
                .collect(Collectors.toSet());

        for (Long playlistId : playlistIds) {
            reindex(playlistId);
        }

        ack.acknowledge();
    }

    private void reindex(Long playlistId) {
        PLEntity pl = plRepository.findById(playlistId).orElse(null);

        // 삭제됐거나 비공개로 전환된 재생목록은 인덱스에서 제거
        if (pl == null || pl.isDeleteState() || pl.getOnOff() != OnOff.Public) {
            elasticsearchOperations.delete(String.valueOf(playlistId), PlaylistDocument.class);
            return;
        }

        PlaylistDocument document = PlaylistDocument.from(pl);
        elasticsearchOperations.save(document);
    }
}
