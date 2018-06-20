package com.alarm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.web.bind.annotation.RestController;

//import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
//import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
//import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
//import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
//import org.springframework.security.oauth2.provider.token.TokenStore;
//import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
//import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@SpringBootApplication
@EnableEurekaClient
@RestController
@EnableAutoConfiguration
@EnableCircuitBreaker
@EnableCaching
//@EnableResourceServer
@CrossOrigin(origins="*")
public class AlarmServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlarmServiceApplication.class, args);
	}

    /*
        @Bean
        public ResourceServerConfigurerAdapter resourceServerConfigurerAdapter() {
            return new ResourceServerConfigurerAdapter() {
                 @Override
                 public void configure(HttpSecurity http) throws Exception {
                    http.headers().frameOptions().disable();
                    http.authorizeRequests()
                          .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                          .antMatchers("/notice", "/notice/**").access("#oauth2.hasScope('read')")
                          .anyRequest().authenticated()
                          .and().cors().and();

                 }

                 @Override
                 public void configure(final ResourceServerSecurityConfigurer config) {
                    config.tokenServices(tokenServices());
                 }
              };
           }

        @Bean
        public TokenStore tokenStore() {
             return new JwtTokenStore(accessTokenConverter());
        }

        @Bean
        public JwtAccessTokenConverter accessTokenConverter() {
           JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
           converter.setSigningKey("123");
           return converter;
        }

        @Bean
        @Primary
        public DefaultTokenServices tokenServices() {
               DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
               defaultTokenServices.setTokenStore(tokenStore());
               defaultTokenServices.setSupportRefreshToken(true);
               return defaultTokenServices;

            }
        @Bean 
        public CorsConfigurationSource corsConfigurationSource() {
         
      CorsConfiguration configuration = new CorsConfiguration();
          
      configuration.addAllowedOrigin("*");
       configuration.addAllowedMethod("*");
       configuration.addAllowedHeader("*");
       configuration.setAllowCredentials(true);
       configuration.setMaxAge(3600L);
       UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
       source.registerCorsConfiguration("/**", configuration);
       return source;

   }
   	*/
}



