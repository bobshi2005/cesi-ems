package com.cesi.framework.config;

import com.cesi.framework.config.properties.PermitAllUrlProperties;
import com.cesi.framework.security.filter.JwtAuthenticationTokenFilter;
import com.cesi.framework.security.handle.AuthenticationEntryPointImpl;
import com.cesi.framework.security.handle.LogoutSuccessHandlerImpl;
import com.cesi.framework.security.single.SingleAuthenticationProvider;
import com.cesi.framework.security.single.SmsCodeAuthenticationProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.filter.CorsFilter;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * spring security配置
 *
 * @author cesi
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    @Resource
    @Qualifier("userDetails")
    private UserDetailsService userDetailsService;

    @Resource
    @Qualifier("userDetailsByPhoneNumber")
    private UserDetailsService smsUserDetailsService;

    @Resource
    private AuthenticationEntryPointImpl unauthorizedHandler;

    @Resource
    private LogoutSuccessHandlerImpl logoutSuccessHandler;

    @Resource
    private JwtAuthenticationTokenFilter authenticationTokenFilter;

    @Resource
    private SingleAuthenticationProvider singleAuthenticationProvider;

    @Resource
    private CorsFilter corsFilter;

    @Resource
    private PermitAllUrlProperties permitAllUrl;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider(userDetailsService);
        daoProvider.setPasswordEncoder(bCryptPasswordEncoder());
        SmsCodeAuthenticationProvider smsProvider = new SmsCodeAuthenticationProvider(smsUserDetailsService);
        return new ProviderManager(List.of(daoProvider, singleAuthenticationProvider, smsProvider));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .cacheControl(cc -> cc.disable())
                .frameOptions(fo -> fo.disable()))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                permitAllUrl.getUrls().forEach(url -> auth.requestMatchers(url).permitAll());
                auth.requestMatchers("/login", "/register", "/captchaImage", "/sms/send", "/loginWithSms", "/loginSSO").permitAll();
                auth.requestMatchers(HttpMethod.GET, "/", "/*.html", "/**/*.html", "/**/*.css", "/**/*.js", "/profile/**").permitAll();
                auth.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/swagger-resources/**", "/webjars/**", "/*/api-docs", "/v3/api-docs/**", "/druid/**").permitAll();
                auth.requestMatchers("/carbonVerification/voucher/preview/**").permitAll();
                auth.anyRequest().authenticated();
            })
            .logout(logout -> logout.logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler))
            .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(corsFilter, JwtAuthenticationTokenFilter.class)
            .addFilterBefore(corsFilter, LogoutFilter.class);

        return httpSecurity.build();
    }
}
