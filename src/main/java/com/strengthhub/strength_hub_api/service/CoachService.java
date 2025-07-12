package com.strengthhub.strength_hub_api.service;

import com.strengthhub.strength_hub_api.repository.CoachRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class CoachService {

    private final CoachRepository coachRepository;
    private final UserService userService;
}
