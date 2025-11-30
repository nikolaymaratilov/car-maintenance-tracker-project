package app.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/register")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/", "/register", "/login").permitAll()
                        .requestMatchers("/dashboard").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .failureHandler((req, res, ex) -> {
                            if (ex instanceof DisabledException) {
                                res.sendRedirect("/login?disabled=true");
                            } else {
                                res.sendRedirect("/login?error=true");
                            }
                        })
                        .defaultSuccessUrl("/home", true)
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, exception) -> {
                            String referer = request.getHeader("Referer");

                            if (referer != null) {
                                response.sendRedirect(referer);
                            } else {
                                response.sendRedirect("/home");
                            }
                        })
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/")
                );

        return httpSecurity.build();
    }
}
