package com.glen;

import java.util.*;
import java.util.function.Predicate;

/**
 * This class provides static methods to satisfy the requirements of the following exercise:
 * <p>
 * "Five Dwarves (Gimli Fili Ilif Ilmig and Mark) met at the Prancing Pony and
 * played a word game to determine which combinations of their names resulted in
 * a palindrome. Write a program in that prints out all of those combinations"
 * <p>
 * To use, call the static playTheGame() method, or run from the command line and let the main() method initiate the call.
 * <p>
 * Assumptions in this implementation:
 * <p>
 * - Performance: At present, readability and simplicity are more important than raw speed.  If performance targets
 * are identified that this implementation does not satisfy, additional effort could be made to improve performance.
 * Until then, such effort is not warranted, especially if it sacrifices readability.
 * <p>
 * - Localization of Input: The regular expression used to filter the input only permits English letters; if we need
 * to allow non-English characters (umlauts, accents, or dwarfish diacriticals), a more sophisticated filtering
 * will be needed.
 * <p>
 * - Localization of Output: The dwarves using this program may speak Dwarf-tongue (or French, or Chinese)
 * rather than English.  To this end, some initial multilingual support has been included.
 * <p>
 * - Parameter checking:  Most of the methods here are are permissive in the parameters they accept; e.g., if a
 * null value is passed in, the method will accept it and continue on (without throwing an NPE, of course).
 * This approach favors keeping the application working over "failing fast".  Other applications may call for a
 * a stricter approach that emphasizes early and loud failing if methods are passed unexpected
 * data.
 * <p>
 * - JDK version: The application supports Java 8 (for the sake of lambda/functional interfaces)
 * <p>
 * - Testing: This code does not include assertions or junit tests; such checks could be added per project standards
 */
class DwarfishWordplay {


    // Localization keys
    // todo: use the application standard for defining keys for a localization mechanism
    private static final String LOCALIZED_STRING_CODE_PALINDROME_RESULTS_LEAD = "palindrome_results_lead";
    private static final String LOCALIZED_STRING_CODE_PALINDROME_NONE_FOUND = "palindrome_none_found";

    // Other Constants
    // todo: look into requirements for non-english characters, and whether this regex covers this properly
    private static final String NON_CHARACTER_REGEX = "[^A-Za-z]";

    // Threshholds for Performance Logging
    private static final long PERFORMANCE_WARNING_MS = 100;
    private static final int WARNING_QTY_PERMUTATIONS = 10000;

    // Static method to double-check that the code is finding and checking the expected number of permutations
    static long fItemsChecked = 0;


    public static void main(String[] pArgs) {
        playTheGame();
    }


    /**
     * Wrapper method for running the exercise
     */
    public static void playTheGame() {
        final List<String> rawInputStrings = getNames();
        final Set<String> palindromes = getMatchingPermutations(rawInputStrings, DwarfishWordplay::isPalindrome);
        publishResultsForPalindromeCheck(palindromes);
    }


    /**
     * Encapsulation of the getting of the strings representing dwarf names.  This implementation could be changed to
     * solicit names from user, get names from webservice call,  make up names randomly, etc.
     * <p>
     * For now, will return a hardcoded list of 5 names
     *
     * @return a List of  N Strings, where N <= Integer.MAX_VALUE.  Strings may be null, or empty.  Will not
     * return a null object.
     */
    public static List<String> getNames() {
        return Arrays.asList(
                "Gimli", "Fili", "Ilif", "Ilmig", "Mark"
        );
        /*
                      , "bIlif", "Ilmigb", "Mark", "a", "nan"
        */
    }


