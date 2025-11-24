package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {

    private static final int TRAGEDY_BASE_AMOUNT = 40000;
    private static final int EXTRA_TRAGEDY_AMOUNT = 1000;
    private static final int TRAGEDY_BASE_AUDIENCE = 30;
    private static final int CENT_DIVISOR = 100;

    private final Invoice invoice;
    private final Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public Map<String, Play> getPlays() {
        return plays;
    }

    /**
     * Returns the Play associated with a given performance.
     *
     * @param performance the performance
     * @return the associated play
     */
    private Play getPlay(Performance performance) {
        return plays.get(performance.getPlayID());
    }

    /**
     * Computes the amount owed for a performance.
     *
     * @param performance the performance
     * @return the amount in cents
     * @throws RuntimeException if play type is unknown
     */
    private int getAmount(Performance performance) {
        final Play play = getPlay(performance);

        final int result;

        switch (play.getType()) {
            case "tragedy":
                int tragedyAmount = TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    tragedyAmount += EXTRA_TRAGEDY_AMOUNT
                            * (performance.getAudience() - TRAGEDY_BASE_AUDIENCE);
                }
                result = tragedyAmount;
                break;

            case "comedy":
                int comedyAmount = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    comedyAmount += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                comedyAmount += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                result = comedyAmount;
                break;

            default:
                throw new RuntimeException(
                        String.format("unknown type: %s", play.getType())
                );
        }

        return result;
    }

    /**
     * Calculates the volume credits for a performance.
     *
     * @param performance the performance
     * @return the number of volume credits earned
     */
    private int getVolumeCredits(Performance performance) {
        int result = 0;

        // base credits
        result += Math.max(
                performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD,
                0
        );

        // extra comedy bonus
        if ("comedy".equals(getPlay(performance).getType())) {
            result += performance.getAudience()
                    / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }

        return result;
    }

    /**
     * Returns a formatted statement of all performances.
     *
     * @return formatted statement
     */
    public String statement() {
        int totalAmount = 0;
        int volumeCredits = 0;

        final StringBuilder result =
                new StringBuilder("Statement for " + invoice.getCustomer() + System.lineSeparator());

        final NumberFormat formatter =
                NumberFormat.getCurrencyInstance(Locale.US);

        for (final Performance performance : invoice.getPerformances()) {

            final Play play = getPlay(performance);
            final int amount = getAmount(performance);

            // add volume credits using helper
            volumeCredits += getVolumeCredits(performance);

            result.append(
                    String.format(
                            "  %s: %s (%s seats)%n",
                            play.getName(),
                            formatter.format(amount / CENT_DIVISOR),
                            performance.getAudience()
                    )
            );

            totalAmount += amount;
        }

        result.append(
                String.format(
                        "Amount owed is %s%n",
                        formatter.format(totalAmount / CENT_DIVISOR)
                )
        );
        result.append(
                String.format(
                        "You earned %s credits%n",
                        volumeCredits
                )
        );

        return result.toString();
    }
}
