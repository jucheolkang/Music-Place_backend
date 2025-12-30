package org.musicplace.follow.controller;

import lombok.RequiredArgsConstructor;
import org.musicplace.follow.dto.FollowSaveDto;
import org.musicplace.follow.dto.FollowResponseDto;
import org.musicplace.follow.service.FollowService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;

    @PostMapping()
    public Long FollowSave(@RequestBody FollowSaveDto followSaveDto) {
        return followService.followSave(followSaveDto);
    }

    @DeleteMapping("/{follow_id}")
    public void FollowDelete(@PathVariable Long follow_id) {
        followService.followDelete(follow_id);
    }

    @GetMapping()
    public List<FollowResponseDto> FollowFindAll() {
        return followService.followFindAll();
    }

    @GetMapping("/count")
    public Long followCount() {
        return followService.followCount();
    }

    @GetMapping("/otherCount/{otherMemberId}")
    public Long otherFollowCount(@PathVariable String otherMemberId) {
        return followService.otherFollowCount(otherMemberId);
    }
}
