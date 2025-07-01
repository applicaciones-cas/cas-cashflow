package ph.com.guanzongroup.cas.cashflow.model;

public class SelectedITems {

    private final String transNo;
    private final String remarks;

    // Constructor with both parameters
    public SelectedITems(String transNo, String remarks) {
        this.transNo = transNo;
        this.remarks = remarks;
    }
    
    // Constructor with only transNo
    public SelectedITems(String transNo) {
        this.transNo = transNo;
        this.remarks = "";  // or null if you prefer
    }

    public String getTransNo() {
        return transNo;
    }

    public String getRemarks() {
        return remarks;
    }

    @Override
    public String toString() {
        return "CertifyItem{"
                + "transNo='" + transNo + '\''
                + ", remarks='" + remarks + '\''
                + '}';
    }
}

