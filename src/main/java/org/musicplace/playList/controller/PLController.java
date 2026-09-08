package org.musicplace.playList.controller;

import lombok.RequiredArgsConstructor;
import org.musicplace.playList.domain.SortType;
import org.musicplace.playList.dto.ResponsePLDto;
import org.musicplace.playList.dto.ResponseSearchDto;
import org.musicplace.playList.search.PLElasticSearchService;
import org.musicplace.playList.service.PLSearchService;
import org.musicplace.playList.service.PLService;
import org.musicplace.playList.dto.PLSaveDto;
import org.musicplace.playList.dto.PLUpdateDto;
import org.musicplace.user.domain.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/playList")
@RequiredArgsConstructor
public class PLController {

    private final PLService plService;
    private final PLSearchService plSearchService;
    private final PLElasticSearchService plElasticSearchService;

    @PostMapping
    public Long save(
            @AuthenticationPrincipal UserEntity loginUser,
            @RequestBody PLSaveDto dto
    ){
        return plService.plSave(
                loginUser.getMemberId(),
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
    public List<ResponsePLDto> plfindall(
            @AuthenticationPrincipal UserEntity loginUser
    ) {
        return plService.findMyPlaylists(loginUser.getMemberId());
    }

    @GetMapping("/public")
    public ResponseEntity<Page<ResponsePLDto>> publicPlaylists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ResponseEntity.ok(plService.findPublicPlaylists(pageable));
    }

    @GetMapping("/count")
    public Long count(
            @AuthenticationPrincipal UserEntity loginUser
    ){
        return plService.countMyPlaylists(loginUser.getMemberId());
    }

    @GetMapping("/otherCount/{otherMemberId}")
    public Long otherPLCount(@PathVariable String otherMemberId) {
        return plService.countOtherPublicPlaylists(otherMemberId);
    }

    @GetMapping("/other/{otherMemberId}")
    public List<ResponsePLDto> getOtherUserPL(@PathVariable String otherMemberId) {
        return plService.getOtherUserPublicPlaylists(otherMemberId);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ResponseSearchDto>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "RELEVANCE") SortType sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ResponseEntity.ok(plSearchService.search(keyword, sort, pageable));
    }

    @GetMapping("/search/es")
    public ResponseEntity<Page<ResponseSearchDto>> searchEs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "RELEVANCE") SortType sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ResponseEntity.ok(plElasticSearchService.search(keyword, sort, pageable));
    }

}
