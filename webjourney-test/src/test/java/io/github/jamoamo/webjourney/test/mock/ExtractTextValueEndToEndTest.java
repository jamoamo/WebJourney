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
package io.github.jamoamo.webjourney.test.mock;

import io.github.jamoamo.webjourney.annotation.ExtractTextValue;
import io.github.jamoamo.webjourney.annotation.ExtractValue;
import io.github.jamoamo.webjourney.annotation.RegexExtractValue;
import io.github.jamoamo.webjourney.reserved.entity.EntityCreator;
import io.github.jamoamo.webjourney.reserved.entity.EntityDefn;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

/**
 * End-to-end tests proving {@code @ExtractTextValue} works through the public entity-creation API,
 * against a mock DOM built with {@link MockElement} - no real browser required.
 * <p>
 * This mirrors the intended real-world usage: a text node (e.g. an "End of over" line) sitting between
 * sibling elements, captured on a field of an entity that is itself extracted from a repeated element.
 */
public class ExtractTextValueEndToEndTest
{
	public static class Over
	{
		@ExtractTextValue(path = "following-sibling::text()[1]", optional = true)
		private String endOfOverLine;

		public String getEndOfOverLine()
		{
			return this.endOfOverLine;
		}

		public void setEndOfOverLine(String endOfOverLine)
		{
			this.endOfOverLine = endOfOverLine;
		}
	}

	public static class Innings
	{
		@ExtractValue(path = "//table")
		private List<Over> overs;

		public List<Over> getOvers()
		{
			return this.overs;
		}

		public void setOvers(List<Over> overs)
		{
			this.overs = overs;
		}
	}

	public static class TextNodes
	{
		@ExtractTextValue(path = "text()")
		private List<String> values;

		public List<String> getValues()
		{
			return this.values;
		}

		public void setValues(List<String> values)
		{
			this.values = values;
		}
	}

	public static class RunsOver
	{
		@RegexExtractValue(
			extractTextValue = @ExtractTextValue(path = "following-sibling::text()[1]"),
			regexes = {".*\\((?<runs>\\d+) runs scored\\).*"},
			groupName = "runs")
		private String runsScored;

		public String getRunsScored()
		{
			return this.runsScored;
		}

		public void setRunsScored(String runsScored)
		{
			this.runsScored = runsScored;
		}
	}

	@Test
	public void testExtractTextValue_singleTextNode_followingSibling() throws Exception
	{
		MockElement root = MockElement.tag("div")
			.child(MockElement.tag("table"))
			.child(MockElement.textNode("End of over 1: (4 runs scored) Sri Lanka 4-0"));

		EntityDefn<Innings> defn = new EntityDefn<>(Innings.class);
		EntityCreator<Innings> creator = new EntityCreator<>(defn, root, null);

		Innings innings = creator.createNewEntity(null);

		assertEquals(1, innings.getOvers().size());
		assertEquals("End of over 1: (4 runs scored) Sri Lanka 4-0", innings.getOvers().get(0).getEndOfOverLine());
	}

	@Test
	public void testExtractTextValue_singleTextNode_optionalMissing() throws Exception
	{
		MockElement root = MockElement.tag("div")
			.child(MockElement.tag("table"));

		EntityDefn<Innings> defn = new EntityDefn<>(Innings.class);
		EntityCreator<Innings> creator = new EntityCreator<>(defn, root, null);

		Innings innings = creator.createNewEntity(null);

		assertEquals(1, innings.getOvers().size());
		assertNull(innings.getOvers().get(0).getEndOfOverLine());
	}

	@Test
	public void testExtractTextValue_nodeSet_boundToList() throws Exception
	{
		MockElement root = MockElement.tag("div")
			.child(MockElement.textNode("0"))
			.child(MockElement.tag("span").text("ignored"))
			.child(MockElement.textNode("1lb"))
			.child(MockElement.textNode("5w"));

		EntityDefn<TextNodes> defn = new EntityDefn<>(TextNodes.class);
		EntityCreator<TextNodes> creator = new EntityCreator<>(defn, root, null);

		TextNodes textNodes = creator.createNewEntity(null);

		assertEquals(List.of("0", "1lb", "5w"), textNodes.getValues());
	}

	@Test
	public void testExtractTextValue_composedWithRegexExtractValue() throws Exception
	{
		MockElement table = MockElement.tag("table");
		MockElement.tag("div")
			.child(table)
			.child(MockElement.textNode("End of over 1: (4 runs scored) Sri Lanka 4-0"));

		EntityDefn<RunsOver> defn = new EntityDefn<>(RunsOver.class);
		EntityCreator<RunsOver> creator = new EntityCreator<>(defn, table, null);

		RunsOver runsOver = creator.createNewEntity(null);

		assertEquals("4", runsOver.getRunsScored());
	}

	@Test
	public void testExtractTextValue_onPageLevelEntity_failsAtDefinitionTime() throws Exception
	{
		EntityDefn<Over> defn = new EntityDefn<>(Over.class);

		org.junit.jupiter.api.Assertions.assertThrows(
			io.github.jamoamo.webjourney.reserved.entity.XEntityDefinitionException.class,
			() -> new EntityCreator<>(defn, false, null));
	}

}
