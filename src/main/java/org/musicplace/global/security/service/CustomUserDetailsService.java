package org.musicplace.global.security.service;

import lombok.RequiredArgsConstructor;
import org.musicplace.user.domain.UserEntity;
import org.musicplace.user.repository.SignInRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SignInRepository signInRepository;


    @Override
    public UserEntity loadUserByUsername(String memberId) throws UsernameNotFoundException {
        UserEntity userEntity = signInRepository.findByMemberId(memberId);
        if (userEntity == null) {
            throw new UsernameNotFoundException("User not found with member_id: " + memberId);
        }
        return userEntity;
    }
}