    /**
     * Get permutations that match the given filter.  The permutations will be Strings that use 1 or more of the
     * raw Strings in the given list, appended to the given prefix.
     * <p>
     * Each string in the given list will only be used once in a single proposed permutation, though if a particular
     * String appears more than once in the raw String List, each version of the String could appear in a single permutation.
     * <p>
     * This method combines the getting of permutations with the checking of the result per a particular Predicate filter.
     * This very slightly non-orthogonal combination of functionality is done in order to support larger data sets
     * (where large here is anything over 8). Having a pure "getPermutations()" method in this case would lead to
     * so many resulting permutations that system memory would quickly be exhausted, leading to out-of-memory errors.
     *
     * @param pRawStrings  a list of raw Strings used to build permutations. May be null or empty.
     *                     Values in the list may be null or empty.
     * @param pFilterToUse the Predicate filter to use, as defined by the consumer of this method
     * @return a Set of permutations of the given strings, each permutation concatenated into a single string, that
     * match the given filter. Will not return null, nor null or empty results within the return object, nor duplicates
     * (naturally, since the return object is a Set)
     */
    public static Set<String> getMatchingPermutations(final List<String> pRawStrings,
                                                      final Predicate<String> pFilterToUse) {
      /*
       This wrapper method uses a particular, fixed implementation for getting the permutations; other implementations
       could be called from this wrapper method, as needed or later determined.

       For example, another implementation that supports the efficient handling of large
       data-sets could be swapped in, without needing to change the signature of this method.
      */

        final Set<String> returnVal;

        if (pRawStrings != null && pRawStrings.size() > 0) {
            // Log Performance Warning (maybe)
            // Log a message if the quantity of the raw strings is above some number N
            // that indicates a potential performance problem
            // Based on testing, it's around 10 raw Strings that the 'cost curve' starts to spike
            final int rawStringSize = pRawStrings.size();
            final int warningQtyRawData = 10;

            final boolean isPotentiallyExcessiveNumber = rawStringSize >= warningQtyRawData;
            if (isPotentiallyExcessiveNumber) {
                long potentialPermutations = getPotentialPermutationsQuantity(rawStringSize);
                renderLoggingLine(
                        "getMatchingPermutations: " + rawStringSize + " raw strings as input; slow performance is likely; " +
                                "potential number of permutations for non-empty raw strings=" + potentialPermutations);
            }


            // Initiate the call to find permutations that match the filter
            // The initial call to this recursive function passes in an empty prefix and the full list of raw strings,
            // and passes along the filter to use
            long startTime = System.currentTimeMillis();
            returnVal = getMatchingPermutationsRecursive("", pRawStrings, pFilterToUse);

            // Log performance warning (maybe)
            // Log a message if the actual time taken to get the filtered permutations is
            // above some time N that is likely unacceptable for performance.
            long totalTime = System.currentTimeMillis() - startTime;
            if (totalTime > PERFORMANCE_WARNING_MS) {
                renderLoggingLine(
                        "getMatchingPermutations: " + totalTime + " ms to get permutations for "
                                + rawStringSize + " raw strings. Number of results=" + returnVal.size());
            }

            // Development double-check on longer runs, to see if the actual number of checks made match the expected number
            // Note that if some of the raw Strings are empty or null, we would not expect the actual to equal the estimated
            if (isPotentiallyExcessiveNumber) {
                renderLoggingLine("getMatchingPermutations: actual number of permutations checked=" + fItemsChecked);
            }
        } else {
            returnVal = new HashSet<>();
        }
        return returnVal;
    }


