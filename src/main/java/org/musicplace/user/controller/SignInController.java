package org.musicplace.user.controller;

import lombok.RequiredArgsConstructor;
import org.musicplace.user.domain.UserEntity;
import org.musicplace.user.dto.SignInGetUserDataDto;
import org.musicplace.user.dto.SignInSaveDto;
import org.musicplace.user.dto.SignInUpdateDto;
import org.musicplace.user.service.SignInService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sign_in")
@RequiredArgsConstructor
@RestControllerAdvice
public class SignInController {

    private final SignInService signInService;

    @PostMapping("/save")
    public void save(
            @RequestBody SignInSaveDto signInSaveDto
    ) {
        signInService.save(signInSaveDto);
    }

    @PatchMapping("/update")
    public void update(
            @AuthenticationPrincipal UserEntity loginUser,
            @RequestBody SignInUpdateDto signInUpdateDto
    ) {
        signInService.update(
                loginUser.getMemberId(),
                signInUpdateDto
        );
    }

    @DeleteMapping("/delete")
    public void delete(
            @AuthenticationPrincipal UserEntity loginUser
    ) {
        signInService.delete(loginUser.getMemberId());
    }

/*    @GetMapping("/{member_id}/{email}/pw")
    public String forgetPw(
            @PathVariable String member_id,
            @PathVariable String email
    ) {
        return signInService.forgetPw(member_id, email);
    }*/

    @GetMapping("/{pw}/{email}/id")
    public String forgetId(
            @PathVariable String pw,
            @PathVariable String email
    ) {
        return signInService.forgetId(pw, email);
    }

    @GetMapping("/{member_id}/sameid")
    public Boolean checkSameId(
            @PathVariable String member_id
    ) {
        return signInService.signInCheckSameId(member_id);
    }

    @GetMapping("/getuser")
    public SignInGetUserDataDto getUserData(
            @AuthenticationPrincipal UserEntity loginUser
    ) {
        return signInService.getUserData(loginUser.getMemberId());
    }
}
