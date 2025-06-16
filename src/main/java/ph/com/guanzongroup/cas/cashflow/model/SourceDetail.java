/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.cashflow.model;

/**
 *
 * @author user
 */
public class SourceDetail {
    private String sourceNo;
    private String sourceCode;
    private String particular;

    public SourceDetail(String sourceNo, String sourceCode, String particular) {
        this.sourceNo = sourceNo;
        this.sourceCode = sourceCode;
        this.particular = particular;
    }

    // Getters (optional, for access)
    public String getSourceNo() { return sourceNo; }
    public String getSourceCode() { return sourceCode; }
    public String getParticular() { return particular; }

    // toString (optional, for easy printing)
    @Override
    public String toString() {
        return "(" + sourceNo + "," + sourceCode + "," + particular + ")";
    }
}