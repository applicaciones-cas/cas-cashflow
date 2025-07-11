package ph.com.guanzongroup.cas.cashflow.status;

public class DisbursementStatic {
        
    public static final String OPEN = "0";
    public static final  String VERIFIED = "1";
    public static final  String CERTIFIED = "2";
    public static final  String CANCELLED = "3";
    public static final  String AUTHORIZED = "4";
    public static final  String VOID = "5";
    public static final  String  DISAPPROVED= "6";
    public static final  String RETURNED = "7";
        
    public static class DisbursementType  {
        public static final String CHECK = "0";
        public static final  String WIRED = "1";
        public static final  String DIGITAL_PAYMENT = "2";
    }
    public static class DefaultValues {
        public static final String default_Branch_Series_No = "0000000001";
        public static final String default_value_string = "0";
        public static final String default_value_string_1 = "1";
        public static final String default_empty_string = "";
        public static final double default_value_double = 0.00;
        public static final double default_value_double_0000 = 0.00;
        public static final int default_value_integer = 0;
    }
     public static class SourceCode  {
        public static final String PAYMENT_REQUEST = "PRF";
        public static final  String ACCOUNTS_PAYABLE = "SOA";
        public static final  String CASH_PAYABLE = "Cche";        
        public static final  String LOAD_ALL = "ALL";
    }
     
     public static final String DEFAULT_VOUCHER_NO = "00000001";
}				
