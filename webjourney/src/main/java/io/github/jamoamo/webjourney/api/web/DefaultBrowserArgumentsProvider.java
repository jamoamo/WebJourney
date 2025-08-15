package io.github.jamoamo.webjourney.api.web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.config.AsyncConfiguration;
import io.github.jamoamo.webjourney.api.config.ConfigurationEnvironmentKeys;

public class DefaultBrowserArgumentsProvider implements IBrowserArgumentsProvider
{
   private static final Logger logger = LoggerFactory.getLogger(DefaultBrowserArgumentsProvider.class);
   
   private final Function<String, String> getenv;
   private final AsyncConfiguration configuration;
   private final BrowserArgumentsValidator validator;
   private final BrowserArgumentsRedactor redactor;

   public DefaultBrowserArgumentsProvider()
   {
      this(System::getenv);
   }

   public DefaultBrowserArgumentsProvider(Function<String, String> getenv)
   {
      this(getenv, null);
   }

   public DefaultBrowserArgumentsProvider(Function<String, String> getenv, AsyncConfiguration configuration)
   {
      this.getenv = getenv;
      this.configuration = configuration != null ? configuration : getDefaultConfiguration();
      
      // Build validator from configuration
      BrowserArgumentsValidator.ValidationMode mode = parseValidationMode(this.configuration.getValidationMode());
      Set<String> denySet = new HashSet<>(this.configuration.getDenyList());
      this.validator = new BrowserArgumentsValidator(denySet, mode);
      
      // Build redactor from configuration  
      Set<String> extraKeys = new HashSet<>(this.configuration.getRedactionExtraKeys());
      this.redactor = new BrowserArgumentsRedactor(extraKeys);
   }

   /**
    * Creates a default configuration with standard settings.
    */
   private static AsyncConfiguration getDefaultConfiguration()
   {
      return new AsyncConfiguration(List.of(), List.of());
   }

   /**
    * Parses validation mode string into enum value.
    */
   private static BrowserArgumentsValidator.ValidationMode parseValidationMode(String mode)
   {
      if ("warn".equalsIgnoreCase(mode))
      {
         return BrowserArgumentsValidator.ValidationMode.WARN;
      }
      return BrowserArgumentsValidator.ValidationMode.REJECT; // default
   }

	
	@Override
	public ResolvedBrowserArguments resolve(StandardBrowser browserType, IJourneyContext journeyContext)
	{
		// Normalize per-journey snapshots (safe if empty)
		List<String> perBrowserJourney = BrowserArgParser.normalize(journeyContext.getBrowserArguments().snapshotForBrowser(browserType));
		List<String> globalJourney = BrowserArgParser.normalize(journeyContext.getBrowserArguments().snapshotGlobal());

		// Environment variables. Use spec-compliant keys.
      List<String> envGlobalArgs = BrowserArgParser.parse(this.getenv.apply(ConfigurationEnvironmentKeys.BROWSER_ARGS));
		List<String> envBrowserArgs = new ArrayList<>();
		switch (browserType)
		{
			case CHROME:
				envBrowserArgs.addAll(BrowserArgParser.parse(this.getenv.apply(ConfigurationEnvironmentKeys.CHROME_ARGS)));
				break;
			case FIREFOX:
				envBrowserArgs.addAll(BrowserArgParser.parse(this.getenv.apply(ConfigurationEnvironmentKeys.FIREFOX_ARGS)));
				break;
			case EDGE:
				envBrowserArgs.addAll(BrowserArgParser.parse(this.getenv.apply(ConfigurationEnvironmentKeys.EDGE_ARGS)));
				break;
			case CHROMIUM:
			case SAFARI:
			case OPERA:
			default:
				// No environment variable support for these browsers yet
				break;
		}

		// Configuration lists from AsyncConfiguration
		List<String> globalConfig = BrowserArgParser.normalize(this.configuration.getGlobalArguments());
		List<String> perBrowserConfig = BrowserArgParser.normalize(getPerBrowserConfig(browserType));

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

		// Validate merged arguments
		BrowserArgumentsValidator.ValidationResult validationResult = this.validator.validate(merged);
		List<String> validatedArgs = validationResult.getAllowed();
		
		// Handle validation violations
		if (validationResult.hasViolations())
		{
			if (this.validator.getMode() == BrowserArgumentsValidator.ValidationMode.WARN)
			{
				logger.warn("Dropping denied browser arguments (mode=warn): {}", validationResult.getViolations());
				// Update merged list to only include allowed arguments
				merged = validatedArgs;
				// Update provenance to remove dropped keys
				updateProvenanceForDroppedArgs(keyToSource, validationResult.getViolations());
			}
			// Note: REJECT mode throws exception in validator.validate()
		}

		// Build final provenance after validation
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

		// Log resolved arguments with provenance (redacted for safety)
		logResolvedArguments(merged, keyToSource);

		return new ResolvedBrowserArguments(merged, provenance);
	}

