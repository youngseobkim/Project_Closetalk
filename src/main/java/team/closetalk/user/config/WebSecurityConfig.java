package team.closetalk.user.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import team.closetalk.user.service.CustomOAuth2UserService;
import team.closetalk.user.utils.JwtFilter;
import team.closetalk.user.utils.OAuth2SuccessHandler;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtFilter jwtFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.csrf(AbstractHttpConfigurer :: disable)
                .authorizeHttpRequests(
                        authHttp -> authHttp
                                .requestMatchers(
                                        "/joinPage"
                                        , "/users/sendEmail"
                                        , "/users/register"
                                        , "/users/login"
                                        ,"/ootd/main"
                                        ,"/ootd/header"
                                        ,"/ootd/list"
                                ).permitAll()
                                .anyRequest()
                                .authenticated()
                )
                //소셜로그인 추가
                .oauth2Login( oauthHttp -> oauthHttp.loginPage("/loginPage")
                        .successHandler(oAuth2SuccessHandler)
                        .failureUrl("/loginPage")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .permitAll()
                )
                .sessionManagement(
                        sessionManagement -> sessionManagement.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                ).addFilterBefore(
                        jwtFilter
                , AuthorizationFilter.class
                );

        return http.build();
    }
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // 정적 자원에 스프링 시큐리티 필터 규칙을 적용하지 않도록 설정
        return (web) -> web.ignoring().requestMatchers("/static/css/**");
    }
}
