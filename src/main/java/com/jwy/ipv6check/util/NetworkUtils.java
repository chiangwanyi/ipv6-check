package com.jwy.ipv6check.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkUtils {
    private NetworkUtils() {
    }

    public static List<String> extractIPv6Addresses(String input) {
        List<String> ipv6Addresses = new ArrayList<>();
//        Pattern pattern = Pattern.compile("(?<=: )([0-9a-fA-F:%]+)");
        Pattern pattern = Pattern.compile("([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            ipv6Addresses.add(matcher.group());
        }

        return ipv6Addresses;
    }
}
