package org.musicplace.common.fixture;

import org.musicplace.user.domain.Gender;
import org.musicplace.user.domain.UserEntity;

public final class UserFixture {


    private UserFixture() {
    }


    public static UserEntity createUser() {

        return UserEntity.builder()
                .memberId("test-user")
                .pw("password123")
                .name("테스트 사용자")
                .gender(Gender.male)
                .email("test@test.com")
                .nickname("tester")
                .role("ROLE_USER")
                .build();
    }


    public static UserEntity createUser(String memberId) {

        return UserEntity.builder()
                .memberId(memberId)
                .pw("password123")
                .name("테스트 사용자")
                .gender(Gender.male)
                .email(memberId + "@test.com")
                .nickname(memberId)
                .role("ROLE_USER")
                .build();
    }


    public static UserEntity createDeletedUser() {

        UserEntity user = createUser();

        user.deleteAccount();

        return user;
    }


    public static UserEntity createAdmin() {

        return UserEntity.builder()
                .memberId("admin-user")
                .pw("admin-password")
                .name("관리자")
                .gender(Gender.male)
                .email("admin@test.com")
                .nickname("admin")
                .role("ROLE_ADMIN")
                .build();
    }

}
