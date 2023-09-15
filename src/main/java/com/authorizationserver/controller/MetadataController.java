package com.authorizationserver.controller;

import com.authorizationserver.model.MetadataResponse;
import com.authorizationserver.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.authorizationserver.constants.Constants.API_V1;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(API_V1 + "/metadata")
public class MetadataController {

    private final AuthorizationService authorizationService;

    /**
     * Fetch metadata of Auth Service
     *
     * @return {@link ResponseEntity}
     */
    @GetMapping("/")
    public ResponseEntity<MetadataResponse> getMetadata() {
        log.info("Get metaData request received.");
        MetadataResponse response = authorizationService.getMetadata();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
