package com.example.nasax.util;

import com.example.nasax.BuildConfig;

/** App-wide constants. */
public final class AppConstants {

    private AppConstants() {}

    // NASA Open APIs key — read from local.properties via BuildConfig
    public static final String NASA_API_KEY = BuildConfig.NASA_API_KEY;

    // APOD was first published on June 16, 1995
    public static final int APOD_MIN_YEAR  = 1995;
    public static final int APOD_MIN_MONTH = 6;
    public static final int APOD_MIN_DAY   = 16;
}
