/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.cashflow.model;

/**
 *
 * @author user
 */
public class SelectedITems {

    private final String transNo;
    private final String remarks;

    public SelectedITems(String transNo, String remarks) {
        this.transNo = transNo;
        this.remarks = remarks;
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
