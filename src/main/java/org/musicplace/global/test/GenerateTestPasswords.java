package org.musicplace.global.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 테스트 계정 비밀번호 생성 유틸리티
 *
 * 실행 방법:
 * 1. IntelliJ에서 우클릭 → Run 'GenerateTestPasswords.main()'
 * 2. 출력된 SQL을 복사하여 test-data.sql 파일로 저장
 */
public class GenerateTestPasswords {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "Test1234!";

        System.out.println("==========================================================");
        System.out.println("Music Place 테스트 계정 비밀번호 생성기");
        System.out.println("==========================================================");
        System.out.println("평문 비밀번호: " + password);
        System.out.println("테이블명: users (실제 DB 스키마에 맞춤)");
        System.out.println();

        System.out.println("==========================================================");
        System.out.println("SQL INSERT 문 생성");
        System.out.println("==========================================================");
        System.out.println("-- 아래 SQL을 복사하여 db/mysql/init/test-data.sql로 저장");
        System.out.println();

        for (int i = 1; i <= 10; i++) {
            String hashedPassword = encoder.encode(password);
            String gender = (i % 2 == 0) ? "female" : "male";

            System.out.println(String.format(
                    "INSERT INTO users (member_id, pw, name, email, nickname, gender) " +
                            "VALUES ('testuser%d', '%s', 'Test User %d', 'testuser%d@test.com', 'Tester%d', '%s');",
                    i, hashedPassword, i, i, i, gender
            ));
        }

        System.out.println();
        System.out.println("==========================================================");
        System.out.println("✅ 생성 완료!");
        System.out.println("==========================================================");
    }
}
