package org.musicplace.follow.controller;

import lombok.RequiredArgsConstructor;
import org.musicplace.follow.dto.FollowResponseDto;
import org.musicplace.follow.dto.FollowSaveDto;
import org.musicplace.follow.service.FollowService;
import org.musicplace.user.domain.UserEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping
    public Long save(
            @AuthenticationPrincipal UserEntity loginUser,
            @RequestBody FollowSaveDto followSaveDto
    ) {
        return followService.followSave(
                loginUser.getMemberId(),
                followSaveDto
        );
    }

    @DeleteMapping("/{follow_id}")
    public void delete(
            @AuthenticationPrincipal UserEntity loginUser,
            @PathVariable Long follow_id
    ) {
        followService.followDelete(
                loginUser.getMemberId(),
                follow_id
        );
    }

    @GetMapping
    public List<FollowResponseDto> findAll(
            @AuthenticationPrincipal UserEntity loginUser
    ) {
        return followService.followFindAll(
                loginUser.getMemberId()
        );
    }

    @GetMapping("/count")
    public Long count(
            @AuthenticationPrincipal UserEntity loginUser
    ) {
        return followService.followCount(
                loginUser.getMemberId()
        );
    }

    @GetMapping("/otherCount/{otherMemberId}")
    public Long otherFollowCount(
            @PathVariable String otherMemberId
    ) {
        return followService.otherFollowCount(otherMemberId);
    }
}
