package com.iig.gcp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.csrf().disable();

		http.authorizeRequests().antMatchers("/", "/login", "/error").permitAll()

		
		.antMatchers("/extraction/ConnectionHome").hasAnyAuthority("ADMIN")
		.antMatchers("/extraction/TargetDetails").hasAnyAuthority("ADMIN")
		.antMatchers("/extraction/SystemHome").hasAnyAuthority("ADMIN")
		.antMatchers("/extraction/DataHome").hasAnyAuthority("ADMIN")
		.antMatchers("/extraction/ExtractHome").hasAnyAuthority("ADMIN")
		
		.and().exceptionHandling().accessDeniedPage("/accessDenied").and().formLogin();

	}

	@Override
	public void configure(AuthenticationManagerBuilder builder) throws Exception {
		builder.authenticationProvider(new CustomAuthenticationProvider());
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/resources/**", "/assets/**");
	}

	@Bean(name = BeanIds.AUTHENTICATION_MANAGER)
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

}