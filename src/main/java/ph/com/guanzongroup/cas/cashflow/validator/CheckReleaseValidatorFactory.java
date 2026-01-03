package ph.com.guanzongroup.cas.cashflow.validator;

import org.guanzon.appdriver.iface.GValidator;

public class CheckReleaseValidatorFactory {

    public static GValidator make(String industryId) {
        switch (industryId) {
            case "00": //Mobile Phone
                return new CheckRelease_MP();
            case "01": //Motorcycle
                return new CheckRelease_MC();
            case "02": //Vehicle
            case "05":
            case "06":
                return new CheckRelease_Car();
            case "03": //Monarch
                return new CheckRelease_Monarch();
            case "04": //Los Pedritos
                return new CheckRelease_LP();
            case "07": //Guanzon Services Office
            case "08": //Main Office
            case "09": //General Purchases
            case "10": //Engineering
                return new CheckRelease_General();
            case "11": //Appliances
                return new CheckRelease_Appliance();
//
//            case "": //Main Office
//                return new CheckRelease_General();
            default:
                return null;
        }
    }

}
