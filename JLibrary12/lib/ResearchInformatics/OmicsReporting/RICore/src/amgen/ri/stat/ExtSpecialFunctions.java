package amgen.ri.stat;

/**
 * Special function calculations
 *
 * @version $Id: ExtSpecialFunctions.java,v 1.1 2011/10/26 04:13:32 cvs Exp $
 */
public class ExtSpecialFunctions {
  static final double ITMAX = 1000; //Maximum allowed number of iterations.
  static final double EPS = 3.0e-7; //Relative accuracy.
  static final double FPMIN = 1.0e-30; //Number near the smallest representable double-precision number.

  /**
   * Calculates the standard normal cumulative distribution function.
   *
   *  The distribution has a mean of 0 (zero) and a standard deviation of one. Use
   * this function in place of a table of standard normal curve areas.
   *
   * @param x double
   * @return double
   */
  public static double normsdist2(double x) {
    if (x <= 0) {
      double erfValue = erffc( -x / Math.sqrt(2));
      return erfValue / 2;
    } else {
      double erfValue = erffc(x / Math.sqrt(2));
      return 1 - erfValue / 2;
    }
  }

  /**
   * Calculates the inverse of the standard normal cumulative distribution.
   *
   * The distribution has a mean of zero and a standard deviation of one.
   *
   * Also described below-
   * Lower tail quantile for standard normal distribution function.
   *
   * This function returns an approximation of the inverse cumulative
   * standard normal distribution function.  I.e., given P, it returns
   * an approximation to the X satisfying P = Pr{Z <= X} where Z is a
   * random variable from the standard normal distribution.
   *
   * The algorithm uses a minimax approximation by rational functions
   * and the result has a relative error whose absolute value is less
   * than 1.15e-9.
   *
   * Author:      Peter J. Acklam
   * (Javascript version by Alankar Misra @ Digital Sutras (alankar@digitalsutras.com))
   * Time-stamp:  2003-05-05 05:15:14
   * E-mail:      pjacklam@online.no
   * WWW URL:     http:*home.online.no/~pjacklam

   * An algorithm with a relative error less than 1.15*10-9 in the entire region.
   */
  public static double normsinv(double p) {
      // Coefficients in rational approximations
      double[] a = new double[] {
          -3.969683028665376e+01, 2.209460984245205e+02,
          -2.759285104469687e+02, 1.383577518672690e+02,
          -3.066479806614716e+01, 2.506628277459239e+00};

      double[] b = new double[] {
          -5.447609879822406e+01, 1.615858368580409e+02,
          -1.556989798598866e+02, 6.680131188771972e+01,
          -1.328068155288572e+01};

      double[] c = new double[] {
          -7.784894002430293e-03, -3.223964580411365e-01,
          -2.400758277161838e+00, -2.549732539343734e+00,
          4.374664141464968e+00, 2.938163982698783e+00};

      double[] d = new double[] {
          7.784695709041462e-03, 3.224671290700398e-01,
          2.445134137142996e+00, 3.754408661907416e+00};

      // Define break-points.
      double plow = 0.02425;
      double phigh = 1 - plow;

      // Rational approximation for lower region:
      if (p < plow) {
          double q = Math.sqrt( -2 * Math.log(p));
          return ( ( ( ( (c[0] * q + c[1]) * q + c[2]) * q + c[3]) * q + c[4]) * q + c[5]) /
              ( ( ( (d[0] * q + d[1]) * q + d[2]) * q + d[3]) * q + 1);
      }

      // Rational approximation for upper region:
      if (phigh < p) {
          double q = Math.sqrt( -2 * Math.log(1 - p));
          return - ( ( ( ( (c[0] * q + c[1]) * q + c[2]) * q + c[3]) * q + c[4]) * q + c[5]) /
              ( ( ( (d[0] * q + d[1]) * q + d[2]) * q + d[3]) * q + 1);
      }

      // Rational approximation for central region:
      double q = p - 0.5;
      double r = q * q;
      return ( ( ( ( (a[0] * r + a[1]) * r + a[2]) * r + a[3]) * r + a[4]) * r + a[5]) * q /
          ( ( ( ( (b[0] * r + b[1]) * r + b[2]) * r + b[3]) * r + b[4]) * r + 1);
    }

  /**
   * Calculates the error function, erf(x)
   *
   * Ported from Numerical Recipes in C, 2nd Ed.
   *
   * @param x double
   * @return double
   */
  public static double erff(double x) {
    double gammaP = gammp(0.5, x * x);

    if (Double.isNaN(gammaP)) {
      return Double.NaN;
    }
    //return x < 0.0 ? -gammp(0.5, x * x) : gammp(0.5, x * x);
    return x < 0.0 ? -gammaP : gammaP;
  }

