package tmp;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class VtM20thAnnEdProbabilityCalcOptimized {
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
            for (int diff = 2; diff <= maxDifficulty; diff++) {
                table[dice][diff] = calculateProbability(dice, diff);
            }
        }

        try (FileWriter writer = new FileWriter("vtm_20thanned.csv")) {
            writer.append("Difficulty/Dice");
            for (int d = 1; d <= maxDice; d++) {
                writer.append(",").append(String.valueOf(d));
            }
            writer.append("\n");

            for (int diff = 2; diff <= maxDifficulty; diff++) {
                writer.append(String.valueOf(diff));
                for (int dice = 1; dice <= maxDice; dice++) {
                    writer.append(",").append(df.format(table[dice][diff]));
                }
                writer.append("\n");
            }
        }
    }

    static double calculateProbability(int nDice, int difficulty) {
        double pSuccess = (10 - difficulty + 1) / 10.0;
        double pOne = 0.1;
        double pFail = Math.max(0.0, 1.0 - pSuccess - pOne);

        double totalProbability = 0.0;

        for (int numSuccesses = 0; numSuccesses <= nDice; numSuccesses++) {
            for (int numOnes = 0; numOnes <= nDice - numSuccesses; numOnes++) {
                int numFailures = nDice - numSuccesses - numOnes;

                if (pFail == 0.0 && numFailures > 0) continue;

                int netSuccesses = numSuccesses - numOnes;

                if (netSuccesses > 0) {
                    double prob = multinomialProbability(nDice, numSuccesses, numOnes, numFailures,
                            pSuccess, pOne, pFail);
                    if (prob > 0)
                        totalProbability += prob;
                }
            }
        }

        return totalProbability;
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
        double pSuccess = (10 - difficulty + 1) / 10.0;
        double pOne = 0.1;
        double pFail = Math.max(0.0, 1.0 - pSuccess - pOne);

        double botchProbability = 0.0;

        for (int numSuccesses = 0; numSuccesses <= nDice; numSuccesses++) {
            for (int numOnes = 1; numOnes <= nDice - numSuccesses; numOnes++) {
                int numFailures = nDice - numSuccesses - numOnes;

                if (pFail == 0.0 && numFailures > 0) continue;

                int netSuccesses = numSuccesses - numOnes;

                if (netSuccesses <= 0) {
                    double prob = multinomialProbability(nDice, numSuccesses, numOnes, numFailures,
                            pSuccess, pOne, pFail);
                    botchProbability += prob;
                }
            }
        }

        return botchProbability;
    }

    static double multinomialProbability(int n, int numSuccesses, int numOnes, int numFailures,
                                         double pSuccess, double pOne, double pFail) {
        if (pSuccess < 0 || pOne < 0 || pFail < 0) return 0.0;

        double coeff = 1.0;

        // Multiplicative multinomial coefficient
        for (int i = 1; i <= n; i++) {
            coeff *= i;
        }
        for (int i = 1; i <= numSuccesses; i++) {
            coeff /= i;
        }
        for (int i = 1; i <= numOnes; i++) {
            coeff /= i;
        }
        for (int i = 1; i <= numFailures; i++) {
            coeff /= i;
        }

        return coeff * Math.pow(pSuccess, numSuccesses)
                * Math.pow(pOne, numOnes)
                * Math.pow(pFail, numFailures);
    }
}

