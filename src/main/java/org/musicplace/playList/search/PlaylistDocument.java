package org.musicplace.playList.search;

import lombok.Getter;
import org.musicplace.playList.domain.PLEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Document(indexName = "playlist")
public class PlaylistDocument {

    @Id
    private String playlistId;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String title;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String comment;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String searchText;

    @Field(type = FieldType.Keyword)
    private String nickname;

    @Field(type = FieldType.Keyword)
    private String memberId;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime registerDate;

    protected PlaylistDocument() {}

    public static PlaylistDocument from(PLEntity pl) {
        PlaylistDocument doc = new PlaylistDocument();
        doc.playlistId = String.valueOf(pl.getPlaylistId());
        doc.title = pl.getTitle();
        doc.comment = pl.getComment();
        doc.searchText = pl.getSearchText();
        doc.nickname = pl.getNickname();
        doc.memberId = pl.getMemberId();
        doc.registerDate = pl.getRegisterDate();
        return doc;
    }
}
