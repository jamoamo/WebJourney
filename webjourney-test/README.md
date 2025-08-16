# webjourney-test

A test utility for `webjourney` consumers to run browserless tests using in-memory mocks.

- Mock implementations: `IBrowser`, `IBrowserWindow`, `IWebPage`, `AElement`
- Router to map URL -> page
- Fluent DOM builder for elements
- Factory to integrate with `PreferredBrowserStrategy`

Quick start

```java
import io.github.jamoamo.webjourney.WebTraveller;
import io.github.jamoamo.webjourney.api.IJourney;
import io.github.jamoamo.webjourney.api.web.DefaultBrowserOptions;
import io.github.jamoamo.webjourney.test.mock.*;

MockRouter router = new MockRouter();
MockWebPage home = Pages.html(
    MockElement.tag("div").attr("id","main")
        .child(MockElement.tag("a").attr("href","https://example/next").text("Next"))
).title("Home");
MockWebPage next = Pages.html(MockElement.tag("h1").text("Done")).title("Next");
router.route("https://example/home", home)
      .route("https://example/next", next);

MockBrowser browser = new MockBrowser(router);
var strategy = MockJourneys.using(browser);

// your journey using strategy via TravelOptions
```

Notes
- `IWebPage.getElement(xPath, optional)` supports a minimal XPath subset: `//tag[@attr='value']`.
- `IBrowserWindow.navigateBack/Forward` throw `XNavigationError` when history is empty.
- Attach element handlers via `onClick` and `onTextEntry` for side-effects. 

