package com.devloopsx.chronelis.configuration;

import com.devloopsx.chronelis.filter.JwtTokenBlacklistFilter;
import java.util.Arrays;
import javax.crypto.spec.SecretKeySpec;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

  private final JwtTokenBlacklistFilter jwtTokenBlacklistFilter;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

  public SecurityConfiguration(
      @Lazy JwtTokenBlacklistFilter jwtTokenBlacklistFilter,
      CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
    this.jwtTokenBlacklistFilter = jwtTokenBlacklistFilter;
    this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
  }

  public static final String[] PUBLIC_ENDPOINTS = {
    // Auth
    "/api/v1/auth/register",
    "/api/v1/auth/verify-active-account",
    "/api/v1/auth/login",
    "/api/v1/auth/refresh",
    "/api/v1/auth/resend-verify",
    "/api/v1/auth/forgot-password",
    "/api/v1/auth/reset-password",
    // WebSocket
    "/ws/**"
  };

  @NonFinal
  @Value("${jwt.access-signer-key}")
  protected String ACCESS_SIGNER_KEY;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .authorizeHttpRequests(
            request ->
                request
                    .requestMatchers("/ws/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, PUBLIC_ENDPOINTS)
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            oauth2 ->
                oauth2
                    .jwt(
                        jwtConfigurer ->
                            jwtConfigurer
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                    .authenticationEntryPoint(customAuthenticationEntryPoint))
        .addFilterBefore(jwtTokenBlacklistFilter, UsernamePasswordAuthenticationFilter.class)
        .cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable);
    return httpSecurity.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(
        Arrays.asList(
            "http://localhost:5173",
            "https://chronelis.vercel.app",
            "https://chronelis.io.vn/",
            "https://www.chronelis.io.vn/"));
    configuration.addAllowedMethod("*");
    configuration.addAllowedHeader("*");
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
        new JwtGrantedAuthoritiesConverter();
    jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
    return jwtAuthenticationConverter;
  }

  @Bean
  JwtDecoder jwtDecoder() {
    SecretKeySpec secretKeySpec = new SecretKeySpec(ACCESS_SIGNER_KEY.getBytes(), "HS512");
    return NimbusJwtDecoder.withSecretKey(secretKeySpec).macAlgorithm(MacAlgorithm.HS512).build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
  }
}
