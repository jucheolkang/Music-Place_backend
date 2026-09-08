package org.musicplace.playList.controller;

import lombok.RequiredArgsConstructor;
import org.musicplace.playList.search.PlaylistSearchBackfillService;
import org.musicplace.playList.service.SearchTextBackfillService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/playlist")
@RequiredArgsConstructor
public class PlaylistAdminController {

    private final PlaylistSearchBackfillService backfillService;
    private final SearchTextBackfillService searchTextBackfillService;

    @PostMapping("/reindex")
    public String reindex() {
        int count = backfillService.backfillAll();
        return count + "건 인덱싱 완료";
    }

    @PostMapping("/reindex-mysql")
    public String reindexMysql() {
        int count = searchTextBackfillService.backfillAll();
        return count + "건 search_text 재계산 완료";
    }
}
