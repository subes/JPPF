/*
 * JPPF.
 * Copyright (C) 2005-2019 JPPF Team.
 * http://www.jppf.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jppf.admin.web.security;

import java.security.Principal;

import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.wicketstuff.wicket.servlet3.auth.UserPrincipalRoles;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Adapted from org.wicketstuff.wicket.servlet3.auth.ServletContainerAuthenticatedWebSession without final for extensibility.
 */
public class JPPFServletContainerAuthenticatedWebSession extends AuthenticatedWebSession
{

	private static final long serialVersionUID = 1L;

	/**
	 * @return Current authenticated web session
	 */
	public static JPPFServletContainerAuthenticatedWebSession get()
	{
		return (JPPFServletContainerAuthenticatedWebSession) Session.get();
	}

	public JPPFServletContainerAuthenticatedWebSession(final Request request)
	{
		super(request);
	}

	/**
	 * Convenience method to retrieve authenticated users id.
	 *
	 * @return name member of Principal object in servlet 3 request
	 */
	public String getUserName()
	{
		final Principal principal = getRequest().getUserPrincipal();
		if (principal == null)
		{
			return null;
		}
		return principal.getName();
	}

	@Override
	public Roles getRoles()
	{
		if (isSignedIn())
		{
			return new UserPrincipalRoles();
		}
		return null;
	}

	@Override
	public void signOut()
	{
		signIn(false);
		if (getRequest().getUserPrincipal() != null)
		{
			try
			{
				getRequest().logout();
			} catch (final ServletException ex)
			{
				throw new RuntimeException(ex);
			}
		}
	}

	@Override
	public boolean authenticate(final String username, final String password)
	{
		try
		{
			//some user is already logged in so logout
			if (getRequest().getUserPrincipal() != null)
			{
				signOut();
			}
			//Login using the 3.0 servlet request call
			getRequest().login(username, password);

			return true;
		} catch (final ServletException ex)
		{
			return false;
		}

	}

	protected HttpServletRequest getRequest()
	{
		return (HttpServletRequest) RequestCycle.get().getRequest().getContainerRequest();
	}

}
