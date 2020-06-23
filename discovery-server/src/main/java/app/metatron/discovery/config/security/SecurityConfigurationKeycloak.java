package app.metatron.discovery.config.security;

import app.metatron.discovery.common.oauth.CookieManager;
import app.metatron.discovery.util.AuthUtils;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationEntryPoint;
import org.keycloak.adapters.springsecurity.authentication.KeycloakCookieBasedRedirect;
import org.keycloak.adapters.springsecurity.facade.SimpleHttpFacade;
import org.keycloak.adapters.springsecurity.filter.AdapterStateCookieRequestMatcher;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.keycloak.adapters.springsecurity.filter.QueryParamPresenceRequestMatcher;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
@ConditionalOnProperty(name="sso.enabled")
public class SecurityConfigurationKeycloak extends WebSecurityDefaultConfiguration {

    public static final String DEFAULT_GROUP = "ID_GROUP_ADMIN";

    private static final RequestMatcher DEFAULT_REQUEST_MATCHER =
            new OrRequestMatcher(
                    new AntPathRequestMatcher(KeycloakAuthenticationEntryPoint.DEFAULT_LOGIN_URI),
                    new QueryParamPresenceRequestMatcher(OAuth2Constants.ACCESS_TOKEN),
                    new AdapterStateCookieRequestMatcher()
            );

    private static final String[] DEFAULT_PERMISSIONS =
            new String[] {
                    "PERM_SYSTEM_MANAGE_USER",
                    "PERM_SYSTEM_MANAGE_SYSTEM",
                    "PERM_SYSTEM_MANAGE_DATASOURCE",
                    "PERM_SYSTEM_MANAGE_PRIVATE_WORKSPACE"
            };


    public SecurityConfigurationKeycloak() {}

    @Bean
    public KeycloakConfigResolver KeycloakConfigResolver() {
        return new EnvConfigResolver();
    }

    @Override
    protected AuthenticationEntryPoint authenticationEntryPoint() throws Exception {
        return new ConfKeycloakAuthenticationEntryPoint(adapterDeploymentContext());
    }

    @Bean
    protected KeycloakAuthenticationProcessingFilter keycloakAuthenticationProcessingFilter() throws Exception {
        KeycloakAuthenticationProcessingFilter filter =
                new KeycloakAuthenticationProcessingFilter(authenticationManagerBean(), DEFAULT_REQUEST_MATCHER);
        filter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy());
        filter.setAuthenticationSuccessHandler(
                new ConfigKeycloakAuthenticationSuccessHandler(adapterDeploymentContext(), null));
        return filter;
    }

    class EnvConfigResolver implements org.keycloak.adapters.KeycloakConfigResolver {
        private KeycloakDeployment deployment;

        @Override
        public KeycloakDeployment resolve(HttpFacade.Request facade) {
            if (deployment != null) {
                return deployment;
            }

            String configPath = System.getenv("KC_CONFIG");
            if (!StringUtils.isEmpty(configPath)) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(new File(configPath, "keycloak.json"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                deployment = KeycloakDeploymentBuilder.build(fis);
                deployment.getAuthUrl();

                return deployment;
            } else {
                throw new RuntimeException("Environment variable `KC_CONFIG` must be set");
            }
        }
    }

    class ConfKeycloakAuthenticationEntryPoint
            extends org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationEntryPoint {

        private AdapterDeploymentContext adapterDeploymentContext;

        public ConfKeycloakAuthenticationEntryPoint(AdapterDeploymentContext adapterDeploymentContext) {
            super(adapterDeploymentContext);
            this.adapterDeploymentContext = adapterDeploymentContext;
        }

        @Override
        protected void commenceLoginRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
            if (request.getSession(false) == null && KeycloakCookieBasedRedirect.getRedirectUrlFromCookie(request) == null) {
                response.addCookie(KeycloakCookieBasedRedirect.createCookieFromRedirectUrl(request.getRequestURI()));
            }

            KeycloakUriBuilder builder = KeycloakUriBuilder
                    .fromUri(request.getRequestURL().toString())
                    .replacePath(request.getContextPath())
                    .path("/sso/login");

            HttpFacade facade = new SimpleHttpFacade(request, response);
            if (adapterDeploymentContext.resolveDeployment(facade).isExposeToken()) {
                builder.scheme("https");
            }

            response.sendRedirect(builder.build().toString());
        }
    }

    class ConfigKeycloakAuthenticationSuccessHandler extends
            org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationSuccessHandler {

        private final AuthenticationSuccessHandler fallback;
        private final AdapterDeploymentContext adapterDeploymentContext;

        public ConfigKeycloakAuthenticationSuccessHandler(AdapterDeploymentContext adapterDeploymentContext,
                                                          AuthenticationSuccessHandler fallback) {
            super(fallback);
            this.adapterDeploymentContext = adapterDeploymentContext;
            this.fallback = fallback;
        }

        @Override
        public void onAuthenticationSuccess(
                HttpServletRequest request, HttpServletResponse response, Authentication authentication)
                throws IOException, ServletException {
            String location = KeycloakCookieBasedRedirect.getRedirectUrlFromCookie(request);
            if (location == null) {
                if (fallback != null) {
                    fallback.onAuthenticationSuccess(request, response, authentication);
                } else {
                    redirectToIndex(request, response);
                }
            } else {
                try {
                    response.addCookie(KeycloakCookieBasedRedirect.createCookieFromRedirectUrl(null));
                    redirectToIndex(request, response);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void redirectToIndex(HttpServletRequest request,
                                     HttpServletResponse response) throws IOException {
            KeycloakUriBuilder builder = KeycloakUriBuilder
                    .fromUri(request.getRequestURL().toString())
                    .replacePath("/");

            CookieManager.addCookie(CookieManager.ACCESS_TOKEN, "keycloak", response);
            CookieManager.addCookie(CookieManager.TOKEN_TYPE, "Keycloak", response);
            CookieManager.addCookie(CookieManager.REFRESH_TOKEN, "keycloak", response);
            CookieManager.addCookie(CookieManager.LOGIN_ID,
                    SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString(), response);

            List<String> permissions = AuthUtils.getKeycloakPermissions();
            if (permissions.isEmpty()) {
                /**
                 * No permission set in keycloak => Default perms (full access)
                 * At least one permission set in keycloak => Respect that
                 */
                permissions.addAll(Arrays.asList(DEFAULT_PERMISSIONS));
            }
            CookieManager.addCookie(CookieManager.PERMISSIONS, String.join( "==", permissions ), response);

            response.setHeader("P3P","CP=\"IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT\"");

            HttpFacade facade = new SimpleHttpFacade(request, response);
            if (adapterDeploymentContext.resolveDeployment(facade).isExposeToken()) {
                builder.scheme("https");
            }

            response.sendRedirect(builder.build().toString());
        }
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        super.configure(auth);
    }
}
