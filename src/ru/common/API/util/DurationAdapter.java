package ru.common.API.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(JsonWriter out, Duration value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        long totalSeconds = value.getSeconds();
        long days = totalSeconds / (24 * 3600);
        long hours = (totalSeconds % (24 * 3600)) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 && days == 0 && hours == 0 && minutes == 0) sb.append(seconds).append("s ");
        if (sb.length() == 0) sb.append("0s");
        out.value(sb.toString().trim());
    }

    @Override
    public Duration read(JsonReader in) throws IOException {
        String str = in.nextString();
        try {
            return Duration.parse(str);
        } catch (Exception ignore) {
            System.out.println("Not ISO-format, custom parsing: " + str);
        }

        Pattern p = Pattern.compile("(\\d+)([dhms])");
        Matcher m = p.matcher(str);
        long days = 0, hours = 0, minutes = 0, seconds = 0;
        while (m.find()) {
            long num = Long.parseLong(m.group(1));
            switch (m.group(2)) {
                case "d" -> days = num;
                case "h" -> hours = num;
                case "m" -> minutes = num;
                case "s" -> seconds = num;
            }
        }
        return Duration.ofDays(days).plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
    }
}
