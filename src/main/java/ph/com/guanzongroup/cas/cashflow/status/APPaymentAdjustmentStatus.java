/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.cashflow.status;

/**
 *
 * @author Arsiela 06/05/2025
 */
public class APPaymentAdjustmentStatus { //PENDING TODO
    public static final String OPEN = "0";
    public static final  String CONFIRMED = "1";
//    public static final  String POSTED = "2"; 
    public static final  String CANCELLED = "3";
    public static final  String VOID = "4";
    public static final  String PAID = "5"; 
    public static final  String RETURNED = "6"; 
    
    public static final  String TRANSTYPE = "0008";  //Adjustment
    
}
