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
package io.github.jamoamo.webjourney.reserved.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.jamoamo.webjourney.api.web.AElement;
import io.github.jamoamo.webjourney.api.web.XElementDoesntExistException;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * ARM-465: a list is resolved once, and its items reuse the elements they were resolved to instead of re-running the
 * list query on every access. A stale item is located again and the operation retried once, including when
 * checking whether a child of the item exists.
 */
class ElementListResolutionTest
{
	private static final By ROWS = By.xpath("//tr");
	private static final By FIRST_CELL = By.xpath("td[1]");
	private static final By SECOND_CELL = By.xpath("td[2]");

	private final RemoteWebDriver driver = mock(RemoteWebDriver.class);

	@Test
	void pageList_runsTheListQueryOnce_howeverOftenItsItemsAreRead() throws XElementDoesntExistException
	{
		WebElement row1 = rowWithText("one");
		WebElement row2 = rowWithText("two");
		WebElement row3 = rowWithText("three");
		when(this.driver.findElements(ROWS)).thenReturn(List.of(row1, row2, row3));

		List<? extends AElement> rows = new SeleniumPage(this.driver).getElements("//tr");
		for (AElement row : rows)
		{
			row.getElementText();
			row.getElementText();
			row.getAttribute("class");
		}

		assertEquals(3, rows.size());
		assertEquals("two", rows.get(1).getElementText());
		verify(this.driver, times(1)).findElements(ROWS);
	}

	@Test
	void pageList_childOfAnItem_isFoundFromTheCachedItem_withoutReQueryingTheList()
		throws XElementDoesntExistException
	{
		WebElement row = mock(WebElement.class);
		WebElement cell = mock(WebElement.class);
		when(cell.getText()).thenReturn("cell");
		when(row.findElement(FIRST_CELL)).thenReturn(cell);
		when(this.driver.findElements(ROWS)).thenReturn(List.of(row));

		AElement item = new SeleniumPage(this.driver).getElements("//tr").get(0);
		AElement child = item.findElement("td[1]");

		assertEquals("cell", child.getElementText());
		assertEquals("cell", child.getElementText());
		verify(this.driver, times(1)).findElements(ROWS);
	}

	@Test
	void pageList_staleItem_isLocatedAgainAndTheReadRetried() throws XElementDoesntExistException
	{
		WebElement staleRow = mock(WebElement.class);
		when(staleRow.getText()).thenThrow(new StaleElementReferenceException("stale"));
		WebElement freshRow = rowWithText("fresh");
		when(this.driver.findElements(ROWS)).thenReturn(List.of(staleRow)).thenReturn(List.of(freshRow));

		AElement item = new SeleniumPage(this.driver).getElements("//tr").get(0);

		assertEquals("fresh", item.getElementText());
		verify(this.driver, times(2)).findElements(ROWS);
	}

	@Test
	void pageList_itemStillStaleAfterRetry_throws()
	{
		WebElement staleRow = mock(WebElement.class);
		when(staleRow.getText()).thenThrow(new StaleElementReferenceException("stale"));
		when(this.driver.findElements(ROWS)).thenReturn(List.of(staleRow));

		AElement item = new SeleniumPage(this.driver).getElements("//tr").get(0);

		assertThrows(StaleElementReferenceException.class, item::getElementText);
		verify(this.driver, times(2)).findElements(ROWS);
	}

	@Test
	void pageList_staleChildOfAnItem_relocatesTheParentItemToo() throws XElementDoesntExistException
	{
		WebElement staleRow = mock(WebElement.class);
		when(staleRow.findElement(FIRST_CELL)).thenThrow(new StaleElementReferenceException("stale"));
		WebElement freshRow = mock(WebElement.class);
		WebElement freshCell = mock(WebElement.class);
		when(freshCell.getText()).thenReturn("fresh cell");
		when(freshRow.findElement(FIRST_CELL)).thenReturn(freshCell);
		when(this.driver.findElements(ROWS)).thenReturn(List.of(staleRow)).thenReturn(List.of(freshRow));

		AElement child = new SeleniumPage(this.driver).getElements("//tr").get(0).findElement("td[1]");

		assertEquals("fresh cell", child.getElementText());
		verify(this.driver, times(2)).findElements(ROWS);
	}

