package org.musicplace.recommend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.musicplace.user.domain.Gender;
import org.musicplace.user.dto.SignInSaveDto;
import org.musicplace.user.repository.SignInRepository;
import org.musicplace.user.service.SignInService;
import org.musicplace.recommend.domain.RecommendEntity;
import org.musicplace.recommend.dto.RecommendSaveDto;
import org.musicplace.recommend.repository.RecommendRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RecommendServiceTest {

    @Autowired
    private RecommendService recommendService;
    private SignInService signInService;

    @Autowired
    private RecommendRepository recommendRepository;
    private SignInRepository signInRepository;

    @BeforeEach
    void setUp() { recommendRepository.deleteAll(); }

    @Test
    @DisplayName("Recommend Save")
    void recommandSave() {

        //given
        String thema = "겨울";
        String genre = "댄스";
        String singer = "강주철";

        String member_id = "jucheolkang";
        String pw = "1234";
        String name = "강주철";
        Gender gender = Gender.male;
        String email = "jucheolkang@naver.com";
        String nickname = "전아협";

        signInService.SignInSave(SignInSaveDto.builder()
                .pw(pw)
                .gender(gender)
                .member_id(member_id)
                .email(email)
                .name(name)
                .nickname(nickname)
                .build());

        //when
        Long recommend_Id = recommendService.RecommendSave(RecommendSaveDto.builder()
                .genre(genre)
                .singer(singer)
                .thema(thema)
                .build(), member_id);

        RecommendEntity recommendEntity = recommendRepository.findById(recommend_Id).get();

        //then
        assertEquals(genre, recommendEntity.getGenre());
        assertEquals(thema, recommendEntity.getThema());
        assertEquals(singer,recommendEntity.getSinger());
    }
}
