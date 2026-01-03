package ph.com.guanzongroup.cas.cashflow.validator;

import org.guanzon.appdriver.iface.GValidator;

public class CheckTransferValidatorFactory {

    public static GValidator make(String industryId) {
        switch (industryId) {
            case "00": //Mobile Phone
                return new CheckTransfer_MP();
            case "01": //Motorcycle
                return new CheckTransfer_MC();
            case "02": //Vehicle
            case "05":
            case "06":
                return new CheckTransfer_Car();
            case "03": //Monarch
                return new CheckTransfer_Monarch();
            case "04": //Los Pedritos
                return new CheckTransfer_LP();
            case "07": //Guanzon Services Office
            case "08": //Main Office
            case "09": //General Purchases
            case "10": //Engineering
                return new CheckTransfer_General();
            case "11": //Appliances
                return new CheckTransfer_Appliance();
//
//            case "": //Main Office
//                return new CheckTransfer_General();
            default:
                return null;
        }
    }

}
