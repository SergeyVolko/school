package ru.hogwarts.school.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.service.AvatarService;
import java.util.List;
@RestController
@RequestMapping("/avatar")
public class AvatarTController {

    private final AvatarService avatarService;


    public AvatarTController(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @GetMapping
    public ResponseEntity<List<Avatar>> findAllAvatarOnPage(@RequestParam("page") Integer pageNumber,
                                                           @RequestParam("size") Integer pageSize) {
        return ResponseEntity.ok(avatarService.getPageOfAvatars(pageNumber, pageSize));
    }
}
