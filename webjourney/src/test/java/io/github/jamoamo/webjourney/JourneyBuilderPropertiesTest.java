package io.github.jamoamo.webjourney;

import net.jqwik.api.From;
import net.jqwik.api.Property;
import net.jqwik.api.ForAll;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Provide;
import net.jqwik.api.Arbitrary;
import io.github.jamoamo.webjourney.api.IJourney;
import io.github.jamoamo.webjourney.annotation.ExtractFromWindowTitle;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JourneyBuilderPropertiesTest
{

    @Property
    public void pathMethodAlwaysReturnsBaseJourneyBuilder(@ForAll("strings") String path)
    {
        BaseJourneyBuilder builder = JourneyBuilder.path();
        assertNotNull(builder);
    }

    @Property
    public void journeyBuildWithNavigationAndTitleExtraction(@ForAll("strings") String path)
    {
        // Ensure the journey can be built with navigation and a page consumption action
        IJourney journey = JourneyBuilder.path()
                .navigateTo("http://" + path)
                .consumePage(io.github.jamoamo.webjourney.integration.testentities.TitleExtractionEntity.class,
                        entity -> assertNotNull(entity.getPageTitle())) // Simple assertion for now
                .build();
        assertNotNull(journey);
    }

    @Provide
    Arbitrary<String> strings() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);
    }
} 