package io.github.jamoamo.webjourney.integration.testentities;

import io.github.jamoamo.webjourney.annotation.ExtractFromWindowTitle;

public class TitleExtractionEntity {

    @ExtractFromWindowTitle
    private String pageTitle;

    public String getPageTitle() {
        return pageTitle;
    }
} 