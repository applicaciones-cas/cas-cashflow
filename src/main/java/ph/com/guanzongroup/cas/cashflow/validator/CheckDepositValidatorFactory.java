package ph.com.guanzongroup.cas.cashflow.validator;

import org.guanzon.appdriver.iface.GValidator;

public class CheckDepositValidatorFactory {

    public static GValidator make(String industryId) {
        switch (industryId) {
            case "01": //Mobile Phone
                return new CheckDeposit_MP();
            case "02": //Motorcycle
                return new CheckDeposit_MC();
            case "03": //Vehicle
                return new CheckDeposit_Car();
            case "04": //Monarch
                return new CheckDeposit_Monarch();
            case "05": //Los Pedritos
                return new CheckDeposit_LP();
            case "06": //General
                return new CheckDeposit_General();
            case "07": //Appliances
                return new CheckDeposit_Appliance();

            case "": //Main Office
                return new CheckDeposit_General();
            default:
                return null;
        }
    }

}
