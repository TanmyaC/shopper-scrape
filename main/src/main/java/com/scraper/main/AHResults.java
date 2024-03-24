package com.scraper.main;

import java.util.List;

public record AHResults(
        List<AHResult> results,
        int size

) {
}
