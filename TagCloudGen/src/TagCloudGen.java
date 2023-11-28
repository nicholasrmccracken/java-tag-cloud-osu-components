import java.util.Comparator;

import components.map.Map;
import components.map.Map1L;
import components.queue.Queue;
import components.queue.Queue1L;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * Counts the word occurrences in a given input file and outputs an HTML
 * document with a table of the words and their respective counts in
 * alphabetical order. Project built from a direct copy of the SW1 Glossary
 * Project.
 *
 * @author Nicholas McCracken
 *
 */
public final class TagCloudGen {

    /**
     * Compare {@code String}s in lexicographic order. Reused from SW1 Glossary.
     */
    private static class StringLT implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
        }
    }

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private TagCloudGen() {
    }

    /**
     * Generates the set of characters in the given {@code str} into the given
     * {@code charSet}. Reused from SW1 Glossary.
     *
     * @param str
     *            the given {@code String}
     * @param charSet
     *            the {@code Set} to be replaced
     * @replaces charSet
     * @ensures charSet = entries(str)
     */
    private static void generateElements(String str, Set<Character> charSet) {
        assert str != null : "Violation of: str is not null";
        assert charSet != null : "Violation of: charSet is not null";

        /*
         * Input each character of the string as a separate element in the
         * temporary set, then replace the formal parameter charSet with the
         * elements from the temporary set.
         */
        Set<Character> strEntries = charSet.newInstance();
        for (int i = 0; i < str.length(); i++) {
            strEntries.add(str.charAt(i));
        }
        charSet.transferFrom(strEntries);
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}. Reused from SW1 Glossary.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        /*
         * Determine if first character is word or separator which determines if
         * the string wordOrSeparator will contain a word or separators.
         */
        String wordOrSeparator = "";
        boolean initialCharacterIsSeparator = separators
                .contains(text.charAt(position));
        int i = position;

        /*
         * Continuously add characters to the string wordOrSeparator until the
         * end of the formal parameter text is reached or the current character
         * is not of the same type (word or separator) as the initial character.
         */
        while (i < text.length() && initialCharacterIsSeparator == separators
                .contains(text.charAt(i))) {
            wordOrSeparator += text.charAt(i);
            i++;
        }
        return wordOrSeparator;
    }

    /**
     * Inputs a list of terms and their definitions from the given file and
     * stores them in the given {@code wordCountMap}. Redesigned from SW1
     * Glossary.
     *
     * @param inputFile
     *            the name of the input file
     * @param wordCountMap
     *            the {@code Map} of unique words and their number of
     *            occurrences
     * @replaces wordCountMap
     * @requires <pre>
     * [file named inputFile exists but is not open, and consists of English
     *  words following standard grammatical convention]
     * </pre>
     * @ensures [wordCountMap contains unique words and their number of
     *          occurrences from the file]
     */
    private static void countWords(String inputFile,
            Map<String, Integer> wordCountMap) {
        assert inputFile != null : "Violation of: inputFile is not null";
        assert wordCountMap != null : "Violation of: termMap is not null";

        /*
         * Open an input stream to read from the file and a set to store all
         * characters that are not present in valid words.
         */
        SimpleReader inFile = new SimpleReader1L(inputFile);
        Set<Character> separators = new Set1L<Character>();
        generateElements(" `~!@#$%^&*()_-+={[]}|\\;:\",./?<>", separators);

        /*
         * Store each word and it's number of occurrences in the map until the
         * end of the file is reached.
         */
        while (!inFile.atEOS()) {
            String text = inFile.nextLine();
            int position = 0;

            while (position < text.length()) {
                String wordOrSeparator = nextWordOrSeparator(text, position,
                        separators);
                position += wordOrSeparator.length();

                /*
                 * Determine if return from wordOrSeparator is a valid word that
                 * should be stored by checking that it's first character is not
                 * a separator.
                 */
                if (!separators.contains(wordOrSeparator.charAt(0))) {
                    /*
                     * Update the occurrence count of the word if it already
                     * exists in the map, and add to the map if it does not.
                     */
                    if (wordCountMap.hasKey(wordOrSeparator)) {
                        int count = wordCountMap.value(wordOrSeparator);
                        wordCountMap.replaceValue(wordOrSeparator, count + 1);
                    } else {
                        wordCountMap.add(wordOrSeparator, 1);
                    }
                }
            }
        }
        // Close file input stream.
        inFile.close();
    }

    /**
     * Inputs a list of terms and their definitions from the given file and
     * stores them alphabetically in the given {@code Queue}. Redesigned from
     * SW1 Glossary.
     *
     * @param wordCountMap
     *            the {@code Map} of unique words and their number of
     *            occurrences
     * @param wordQueue
     *            the {@code Queue} of unique words in alphabetical order
     * @replaces termQueue
     * @requires <pre>
     * [file named fileName exists but is not open, and has the
     *  format of one term (unique in the file) on a line followed by it's
     *  definition on the next line and empty lines separating each term
     *  definition pair]
     * </pre>
     * @ensures [Queue contains terms ordered alphabetically -> term queueing
     *          from Map]
     */
    private static void alphabetizeWords(Map<String, Integer> wordCountMap,
            Queue<String> wordQueue) {
        assert wordCountMap != null : "Violation of: wordCountMap is not null";
        assert wordQueue != null : "Violation of: wordQueue is not null";

        /*
         * Store each unique word from the map in the queue until the end of the
         * file is reached, then close the input stream.
         */
        for (Map.Pair<String, Integer> pair : wordCountMap) {
            wordQueue.enqueue(pair.key());
        }

        // Use comparator to sort the words in alphabetical order.
        Comparator<String> alphabetize = new StringLT();
        wordQueue.sort(alphabetize);
    }

    /**
     * Generates an HTML file which lists each word from an input file in an
     * alphabetized table, along with it's number of occurrences in said file.
     * Redesigned from SW1 Glossary.
     *
     * @param outputFile
     *            the name of the output file
     * @param wordCountMap
     *            the {@code Map} of unique words and their number of
     *            occurrences
     * @param wordQueue
     *            the {@code Queue} of unique words in alphabetical order
     * @clears wordQueue
     * @ensures <pre>
     * [generates HTML file with each unique word from the input file in an
     * alphabetized table along with it's number of occurrences in said file]
     * </pre>
     */
    private static void generateWordCountTable(String outputFile,
            Map<String, Integer> wordCountMap, Queue<String> wordQueue) {
        assert outputFile != null : "Violation of: outputFile is not null";
        assert wordCountMap != null : "Violation of: wordCountMap is not null";
        assert wordQueue != null : "Violation of: wordQueue is not null";

        // Open an output stream to write to a file stored in folder.
        SimpleWriter fileOut = new SimpleWriter1L(outputFile);

        // Create opening tags including a title, heading, and table heading.
        fileOut.println("<html>");
        fileOut.println("<head>");
        fileOut.println("<title>Word Counter</title>");
        fileOut.println("<style>");
        fileOut.println("table, th, td {border: 1px solid black;}");
        fileOut.println("</style>");
        fileOut.println("</head>");
        fileOut.println("<body>");
        fileOut.println("<h1>Words Counted in " + outputFile + "</h1>");
        fileOut.println("<hr>");
        fileOut.println("<table>");
        fileOut.println("<tr>");
        fileOut.println("<th>Words</th>");
        fileOut.println("<th>Counts</th>");
        fileOut.println("</tr>");

        /*
         * For each word stored in alphabetizedWords, add a row to the table
         * including the word itself and it's number of occurrences in the file.
         */
        while (wordQueue.length() > 0) {
            String word = wordQueue.dequeue();

            fileOut.println("<tr>");
            fileOut.print("<td>");
            fileOut.print(word);
            fileOut.println("</td>");
            fileOut.print("<td>");
            fileOut.print(wordCountMap.value(word));
            fileOut.println("</td>");
            fileOut.println("</tr>");
        }

        // Close all opened tags and output stream.
        fileOut.println("</table>");
        fileOut.println("</body>");
        fileOut.println("</html>");
        fileOut.close();
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        // Open input and output streams to console.
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        /*
         * Prompt user for the name of an input file to read words from and an
         * output folder to create a table of all the word's and their
         * respective number of occurrences in.
         */
        out.print("Enter the name "
                + "of an input file and it's path with a .txt extension: ");
        String inputFile = in.nextLine();
        out.print("Enter the name "
                + "of an output file and it's path with a .html extension: ");
        String outputFile = in.nextLine();

        // Store all words and their respective counts from input file in a map.
        Map<String, Integer> wordCountMap = new Map1L<String, Integer>();
        countWords(inputFile, wordCountMap);

        // Store all words from map in an alphabetized queue.
        Queue<String> wordQueue = new Queue1L<String>();
        alphabetizeWords(wordCountMap, wordQueue);

        /*
         * Generates an HTML file which lists each word from an input file in an
         * alphabetized table, along with it's number of occurrences in said
         * file.
         */
        generateWordCountTable(outputFile, wordCountMap, wordQueue);

        // Close input and output streams.
        in.close();
        out.close();
    }
}
