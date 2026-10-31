package de.voteeasy.configuration;

import de.voteeasy.login.LeaveEventsUponLogoutSuccessHandler;
import de.voteeasy.login.LoginTenantAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import javax.sql.DataSource;
import java.util.List;

@Configuration
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true) // to make PreAuthorize work
@EnableWebSecurity
public class AuthenticationConfiguration {

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Qualifier("VoteEasyUserDetailsService")
    @Autowired
    private UserDetailsService userDetailsService;

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    private LeaveEventsUponLogoutSuccessHandler leaveEventsUponLogoutSuccessHandler;

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    private DataSource dataSource;

    private String[] permitAllUris() {
        return List.of("/manifest.json", "/log**", "/register*", "/qr/**", "/static/**", "/img/**", "/css/**", "/js/**", "/**.png", "/**.ico").toArray(new String[]{});
    }

    @Bean
    @Order(1)
    public SecurityFilterChain filterChain(HttpSecurity http, RememberMeServices rememberMeServices) throws Exception {
        http.addFilterBefore(loginTenantAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.securityContext(
                (securityContext) -> securityContext.securityContextRepository(securityContextRepository()));

        http.authorizeHttpRequests((authorizeRequests) ->
                authorizeRequests
                        .requestMatchers(permitAllUris()).permitAll()
                        .anyRequest().authenticated()
        );

        http.csrf(AbstractHttpConfigurer::disable);
        http.csrf(AbstractHttpConfigurer::disable);

        http.formLogin(formLogin ->
                formLogin
                        .loginPage("/login")
                        .defaultSuccessUrl("/")
                        .failureHandler(failureHandler())
                        .permitAll()
        );

        http.userDetailsService(userDetailsService);

        http.logout(logoutConfiguration ->
                logoutConfiguration.permitAll()
                        .logoutSuccessUrl("/login?success") // does not work with custom handler
                        .logoutSuccessHandler(leaveEventsUponLogoutSuccessHandler)
                        .logoutRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher("/logout"))
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .deleteCookies("remember-me", "JSESSIONID")
        );

        http.rememberMe(rememberMeConfiguration ->
                rememberMeConfiguration
                        .rememberMeServices(rememberMeServices)
                        .tokenValiditySeconds(20_000)
        );

        return http.build();
    }

    @Bean
    public RememberMeServices rememberMeServices(@Autowired final UserDetailsService userDetailsService) {
        TokenBasedRememberMeServices.RememberMeTokenAlgorithm encodingAlgorithm = TokenBasedRememberMeServices.RememberMeTokenAlgorithm.SHA256;
        TokenBasedRememberMeServices rememberMe = new TokenBasedRememberMeServices("myKey", userDetailsService,
                encodingAlgorithm);
        rememberMe.setMatchingAlgorithm(TokenBasedRememberMeServices.RememberMeTokenAlgorithm.MD5);
        return rememberMe;
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl db = new JdbcTokenRepositoryImpl();
        db.setDataSource(dataSource);
        return db;
    }

    @Bean
    public LoginTenantAuthenticationFilter loginTenantAuthenticationFilter() throws Exception {
        final LoginTenantAuthenticationFilter filter = new LoginTenantAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager(null));
        filter.setSecurityContextRepository(securityContextRepository());
        filter.setAuthenticationFailureHandler(failureHandler());
        return filter;
    }

    @Bean
    public SimpleUrlAuthenticationFailureHandler failureHandler() {
        return new SimpleUrlAuthenticationFailureHandler("/login?error");
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository());
    }

    @Bean
    public AuthenticationManager authenticationManager(final HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class).build();
    }

}
