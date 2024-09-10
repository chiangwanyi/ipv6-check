package com.jwy.ipv6check.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkUtils {
    private NetworkUtils() {
    }

    public static List<String> extractIPv6Addresses(String output) {
        List<String> ipv6Addresses = new ArrayList<>();
        Pattern pattern = Pattern.compile("(?<=: )([0-9a-fA-F:%]+)");
        Matcher matcher = pattern.matcher(output);

        while (matcher.find()) {
            ipv6Addresses.add(matcher.group(1));
        }

        return ipv6Addresses;
    }
}
