package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a formatted statement for a given invoice of performances.
 */
public class StatementPrinter {

    private static final int TRAGEDY_BASE_AMOUNT = 40000;
    private static final int EXTRA_TRAGEDY_AMOUNT = 1000;
    private static final int TRAGEDY_BASE_AUDIENCE = 30;
    private static final int CENT_DIVISOR = 100;

    private final Invoice invoice;
    private final Map<String, Play> plays;

    /**
     * Constructs a StatementPrinter with the given invoice and play map.
     *
     * @param invoice the invoice containing performances
     * @param plays   the mapping of play IDs to Play objects
     */
    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns the invoice associated with this printer.
     *
     * @return the invoice
     */
    public Invoice getInvoice() {
        return invoice;
    }

    /**
     * Returns the map of plays.
     *
     * @return the play map
     */
    public Map<String, Play> getPlays() {
        return plays;
    }

    /**
     * Returns the Play associated with a given performance.
     *
     * @param performance the performance
     * @return the associated Play
     */
    private Play getPlay(Performance performance) {
        return plays.get(performance.getPlayID());
    }

    /**
     * Computes the amount owed for a performance.
     *
     * @param performance the performance
     * @return the amount in cents owed for the performance
     * @throws RuntimeException if the play type is unknown
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
     * Calculates the volume credits earned for a single performance.
     *
     * @param performance the performance
     * @return the number of volume credits earned
     */
    private int getVolumeCredits(Performance performance) {
        int result = 0;

        result += Math.max(
                performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD,
                0
        );

        if ("comedy".equals(getPlay(performance).getType())) {
            result += performance.getAudience()
                    / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }

        return result;
    }

    /**
     * Converts a cent value into a formatted USD currency string.
     *
     * @param amountInCents the amount in cents
     * @return the formatted USD amount
     */
    private String usd(int amountInCents) {
        return NumberFormat.getCurrencyInstance(Locale.US)
                .format(amountInCents / CENT_DIVISOR);
    }

    /**
     * Computes the total volume credits for all performances.
     *
     * @return the total volume credits earned
     */
    private int getTotalVolumeCredits() {
        int result = 0;
        for (final Performance performance : invoice.getPerformances()) {
            result += getVolumeCredits(performance);
        }
        return result;
    }

    /**
     * Computes the total amount owed across all performances.
     *
     * @return the total amount owed, in cents
     */
    private int getTotalAmount() {
        int result = 0;
        for (final Performance performance : invoice.getPerformances()) {
            result += getAmount(performance);
        }
        return result;
    }

    /**
     * Generates and returns a formatted customer statement including
     * individual performance charges, total amount owed, and total volume credits.
     *
     * @return the formatted customer statement as a String
     */
    public String statement() {

        final StringBuilder result =
                new StringBuilder("Statement for " + invoice.getCustomer() + System.lineSeparator());

        final int totalVolumeCredits = getTotalVolumeCredits();
        final int totalAmount = getTotalAmount();

        for (final Performance performance : invoice.getPerformances()) {
            final Play play = getPlay(performance);
            final int amount = getAmount(performance);

            result.append(
                    String.format(
                            "  %s: %s (%s seats)%n",
                            play.getName(),
                            usd(amount),
                            performance.getAudience()
                    )
            );
        }

        result.append(String.format("Amount owed is %s%n", usd(totalAmount)));
        result.append(String.format("You earned %s credits%n", totalVolumeCredits));

        return result.toString();
    }
}

