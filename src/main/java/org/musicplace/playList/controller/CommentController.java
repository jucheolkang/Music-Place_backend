package org.musicplace.playList.controller;

import lombok.RequiredArgsConstructor;
import org.musicplace.playList.dto.ResponseCommentDto;
import org.musicplace.playList.service.CommentService;
import org.musicplace.playList.dto.CommentSaveDto;
import org.musicplace.user.domain.UserEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/playList/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{PLId}")
    public Long commentSave(@AuthenticationPrincipal UserEntity loginUser, @PathVariable Long PLId, @RequestBody CommentSaveDto commentSaveDto) {
        return commentService.commentSave(loginUser.getMemberId(), PLId,commentSaveDto);
    }

    @DeleteMapping("/{PLId}/{CommentId}")
    public boolean commentDelete(@PathVariable Long PLId, @PathVariable Long CommentId) {
        return commentService.commentDelete(PLId, CommentId);
    }

    @GetMapping("/{PLId}")
    public List<ResponseCommentDto> commentFindAll(@AuthenticationPrincipal UserEntity loginUser, @PathVariable Long PLId){
        List<ResponseCommentDto> AllComment = commentService.commentFindAll(loginUser.getMemberId(), PLId);
        return AllComment;
    }
}
