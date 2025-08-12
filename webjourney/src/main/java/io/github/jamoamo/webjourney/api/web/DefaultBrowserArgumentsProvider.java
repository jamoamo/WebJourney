package io.github.jamoamo.webjourney.api.web;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.config.ConfigurationEnvironmentKeys;

public class DefaultBrowserArgumentsProvider implements IBrowserArgumentsProvider
{
   private final Function<String, String> getenv;

   public DefaultBrowserArgumentsProvider()
   {
      this(System::getenv);
   }

   public DefaultBrowserArgumentsProvider(Function<String, String> getenv)
   {
      this.getenv = getenv;
   }

	
	@Override
	public ResolvedBrowserArguments resolve(StandardBrowser browserType, IJourneyContext journeyContext)
	{
		// Normalize per-journey snapshots (safe if empty)
		List<String> perBrowserJourney = BrowserArgParser.normalize(journeyContext.getBrowserArguments().snapshotForBrowser(browserType));
		List<String> globalJourney = BrowserArgParser.normalize(journeyContext.getBrowserArguments().snapshotGlobal());

		// Environment variables (Chrome only for now). Use spec-compliant keys.
      List<String> envGlobalArgs = BrowserArgParser.parse(this.getenv.apply(ConfigurationEnvironmentKeys.BROWSER_ARGS));
		List<String> envBrowserArgs = new ArrayList<>();
		if (browserType == StandardBrowser.CHROME)
		{
         envBrowserArgs.addAll(BrowserArgParser.parse(this.getenv.apply(ConfigurationEnvironmentKeys.CHROME_ARGS)));
		}

		// Configuration lists (not yet implemented). Leave placeholders as empty lists
		List<String> globalConfig = List.of();
		List<String> perBrowserConfig = List.of();

		// Merge by precedence: globalConfig, perBrowserConfig, env, per-journey
		List<String> merged = BrowserArgumentsMerge.mergeByPrecedence(
				globalConfig,
				perBrowserConfig,
				BrowserArgParser.normalize(envGlobalArgs),
				BrowserArgParser.normalize(envBrowserArgs),
				globalJourney,
				perBrowserJourney);

		// Build provenance: track last-writer layer per canonical key.
		Map<String, BrowserArgumentSource> keyToSource = new LinkedHashMap<>();
		applyProvenanceLayer(keyToSource, globalConfig, BrowserArgumentSource.GLOBAL_CONFIG);
		applyProvenanceLayer(keyToSource, perBrowserConfig, BrowserArgumentSource.PER_BROWSER_CONFIG);
		applyProvenanceLayer(keyToSource, envGlobalArgs, BrowserArgumentSource.ENVIRONMENT);
		applyProvenanceLayer(keyToSource, envBrowserArgs, BrowserArgumentSource.ENVIRONMENT);
		applyProvenanceLayer(keyToSource, globalJourney, BrowserArgumentSource.PER_JOURNEY);
		applyProvenanceLayer(keyToSource, perBrowserJourney, BrowserArgumentSource.PER_JOURNEY);

		List<ProvenancedArgument> provenance = new ArrayList<>();
		for(String token : merged)
		{
			String key = BrowserArgumentsMerge.canonicalKey(token);
			String value = null;
			int eq = token.indexOf('=');
			if(eq >= 0 && eq + 1 < token.length())
			{
				value = token.substring(eq + 1);
			}
			BrowserArgumentSource source = keyToSource.get(key);
			provenance.add(new ProvenancedArgument(key, value, source));
		}

		return new ResolvedBrowserArguments(merged, provenance);
	}

	private static void applyProvenanceLayer(Map<String, BrowserArgumentSource> keyToSource, List<String> tokens, BrowserArgumentSource source)
	{
		if(tokens == null)
		{
			return;
		}
		for(String token : tokens)
		{
			if(token == null || token.isBlank())
			{
				continue;
			}
			String key = BrowserArgumentsMerge.canonicalKey(token.trim());
			if(key == null || key.isBlank())
			{
				continue;
			}
			// last writer wins
			if(keyToSource.containsKey(key))
			{
				keyToSource.remove(key);
			}
			keyToSource.put(key, source);
		}
	}
}
