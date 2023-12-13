package net.zjvis.flint.data.hub.security;

import net.zjvis.flint.data.hub.security.fliter.DingTalkAuthenticationFilter;
import net.zjvis.flint.data.hub.security.provider.DatabaseAuthenticationProvider;
import net.zjvis.flint.data.hub.security.provider.DingTalkAuthenticationProvider;
import net.zjvis.flint.data.hub.service.account.TokenUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSecurityConfig.class);
    private final DingTalkAuthenticationProvider dingTalkAuthenticationProvider;
    private final DatabaseAuthenticationProvider databaseAuthenticationProvider;
    public final String dingTalkLoginPath;
    public final String databaseLoginPath;
    private final String logoutPath;
    private final PersistentTokenBasedRememberMeServices persistentTokenBasedRememberMeServices;

    public WebSecurityConfig(
            DingTalkAuthenticationProvider dingTalkAuthenticationProvider,
            DatabaseAuthenticationProvider databaseAuthenticationProvider,
            @Value("${application.login.path.dingtalk:/user/ding-talk-user/login}") String dingTalkLoginPath,
            @Value("${application.login.path.database:/user/login}") String databaseLoginPath,
            @Value("${application.logout.path:/user/logout}") String logoutPath,
            @Value("${application.account.rememberMe.tokenValidityInSeconds:86400}") int tokenValidityInSeconds,
            TokenUserDetailsService tokenUserDetailsService
    ) {
        this.dingTalkAuthenticationProvider = dingTalkAuthenticationProvider;
        this.databaseAuthenticationProvider = databaseAuthenticationProvider;
        this.dingTalkLoginPath = dingTalkLoginPath;
        this.databaseLoginPath = databaseLoginPath;
        this.logoutPath = logoutPath;
        this.persistentTokenBasedRememberMeServices = new PersistentTokenBasedRememberMeServices(
                // token-key
                "remember-me",
                tokenUserDetailsService,
                new InMemoryTokenRepositoryImpl()
        );
        // TODO use constructor to initialize persistentTokenBasedRememberMeServices
        // parameter-name
        persistentTokenBasedRememberMeServices.setParameter("remember-me");
        persistentTokenBasedRememberMeServices.setTokenValiditySeconds(tokenValidityInSeconds);
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        DingTalkAuthenticationFilter dingTalkAuthenticationFilter = new DingTalkAuthenticationFilter(
                dingTalkLoginPath, new ProviderManager(dingTalkAuthenticationProvider));
        dingTalkAuthenticationFilter.setRememberMeServices(persistentTokenBasedRememberMeServices);
        configureAuthenticationFilter(dingTalkAuthenticationFilter);
        UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter
                = new UsernamePasswordAuthenticationFilter(new ProviderManager(databaseAuthenticationProvider));
        usernamePasswordAuthenticationFilter.setFilterProcessesUrl(databaseLoginPath);
        usernamePasswordAuthenticationFilter.setRememberMeServices(persistentTokenBasedRememberMeServices);
        configureAuthenticationFilter(usernamePasswordAuthenticationFilter);
        httpSecurity.authorizeRequests(authorizeRequests
                        -> authorizeRequests.antMatchers("/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/favicon.ico",
                                "/actuator/**",
                                "/h2-console/**",
                                "/error",
                                "/database-user/register",
                                "/ding-talk-user/url/auth",
                                "/algorithm/**/**/callback",
                                dingTalkLoginPath,
                                databaseLoginPath)
                        .permitAll()
                        .antMatchers("/user/info").authenticated()
                        .anyRequest().authenticated())
                .addFilterAt(dingTalkAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(usernamePasswordAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin().disable()
                .logout(logout -> logout.logoutUrl(logoutPath)
                        .logoutSuccessHandler((request, response, authentication)
                                -> response.getWriter().write("logout success"))
                        .permitAll()
                        .deleteCookies("JSESSIONID"))
                .rememberMe(httpSecurityRememberMeConfigurer -> httpSecurityRememberMeConfigurer
                        .rememberMeServices(persistentTokenBasedRememberMeServices)
                )
                .headers().frameOptions().disable();
    }

    public void configureAuthenticationFilter(AbstractAuthenticationProcessingFilter authenticationProcessingFilter) {
        authenticationProcessingFilter.setAuthenticationSuccessHandler((request, response, authentication) -> {
            // disable redirect
            response.setStatus(HttpStatus.NO_CONTENT.value());
        });
        authenticationProcessingFilter.setAuthenticationFailureHandler((request, response, ex) -> {
            String message = String.format("%s: %s", HttpStatus.UNAUTHORIZED.getReasonPhrase(), ex.getMessage());
            LOGGER.warn(message, ex);
            response.sendError(HttpStatus.UNAUTHORIZED.value(), message);
        });
    }
}