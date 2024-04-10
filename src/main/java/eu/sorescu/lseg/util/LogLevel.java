package eu.sorescu.lseg.util;

public enum LogLevel {
    DEBUG(-10),
    SUCCESS(0),
    WARN(10),
    FAIL(20,"Business-wise failure, but no error; perhaps due to wrong arguments"),
    ERROR(30,"Technical problem - could not read/parse data?"),
    FATAL(40,"Kha-boom");

    public final int level;
    public final String description;

    LogLevel(int level) {
        this(level,"");
    }
    LogLevel(int level, String description) {
        this.level=level;
        this.description=description;
    }

    public LogEvent on(String s) {
        return new LogEvent(this,s);
    }
}