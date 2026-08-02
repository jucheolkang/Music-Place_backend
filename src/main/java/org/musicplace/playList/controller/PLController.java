package org.musicplace.playList.controller;

import lombok.RequiredArgsConstructor;
import org.musicplace.global.security.CurrentUserProvider;
import org.musicplace.playList.dto.ResponsePLDto;
import org.musicplace.playList.service.PLService;
import org.musicplace.playList.dto.PLSaveDto;
import org.musicplace.playList.dto.PLUpdateDto;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/playList")
@RequiredArgsConstructor
public class PLController {

    private final PLService plService;

    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public Long save(@RequestBody PLSaveDto dto) {

        return plService.plSave(
                currentUserProvider.getMemberId(),
                dto
        );
    }

    @PatchMapping("/{pl_id}")
    public void plupdate(@PathVariable Long pl_id,
                         @RequestBody PLUpdateDto plUpdateDto) {
        plService.plUpdate(pl_id, plUpdateDto);
    }

    @DeleteMapping("/{pl_id}")
    public void pldelete(@PathVariable Long pl_id) {
        plService.plDelete(pl_id);
    }

    @GetMapping
    public List<ResponsePLDto> plfindall() {

        return plService.findMyPlaylists(
                currentUserProvider.getMemberId()
        );
    }

    @GetMapping("/public")
    public List<ResponsePLDto> PLFindPublic() {
        return plService.findPublicPlaylists();
    }

    @GetMapping("/count")
    public Long count() {

        return plService.countMyPlaylists(
                currentUserProvider.getMemberId()
        );
    }

    @GetMapping("/otherCount/{otherMemberId}")
    public Long otherPLCount(@PathVariable String otherMemberId) {
        return plService.countOtherPublicPlaylists(otherMemberId);
    }

    @GetMapping("/other/{otherMemberId}")
    public List<ResponsePLDto> getOtherUserPL(@PathVariable String otherMemberId) {
        return plService.getOtherUserPublicPlaylists(otherMemberId);
    }

}
