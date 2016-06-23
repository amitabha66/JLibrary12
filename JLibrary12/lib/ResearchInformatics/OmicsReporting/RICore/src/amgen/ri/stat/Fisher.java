package amgen.ri.stat;

/**
 * Fisher�s Exact Test
 * Perform a Fisher's exact test on a 2 x 2 contingency table.
 * Input 2 x 2 contingency table:
 *     n11    n12
 *     n21    n22
 * Returned is an array with the following elements:
 * [0] left one-tailed test
 * [1] right one-tailed test
 * [2] two-tailed test
 *
 * @author Jeffrey McDowell
 * @version 1.0
 */
public class Fisher {
    private double sn11 = 0;
    private double sn1_ = 0;
    private double sn_1 = 0;
    private double sn = 0;
    private double sprob = 0;

    private double sleft;
    private double sright;
    private double sless;
    private double slarg;

    private double left;
    private double right;
    private double twotail;
    private double n11old = -1;
    private double n12old = -1;
    private double n21old = -1;
    private double n22old = -1;

    public Fisher() {}

    /**
     * Perform a Fisher's exact test on a 2 x 2 contingency table.
     * Input 2 x 2 contingency table:
     *     n11    n12
     *     n21    n22
     * Returned is an array with the following elements:
     * [0] left one-tailed test
     * [1] right one-tailed test
     * [2] two-tailed test
     */
    public static double[] exactTest(int n11, int n12, int n21, int n22) {
        return new Fisher().exact22(n11, n12, n21, n22);
    }

    private double hyper_323(double n11, double n1_, double n_1, double n) {
        return (Math.exp(ExtMath.lnbico(n1_, n11) + ExtMath.lnbico(n - n1_, n_1 - n11) - ExtMath.lnbico(n, n_1)));
    }

    private double hyper(double n11) {
        return (hyper0(n11, 0, 0, 0));
    }

    /**
     * Hypergeometric function
     * @param n11i
     * @param n1_i
     * @param n_1i
     * @param ni
     * @return
     */
    private double hyper0(double n11i, int n1_i, int n_1i, int ni) {

        if ( (n1_i | n_1i | ni) == 0) {
            if (! (n11i % 10 == 0)) {
                if (n11i == sn11 + 1) {
                    sprob *= ( (sn1_ - sn11) / (n11i)) * ( (sn_1 - sn11) / (n11i + sn - sn1_ - sn_1));
                    sn11 = n11i;
                    return sprob;
                }
                if (n11i == sn11 - 1) {
                    sprob *= ( (sn11) / (sn1_ - n11i)) * ( (sn11 + sn - sn1_ - sn_1) / (sn_1 - n11i));
                    sn11 = n11i;
                    return sprob;
                }
            }
            sn11 = n11i;
        } else {
            sn11 = n11i;
            sn1_ = n1_i;
            sn_1 = n_1i;
            sn = ni;
        }
        sprob = hyper_323(sn11, sn1_, sn_1, sn);
        return sprob;
    }

    private double exact(double n11, int n1_, int n_1, int n) {
        double p, i, j, prob;
        double max = n1_;
        if (n_1 < max) {
            max = n_1;
        }
        double min = n1_ + n_1 - n;
        if (min < 0) {
            min = 0;
        }
        if (min == max) {
            sless = 1;
            sright = 1;
            sleft = 1;
            slarg = 1;
            return 1;
        }
        prob = hyper0(n11, n1_, n_1, n);
        sleft = 0;
        p = hyper(min);
        for (i = min + 1; p < 0.99999999 * prob; i++) {
            sleft += p;
            p = hyper(i);
        }
        i--;
        if (p < 1.00000001 * prob) {
            sleft += p;
        } else {
            i--;
        }
        sright = 0;
        p = hyper(max);
        for (j = max - 1; p < 0.99999999 * prob; j--) {
            sright += p;
            p = hyper(j);
        }
        j++;
        if (p < 1.00000001 * prob) {
            sright += p;
        } else {
            j++;
        }
        if (Math.abs(i - n11) < Math.abs(j - n11)) {
            sless = sleft;
            slarg = 1 - sleft + prob;
        } else {
            sless = 1 - sright + prob;
            slarg = sright;
        }
        return prob;
    }

    /**
     * Perform a Fisher's exact test on a 2 x 2 contingency table.
     * Input 2 x 2 contingency table:
     *     n11    n12
     *     n21    n22
     * Returned is an array with the following elements:
     * [0] left one-tailed test
     * [1] right one-tailed test
     * [2] two-tailed test
     */
    private double[] exact22(int n11, int n12, int n21, int n22) {
        int n11_ = n11;
        int n12_ = n12;
        int n21_ = n21;
        int n22_ = n22;
        if (n11_ < 0) {
            n11_ *= -1;
        }
        if (n12_ < 0) {
            n12_ *= -1;
        }
        if (n21_ < 0) {
            n21_ *= -1;
        }
        if (n22_ < 0) {
            n22_ *= -1;
        }
        if (n11old == n11_ && n12old == n12_ && n21old == n21_ && n22old == n22_) {
            return new double[0];
        }
        n11old = n11_;
        n12old = n12_;
        n21old = n21_;
        n22old = n22_;
        int n1_ = n11_ + n12_;
        int n_1 = n11_ + n21_;
        int n = n11_ + n12_ + n21_ + n22_;
        double prob = exact(n11_, n1_, n_1, n);
        left = sless;
        right = slarg;
        twotail = sleft + sright;
        if (twotail > 1) {
            twotail = 1;
        }
        return new double[] {
            left, right, twotail};
    }

    public static void main(String[] args) {
        //double[] r= new Fisher().exact22(3, 1, 1, 3);
        double[] r = new Fisher().exact22(1, 9, 11, 3);
        System.out.println(
            "Left   : p-value = " + r[0] + "\n" +
            "Right  : p-value = " + r[1] + "\n" +
            "2-Tail : p-value = " + r[2]);
    }

}
