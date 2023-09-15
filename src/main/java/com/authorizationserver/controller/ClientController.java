package com.authorizationserver.controller;

import com.authorizationserver.model.ClientResponse;
import com.authorizationserver.model.PatchClientRequest;
import com.authorizationserver.model.RegisterClientRequest;
import com.authorizationserver.service.AuthorizationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.authorizationserver.constants.Constants.*;

/**
 * Entry point for authorization requests
 *
 * @author Blajan George
 */
@Slf4j
@RestController
@RequestMapping(path = API_V1 + "/client")
public class ClientController {
    /**
     * Service that provide authorization operations
     */
    private final AuthorizationService authorizationService;

    public ClientController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    /**
     * @param registerClientRequest {@link RegisterClientRequest} request entity for registering client
     * @param authorizationSecret   authorization secret to confirm identity
     * @return {@link ResponseEntity}
     */
    @PostMapping
    public ResponseEntity<Void> registerOauth2Client(@Valid @RequestBody RegisterClientRequest registerClientRequest,
                                                     @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authorizationSecret) {
        log.info("Register client with id {} request received.", registerClientRequest.clientId());
        authorizationService.registerOauth2Client(registerClientRequest, authorizationSecret);
        return ResponseEntity.ok().build();
    }

    /**
     * @param patchClientRequest  {@link PatchClientRequest} request entity for patching client
     * @param authorizationSecret authorization secret to confirm identity
     * @return {@link ResponseEntity}
     */
    @PatchMapping("/{client_id}")
    public ResponseEntity<Void> patchOauth2Client(@Valid @RequestBody PatchClientRequest patchClientRequest,
                                                  @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authorizationSecret,
                                                  @PathVariable("client_id") String clientId) {
        log.info("Update client with id {} request received.", clientId);
        authorizationService.patchOauth2Client(patchClientRequest, authorizationSecret, clientId);
        return ResponseEntity.ok().build();
    }

    /**
     * @param authorizationSecret authorization secret to confirm identity
     * @return {@link ResponseEntity}
     */
    @DeleteMapping("/{client_id}")
    public ResponseEntity<Void> deleteOauth2Client(@RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authorizationSecret,
                                                   @PathVariable("client_id") String clientId) {
        log.info("Delete client with id {} request received.", clientId);
        authorizationService.deleteOauth2Client(authorizationSecret, clientId);
        return ResponseEntity.ok().build();
    }

    /**
     * @param authorizationSecret authorization secret to confirm identity
     * @return {@link ResponseEntity}
     */
    @GetMapping("/{client_id}")
    public ResponseEntity<ClientResponse> getOauth2ClientById(@RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authorizationSecret,
                                                              @PathVariable("client_id") String clientId) {
        log.info("Get client with id {} request received.", clientId);
        ClientResponse response = authorizationService.getOauth2Client(authorizationSecret, clientId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * @param authorizationSecret authorization secret to confirm identity
     * @return {@link ResponseEntity}
     */
    @GetMapping
    public ResponseEntity<List<ClientResponse>> getOauth2Clients(@RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authorizationSecret) {
        log.info("Get all clients request received.");
        var response = authorizationService.getOauth2Clients(authorizationSecret);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
