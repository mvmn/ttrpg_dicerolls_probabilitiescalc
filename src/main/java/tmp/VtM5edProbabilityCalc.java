package tmp;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class VtM5edProbabilityCalc {

    public static void main(String[] args) throws Exception {
        exportCSV();
    }

    public static void exportCSV() throws IOException {
        int maxDice = 20;
        int maxDifficulty = 40;
        double[][] table = new double[maxDice + 1][maxDifficulty + 1];
        DecimalFormat df = new DecimalFormat("#.####################");

        for (int dice = 1; dice <= maxDice; dice++) {
            for (int diff = 1; diff <= maxDifficulty; diff++) {
                table[dice][diff] = calculateProbability(dice, diff);
            }
        }

        // Write CSV
        try (FileWriter writer = new FileWriter("vtm_5th_ed.csv")) {
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

        System.out.println("Corrected CSV created.");
    }

    static double calculateProbability(int nDice, int difficulty) {
        double totalProbability = 0.0;

        for (int num10s = 0; num10s <= nDice; num10s++) {
            for (int numSuccesses = 0; numSuccesses <= nDice - num10s; numSuccesses++) {
                int numFailures = nDice - num10s - numSuccesses;
                if (numFailures < 0)
                    continue;

                // Calculate probability using multinomial formula
                double prob = multinomialProbability(nDice, num10s, numSuccesses, numFailures);

                int criticalBonus = 2 * (num10s / 2); // +2 for every pair of 10s
                int totalSuccesses = numSuccesses + num10s + criticalBonus;

                if (totalSuccesses >= difficulty) {
                    totalProbability += prob;
                }
            }
        }

        return totalProbability;
    }

    static double multinomialProbability(int n, int num10s, int numSuccesses, int numFailures) {
        double p10 = 0.1;
        double pSuccess = 0.4;
        double pFail = 0.5;

        long multinomialCoeff = factorial(n) / (factorial(num10s) * factorial(numSuccesses) * factorial(numFailures));

        return multinomialCoeff * Math.pow(p10, num10s) * Math.pow(pSuccess, numSuccesses)
                * Math.pow(pFail, numFailures);
    }

    static long factorial(int n) {
        long result = 1;
        for (int i = 2; i <= n; i++)
            result *= i;
        return result;
    }
}
