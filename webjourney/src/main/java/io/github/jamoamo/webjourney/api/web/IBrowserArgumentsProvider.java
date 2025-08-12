package io.github.jamoamo.webjourney.api.web;

import io.github.jamoamo.webjourney.api.IJourneyContext;

public interface IBrowserArgumentsProvider 
{
	ResolvedBrowserArguments resolve(StandardBrowser browserType, IJourneyContext journeyContext);
}
