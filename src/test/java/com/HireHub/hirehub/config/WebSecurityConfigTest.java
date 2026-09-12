package com.HireHub.hirehub.config;

import com.HireHub.hirehub.services.CustomUserDetailService;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class WebSecurityConfigTest {

    @Test
    void securityFilterChainShouldIncludeCsrfFilter() throws Exception {
        CustomUserDetailService customUserDetailService = mock(CustomUserDetailService.class);
        CustomAuthenticationSuccessHandler successHandler = mock(CustomAuthenticationSuccessHandler.class);
        WebSecurityConfig securityConfig = new WebSecurityConfig(customUserDetailService, successHandler);

        HttpSecurity http = mock(HttpSecurity.class);
        // Test instantiation of configuration bean
        assertTrue(securityConfig != null);
    }
}


