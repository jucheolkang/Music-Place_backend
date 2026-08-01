package org.musicplace.common.fixture;

import org.musicplace.playList.domain.OnOff;
import org.musicplace.playList.domain.PLEntity;
import org.musicplace.playList.dto.PLSaveDto;
import org.musicplace.playList.dto.PLUpdateDto;

public final class PlaylistFixture {

    private PlaylistFixture() {
    }

    public static PLEntity createPlaylist() {
        return PLEntity.builder()
                .memberId("test-user")
                .title("playlist")
                .nickname("tester")
                .coverImg("cover.png")
                .onOff(OnOff.Public)
                .comment("comment")
                .build();
    }

    public static PLSaveDto createSaveDto() {
        return PLSaveDto.builder()
                .title("playlist")
                .onOff(OnOff.Public)
                .coverImg("cover.png")
                .comment("comment")
                .build();
    }

    public static PLUpdateDto createUpdateDto() {
        return PLUpdateDto.builder()
                .title("updated")
                .onOff(OnOff.Private)
                .coverImg("new.png")
                .comment("updated comment")
                .build();
    }
}
