package theater;

/**
 * Represents a play with a name and a type.
 *
 * @null Both name and type may be null.
 */
public class Play {

    /**
     * The name of the play.
     *
     * @null This value may be null.
     */
    private String name;

    /**
     * The type of the play, such as "tragedy" or "comedy".
     *
     * @null This value may be null.
     */
    private String type;

    /**
     * Creates a new play with the given name and type.
     *
     * @param name the name of the play
     * @param type the type of the play
     */
    public Play(String name, String type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Returns the name of the play.
     *
     * @return the play name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of the play.
     *
     * @return the play type
     */
    public String getType() {
        return type;
    }
}
