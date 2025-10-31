package ph.com.guanzongroup.cas.cashflow.validator;

import org.guanzon.appdriver.iface.GValidator;

public class CheckReleaseValidatorFactory {

    public static GValidator make(String industryId) {
        switch (industryId) {
            case "01": //Mobile Phone
                return new CheckRelease_MP();
            case "02": //Motorcycle
                return new CheckRelease_MC();
            case "03": //Vehicle
                return new CheckRelease_Car();
            case "04": //Monarch
                return new CheckRelease_Monarch();
            case "05": //Los Pedritos
                return new CheckRelease_LP();
            case "06": //General
                return new CheckRelease_General();
            case "07": //Appliances
                return new CheckRelease_Appliance();

            case "": //Main Office
                return new CheckRelease_General();
            default:
                return null;
        }
    }

}
