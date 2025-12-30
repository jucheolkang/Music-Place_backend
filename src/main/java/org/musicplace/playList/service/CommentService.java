package org.musicplace.playList.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.musicplace.global.exception.ErrorCode;
import org.musicplace.global.exception.ExceptionHandler;
import org.musicplace.playList.domain.CommentEntity;
import org.musicplace.playList.dto.CommentSaveDto;
import org.musicplace.playList.dto.ResponseCommentDto;
import org.musicplace.playList.repository.CommentRepository;
import org.musicplace.user.domain.UserEntity;
import org.musicplace.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PLService plService;

    @Transactional
    public Long commentSave(String memberId, Long playlistId, CommentSaveDto dto) {

        UserEntity user = userRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ExceptionHandler(ErrorCode.ID_NOT_FOUND));

        if (user.getDeleteAccount()) {
            throw new ExceptionHandler(ErrorCode.MEMBER_DELETED);
        }

        plService.validatePlaylistActive(playlistId);

        CommentEntity comment = CommentEntity.builder()
                .playlistId(playlistId)
                .memberId(memberId)
                .nickname(user.getNickname())
                .userComment(dto.getComment())
                .profileImgUrl(dto.getProfile_img_url())
                .build();

        commentRepository.save(comment);
        return comment.getCommentId();
    }

    @Transactional
    public Boolean commentDelete(Long playlistId, Long commentId) {
        plService.validatePlaylistActive(playlistId);

        CommentEntity comment = commentRepository
                .findByIdAndPlaylistId(commentId, playlistId)
                .orElseThrow(() -> new ExceptionHandler(ErrorCode.ID_NOT_FOUND));

        if (comment.isCommentDelete()) {
            throw new ExceptionHandler(ErrorCode.ID_DELETE);
        }

        comment.delete();
        return comment.isCommentDelete();
    }

    public List<ResponseCommentDto> commentFindAll(String memberId, Long playlistId) {

        UserEntity user = userRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ExceptionHandler(ErrorCode.ID_NOT_FOUND));

        if (user.getDeleteAccount()) {
            throw new ExceptionHandler(ErrorCode.MEMBER_DELETED);
        }

        plService.validatePlaylistActive(playlistId);

        return commentRepository
                .findByPlaylistIdAndCommentDeleteFalse(playlistId)
                .stream()
                .map(c -> ResponseCommentDto.builder()
                        .memberId(c.getMemberId())
                        .nickName(user.getNickname())
                        .userComment(c.getUserComment())
                        .profile_img_url(c.getProfileImgUrl())
                        .build())
                .toList();
    }
}

