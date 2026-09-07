package org.musicplace.playList.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseSearchDto {
    private Long playlistId;
    private String title;      // 하이라이트 적용된 상태
    private String nickname;
    private String coverImg;
    private String onOff;
    private String comment;    // 하이라이트 적용된 상태
    private String memberId;
}
