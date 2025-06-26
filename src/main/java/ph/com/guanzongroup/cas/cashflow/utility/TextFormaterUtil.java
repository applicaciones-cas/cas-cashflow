/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.cashflow.utility;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import javafx.scene.control.TextFormatter;

/**
 * Date : 3/14/2023
 *
 * @author Arsiela
 */
public class TextFormaterUtil extends TextFormatter<String> {

    public TextFormaterUtil(Pattern pattern) {
        super(new TextUnaryOperator(pattern));
    }

    private static class TextUnaryOperator implements UnaryOperator<TextFormatter.Change> {

        private final Pattern pattern;

        public TextUnaryOperator(Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public TextFormatter.Change apply(TextFormatter.Change change) {
            if (pattern.matcher(change.getControlNewText()).matches()) {
                return change;
            } else {

                return null;
            }
        }
    }

}
