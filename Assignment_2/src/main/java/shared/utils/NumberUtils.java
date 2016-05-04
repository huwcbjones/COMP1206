package shared.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Number Utilities
 *
 * @author Huw Jones
 * @since 03/05/2016
 */
public class NumberUtils {


    public static BigDecimal currencyToBigDecimal(String currencyString) throws ParseException {
        return currencyToBigDecimal(currencyString, NumberFormat.getCurrencyInstance());
    }

    public static BigDecimal currencyToBigDecimal(String currencyString, NumberFormat format) throws ParseException {
        Number number = format.parse(currencyString);
        return new BigDecimal(number.toString());
    }
}