	/**
	 * Gets the per-browser configuration arguments for the specified browser type.
	 */
	private List<String> getPerBrowserConfig(StandardBrowser browserType)
	{
		switch (browserType)
		{
			case CHROME:
				return this.configuration.getChromeArguments();
			case FIREFOX:
				return this.configuration.getFirefoxArguments();
			case EDGE:
				return this.configuration.getEdgeArguments();
			default:
				return List.of(); // Other browsers not yet supported in configuration
		}
	}

	/**
	 * Updates provenance map to remove keys for dropped arguments.
	 */
	private void updateProvenanceForDroppedArgs(Map<String, BrowserArgumentSource> keyToSource, List<String> droppedTokens)
	{
		for (String token : droppedTokens)
		{
			String key = BrowserArgumentsMerge.canonicalKey(token.trim());
			keyToSource.remove(key);
		}
	}

	/**
	 * Logs resolved arguments with redaction and provenance information.
	 */
	private void logResolvedArguments(List<String> resolvedArgs, Map<String, BrowserArgumentSource> keyToSource)
	{
		String logLevel = this.configuration.getLogLevel();
		if (!isLogLevelEnabled(logLevel))
		{
			return; // Skip logging if level not enabled
		}

		// Apply redaction for safe logging
		List<String> redactedArgs = this.redactor.redact(resolvedArgs);
		
		// Build provenance information
		StringBuilder provenanceInfo = new StringBuilder();
		provenanceInfo.append("Provenance: ");
		for (Map.Entry<String, BrowserArgumentSource> entry : keyToSource.entrySet())
		{
			provenanceInfo.append(entry.getKey()).append("->").append(entry.getValue()).append(", ");
		}
		if (provenanceInfo.length() > 12) // Remove trailing ", "
		{
			provenanceInfo.setLength(provenanceInfo.length() - 2);
		}

		// Log at appropriate level
		switch (logLevel.toUpperCase())
		{
			case "TRACE":
				logger.trace("Resolved args (redacted): {} | {}", redactedArgs, provenanceInfo);
				break;
			case "DEBUG":
				logger.debug("Resolved args (redacted): {} | {}", redactedArgs, provenanceInfo);
				break;
			case "INFO":
				logger.info("Resolved args (redacted): {} | {}", redactedArgs, provenanceInfo);
				break;
			case "WARN":
				logger.warn("Resolved args (redacted): {} | {}", redactedArgs, provenanceInfo);
				break;
			case "ERROR":
				logger.error("Resolved args (redacted): {} | {}", redactedArgs, provenanceInfo);
				break;
			default:
				logger.debug("Resolved args (redacted): {} | {}", redactedArgs, provenanceInfo);
				break;
		}
	}

	/**
	 * Checks if the specified log level is enabled for the logger.
	 */
	private boolean isLogLevelEnabled(String logLevel)
	{
		switch (logLevel.toUpperCase())
		{
			case "TRACE":
				return logger.isTraceEnabled();
			case "DEBUG":
				return logger.isDebugEnabled();
			case "INFO":
				return logger.isInfoEnabled();
			case "WARN":
				return logger.isWarnEnabled();
			case "ERROR":
				return logger.isErrorEnabled();
			default:
				return logger.isDebugEnabled(); // default to DEBUG
		}
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
