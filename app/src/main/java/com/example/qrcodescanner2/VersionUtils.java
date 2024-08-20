package com.example.qrcodescanner2;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VersionUtils {

    // Define MAJOR and MINOR versions
    private static final int MAJOR = 1;
    private static final int MINOR = 0;

    public static String getVersionInfo() {
        // Get current date and time
        Date now = new Date();

        // Format for BUILD number (yyyyMMdd)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String buildNumber = dateFormat.format(now);

        // Calculate RELEASE number (seconds since midnight)
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String timeString = timeFormat.format(now);
        Date startOfDay = new Date(now.getYear(), now.getMonth(), now.getDate());
        long releaseNumber = (now.getTime() - startOfDay.getTime()) / 1000;

        // Construct version name
        return MAJOR + "." + MINOR + "." + buildNumber + "." + releaseNumber;
    }
}
