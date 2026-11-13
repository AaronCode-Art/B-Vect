package com.vect.vect.dto.response;

public record AuthFlowResponse(
    String status,
    String challengeToken,
    String otpauthUri,
    AuthResponse auth
) {
    public static AuthFlowResponse setupRequired(String challengeToken, String otpauthUri) {
        return new AuthFlowResponse("MFA_SETUP_REQUIRED", challengeToken, otpauthUri, null);
    }

    public static AuthFlowResponse codeRequired(String challengeToken) {
        return new AuthFlowResponse("MFA_CODE_REQUIRED", challengeToken, null, null);
    }

    public static AuthFlowResponse authenticated(AuthResponse auth) {
        return new AuthFlowResponse("AUTHENTICATED", null, null, auth);
    }
}