  /**
   * Calculates the complementary error function, erffc(x)
   *
   * Ported from Numerical Recipes in C, 2nd Ed.
   *
   * @param x double
   * @return double
   */
  public static double erffc(double x) {
    double gammaP = gammp(0.5, x * x);
    double gammaQ = gammq(0.5, x * x);

    if (Double.isNaN(gammaP) || Double.isNaN(gammaQ)) {
      return Double.NaN;
    }
    //return x < 0.0 ? 1.0 + gammp(0.5, x * x) : gammq(0.5, x * x);
    return x < 0.0 ? 1.0 + gammaP : gammaQ;
  }

  /**
   * Calculates natural log of the gamma function- ln(gamma)
   *
   * Ported from Numerical Recipes in C, 2nd Ed.
   *
   * @param xx double
   * @return double
   */
  public static double gammln(double xx) {
    double x, y, tmp, ser;
    double[] cof = {
        76.18009172947146, -86.50532032941677,
        24.01409824083091, -1.231739572450155,
        0.1208650973866179e-2, -0.5395239384953e-5};
    int j;
    y = x = xx;
    tmp = x + 5.5;
    tmp -= (x + 0.5) * Math.log(tmp);
    ser = 1.000000000190015;
    for (j = 0; j <= 5; j++) {
      ser += cof[j] / ++y;
    }
    return -tmp + Math.log(2.5066282746310005 * ser / x);
  }

  /**
   * Calculates the incomplete gamma function P(a,x)
   *
   * Ported from Numerical Recipes in C, 2nd Ed.
   *
   * @param a double
   * @param x double
   * @return double
   */
  public static double gammp(double a, double x) throws ArithmeticException {
    double gamser, gammcf, gln;
    if (x < 0.0 || a <= 0.0) {
        throw new ArithmeticException("Invalid arguments in routine gammp");
    }
    if (x < (a + 1.0)) {
      gamser = gser(a, x);
      if (Double.isNaN(gamser)) {
        return Double.NaN;
      }
      return gamser;
    } else {
      gammcf = gcf(a, x);
      if (Double.isNaN(gammcf)) {
        return Double.NaN;
      }
      return 1.0 - gammcf;
    }
  }

  /**
   * Calculates the incomplete gamma function Q(a, x) ~ 1- P(a,x)
   *
   * Ported from Numerical Recipes in C, 2nd Ed.
   *
   * @param a double
   * @param x double
   * @return double
   */
  public static double gammq(double a, double x) throws ArithmeticException {
    double gamser, gammcf, gln;
    if (x < 0.0 || a <= 0.0) {
      throw new ArithmeticException("Invalid arguments in routine gammq");
    }
    if (x < (a + 1.0)) {
      gamser = gser(a, x);
      if (Double.isNaN(gamser)) {
        return Double.NaN;
      }
      return 1.0 - gamser;
    } else {
      gammcf = gcf(a, x);
      if (Double.isNaN(gammcf)) {
        return Double.NaN;
      }
      return gammcf;
    }
  }

  /**
   * Calculates the incomplete gamma function P(a, x) evaluated by its series
   * representation.
   *
   * Ported from Numerical Recipes in C, 2nd Ed.
   *
   * @param a double
   * @param x double
   * @return double
   */
  public static double gser(double a, double x) throws ArithmeticException {
    int n;
    double sum, del, ap;
    double gln = gammln(a);
    if (x <= 0.0) {
      if (x < 0.0) {
        throw new ArithmeticException("x less than 0 in routine gser");
      }
      return 0.0;
    } else {
      ap = a;
      del = sum = 1.0 / a;
      for (n = 1; n <= ITMAX; n++) {
        ++ap;
        del *= x / ap;
        sum += del;
        if (Math.abs(del) < Math.abs(sum) * EPS) {
          return sum * Math.exp( -x + a * Math.log(x) - (gln));
        }
      }
      throw new ArithmeticException("a too large, ITMAX too small in routine gser");
    }
  }

  /**
   * Calculates the incomplete gamma function Q(a, x) evaluated by its continued
   * faction representations.
   *
   * Ported from Numerical Recipes in C, 2nd Ed.
   *
   * @param a double
   * @param x double
   * @return double
   */
  public static double gcf(double a, double x) {
    int i;
    double an, b, c, d, del, h;
    double gln = gammln(a);
    b = x + 1.0 - a;
    c = 1.0 / FPMIN;
    d = 1.0 / b;
    h = d;
    for (i = 1; i <= ITMAX; i++) {
      an = -i * (i - a);
      b += 2.0;
      d = an * d + b;
      if (Math.abs(d) < FPMIN) {
        d = FPMIN;
      }
      c = b + an / c;
      if (Math.abs(c) < FPMIN) {
        c = FPMIN;
      }
      d = 1.0 / d;
      del = d * c;
      h *= del;
      if (Math.abs(del - 1.0) < EPS) {
        break;
      }
    }
    if (i > ITMAX) {
      throw new ArithmeticException("a too large, ITMAX too small in gcf");
    }
    return Math.exp( -x + a * Math.log(x) - (gln)) * h;
  }
}
