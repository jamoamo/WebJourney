/*
 * The MIT License
 *
 * Copyright 2023 James Amoore.
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
package com.github.jamoamo.webjourney.api;

import com.github.jamoamo.webjourney.reserved.entity.EntityDefn;
import java.util.function.Consumer;

/**
 * Scrape a web page.
 * @author James Amoore
 * @param <E> The entity to scrape.
 */
public final class ScrapePage<E>
{
	private final EntityDefn<E> entityDefn;
	private Consumer<E> resultConsumer;
	
	private ScrapePage(EntityDefn<E> defn)
	{
		this.entityDefn = defn;
	}
	
	/**
	 * Scrapes a page for the entity.
	 * @param <E> the entity type
	 * @param entityClass the entity type class.
	 * @return a new Scrape Page instance.
	 */
	public static <E> ScrapePage forEntity(Class<E> entityClass)
	{
		EntityDefn<E> entityDefn = new EntityDefn<>(entityClass);
		return new ScrapePage<>(entityDefn);
	}
	
	/**
	 * Consume the scraped result using the provided consumer.
	 * @param resultConsumer the consumer to consume the scraped result with.
	 * @return the scrape page instance.
	 */
	public ScrapePage<E> consumeWith(Consumer<E> resultConsumer)
	{
		this.resultConsumer = resultConsumer;
		return this;
	}
	
	/**
	 * gets the entity defn to scrape.
	 * @return the entity defn.
	 */
	public EntityDefn<E> getEntityDefn()
	{
		return this.entityDefn;
	}
	
	/**
	 * Gets the consumer that consumes the result of the scrape.
	 * @return the scrape consumer.
	 */
	public Consumer<E> getScrapeConsumer()
	{
		return this.resultConsumer;
	}
}
