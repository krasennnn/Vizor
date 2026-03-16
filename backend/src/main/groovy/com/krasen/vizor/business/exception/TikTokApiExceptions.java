package com.krasen.vizor.business.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class TikTokApiExceptions {
    public static ResponseStatusException invalidVideoId(String platformVideoId) {
        return new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Invalid TikTok video ID: " + platformVideoId
        );
    }
}

