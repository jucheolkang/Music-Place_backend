package org.musicplace.playList.dto;

public interface PlaylistSearchProjection {
    Long getPlaylistId();
    String getTitle();
    String getNickname();
    String getCoverImg();
    String getOnoff();
    String getComment();
    String getMemberId();
    Double getRelevanceScore();
}
