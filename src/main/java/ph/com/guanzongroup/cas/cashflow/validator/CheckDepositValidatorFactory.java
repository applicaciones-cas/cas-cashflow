package ph.com.guanzongroup.cas.cashflow.validator;

import org.guanzon.appdriver.iface.GValidator;

public class CheckDepositValidatorFactory {

    public static GValidator make(String industryId) {
        switch (industryId) {
            case "00": //Mobile Phone
                return new CheckDeposit_MP();
            case "01": //Motorcycle
                return new CheckDeposit_MC();
            case "02": //Vehicle
            case "05":
            case "06":
                return new CheckDeposit_Car();
            case "03": //Monarch
                return new CheckDeposit_Monarch();
            case "04": //Los Pedritos
                return new CheckDeposit_LP();
            case "07": //Guanzon Services Office
            case "08": //Main Office
            case "09": //General Purchases
            case "10": //Engineering
                return new CheckDeposit_General();
            case "11": //Appliances
                return new CheckDeposit_Appliance();

//            case "": //Main Office
//                return new CheckDeposit_General();
            default:
                return null;
        }
    }

}
