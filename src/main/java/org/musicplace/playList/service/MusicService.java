package org.musicplace.playList.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.musicplace.global.exception.ErrorCode;
import org.musicplace.global.exception.ExceptionHandler;
import org.musicplace.playList.domain.MusicEntity;
import org.musicplace.playList.dto.MusicSaveDto;
import org.musicplace.playList.dto.ResponseMusicDto;
import org.musicplace.playList.repository.MusicRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MusicService {

    private final MusicRepository musicRepository;
    private final PLService plService;

    @Transactional
    public Long musicSave(Long playlistId, MusicSaveDto dto) {
        plService.validatePlaylistActive(playlistId);

        MusicEntity music = MusicEntity.builder()
                .playlistId(playlistId)
                .videoTitle(dto.getVidioTitle())
                .videoId(dto.getVidioId())
                .videoImage(dto.getVidioImage())
                .build();

        musicRepository.save(music);
        return music.getMusicId();
    }

    @Transactional
    public boolean musicDelete(Long playlistId, List<Long> musicIds) {
        plService.validatePlaylistActive(playlistId);

        List<MusicEntity> musics =
                musicRepository.findAllByIdInAndPlaylistId(musicIds, playlistId);

        if (musics.size() != musicIds.size()) {
            throw new ExceptionHandler(ErrorCode.ID_NOT_FOUND);
        }

        for (MusicEntity music : musics) {
            if (music.isMusicDelete()) {
                throw new ExceptionHandler(ErrorCode.ID_DELETE);
            }
            music.delete();
        }
        return true;
    }

    public List<ResponseMusicDto> musicFindAll(Long playlistId) {
        plService.validatePlaylistActive(playlistId);

        return musicRepository
                .findByPlaylistIdAndMusicDeleteFalse(playlistId)
                .stream()
                .map(m -> ResponseMusicDto.builder()
                        .music_id(m.getMusicId())
                        .vidioId(m.getVideoId())
                        .vidioImage(m.getVideoImage())
                        .vidioTitle(m.getVideoTitle())
                        .build())
                .toList();
    }
}

