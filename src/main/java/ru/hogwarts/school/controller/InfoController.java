package ru.hogwarts.school.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hogwarts.school.service.InfoService;

import java.util.stream.Stream;

@RestController
@RequestMapping("/port")
public class InfoController {
    private final InfoService infoService;
    @Autowired
    public InfoController(InfoService infoService) {
        this.infoService = infoService;
    }

    @GetMapping
    public Integer getPort() {
        return infoService.getPort();
    }

    @GetMapping("/sum")
    public Integer getSum() {
        return Stream.iterate(1, a -> a + 1)
                .limit(1_000_000)
                .parallel()
                .reduce(0, Integer::sum);
    }
}
