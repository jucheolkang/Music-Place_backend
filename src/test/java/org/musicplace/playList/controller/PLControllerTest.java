package org.musicplace.playList.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.musicplace.common.config.BaseControllerTest;
import org.musicplace.global.logging.LoggingMdcFilter;
import org.musicplace.global.security.jwt.JwtAuthenticationFilter;
import org.musicplace.playList.domain.OnOff;
import org.musicplace.playList.dto.PLUpdateDto;
import org.musicplace.playList.dto.ResponsePLDto;
import org.musicplace.playList.service.PLService;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PLController.class)
@AutoConfigureMockMvc(addFilters = false)
class PLControllerTest extends BaseControllerTest {

    @MockBean
    private PLService plService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private LoggingMdcFilter loggingMdcFilter;

    @MockBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;


    @Test
    @DisplayName("플레이리스트를 수정한다")
    void plupdate() throws Exception {

        // given
        PLUpdateDto dto = PLUpdateDto.builder()
                .title("updated")
                .onOff(OnOff.Private)
                .coverImg("new.png")
                .comment("new comment")
                .build();

        // when
        mockMvc.perform(patch("/playList/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        // then
        ArgumentCaptor<PLUpdateDto> captor =
                ArgumentCaptor.forClass(PLUpdateDto.class);

        verify(plService).plUpdate(eq(1L), captor.capture());

        PLUpdateDto updated = captor.getValue();

        assertThat(updated.getTitle()).isEqualTo("updated");
        assertThat(updated.getOnOff()).isEqualTo(OnOff.Private);
        assertThat(updated.getCoverImg()).isEqualTo("new.png");
        assertThat(updated.getComment()).isEqualTo("new comment");
    }

    @Test
    @DisplayName("플레이리스트를 삭제한다")
    void pldelete() throws Exception {

        mockMvc.perform(delete("/playList/{id}", 1L))
                .andExpect(status().isOk());

        verify(plService).plDelete(1L);
    }

    @Test
    @DisplayName("플레이리스트를 수정한다")
    void plupdateTest() throws Exception {

        // given
        PLUpdateDto dto = PLUpdateDto.builder()
                .title("new title")
                .onOff(OnOff.Private)
                .coverImg("new.png")
                .comment("updated")
                .build();

        // when & then
        mockMvc.perform(
                        patch("/playList/{pl_id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk());

        ArgumentCaptor<PLUpdateDto> captor =
                ArgumentCaptor.forClass(PLUpdateDto.class);

        verify(plService)
                .plUpdate(eq(1L), captor.capture());

        PLUpdateDto updated = captor.getValue();

        assertThat(updated.getTitle()).isEqualTo("new title");
        assertThat(updated.getOnOff()).isEqualTo(OnOff.Private);
        assertThat(updated.getCoverImg()).isEqualTo("new.png");
        assertThat(updated.getComment()).isEqualTo("updated");
    }

    @Test
    @DisplayName("플레이리스트를 삭제한다")
    void pldeleteTest() throws Exception {

        mockMvc.perform(delete("/playList/{pl_id}", 1L))
                .andExpect(status().isOk());

        verify(plService).plDelete(1L);
    }

    @Test
    @DisplayName("내 플레이리스트를 조회한다")
    void plfindall() throws Exception {

        List<ResponsePLDto> result = List.of(
                new ResponsePLDto(
                        1L,
                        "playlist",
                        "tester",
                        "cover.png",
                        OnOff.Public,
                        "comment",
                        "test-user"
                )
        );

        when(plService.findMyPlaylists("test-user"))
                .thenReturn(result);

        mockMvc.perform(get("/playList")

                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        "test-user",
                                        null
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].playlistId").value(1))
                .andExpect(jsonPath("$[0].plTitle").value("playlist"))
                .andExpect(jsonPath("$[0].nickname").value("tester"))
                .andExpect(jsonPath("$[0].coverImg").value("cover.png"))
                .andExpect(jsonPath("$[0].comment").value("comment"))
                .andExpect(jsonPath("$[0].memberId").value("test-user"));

        verify(plService).findMyPlaylists("test-user");
    }

    @Test
    @DisplayName("공개 플레이리스트를 조회한다")
    void findPublicPlaylists() throws Exception {

        List<ResponsePLDto> result = List.of(
                new ResponsePLDto(
                        1L,
                        "playlist",
                        "tester",
                        "cover.png",
                        OnOff.Public,
                        "comment",
                        "user1"
                )
        );

        when(plService.findPublicPlaylists())
                .thenReturn(result);

        mockMvc.perform(get("/playList/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].playlistId").value(1))
                .andExpect(jsonPath("$[0].plTitle").value("playlist"));

        verify(plService).findPublicPlaylists();
    }

    @Test
    @DisplayName("내 플레이리스트 개수를 조회한다")
    void countMyPlaylists() throws Exception {

        when(plService.countMyPlaylists("test-user"))
                .thenReturn(5L);

        mockMvc.perform(get("/playList/count")
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        "test-user",
                                        null
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(plService).countMyPlaylists("test-user");
    }

    @Test
    @DisplayName("다른 유저의 공개 플레이리스트 개수를 조회한다")
    void otherPlaylistCount() throws Exception {

        when(plService.countOtherPublicPlaylists("other"))
                .thenReturn(3L);

        mockMvc.perform(get("/playList/otherCount/other"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        verify(plService).countOtherPublicPlaylists("other");
    }

    @Test
    @DisplayName("다른 유저의 공개 플레이리스트를 조회한다")
    void getOtherUserPlaylists() throws Exception {

        List<ResponsePLDto> result = List.of(
                new ResponsePLDto(
                        10L,
                        "other playlist",
                        "other",
                        "cover.png",
                        OnOff.Public,
                        "comment",
                        "other"
                )
        );

        when(plService.getOtherUserPublicPlaylists("other"))
                .thenReturn(result);

        mockMvc.perform(get("/playList/other/other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].playlistId").value(10))
                .andExpect(jsonPath("$[0].plTitle").value("other playlist"));

        verify(plService).getOtherUserPublicPlaylists("other");
    }
}