	@Test
	void pageList_existsOnAChildOfAStaleItem_relocatesTheParentItem() throws XElementDoesntExistException
	{
		mockStaleThenFreshRowWithSecondCell();

		AElement child = new SeleniumPage(this.driver).getElements("//tr").get(0).findElement("td[2]");

		assertTrue(child.exists());
		verify(this.driver, times(2)).findElements(ROWS);
	}

	@Test
	void pageList_existsOnAWaitingChildOfAStaleItem_relocatesTheParentItem() throws XElementDoesntExistException
	{
		mockStaleThenFreshRowWithSecondCell();

		AElement child = new SeleniumPage(this.driver).getElements("//tr").get(0)
			.findElement("td[2]", false, Duration.ofSeconds(1));

		assertTrue(child.exists());
		verify(this.driver, times(2)).findElements(ROWS);
	}

	@Test
	void pageList_existsOnAChildOfAnItemStillStaleAfterRetry_throws() throws XElementDoesntExistException
	{
		WebElement staleRow = mock(WebElement.class);
		when(staleRow.findElement(SECOND_CELL)).thenThrow(new StaleElementReferenceException("stale"));
		when(this.driver.findElements(ROWS)).thenReturn(List.of(staleRow));

		AElement child = new SeleniumPage(this.driver).getElements("//tr").get(0).findElement("td[2]");

		assertThrows(StaleElementReferenceException.class, child::exists);
		verify(this.driver, times(2)).findElements(ROWS);
	}

	@Test
	void childList_runsTheChildQueryOnce_howeverOftenItsItemsAreRead() throws XElementDoesntExistException
	{
		WebElement parent = mock(WebElement.class);
		WebElement child1 = rowWithText("a");
		WebElement child2 = rowWithText("b");
		By cells = By.xpath("td");
		when(parent.findElements(cells)).thenReturn(List.of(child1, child2));
		SeleniumElement element = new SeleniumElement(new ElementListItemLocator(this.driver, ROWS, 0, parent));

		List<? extends AElement> children = element.findElements("td");
		for (AElement child : children)
		{
			child.getElementText();
			child.getElementText();
		}

		assertEquals("b", children.get(1).getElementText());
		verify(parent, times(1)).findElements(cells);
		verify(this.driver, never()).findElements(ROWS);
	}

	@Test
	void childList_byTag_runsTheChildQueryOnce() throws XElementDoesntExistException
	{
		WebElement parent = mock(WebElement.class);
		By tds = By.tagName("td");
		List<WebElement> cells = List.of(rowWithText("a"), rowWithText("b"));
		when(parent.findElements(tds)).thenReturn(cells);
		SeleniumElement element = new SeleniumElement(new ElementListItemLocator(this.driver, ROWS, 0, parent));

		List<? extends AElement> children = element.getChildrenByTag("td");
		children.get(0).getElementText();
		children.get(1).getElementText();

		verify(parent, times(1)).findElements(tds);
	}

	@Test
	void listItemLocator_withoutAResolvedElement_locatesOnceAndCaches() throws XElementDoesntExistException
	{
		WebElement row = mock(WebElement.class);
		when(this.driver.findElements(ROWS)).thenReturn(List.of(row));
		ElementListItemLocator locator = new ElementListItemLocator(this.driver, ROWS, 0);

		assertEquals(row, locator.findElement());
		assertEquals(row, locator.findElement());
		verify(this.driver, times(1)).findElements(ROWS);

		locator.invalidate();
		assertEquals(row, locator.findElement());
		verify(this.driver, times(2)).findElements(ROWS);
	}

	@Test
	void listItemLocator_itemNoLongerThere_throwsElementDoesntExist()
	{
		when(this.driver.findElements(ROWS)).thenReturn(List.of());
		ElementListItemLocator locator = new ElementListItemLocator(this.driver, ROWS, 0);

		assertThrows(XElementDoesntExistException.class, locator::findElement);
	}

	private void mockStaleThenFreshRowWithSecondCell()
	{
		WebElement staleRow = mock(WebElement.class);
		when(staleRow.findElement(SECOND_CELL)).thenThrow(new StaleElementReferenceException("stale"));
		WebElement freshRow = mock(WebElement.class);
		when(freshRow.findElement(SECOND_CELL)).thenReturn(mock(WebElement.class));
		when(this.driver.findElements(ROWS)).thenReturn(List.of(staleRow)).thenReturn(List.of(freshRow));
	}

	private static WebElement rowWithText(String text)
	{
		WebElement row = mock(WebElement.class);
		when(row.getText()).thenReturn(text);
		return row;
	}
}
