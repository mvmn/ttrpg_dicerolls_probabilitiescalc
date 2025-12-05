package tmp;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;

public class VtM20thAnnEdProbabilityCalc {

    public static void main(String[] args) throws Exception {
        exportCSV();
        exportBotchCSV();
    }

    public static void exportCSV() throws IOException {
        int maxDice = 40;
        int maxDifficulty = 10;
        double[][] table = new double[maxDice + 1][maxDifficulty + 1];
        DecimalFormat df = new DecimalFormat("#.####################");

        for (int dice = 1; dice <= maxDice; dice++) {
            for (int diff = 1; diff <= maxDifficulty; diff++) {
                table[dice][diff] = calculateProbability(dice, diff);
            }
        }

        // Write CSV
        try (FileWriter writer = new FileWriter("vtm_20thanned.csv")) {
            writer.append("Difficulty/Dice");
            for (int d = 1; d <= maxDice; d++) {
                writer.append(",").append(String.valueOf(d));
            }
            writer.append("\n");

            for (int diff = 1; diff <= maxDifficulty; diff++) {
                writer.append(String.valueOf(diff));
                for (int dice = 1; dice <= maxDice; dice++) {
                    writer.append(",").append(df.format(table[dice][diff]));
                }
                writer.append("\n");
            }
        }
    }

    static double calculateProbability(int nDice, int difficulty) {
        if (difficulty < 2 || difficulty > 10)
            return 0.0;

        double totalProbability = 0.0;

        for (int numSuccesses = 0; numSuccesses <= nDice; numSuccesses++) {
            for (int numOnes = 0; numOnes <= nDice - numSuccesses; numOnes++) {
                int numFailures = nDice - numSuccesses - numOnes;

                int netSuccesses = numSuccesses - numOnes;

                if (netSuccesses > 0) {
                    double prob = multinomialProbability(nDice, numSuccesses, numOnes, numFailures, difficulty);
                    if (prob > 0)
                        totalProbability += prob;
                }
            }
        }

        return totalProbability;
    }

    static double multinomialProbability(int n, int numSuccesses, int numOnes, int numFailures, int difficulty) {
        double pSuccess = (10 - difficulty + 1) / 10.0;
        double pOne = 0.1;
        double pFail = Math.max(0.0, 1.0 - pSuccess - pOne);

        if (pFail < 0 || pSuccess < 0)
            return 0.0;

        BigInteger numerator = factorial(n);
        BigInteger denominator = factorial(numSuccesses).multiply(factorial(numOnes)).multiply(factorial(numFailures));

        BigInteger coefficient = numerator.divide(denominator);

        // Convert coefficient to double (safe for values < ~1e308)
        double coeff = new BigDecimal(coefficient).doubleValue();

        return coeff * Math.pow(pSuccess, numSuccesses) * Math.pow(pOne, numOnes) * Math.pow(pFail, numFailures);
    }

    static BigInteger factorial(int n) {
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }

    public static void exportBotchCSV() throws IOException {
        int maxDice = 40;
        int maxDifficulty = 10;
        double[][] botchTable = new double[maxDice + 1][maxDifficulty + 1];
        DecimalFormat df = new DecimalFormat("#.####################");

        for (int dice = 1; dice <= maxDice; dice++) {
            for (int diff = 2; diff <= maxDifficulty; diff++) {
                botchTable[dice][diff] = calculateBotchProbability(dice, diff);
            }
        }

        try (FileWriter writer = new FileWriter("vtm_20th_botch_probabilities.csv")) {
            writer.append("Difficulty/Dice");
            for (int d = 1; d <= maxDice; d++) {
                writer.append(",").append(String.valueOf(d));
            }
            writer.append("\n");

            for (int diff = 2; diff <= maxDifficulty; diff++) {
                writer.append(String.valueOf(diff));
                for (int dice = 1; dice <= maxDice; dice++) {
                    writer.append(",").append(df.format(botchTable[dice][diff]));
                }
                writer.append("\n");
            }
        }
    }

    static double calculateBotchProbability(int nDice, int difficulty) {
        double botchProbability = 0.0;

        // VtM 20th: 1s cancel successes. Botch occurs if net successes == 0 and there
        // is at least one 1.
        for (int numSuccesses = 0; numSuccesses <= nDice; numSuccesses++) {
            for (int numOnes = 1; numOnes <= nDice - numSuccesses; numOnes++) { // must be ≥1 one
                int numFailures = nDice - numSuccesses - numOnes;

                int netSuccesses = numSuccesses - numOnes;

                if (netSuccesses < 0) {
                    double prob = multinomialBotchProbability(nDice, numSuccesses, numOnes, numFailures, difficulty);
                    botchProbability += prob;
                }
            }
        }

        return botchProbability;
    }

    static double multinomialBotchProbability(int n, int numSuccesses, int numOnes, int numFailures, int difficulty) {
        double pSuccess = (10 - difficulty + 1) / 10.0; // [difficulty..10]
        double pOne = 0.1; // rolls of 1
        double pFail = 1.0 - pSuccess - pOne; // all others

        if (pFail < 0)
            return 0.0;

        BigInteger multinomialCoeff = factorial(n).divide(factorial(numSuccesses))
                .divide(factorial(numOnes))
                .divide(factorial(numFailures));

        return multinomialCoeff.doubleValue() * Math.pow(pSuccess, numSuccesses) * Math.pow(pOne, numOnes)
                * Math.pow(pFail, numFailures);
    }
}
