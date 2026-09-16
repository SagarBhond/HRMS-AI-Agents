// package com.shrija.orchestrator.security;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.shrija.orchestrator.config.JwtProperties;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import java.io.IOException;
// import java.time.Instant;
// import java.util.Map;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;
//
/// **
// * Validates the "Authorization: Bearer <JWT>" header issued by the Auth Service (:8081), then
// * populates AuthenticatedUser (userId, username, role, employeeCode) as a request attribute for
// * the controller/service layer to ground every downstream agent call in. This is the ONLY place
// * in the whole agent mesh where a bearer token is verified; every Orchestrator -> sub-agent call
// * after this point is trusted network-internal A2A traffic, matching the Auth Service -> ADK
// * Service boundary in the program flow.
// */
// @Component
// public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//  private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
//  private static final String BEARER_PREFIX = "Bearer ";
//
//  private final JwtUtil jwtUtil;
//  private final JwtProperties jwtProperties;
//  private final ObjectMapper objectMapper = new ObjectMapper();
//
//  public JwtAuthenticationFilter(JwtUtil jwtUtil, JwtProperties jwtProperties) {
//    this.jwtUtil = jwtUtil;
//    this.jwtProperties = jwtProperties;
//  }
//
//  @Override
//  protected boolean shouldNotFilter(HttpServletRequest request) {
//    String path = request.getRequestURI();
//    return path.startsWith("/actuator");
//  }
//
//  @Override
//  protected void doFilterInternal(
//      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//      throws ServletException, IOException {
//    String header = request.getHeader("Authorization");
//    if (header == null || !header.startsWith(BEARER_PREFIX)) {
//      unauthorized(response, "Missing or malformed Authorization header.");
//      return;
//    }
//
//    try {
//      String token = header.substring(BEARER_PREFIX.length());
//      Map<String, Object> claims = jwtUtil.verifyAndExtractClaims(token, jwtProperties.secret());
//
//      String userId = stringClaim(claims, jwtProperties.userIdClaimOrDefault());
//      String username = stringClaim(claims, jwtProperties.usernameClaimOrDefault());
//      String role = stringClaim(claims, jwtProperties.roleClaimOrDefault());
//      String employeeCode = stringClaim(claims, jwtProperties.employeeCodeClaimOrDefault());
//
//      if (userId == null || role == null) {
//        unauthorized(response, "JWT is missing required claims (userId/role).");
//        return;
//      }
//
//      AuthenticatedUser authenticatedUser =
//          new AuthenticatedUser(userId, username, role, employeeCode);
//      request.setAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE, authenticatedUser);
//
//      filterChain.doFilter(request, response);
//    } catch (JwtAuthenticationException ex) {
//      log.warn("JWT rejected: {}", ex.getMessage());
//      unauthorized(response, ex.getMessage());
//    }
//  }
//
//  private String stringClaim(Map<String, Object> claims, String key) {
//    Object value = claims.get(key);
//    return value == null ? null : String.valueOf(value);
//  }
//
//  private void unauthorized(HttpServletResponse response, String message) throws IOException {
//    response.setStatus(HttpStatus.UNAUTHORIZED.value());
//    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//    response
//        .getWriter()
//        .write(
//            objectMapper.writeValueAsString(
//                Map.of("timestamp", Instant.now().toString(), "error", message)));
//  }
// }

package com.shrija.orchestrator.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shrija.orchestrator.config.JwtProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Validates the "Authorization: Bearer <JWT>" header issued by the Auth Service (:8081), then
 * populates AuthenticatedUser (userId, username, role, employeeCode) as a request attribute for the
 * controller/service layer to ground every downstream agent call in. This is the ONLY place in the
 * whole agent mesh where a bearer token is verified; every Orchestrator -> sub-agent call after
 * this point is trusted network-internal A2A traffic, matching the Auth Service -> ADK Service
 * boundary in the program flow.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtUtil jwtUtil;
  private final JwtProperties jwtProperties;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public JwtAuthenticationFilter(JwtUtil jwtUtil, JwtProperties jwtProperties) {
    this.jwtUtil = jwtUtil;
    this.jwtProperties = jwtProperties;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/actuator") || "OPTIONS".equalsIgnoreCase(request.getMethod());
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith(BEARER_PREFIX)) {
      unauthorized(response, "Missing or malformed Authorization header.");
      return;
    }

    try {
      String token = header.substring(BEARER_PREFIX.length());
      Map<String, Object> claims = jwtUtil.verifyAndExtractClaims(token, jwtProperties.secret());

      String userId = stringClaim(claims, jwtProperties.userIdClaimOrDefault());
      String username = stringClaim(claims, jwtProperties.usernameClaimOrDefault());
      String role = stringClaim(claims, jwtProperties.roleClaimOrDefault());
      String employeeCode = stringClaim(claims, jwtProperties.employeeCodeClaimOrDefault());

      if (userId == null || role == null) {
        unauthorized(response, "JWT is missing required claims (userId/role).");
        return;
      }

      AuthenticatedUser authenticatedUser =
          new AuthenticatedUser(userId, username, role, employeeCode);
      request.setAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE, authenticatedUser);

      filterChain.doFilter(request, response);
    } catch (JwtAuthenticationException ex) {
      log.warn("JWT rejected: {}", ex.getMessage());
      unauthorized(response, ex.getMessage());
    }
  }

  private String stringClaim(Map<String, Object> claims, String key) {
    Object value = claims.get(key);
    return value == null ? null : String.valueOf(value);
  }

  private void unauthorized(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response
        .getWriter()
        .write(
            objectMapper.writeValueAsString(
                Map.of("timestamp", Instant.now().toString(), "error", message)));
  }
}
