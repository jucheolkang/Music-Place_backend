package org.musicplace.global.test;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/hello")
public class testHello {
    @GetMapping()
    public String getHello() {
        return "hello world";
    }
}
