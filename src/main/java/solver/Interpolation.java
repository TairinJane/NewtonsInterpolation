package solver;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Interpolation {

    static double[][] getDiffTable(double[] x, double[][] y) {

        for (int j = 1; j < x.length; j++) {
            for (int i = 0; i < x.length - j; i++) {
                y[i][j] = (y[i + 1][j - 1] - y[i][j - 1]) / (x[i + j] - x[i]);
                System.out.println(String.format("y[%d][%d] = (%f - %f) / (%f - %f)",
                        i, j, y[i + 1][j - 1], y[i][j - 1], x[i + j], x[i]));
            }
        }
        return y;
    }

    static double getNthTerm(int n, double value, double[] x) {
        double term = 1f;
        for (int i = 0; i < n; i++) {
            System.out.print("*(" + value + " - " + x[i] + ")");
            term *= value - x[i];
        }
        return term;
    }

    static double getInterpolationY(double value, double[][] differences, double[] x) {
        double y = differences[0][0];
        System.out.print("y = " + y);
        for (int i = 1; i < x.length; i++) {
            System.out.print(" + " + differences[0][i]);
            y += differences[0][i] * getNthTerm(i, value, x);
        }
        return y;
    }


    static void printDiffTable(double[][] y, int n) {
        DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.setMaximumFractionDigits(5);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n - i; j++) {
                System.out.print(String.format("%-10s", df.format(y[i][j])));
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        double value = 11;
        double[][] y = new double[10][10];
        double[] x = {5, 6, 9, 11};

        y[0][0] = 12;
        y[1][0] = 13;
        y[2][0] = 14;
        y[3][0] = 16;

        getDiffTable(x, y);

        printDiffTable(y, x.length);

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_UP);

        for (double i = x[0]; i <= x[x.length - 1]; i+=1) {
            System.out.println("\nValue at " + df.format(i) + " is "
                    + df.format(getInterpolationY(i, y, x)));
        }
    }
}
