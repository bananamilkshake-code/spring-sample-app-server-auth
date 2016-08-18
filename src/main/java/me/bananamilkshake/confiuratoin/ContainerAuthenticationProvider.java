/*
 * Copyright (C) 2016 Liza Lukicheva
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package me.bananamilkshake.confiuratoin;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
public class ContainerAuthenticationProvider implements AuthenticationProvider {

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {

		UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) authentication;

		// HttpServletRequest object is obtained to perform authentication with application server
		HttpServletRequest request = getHttpServletRequest();

		final String username = authToken.getPrincipal().toString();
		final String password = authToken.getCredentials().toString();

		try {
			// Trying to authenticate user with application server
			request.login(username, password);
		} catch (ServletException exception) {
			// If there is no user with such credentials throw an exception
			// and Spring will try another AuthenticationProvider provider
			throw new BadCredentialsException("Login with application server failed", exception);
		}

		try {
			UserDetails user = createUserDetails(username, password, request);

			return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
		} catch (Exception exception) {
			try {
				// If any error occured logout user from application server to be able
				// to use correct user later
				request.logout();
			} catch (ServletException logoutException) {
				log.warn("HttpServletRequest.logout() failed", logoutException);
			}

			throw new BadCredentialsException("Failed to load principal from userDetailsService", exception);
		}
	}

	private UserDetails createUserDetails(String username, String password, HttpServletRequest request) {
		List<GrantedAuthority> authorities = new ArrayList<>();
		if (request.isUserInRole("ROLE_USER")) {
			// We only need to check one role now
			authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		}

		return new User(username, password, true, true, true, true, authorities);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

	/**
	 * @return HttpServletRequest for current thread.
	 */
	private HttpServletRequest getHttpServletRequest() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		return ((ServletRequestAttributes) requestAttributes).getRequest();
	}
}
