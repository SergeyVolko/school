package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("profile")
public class InfoServiceImpl implements InfoService {

    @Value("${server.port}")
    private Integer port;
    @Override
    public Integer getPort() {
        return port;
    }
}
