/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.cashflow.status;

import org.guanzon.cas.inv.InvTransCons;

/**
 *
 * @author Arsiela
 */
public class SOATaggingStatic {
    //Payable Type
    public static final String PaymentRequest = "PRFx";
//    public static final String CachePayable = "CcPy";
    public static final String APPaymentAdjustment = "APAd";
    public static final String POReceiving = InvTransCons.PURCHASE_RECEIVING;
    
    public static class Reverse  {
        public static final  String INCLUDE = "+"; 
        public static final  String EXCLUDE = "-"; 
    }
}
