package org.musicplace.playList.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.musicplace.global.exception.ErrorCode;
import org.musicplace.global.exception.BusinessException;
import org.musicplace.playList.domain.PLEntity;
import org.musicplace.playList.dto.PLSaveDto;
import org.musicplace.playList.dto.PLUpdateDto;
import org.musicplace.playList.dto.ResponsePLDto;
import org.musicplace.playList.repository.PLRepository;
import org.musicplace.user.domain.UserEntity;
import org.musicplace.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PLService {

    private final PLRepository plRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long plSave(String memberId, PLSaveDto dto) {

        UserEntity user = userRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ID_NOT_FOUND));

        if (user.getDeleteAccount()) {
            throw new BusinessException(ErrorCode.MEMBER_DELETED);
        }

        PLEntity playlist = PLEntity.builder()
                .memberId(memberId)
                .title(dto.getTitle())
                .onOff(dto.getOnOff())
                .comment(dto.getComment())
                .coverImg(dto.getCoverImg())
                .nickname(user.getNickname())
                .build();

        plRepository.save(playlist);
        return playlist.getPlaylistId();
    }


    @Transactional
    public void plUpdate(Long playlistId, PLUpdateDto dto) {
        PLEntity pl = findActivePlaylist(playlistId);
        pl.plUpdate(
                dto.getTitle(),
                dto.getOnOff(),
                dto.getCoverImg(),
                dto.getComment()
        );
    }

    @Transactional
    public void plDelete(Long playlistId) {
        PLEntity pl = findActivePlaylist(playlistId);
        pl.delete();
    }


    public List<ResponsePLDto> findMyPlaylists(String memberId) {
        return plRepository.findMyPlaylists(memberId);
    }

    public Long countMyPlaylists(String memberId) {
        return plRepository.countMyPlaylists(memberId);
    }

    public Long countOtherPublicPlaylists(String otherMemberId) {
        return plRepository.countOtherPublicPlaylists(otherMemberId);
    }

    public List<ResponsePLDto> getOtherUserPublicPlaylists(String otherMemberId) {
        return plRepository.findOtherUserPublicPlaylists(otherMemberId);
    }

    public List<ResponsePLDto> findPublicPlaylists() {
        return plRepository.findAllPublicPlaylists();
    }


    public void validatePlaylistActive(Long playlistId) {
        boolean exists =
                plRepository.existsByPlaylistIdAndDeleteStateFalse(playlistId);

        if (!exists) {
            throw new BusinessException(ErrorCode.MEMBER_DELETED);
        }
    }

    private PLEntity findActivePlaylist(Long playlistId) {
        PLEntity pl = plRepository.findById(playlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ID_NOT_FOUND));

        if (pl.isDeleteState()) {
            throw new BusinessException(ErrorCode.MEMBER_DELETED);
        }
        return pl;
    }
}
