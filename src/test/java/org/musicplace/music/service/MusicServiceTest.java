package org.musicplace.music.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.musicplace.global.exception.BusinessException;
import org.musicplace.global.exception.ErrorCode;
import org.musicplace.playList.domain.MusicEntity;
import org.musicplace.playList.dto.MusicSaveDto;
import org.musicplace.playList.dto.ResponseMusicDto;
import org.musicplace.playList.repository.MusicRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.musicplace.playList.service.MusicService;
import org.musicplace.playList.service.PLService;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MusicServiceTest {

    @Mock
    private MusicRepository musicRepository;

    @Mock
    private PLService plService;

    @InjectMocks
    private MusicService musicService;

    private MusicEntity music;

    @BeforeEach
    void setUp() {

        music = MusicEntity.builder()
                .playlistId(1L)
                .videoId("videoId")
                .videoTitle("title")
                .videoImage("image")
                .build();
    }

    @Test
    @DisplayName("음악 저장 성공")
    void musicSave_success() {

        MusicSaveDto dto = MusicSaveDto.builder()
                .vidioId("videoId")
                .vidioTitle("title")
                .vidioImage("image")
                .build();

        when(musicRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        musicService.musicSave(1L, dto);

        verify(plService).validatePlaylistActive(1L);
        verify(musicRepository).save(any(MusicEntity.class));
    }

    @Test
    @DisplayName("삭제 성공")
    void delete_success() {

        when(musicRepository.findAllByMusicIdInAndPlaylistId(
                List.of(1L),1L))
                .thenReturn(List.of(music));

        boolean result = musicService.musicDelete(1L,List.of(1L));

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 musicId")
    void delete_fail_notFound() {

        when(musicRepository.findAllByMusicIdInAndPlaylistId(
                List.of(1L),1L))
                .thenReturn(List.of());

        assertThatThrownBy(() ->
                musicService.musicDelete(1L,List.of(1L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ID_NOT_FOUND);
    }

    @Test
    @DisplayName("이미 삭제된 음악")
    void delete_fail_deleted() {

        music.delete();

        when(musicRepository.findAllByMusicIdInAndPlaylistId(
                List.of(1L),1L))
                .thenReturn(List.of(music));

        assertThatThrownBy(() ->
                musicService.musicDelete(1L,List.of(1L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MUSIC_NOT_FOUND);
    }

    @Test
    @DisplayName("음악 조회")
    void findAll_success() {

        when(musicRepository.findByPlaylistIdAndMusicDeleteFalse(1L))
                .thenReturn(List.of(music));

        List<ResponseMusicDto> result =
                musicService.musicFindAll(1L);

        assertThat(result).hasSize(1);

        assertThat(result.get(0).getVidioId())
                .isEqualTo("videoId");
    }

    @Test
    @DisplayName("조회 결과 없음")
    void findAll_empty() {

        when(musicRepository.findByPlaylistIdAndMusicDeleteFalse(1L))
                .thenReturn(List.of());

        List<ResponseMusicDto> result =
                musicService.musicFindAll(1L);

        assertThat(result).isEmpty();
    }
}
