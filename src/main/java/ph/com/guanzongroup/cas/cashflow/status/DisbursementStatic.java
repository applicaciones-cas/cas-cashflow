package ph.com.guanzongroup.cas.cashflow.status;

import org.guanzon.cas.inv.InvTransCons;

public class DisbursementStatic {
        
    public static final String OPEN = "0";
    public static final  String VERIFIED = "1";
    public static final  String CERTIFIED = "2";
    public static final  String CANCELLED = "3";
    public static final  String AUTHORIZED = "4";
    public static final  String VOID = "5";
    public static final  String  DISAPPROVED= "6";
    public static final  String RETURNED = "9";
        
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
         public static final String PAYMENT_REQUEST = "PRFx"; //PRF
        public static final  String ACCOUNTS_PAYABLE = "SOAt"; //SOA
        public static final  String CASH_PAYABLE = "CcPy";      
        public static final  String PO_RECEIVING = InvTransCons.PURCHASE_RECEIVING; //"PORc";      
        public static final  String AP_ADJUSTMENT = "APAd";      
        public static final  String PURCHASE_ORDER = InvTransCons.PURCHASE_ORDER; //"PO";  
        public static final  String DISBURSEMENT_VOUCHER = "DISb"; 
        
        public static final  String LOAD_ALL = "ALL";
    }
    
    public static class CASH_PAYABLE_Source  {
        public static final String PO_Receiving = "PORc";
    } 
    
    //Category
    public static class Category  {
        public static final String MOBILEPHONE = "0000001";   //Cellphone    
        public static final String APPLIANCES  = "0000002";   //Appliances   
        public static final String MOTORCYCLE  = "0000003";   //Motorcycle   
        public static final String SPMC        = "0000004";   //Motorcycle SP
        public static final String CAR         = "0000005";   //CAR          
        public static final String SPCAR       = "0000006";   //CAR SP       
        public static final String GENERAL     = "0000007";   //General      
        public static final String FOOD        = "0000008";   //Food         
        public static final String HOSPITALITY = "0000009";   //Hospitality  
    }
    
    public static final String DEFAULT_VOUCHER_NO = "00000001";
     
    public static class Reverse  {
        public static final  String INCLUDE = "+"; 
        public static final  String EXCLUDE = "-"; 
    }
}				
