package org.musicplace.playList.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.musicplace.playList.domain.OnOff;

@Getter
@NoArgsConstructor
public class ResponsePLDto {

    private Long playlistId;

    private String nickname;

    private String plTitle;

    private String coverImg;

    private OnOff onOff;

    private String comment;

    private String memberId;

    public ResponsePLDto(
            Long playlistId,
            String plTitle,
            String nickname,
            String coverImg,
            OnOff onOff,
            String comment,
            String memberId
    ) {
        this.playlistId = playlistId;
        this.plTitle = plTitle;
        this.nickname = nickname;
        this.coverImg = coverImg;
        this.onOff = onOff;
        this.comment = comment;
        this.memberId = memberId;
    }
}
