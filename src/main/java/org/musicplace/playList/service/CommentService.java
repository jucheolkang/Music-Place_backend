package org.musicplace.playList.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.musicplace.global.exception.ErrorCode;
import org.musicplace.global.exception.BusinessException;
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
                .orElseThrow(() -> new BusinessException(ErrorCode.ID_NOT_FOUND));

        if (user.getDeleteAccount()) {
            throw new BusinessException(ErrorCode.MEMBER_DELETED);
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
                .findByCommentIdAndPlaylistId(commentId, playlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ID_NOT_FOUND)); // COMMENT_NOT_FOUND로 교체 시 여기

        if (comment.isCommentDelete()) {
            throw new BusinessException(ErrorCode.COMMENT_DELETED); // MEMBER_DELETED → COMMENT_DELETED
        }

        comment.delete();
        return comment.isCommentDelete();
    }

    public List<ResponseCommentDto> commentFindAll(String memberId, Long playlistId) {

        UserEntity user = userRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ID_NOT_FOUND));

        if (user.getDeleteAccount()) {
            throw new BusinessException(ErrorCode.MEMBER_DELETED);
        }

        plService.validatePlaylistActive(playlistId);

        return commentRepository
                .findByPlaylistIdAndCommentDeleteFalse(playlistId)
                .stream()
                .map(c -> ResponseCommentDto.builder()
                        .memberId(c.getMemberId())
                        .nickName(c.getNickname())
                        .userComment(c.getUserComment())
                        .profile_img_url(c.getProfileImgUrl())
                        .build())
                .toList();
    }
}

