/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.cashflow.utility;

import java.math.BigDecimal;

/**
 *
 * @author user
 */
public class NumberToWords {
    private static final String[] units = {
        "", "One", "Two", "Three", "Four", "Five",
        "Six", "Seven", "Eight", "Nine", "Ten", "Eleven",
        "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] tens = {
        "", "", "Twenty", "Thirty", "Forty", "Fifty",
        "Sixty", "Seventy", "Eighty", "Ninety"
    };

    public static String convertToWords(BigDecimal amount) {
        if (amount == null) return "";

        long pesos = amount.longValue();
        int centavos = amount.remainder(BigDecimal.ONE).movePointRight(2).intValue();

        String pesoWord = convert(pesos) + (pesos == 1 ? " Peso" : " Pesos");
        String centavoWord = centavos > 0 ? " and " + convert(centavos) + (centavos == 1 ? " Centavo" : " Centavos") : "";

        return pesoWord + centavoWord;
    }

    private static String convert(long number) {
        if (number < 20)
            return units[(int) number];
        if (number < 100)
            return tens[(int) number / 10] + ((number % 10 != 0) ? "-" + units[(int) number % 10] : "");
        if (number < 1000)
            return units[(int) number / 100] + " Hundred" + ((number % 100 != 0) ? " " + convert(number % 100) : "");
        if (number < 1000000)
            return convert(number / 1000) + " Thousand" + ((number % 1000 != 0) ? " " + convert(number % 1000) : "");
        if (number < 1000000000)
            return convert(number / 1000000) + " Million" + ((number % 1000000 != 0) ? " " + convert(number % 1000000) : "");
        return convert(number / 1000000000) + " Billion" + ((number % 1000000000 != 0) ? " " + convert(number % 1000000000) : "");
    }
}
