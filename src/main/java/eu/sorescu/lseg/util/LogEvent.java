package eu.sorescu.lseg.util;

import java.time.Instant;

public class LogEvent {

    public final Instant ts;
    public final LogLevel level;
    public final String message;

    public LogEvent(LogLevel logLevel, String message) {
        this.ts= Instant.now();
        this.level=logLevel;
        this.message=message;
    }

    @Override
    public String toString() {
        return "["+level+"]@"+ts+": "+message;
    }
}
