package org.musicplace.playList.dto;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.musicplace.playList.domain.OnOff;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PLSaveDto {

    private String title;

    private OnOff onOff;

    private String coverImg;

    private String comment;

    @Builder
    public PLSaveDto(String title, OnOff onOff, String coverImg, String comment) {
        this.title = title;
        this.onOff = onOff;
        this.comment = comment;
        this.coverImg = coverImg;
    }

}