    /**
     * Get permutations that match the given filter.  The permutations will be Strings that use 1 or more of the
     * raw Strings in the given list, appended to the given prefix.
     * <p>
     * Each string in the given list will only be used once in a single proposed permutation, though if a particular
     * String appears more than once in the raw String List, each version of the String could appear in a single permutation.
     * <p>
     * This method combines the getting of permutations with the checking of the result per a particular Predicate filter.
     * This very slightly non-orthogonal combination of functionality is done in order to support larger data sets
     * (where large here is anything over 8). Having a pure "getPermutations()" method would lead to so many resulting
     * permutations that the memory needed for larger datasets would quickly run out, leading to out-of-memory errors.
     *
     * @param pCurrentPrefix will be used as a prefix for all permutations that can be built from the given list of Strings.
     *                       May be empty or null.
     * @param pRawStrings    a list of raw Strings used to build permutations. May be null or empty.
     *                       Values in the list may be null or empty.
     * @param pFilterToUse   the Predicate filter to use, as defined by the consumer of this method
     * @return a list of String, representing the permutations that match the tst in the given predicate
     */
    private static Set<String> getMatchingPermutationsRecursive(final String pCurrentPrefix,
                                                                final List<String> pRawStrings,
                                                                final Predicate<String> pFilterToUse) {
        final String prefixToUse = (pCurrentPrefix == null) ? "" : pCurrentPrefix;

      /*
         For each raw string in the 'remaining' list...
         1. Create a 'candidate' by appending the current raw string to the given prefix
         2. Check if the candidate satisfies the test of the given Predicate
         3. If it does satisfy the test, add the candidate to the return value
         4. Recursively call this method with the following parameters
            a. the candidate as the new prefix
            b. the raw string list with the current raw string removed
       */

        Set<String> returnVal = new HashSet<>();
        if (pRawStrings != null) {
            // Loop through each raw String
            for (int i = 0; i < pRawStrings.size(); i++) {
                String suffix = pRawStrings.get(i);
                if (suffix != null && suffix.length() > 0) {
                    // Check the candidate permutations
                    String newPermutation = prefixToUse + suffix;

                    // Development/testing purposes:  Increment the static counter, for later comparison
                    // to a calculated estimated number of checks
                    fItemsChecked++;

                    if (pFilterToUse.test(newPermutation)) {
                        returnVal.add(newPermutation);
                    }

                    // Maybe recurse
                    // Don't recurse if the new list would be empty
                    if (pRawStrings.size() > 1) {
                        List<String> newList = new ArrayList<>(pRawStrings);
                        newList.remove(i);
                        returnVal.addAll(getMatchingPermutationsRecursive(newPermutation, newList, pFilterToUse));
                    }
                }
            }

            // Performance logging - if we're getting to what seems dangerous levels, issue a log warning,
            // for later diagnostic help
            if (returnVal.size() > WARNING_QTY_PERMUTATIONS) {
                renderLoggingLine(
                        "getMatchingPermutationsRecursive: " + returnVal.size()
                                + " permutations and counting; warning will robinson, potential memory problems");
            }
        }
        return returnVal;
    }


    /**
     * Check the given string to determine if it is a palindrome.
     * <p>
     * Makes the following assumptions regarding what defines a palindrome
     * - Non-characters (anything other than A-Z) are ignored
     * - Checks are case-insensitive
     * - Empty Strings, or null Strings, do not qualify as palindromes
     * <p>
     * So: ab123ba would be a palindrome, as would "Madam, I'm Adam"
     *
     * @param pPotentialPalindrome may be null or empty.
     * @return true if the given String is a palindrome.  False if it's not, including if it's null or empty.
     */
    private static boolean isPalindrome(String pPotentialPalindrome) {
        boolean returnVal = false;
        if (pPotentialPalindrome != null && pPotentialPalindrome.length() > 0) {
            // Massage the input: remove non-characters, make lowercase, put into a StringBuilder
            StringBuilder massagedVersion = new StringBuilder(
                    pPotentialPalindrome.replace(NON_CHARACTER_REGEX, "").toLowerCase());

            // This check is optimized for plain old readability.  Could be optimized for speed if desired.
            returnVal = massagedVersion.toString().equals(massagedVersion.reverse().toString());
        }
        return returnVal;
    }

