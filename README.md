# Entity Scraper

## Overview
Java library for scraping web page data into a java class using only annotations. Class fields are annotated with the XPath query that indicates how the value should be extracted from the web page.

## Features

### HTML parsing
* Jsoup based implementation by default
* Custom implementations can be provided

### Querying of Document Model
* XPath querying implementation provided by Jaxen
* Custom implementations can be provided

### Supported Value Types
* String
* Integer\int
* Double\double
* Custom Mapper implementations can be provided for unsupported value types.

## Example

### Annotations

    @Entity (basePath = "/html")
    public class WebPage 
    {
       @XPath(path = "/head/title")
       private String pageTitle;
       
       public String getPageTitle()
       {
          return this.pageTitle;
       }

       public void setPageTitle(String pageTitle)
       {
         this.pageTitle = pageTitle;
       }
    }

### Scraper

    EntityScraper scraper = EntityScraperBuilder.entityClass(WebPage.class).build();
    WebPage webpage = scraper.scrape(new URL("www.google.com"));

##Diclaimer

Backwards compatible changes to be expected until the API is considered stable when v1.0 is released. Any backwards compatibility differences will be documented here:
* No compatibility changes yet.
