package org.musicplace.playList.controller;

import lombok.RequiredArgsConstructor;
import org.musicplace.playList.dto.ResponsePLDto;
import org.musicplace.playList.service.PLService;
import org.musicplace.playList.dto.PLSaveDto;
import org.musicplace.playList.dto.PLUpdateDto;
import org.springframework.security.core.Authentication;
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

    @PostMapping
    public Long plsave(Authentication authentication,
                       @RequestBody PLSaveDto plSaveDto) {

        return plService.plSave(authentication.getName(), plSaveDto);
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
    public List<ResponsePLDto> plfindall(Authentication authentication) {
        return plService.findMyPlaylists(authentication.getName());
    }

    @GetMapping("/public")
    public List<ResponsePLDto> PLFindPublic() {
        return plService.findPublicPlaylists();
    }

    @GetMapping("/count")
    public Long PLCount(Authentication authentication) {
        return plService.countMyPlaylists(authentication.getName());
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
