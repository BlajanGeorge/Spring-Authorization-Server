package com.authorizationserver.constants;

import lombok.experimental.UtilityClass;

/**
 * Utility class to store constants
 *
 * @author Blajan George
 */
@SuppressWarnings("java:S1075")
@UtilityClass
public class Constants {
    public static final Integer DEFAULT_TOKEN_AVAILABILITY_IN_MINUTES = 30;
    public static final String AUTHORIZATION_HEADER = "Authorization";

    // Path constants
    public static final String API_V1 = "/api/v1/oauth2";
    public static final String CLIENT_BY_ID_PATH = "/client/{client_id}";
    public static final String GENERATE_AUTH_TOKEN_PATH = API_V1 + "/token";
    public static final String GET_JWK_SET_PATH = API_V1 + "/jwk-set";
    public static final String GET_METADATA_PATH = API_V1 + "/metadata";
}