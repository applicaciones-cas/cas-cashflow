package ph.com.guanzongroup.cas.cashflow.validator;

import org.guanzon.appdriver.iface.GValidator;

public class CheckTransferValidatorFactory {

    public static GValidator make(String industryId) {
        switch (industryId) {
            case "01": //Mobile Phone
                return new CheckTransfer_MP();
            case "02": //Motorcycle
                return new CheckTransfer_MC();
            case "03": //Vehicle
                return new CheckTransfer_Car();
            case "04": //Monarch
                return new CheckTransfer_Monarch();
            case "05": //Los Pedritos
                return new CheckTransfer_LP();
            case "06": //General
                return new CheckTransfer_General();
            case "07": //Appliances
                return new CheckTransfer_Appliance();

            case "": //Main Office
                return new CheckTransfer_General();
            default:
                return null;
        }
    }

}
