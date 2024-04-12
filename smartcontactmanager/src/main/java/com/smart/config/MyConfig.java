package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class MyConfig {

	@Bean
	public UserDetailsService getUserDetailService() {
		return new UserDetailsServiceImpl();
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
		
	 @Bean
     public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
         return authenticationConfiguration.getAuthenticationManager();
     }
//	configure method
    
	  @Bean
	   public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
	        
	       http.cors().and().formLogin()
	       .loginPage("/signin")
	       .loginProcessingUrl("/do_login")
	       .defaultSuccessUrl("/user/index")
	       .and()
	       .csrf()
	       .disable()
	               .authorizeRequests()
	               .requestMatchers("/admin/**").hasRole("ADMIN")
	               .requestMatchers("/user/**").hasRole("USER")
	               .requestMatchers("/**").permitAll();

	       return http.build();
	   }
}
