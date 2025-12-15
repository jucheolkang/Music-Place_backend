package org.musicplace.user.controller;

import lombok.RequiredArgsConstructor;
import org.musicplace.user.dto.SignInGetUserDataDto;
import org.musicplace.user.dto.SignInSaveDto;
import org.musicplace.user.dto.SignInUpdateDto;
import org.musicplace.user.service.SignInService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sign_in")
@RequiredArgsConstructor
public class SignInController {
    private final SignInService signInService;

    @PostMapping("/save")
    public void SignInSave(@RequestBody SignInSaveDto signInSaveDto) {
        signInService.SignInSave(signInSaveDto);
    }

    @PatchMapping("/update")
    public void SignInUpdate(@RequestBody SignInUpdateDto signInUpdateDto) {
        signInService.SignInUpdate(signInUpdateDto);
    }

    @DeleteMapping("/delete")
    public void SignInDelete() {
        signInService.SignInDelete();
    }

    @GetMapping("/{member_id}/{email}/pw")
    public String ForgetPw(@PathVariable String member_id, @PathVariable String email) {
        return signInService.ForgetPw(member_id, email);
    }

    @GetMapping("/{pw}/{email}/id")
    public String ForgetId(@PathVariable String pw, @PathVariable String email) {
        return signInService.ForgetId(pw, email);
    }

    @GetMapping("/{member_id}/sameid")
    public Boolean SignInCheckSameId(@PathVariable String member_id) {
        return signInService.SignInCheckSameId(member_id);
    }

    @GetMapping("/getuser")
    public SignInGetUserDataDto SignInGetUserData() {
        return signInService.SignInGetUserData();
    }

}
