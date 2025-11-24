package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {

    private final Invoice invoice;
    private final Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice.
     *
     * @return formatted statement
     */
    public String statement() {
        final StringBuilder result =
                new StringBuilder("Statement for " + invoice.getCustomer() + System.lineSeparator());

        for (Performance p : invoice.getPerformances()) {
            final Play play = getPlay(p);
            final int thisAmount = getAmount(p, play);

            result.append(String.format(
                    "  %s: %s (%s seats)%n",
                    play.name,
                    usd(thisAmount),
                    p.audience));
        }

        result.append(String.format("Amount owed is %s%n", usd(getTotalAmount())));
        result.append(String.format("You earned %s credits%n", getTotalVolumeCredits()));

        return result.toString();
    }

    /**
     * Returns the play for a performance.
     *
     * @param p performance
     * @return play
     */
    public Play getPlay(Performance p) {
        return plays.get(p.playID);
    }

    /**
     * Computes the cost of a specific performance.
     *
     * @param p performance
     * @param play play info
     * @return cost in cents
     */
    public int getAmount(Performance p, Play play) {
        final int audience = p.audience;
        int result;

        switch (play.type) {
            case "tragedy":
                result = Constants.TRAGEDY_BASE_AMOUNT;
                if (audience > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (audience - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;

            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (audience > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (audience - Constants.COMEDY_AUDIENCE_THRESHOLD);
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE * audience;
                break;

            case "history":
                result = Constants.HISTORY_BASE_AMOUNT;
                if (audience > Constants.HISTORY_AUDIENCE_THRESHOLD) {
                    result += Constants.HISTORY_OVER_BASE_CAPACITY_PER_PERSON
                            * (audience - Constants.HISTORY_AUDIENCE_THRESHOLD);
                }
                break;

            case "pastoral":
                result = Constants.PASTORAL_BASE_AMOUNT;
                if (audience > Constants.PASTORAL_AUDIENCE_THRESHOLD) {
                    result += Constants.PASTORAL_OVER_BASE_CAPACITY_PER_PERSON
                            * (audience - Constants.PASTORAL_AUDIENCE_THRESHOLD);
                }
                break;

            default:
                throw new RuntimeException("unknown type: " + play.type);
        }

        return result;
    }

    /**
     * Computes volume credits for a performance.
     *
     * @param p performance
     * @param play play info
     * @return credits earned
     */
    public int getVolumeCredits(Performance p, Play play) {
        final int audience = p.audience;
        final String type = play.type;
        int credits = 0;

        switch (type) {
            case "tragedy":
            case "comedy":
                credits += Math.max(audience - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
                break;

            case "history":
                credits += Math.max(audience - Constants.HISTORY_VOLUME_CREDIT_THRESHOLD, 0);
                break;

            case "pastoral":
                credits += Math.max(audience - Constants.PASTORAL_VOLUME_CREDIT_THRESHOLD, 0);
                credits += audience / 2;
                break;

            default:
                break;
        }

        if ("comedy".equals(type)) {
            credits += audience / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }

        return credits;
    }

    /**
     * Computes the total amount owed.
     *
     * @return total amount in cents
     */
    public int getTotalAmount() {
        int total = 0;
        for (Performance p : invoice.getPerformances()) {
            final Play play = getPlay(p);
            total += getAmount(p, play);
        }
        return total;
    }

    /**
     * Computes total volume credits.
     *
     * @return total credits
     */
    public int getTotalVolumeCredits() {
        int total = 0;
        for (Performance p : invoice.getPerformances()) {
            final Play play = getPlay(p);
            total += getVolumeCredits(p, play);
        }
        return total;
    }

    /**
     * Formats an amount in cents as USD.
     *
     * @param amount amount in cents
     * @return formatted currency string
     */
    public String usd(int amount) {
        final NumberFormat frmt = NumberFormat.getCurrencyInstance(Locale.US);
        return frmt.format(amount / 100.0);
    }
}
