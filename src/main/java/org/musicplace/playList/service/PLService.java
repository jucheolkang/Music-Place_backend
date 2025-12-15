package org.musicplace.playList.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.musicplace.global.security.authorizaion.MemberAuthorizationUtil;
import org.musicplace.global.exception.ErrorCode;
import org.musicplace.global.exception.ExceptionHandler;
import org.musicplace.user.domain.UserEntity;
import org.musicplace.user.service.SignInService;
import org.musicplace.playList.domain.OnOff;
import org.musicplace.playList.domain.PLEntity;
import org.musicplace.playList.dto.PLSaveDto;
import org.musicplace.playList.dto.PLUpdateDto;
import org.musicplace.playList.dto.ResponsePLDto;
import org.musicplace.playList.repository.PLRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PLService {

    private final PLRepository plRepository;
    private final SignInService signInService;

    @Transactional
    public Long PLsave(PLSaveDto plSaveDto) {
        String member_id = MemberAuthorizationUtil.getLoginMemberId();
        UserEntity userEntity = signInService.SignInFindById(member_id);
        signInService.CheckSignInDelete(userEntity);
        PLEntity plEntity = plRepository.save(PLEntity.builder()
                .title(plSaveDto.getTitle())
                .onOff(plSaveDto.getOnOff())
                .comment(plSaveDto.getComment())
                .cover_img(plSaveDto.getCover_img())
                .nickname(userEntity.getNickname())
                .build());
        userEntity.getPlaylistEntities().add(plEntity);
        plEntity.SignInEntity(userEntity);
        plRepository.save(plEntity);
        return plEntity.getPlaylist_id();
    }

    @Transactional
    public void PLUpdate(Long id, PLUpdateDto plUpdateDto) {
        PLEntity plEntity = PLFindById(id);
        CheckPLDeleteStatus(plEntity);
        plEntity.PLUpdate(
                plUpdateDto.getTitle(),
                plUpdateDto.getOnOff(),
                plUpdateDto.getCover_img(),
                plUpdateDto.getComment());
    }

    @Transactional
    public void PLDelete(Long id) {
        PLEntity plEntity = PLFindById(id);
        CheckPLDeleteStatus(plEntity);
        plEntity.delete();
    }

    public Long PLCount() {
        String member_id = MemberAuthorizationUtil.getLoginMemberId();
        UserEntity userEntity = signInService.SignInFindById(member_id);
        return userEntity.getPlaylistEntities().stream().count();
    }

    public Long otherPLCount(String otherMemberId) {
        UserEntity userEntity = signInService.SignInFindById(otherMemberId);
        return userEntity.getPlaylistEntities().stream()
                .filter(plEntity -> plEntity.getOnOff().equals(OnOff.Public))
                .count();
    }

    public List<ResponsePLDto> PLFindAll() {
        String member_id = MemberAuthorizationUtil.getLoginMemberId();
        UserEntity userEntity = signInService.SignInFindById(member_id);
        List<ResponsePLDto> nonDeletedPlayLists = userEntity.getPlaylistEntities()
                .stream()
                .filter(plEntity -> !plEntity.isPLDelete())
                .map(plEntity -> ResponsePLDto.builder()
                        .playlist_id(plEntity.getPlaylist_id())
                        .nickname(plEntity.getNickname())
                        .PLTitle(plEntity.getPLTitle())
                        .cover_img(plEntity.getCover_img())
                        .onOff(plEntity.getOnOff())
                        .comment(plEntity.getComment())
                        .build())
                .collect(Collectors.toList());
        return nonDeletedPlayLists;
    }

    public List<ResponsePLDto> getOtherUserPL(String memberId) {
        UserEntity userEntity = signInService.SignInFindById(memberId);
        List<ResponsePLDto> publicPlaylist = userEntity.getPlaylistEntities()
                .stream()
                .filter(plEntity -> plEntity.getOnOff().equals(OnOff.Public))
                .map(plEntity -> ResponsePLDto.builder()
                        .playlist_id(plEntity.getPlaylist_id())
                        .nickname(plEntity.getNickname())
                        .PLTitle(plEntity.getPLTitle())
                        .cover_img(plEntity.getCover_img())
                        .onOff(plEntity.getOnOff())
                        .comment(plEntity.getComment())
                        .build())
                .collect(Collectors.toList());
        return publicPlaylist;
    }


    public List<ResponsePLDto> PLFindPublic() {
        List<PLEntity> playListAll = plRepository.findAll();
        List<ResponsePLDto> publicPlayLists = playListAll.stream()
                .filter(plEntity -> plEntity.getOnOff().equals(OnOff.Public) && !plEntity.isPLDelete())
                .map(plEntity -> ResponsePLDto.builder()
                        .playlist_id(plEntity.getPlaylist_id())
                        .member_id(plEntity.getUserEntity().getMemberId())
                        .PLTitle(plEntity.getPLTitle())
                        .nickname(plEntity.getNickname())
                        .cover_img(plEntity.getCover_img())
                        .comment(plEntity.getComment())
                        .build())
                .collect(Collectors.toList());

        return publicPlayLists;
    }

    public PLEntity PLFindById(Long id) {
        PLEntity plEntity = plRepository.findById(id)
                .orElseThrow(() -> new ExceptionHandler(ErrorCode.ID_NOT_FOUND));
        return plEntity;
    }

    public void CheckPLDeleteStatus(PLEntity plEntity) {
        if (plEntity.isPLDelete()) {
            throw new ExceptionHandler(ErrorCode.ID_DELETE);
        }
    }


}
