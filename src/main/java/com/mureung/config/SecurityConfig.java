package com.mureung.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// Import SecurityBuilder if absolutely needed, but usually not required for filterChain bean
// import org.springframework.security.config.annotation.SecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // For disabling CSRF/httpBasic cleanly
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.mureung.config.jwt.JwtAuthenticationFilter;
import com.mureung.config.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // REST API이므로 basic auth 및 csrf 보안을 사용하지 않음 (using lambda DSL)
                .httpBasic(AbstractHttpConfigurer::disable) // Disable HTTP Basic
                .csrf(AbstractHttpConfigurer::disable)      // Disable CSRF

                // JWT를 사용하기 때문에 세션을 사용하지 않음
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(authz -> authz
                        // 해당 API에 대해서는 모든 요청을 허가
                        .antMatchers("/member/login").permitAll()
                        // USER 권한이 있어야 요청할 수 있음
                        .antMatchers("/members/getMember").hasRole("USER")
                        // 이 밖에 모든 요청에 대해서 인증을 필요로 한다는 설정
                        .anyRequest().authenticated()
                )

                // JWT 인증을 위하여 직접 구현한 필터를 UsernamePasswordAuthenticationFilter 전에 실행
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)

                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // 마지막에 httpSecurity 객체에 대해 build() 호출
        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt Encoder 사용 (DelegatingPasswordEncoder recommended)
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

        // Consider specifying allowed origins instead of "*" in production
		configuration.setAllowedOriginPatterns(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("HEAD", "POST", "GET", "DELETE", "PUT", "OPTIONS")); // OPTIONS might be needed for preflight
		configuration.setAllowedHeaders(Arrays.asList("*")); // Specify headers if possible
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration); // Apply CORS to all paths
		return source;
	}

}