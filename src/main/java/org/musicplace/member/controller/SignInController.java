package org.musicplace.member.controller;

import lombok.RequiredArgsConstructor;
import org.musicplace.member.dto.SignInGetUserDataDto;
import org.musicplace.member.dto.SignInSaveDto;
import org.musicplace.member.dto.SignInUpdateDto;
import org.musicplace.member.service.SignInService;
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
