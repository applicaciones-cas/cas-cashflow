package ph.com.guanzongroup.cas.cashflow.status;

public class CheckStatus {
    
    public static final String OPEN = "0";
    public static final  String PRINTED = "1";
    public static final  String RELEASED = "2";
    public static final  String CANCELLED = "3";
    public static final  String TRANSFERRED = "4";
    public static final  String RECEIVED = "5";
    public static final  String ENCASH = "4";
    public static final  String STALE = "5";
        
    public static class CheckState  {
        public static final String CLEAR = "0";
        public static final  String CANCELLATION = "1";
        public static final  String STALE = "2";
        public static final  String HOLD = "1";
        public static final  String BOUNCE_OR_DISHONORED = "2";
    }
    public static class DefaultValues {
        public static final String default_Branch_Series_No = "0000000001";
        public static final String default_value_string = "0";
        public static final String default_value_string_one = "1";
        public static final String default_empty_string = "";
        public static final double default_value_double = 0.00;
        public static final int default_value_integer = 0;
    }
}				
