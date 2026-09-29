/*
 * The MIT License
 *
 * Copyright 2026 James Amoore.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.jamoamo.webjourney.api.web;

import java.time.Instant;
import java.util.Objects;

/**
 * An immutable browser cookie, as read from or written to a browser session.
 * <p>
 * <b>The value of a cookie is a credential.</b> It must never be logged or included in an exception message.
 * To help with that, {@link #toString()} deliberately omits the value, and error messages produced by this
 * library carry the cookie's name and domain only. {@link #equals(Object)} and {@link #hashCode()} do take the
 * value into account (so a changed value can be detected) but never print it.
 * <p>
 * This type is intentionally not {@link java.io.Serializable} and has no JSON support: persisting a cookie is
 * the caller's decision, and the caller is then responsible for protecting it.
 *
 * @author James Amoore
 * @since the next release
 */
public final class SessionCookie
{
	/**
	 * The SameSite attribute of a cookie.
	 */
	public enum SameSite
	{
		/** Sent on all requests. */
		NONE,
		/** Sent on same-site requests and top-level navigations. */
		LAX,
		/** Sent on same-site requests only. */
		STRICT
	}

	private static final String DEFAULT_PATH = "/";

	private final String name;
	private final String value;
	private final String domain;
	private final String path;
	private final Instant expiry;
	private final boolean secure;
	private final boolean httpOnly;
	private final SameSite sameSite;

	private SessionCookie(Builder builder)
	{
		this.name = builder.name;
		this.value = builder.value;
		this.domain = builder.domain;
		this.path = builder.path;
		this.expiry = builder.expiry;
		this.secure = builder.secure;
		this.httpOnly = builder.httpOnly;
		this.sameSite = builder.sameSite;
	}

	/**
	 * Starts building a cookie.
	 *
	 * @param name the cookie name, not blank
	 * @param value the cookie value (a credential), not null
	 * @param domain the cookie domain, not blank. A leading dot (e.g. ".example.com") is preserved as given.
	 * @return a new builder
	 * @throws IllegalArgumentException if the name or domain is blank, or the value is null. The message never
	 * contains the value.
	 */
	public static Builder builder(String name, String value, String domain)
	{
		return new Builder(name, value, domain);
	}

	/**
	 * @return the cookie name
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Returns the cookie value. This is a credential: do not log it.
	 *
	 * @return the cookie value
	 */
	public String getValue()
	{
		return this.value;
	}

	/**
	 * @return the cookie domain, exactly as the browser reported or the caller supplied it
	 */
	public String getDomain()
	{
		return this.domain;
	}

	/**
	 * @return the cookie path, "/" by default
	 */
	public String getPath()
	{
		return this.path;
	}

	/**
	 * @return the expiry instant, or null for a session cookie
	 */
	public Instant getExpiry()
	{
		return this.expiry;
	}

	/**
	 * @return true if the cookie is only sent over secure connections
	 */
	public boolean isSecure()
	{
		return this.secure;
	}

	/**
	 * @return true if the cookie is hidden from page scripts
	 */
	public boolean isHttpOnly()
	{
		return this.httpOnly;
	}

	/**
	 * @return the SameSite attribute, or null if the browser did not report one
	 */
	public SameSite getSameSite()
	{
		return this.sameSite;
	}

	/**
	 * Whether the cookie has expired.
	 *
	 * @param now the instant to compare against
	 * @return true if the cookie has an expiry that is not after {@code now}. A session cookie never expires.
	 */
	public boolean isExpired(Instant now)
	{
		Objects.requireNonNull(now, "now");
		return this.expiry != null && !this.expiry.isAfter(now);
	}

	@Override
	public boolean equals(Object other)
	{
		if(this == other)
		{
			return true;
		}
		if(!(other instanceof SessionCookie))
		{
			return false;
		}
		SessionCookie that = (SessionCookie) other;
		return this.secure == that.secure
			&& this.httpOnly == that.httpOnly
			&& this.name.equals(that.name)
			&& this.value.equals(that.value)
			&& this.domain.equals(that.domain)
			&& this.path.equals(that.path)
			&& Objects.equals(this.expiry, that.expiry)
			&& this.sameSite == that.sameSite;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.name, this.value, this.domain, this.path, this.expiry, this.secure, this.httpOnly,
			this.sameSite);
	}

	/**
	 * Describes the cookie without its value.
	 *
	 * @return the name, domain, path, expiry and flags of the cookie
	 */
	@Override
	public String toString()
	{
		return "SessionCookie{name=" + this.name + ", domain=" + this.domain + ", path=" + this.path
			+ ", expiry=" + this.expiry + ", secure=" + this.secure + ", httpOnly=" + this.httpOnly
			+ ", sameSite=" + this.sameSite + ", value=<redacted>}";
	}

	/**
	 * Builder for {@link SessionCookie}.
	 */
	public static final class Builder
	{
		private final String name;
		private final String value;
		private final String domain;
		private String path = DEFAULT_PATH;
		private Instant expiry;
		private boolean secure;
		private boolean httpOnly;
		private SameSite sameSite;

		private Builder(String name, String value, String domain)
		{
			requireNonBlank(name, "Cookie name must not be blank.");
			requireNonBlank(domain, "Cookie domain must not be blank (cookie: " + name + ").");
			requireNonNull(value, "Cookie value must not be null (cookie: " + name + ").");
			this.name = name;
			this.value = value;
			this.domain = domain;
		}

		/**
		 * Throws {@link IllegalArgumentException} with the given message if {@code value} is null or blank.
		 *
		 * @param value the value to check
		 * @param message the exception message to use if the check fails
		 */
		private static void requireNonBlank(String value, String message)
		{
			if(value == null || value.isBlank())
			{
				throw new IllegalArgumentException(message);
			}
		}

		/**
		 * Throws {@link IllegalArgumentException} with the given message if {@code value} is null.
		 *
		 * @param value the value to check
		 * @param message the exception message to use if the check fails
		 */
		private static void requireNonNull(Object value, String message)
		{
			if(value == null)
			{
				throw new IllegalArgumentException(message);
			}
		}

		/**
		 * @param path the cookie path; null or blank means "/"
		 * @return this builder
		 */
		public Builder withPath(String path)
		{
			this.path = path == null || path.isBlank() ? DEFAULT_PATH : path;
			return this;
		}

		/**
		 * @param expiry the expiry instant, or null for a session cookie
		 * @return this builder
		 */
		public Builder withExpiry(Instant expiry)
		{
			this.expiry = expiry;
			return this;
		}

		/**
		 * @param secure whether the cookie is secure
		 * @return this builder
		 */
		public Builder withSecure(boolean secure)
		{
			this.secure = secure;
			return this;
		}

		/**
		 * @param httpOnly whether the cookie is HttpOnly
		 * @return this builder
		 */
		public Builder withHttpOnly(boolean httpOnly)
		{
			this.httpOnly = httpOnly;
			return this;
		}

		/**
		 * @param sameSite the SameSite attribute, or null to leave it unspecified
		 * @return this builder
		 */
		public Builder withSameSite(SameSite sameSite)
		{
			this.sameSite = sameSite;
			return this;
		}

		/**
		 * @return the immutable cookie
		 */
		public SessionCookie build()
		{
			return new SessionCookie(this);
		}
	}
}
