package ph.com.guanzongroup.cas.cashflow.status;

import java.util.Arrays;
import java.util.List;

public class CheckReleaseStatus {

    public static final String OPEN = "0";
    public static final String CONFIRMED = "1";
    public static final String RELEASED = "2";
    public static final String CANCELLED = "3";
    public static final String VOID = "4";
    public static final String NOT_PRINTED = "0";
    public static final String PRINTED = "1";
    public static final List<String> STATUS = Arrays.asList(
            "OPEN",
            "CONFIRMED",
            "RELEASED",
            "CANCELLED",
            "VOID",
            "NOT_PRINTED",
            "PRINTED"
    );
}
