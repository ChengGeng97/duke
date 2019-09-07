/**
 * A class that handles text formatting for Duke's responses to the user.
 * 
 * @deprecated This class has no functionality now that formatting is resolved through the GUI.
 */
public class DukeTextFormatter {
    /**
     * Formats a <code>String</code> to look nicer when printed to the console. Generally, this method should also be 
     * called whenever a call is made to one of DukeUi's messages.
     * 
     * @deprecated This method now returns the its input as-is, as formatting is resolved through the GUI. 
     * 
     * @param inputString The <code>String</code> to be printed to the console.
     * @return The formatted <code>String</code>
     */
    @Deprecated
    public static String makeFormattedText(String inputString) {
        return inputString;
    }
}
