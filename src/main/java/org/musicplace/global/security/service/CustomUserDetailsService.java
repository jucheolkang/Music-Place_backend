package org.musicplace.global.security.service;

import lombok.RequiredArgsConstructor;
import org.musicplace.user.domain.UserEntity;
import org.musicplace.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserEntity loadUserByUsername(String memberId) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByMemberId(memberId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with member_id: " + memberId));
        return userEntity;
    }
}
