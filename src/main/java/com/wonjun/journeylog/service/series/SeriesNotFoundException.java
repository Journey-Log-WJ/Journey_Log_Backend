package com.wonjun.journeylog.service.series;

public class SeriesNotFoundException extends RuntimeException {

    public SeriesNotFoundException(String slug) {
        super("Series not found: " + slug);
    }
}