    /**
     * Write the found palindromes to System out println. (Could be refactored to render the results in other ways)
     *
     * @param pFoundResults a list of Strings that have been determined to be palindromes
     */
    private static void publishResultsForPalindromeCheck(Set<String> pFoundResults) {

        final Locale locale = getLocale();
        renderResultLine("\n");
        if (pFoundResults == null || pFoundResults.size() == 0) {
            final String localizedString = localizeString(locale, LOCALIZED_STRING_CODE_PALINDROME_NONE_FOUND, null,
                    "No palindromes were found - you may have to 'borrow or rob' some from elsewhere.");
            renderResultLine(localizedString);
        } else {

            final int setSize = pFoundResults.size();
            Object[] args = {localizeNumber(locale, setSize)};
            renderResultLine(
                    localizeString(locale, LOCALIZED_STRING_CODE_PALINDROME_RESULTS_LEAD, args,
                            "Here are the resulting " + setSize + " palindromes:"));
            for (String foundPalindrome : pFoundResults) {
                renderResultLine("  - " + foundPalindrome);
            }
        }
    }

    /**
     * @param pNumberOfElements the number of raw elements that we'll use to find all possible permutations
     * @return the potential number of permutations for the given number of items.  Returns 0 if given number
     * is <= 0.
     */
    private static long getPotentialPermutationsQuantity(final int pNumberOfElements) {
        long returnVal = 0;
        if (pNumberOfElements > 0) {
            for (int slots = 1; slots <= pNumberOfElements; slots++) {
                returnVal += getFactorialWithSlots(pNumberOfElements, slots);
            }
        }
        return returnVal;
    }

    /**
     * Calculate the number of potential permutations for N items in X slots,
     * or N! / (N! - X!),
     * or Number! / (Number - Slots)!
     *
     * @param pNumber must be a positive integer
     * @param pSlots  must be a positive integer
     * @return the number of permutations calculated for the inputs.
     */
    private static long getFactorialWithSlots(final int pNumber, final int pSlots) {
        if (pSlots > pNumber) {
            throw new IllegalArgumentException("given slots may not be larger than the given number of items");
        }
        if (pSlots <= 0) {
            throw new IllegalArgumentException("given slots must be a positive integer");
        }
        if (pNumber <= 0) {
            throw new IllegalArgumentException("given number of items must be a positive integer");
        }

        long returnVal = 1;
        if (pNumber > 1) {
            for (int i = 0; i < pSlots; i++) {
                returnVal *= (pNumber - i);
            }
        }
        return returnVal;
    }

    /**
     * Render the line to the user.  At present, simply renders the text to System.out.
     *
     * @param pLocalizedString a String to render to the user
     */
    private static void renderResultLine(final String pLocalizedString) {
        // Refactor this, depending on how we wish to render the data to the user
        System.out.println(pLocalizedString);
    }

    /**
     * Render the give message to an internal log.  At present, simply renders the text to System.out.
     *
     * @param pLoggingString a String to render to our logging mechanism
     */
    private static void renderLoggingLine(final String pLoggingString) {
        // Refactor this, depending on how we render logging messages
        System.out.println("Logging Message: " + pLoggingString);
    }

    /**
     * Get the correct locale to use for messages presented to (potential multilingual) user
     *
     * @return the system default locale, for now.  Not yet fully implemented
     */
    private static Locale getLocale() {
        // Not yet fully implemented; use the application standard for determining the user's locale
        return Locale.getDefault();
    }

    /**
     * Not yet full implemented.  could go to the proper resource bundle to get a localized message
     *
     * @param pLocale  not yet used
     * @param pCode    not yet used
     * @param pArgs    not yet used
     * @param pDefault the default value to use, if no matching item found in the resource bundle
     * @return the given default value, for now, until properly implemented
     */
    private static String localizeString(Locale pLocale, String pCode, Object[] pArgs, String pDefault) {
        // not yet fully implemented; will just return the default.
        return pDefault;

    }

    /**
     * Not yet fully implemented.  Return a localized rendering of the number.
     *
     * @param pLocale
     * @param pNumber
     * @return a localized version of the given number.
     */
    private static String localizeNumber(Locale pLocale, long pNumber) {
        // not yet fully implemented; will just return a String version of the number
        return String.valueOf(pNumber);

    }


}
