package tmp;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class VtM5edProbabilityCalcOptimized {

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

                double prob = multinomialProbabilityBD(nDice, num10s, numSuccesses, numFailures, difficulty).doubleValue();

                int criticalBonus = 2 * (num10s / 2);
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

        long multinomialCoeff = multinomialCoefficient(n, num10s, numSuccesses, numFailures);

        return multinomialCoeff * Math.pow(p10, num10s) * Math.pow(pSuccess, numSuccesses)
                * Math.pow(pFail, numFailures);
    }

    static long multinomialCoefficient(int n, int k1, int k2, int k3) {
        long result = 1;
        int[] ks = { k1, k2, k3 };
        int sum = 0;

        for (int k : ks) {
            for (int i = 1; i <= k; i++) {
                result *= (n - sum);
                result /= i;
                sum++;
            }
        }
        return result;
    }

    static BigDecimal multinomialProbabilityBD(int n, int numSuccesses, int numOnes, int numFailures, int difficulty) {

        MathContext mc = new MathContext(50, RoundingMode.HALF_UP);

        BigDecimal pSuccess = BigDecimal.valueOf(10 - difficulty + 1).divide(BigDecimal.TEN, mc);
        BigDecimal pOne = new BigDecimal("0.1");
        BigDecimal pFail = BigDecimal.ONE.subtract(pSuccess, mc).subtract(pOne, mc);

        if (pSuccess.compareTo(BigDecimal.ZERO) < 0 || pFail.compareTo(BigDecimal.ZERO) < 0)
            return BigDecimal.ZERO;

        BigDecimal coeff = multinomialCoefficientBD(n, numSuccesses, numOnes, numFailures);

        return coeff.multiply(pSuccess.pow(numSuccesses, mc), mc)
                .multiply(pOne.pow(numOnes, mc), mc)
                .multiply(pFail.pow(numFailures, mc), mc);
    }

    static BigDecimal multinomialCoefficientBD(int n, int... ks) {
        BigDecimal result = BigDecimal.ONE;
        int sum = 0;

        for (int k : ks) {
            for (int i = 1; i <= k; i++) {
                result = result.divide(BigDecimal.valueOf(i), MathContext.DECIMAL128);
            }
            sum += k;
        }

        for (int i = 2; i <= sum; i++) {
            result = result.multiply(BigDecimal.valueOf(i));
        }

        return result;
    }

}
