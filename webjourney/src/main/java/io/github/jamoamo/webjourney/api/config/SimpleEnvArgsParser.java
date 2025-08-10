package io.github.jamoamo.webjourney.api.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

/**
 * A simple parser for environment variables.
 */
public final class SimpleEnvArgsParser 
{
	private SimpleEnvArgsParser() 
	{
	}

	/**
	 * Parses the environment variable.
	 * 
	 * @param rawCsv The raw CSV string to parse.
	 * @return The list of strings.
	 */
	@SuppressWarnings("checkstyle:MethodLength")
	public static List<String> parseCsv(String rawCsv) 
	{
		if (rawCsv == null || rawCsv.isBlank()) 
		{
			return List.of();
		}

		String input = rawCsv.trim();

		CSVFormat format = CSVFormat.DEFAULT
				.builder()
				.setIgnoreSurroundingSpaces(true)
				.setTrim(true)
				.build();

		List<String> result = new ArrayList<>();
		try (CSVParser parser = CSVParser.parse(input, format)) 
		{
			if (parser.iterator().hasNext()) 
			{
				var record = parser.iterator().next();
				for (String value : record)
				{
					if (value == null) 
					{
						continue;
					}
					String token = value.trim();
					if (!token.isEmpty()) 
					{
						result.add(token);
					}
				}
			}
		} 
		catch (IOException e)
		{
			for (String token : input.split(",")) 
			{
				String t = token.trim();
				if (!t.isEmpty()) 
				{
					result.add(t);
				}
			}
		}

		return result;
	}
}
