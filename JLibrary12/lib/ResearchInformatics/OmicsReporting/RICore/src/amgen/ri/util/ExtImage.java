package amgen.ri.util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.zip.GZIPInputStream;
import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import sun.awt.image.BufferedImageGraphicsConfig;

/**
 * Handles Image Manipulations Conversions
 *
 * @version $Id: ExtImage.java,v 1.6 2013/04/13 02:12:28 jemcdowe Exp $
 *
 */
public final class ExtImage {
  /**
   * Resizes an image with flags set to maintain the quality as much as
   * possible. AlphaComposite is also maintained
   *
   * @param image BufferedImage
   * @param width int
   * @param height int
   * @return BufferedImage
   */
  public static BufferedImage resize(BufferedImage image, int width, int height) {
    if (image.getHeight() == height && image.getWidth() == width) {
      return image;
    }
    int type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType();
    BufferedImage resizedImage = new BufferedImage(width, height, type);
    Graphics2D g = resizedImage.createGraphics();
    g.setComposite(AlphaComposite.Src);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.drawImage(image, 0, 0, width, height, null);
    g.dispose();
    return resizedImage;
  }

  public static BufferedImage resize(BufferedImage source, int destWidth, int destHeight, Object interpolation) {
    if (source == null) {
      throw new NullPointerException("source image is NULL!");
    }
    if (destWidth <= 0 && destHeight <= 0) {
      throw new IllegalArgumentException("destination width & height are both <=0!");
    }
    int sourceWidth = source.getWidth();
    int sourceHeight = source.getHeight();
    double xScale = ((double) destWidth) / (double) sourceWidth;
    double yScale = ((double) destHeight) / (double) sourceHeight;
    if (destWidth <= 0) {
      xScale = yScale;
      destWidth = (int) Math.rint(xScale * sourceWidth);
    }
    if (destHeight <= 0) {
      yScale = xScale;
      destHeight = (int) Math.rint(yScale * sourceHeight);
    }
    GraphicsConfiguration gc = BufferedImageGraphicsConfig.getConfig(source);
    BufferedImage result = gc.createCompatibleImage(destWidth, destHeight, source.getColorModel().getTransparency());
    Graphics2D g2d = null;
    try {
      g2d = result.createGraphics();
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation);
      AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
      g2d.drawRenderedImage(source, at);
    } finally {
      if (g2d != null) {
        g2d.dispose();
      }
    }
    return result;
  }

  /**
   * Scales an image to fit into a space by resizing and writing to a canvas
   * while maintaining the quality as much as possible. AlphaComposite is also
   * maintained
   *
   * @param image BufferedImage
   * @param width int
   * @param height int
   * @return BufferedImage
   */
  public static BufferedImage scale(BufferedImage image, int width, int height) {
    return scale(image, width, height, 0);
  }

  /**
   * Scales an image to fit into a space by resizing and writing to a canvas
   * while maintaining the quality as much as possible. AlphaComposite is also
   * maintained. A margin may also be added.
   *
   * @param image BufferedImage
   * @param width int
   * @param height int
   * @param margin int
   * @return BufferedImage
   */
  public static BufferedImage scale(BufferedImage image, int width, int height, int margin) {
    if (image.getHeight() == height && image.getWidth() == width && margin == 0) {
      return image;
    }
    if (image.getHeight() > height && image.getWidth() > width) {
      if (image.getHeight() > image.getWidth()) {
        double ratio = (double) image.getWidth() / (double) image.getHeight();
        double newHeight = (double) height;
        double newWidth = newHeight * ratio;
        image = resize(image, (int) Math.round(newWidth), (int) Math.round(newHeight));
      } else {
        double ratio = (double) image.getHeight() / (double) image.getWidth();
        double newWidth = (double) width;
        double newHeight = newWidth * ratio;
        image = resize(image, (int) Math.round(newWidth), (int) Math.round(newHeight));
      }
    } else if (image.getHeight() > height) {
      double ratio = (double) image.getWidth() / (double) image.getHeight();
      double newHeight = (double) height;
      double newWidth = newHeight * ratio;
      image = resize(image, (int) Math.round(newWidth), (int) Math.round(newHeight));
    } else if (image.getWidth() > width) {
      double ratio = (double) image.getHeight() / (double) image.getWidth();
      double newWidth = (double) width;
      double newHeight = newWidth * ratio;
      image = resize(image, (int) Math.round(newWidth), (int) Math.round(newHeight));
    }

    int fullWidth = image.getWidth() + margin;
    int fullHeight = image.getHeight() + margin;

    GraphicsConfiguration gc = BufferedImageGraphicsConfig.getConfig(image);
    BufferedImage scaledImage = gc.createCompatibleImage(fullWidth, fullHeight, Transparency.TRANSLUCENT);
    Graphics2D g2 = scaledImage.createGraphics();
    double x = (double) (fullWidth - image.getWidth()) / 2;
    double y = (double) (fullHeight - image.getHeight()) / 2;
    g2.drawImage(image, (int) Math.round(x), (int) Math.round(y), image.getWidth(), image.getHeight(), null);
    g2.dispose();
    return scaledImage;
  }

  /**
   * Scales an image to fit into a space by resizing and writing to a canvas
   * while maintaining the quality as much as possible. AlphaComposite is also
   * maintained. A margin may also be added.
   *
   * @param image BufferedImage
   * @param width int
   * @param height int
   * @param margin int
   * @return BufferedImage
   */
  public static BufferedImage fillX(BufferedImage image, int width, int height, int margin) {
    if (image.getHeight() == height && image.getWidth() == width && margin == 0) {
      return image;
    }
    if (image.getHeight() > height && image.getWidth() > width) {
      return image;
    }
    if (image.getHeight() < height) {
      double ratio = (double) image.getHeight() / (double) image.getWidth();
      double newWidth = (double) width;
      double newHeight = newWidth * ratio;
      image = resize(image, (int) Math.round(newWidth), (int) Math.round(newHeight));
    } else if (image.getWidth() < width) {
      double ratio = (double) image.getWidth() / (double) image.getHeight();
      double newHeight = (double) height;
      double newWidth = newHeight * ratio;
      image = resize(image, (int) Math.round(newWidth), (int) Math.round(newHeight));
    }

    int fullWidth = width + margin;
    int fullHeight = height + margin;

    GraphicsConfiguration gc = BufferedImageGraphicsConfig.getConfig(image);
    BufferedImage scaledImage = gc.createCompatibleImage(fullWidth, fullHeight, Transparency.TRANSLUCENT);
    Graphics2D g2 = scaledImage.createGraphics();
    double x = (double) (fullWidth - image.getWidth()) / 2;
    double y = (double) (fullHeight - image.getHeight()) / 2;
    g2.drawImage(image, (int) Math.round(x), (int) Math.round(y), image.getWidth(), image.getHeight(), null);
    g2.dispose();
    return scaledImage;
  }

  /**
   * Scales an image to fit into a space by resizing and writing to a canvas
   * while maintaining the quality as much as possible. AlphaComposite is also
   * maintained. A margin may also be added.
   *
   * @param image BufferedImage
   * @param width int
   * @param height int
   * @param margin int
   * @return BufferedImage
   */
  public static BufferedImage fillY(BufferedImage image, int width, int height, int margin) {
    if (image.getHeight() == height && image.getWidth() == width && margin == 0) {
      return image;
    }
    if (image.getHeight() > height && image.getWidth() > width) {
      return image;
    }
    if (image.getHeight() < height) {
      double ratio = (double) image.getWidth() / (double) image.getHeight();
      double newHeight = (double) height;
      double newWidth = newHeight * ratio;
      image = resize(image, (int) Math.round(newWidth), (int) Math.round(newHeight));
    } else if (image.getWidth() < width) {
      double ratio = (double) image.getHeight() / (double) image.getWidth();
      double newWidth = (double) width;
      double newHeight = newWidth * ratio;
      image = resize(image, (int) Math.round(newWidth), (int) Math.round(newHeight));
    }

    int fullWidth = width + margin;
    int fullHeight = height + margin;

    GraphicsConfiguration gc = BufferedImageGraphicsConfig.getConfig(image);
    BufferedImage scaledImage = gc.createCompatibleImage(fullWidth, fullHeight, Transparency.TRANSLUCENT);
    Graphics2D g2 = scaledImage.createGraphics();
    double x = (double) (fullWidth - image.getWidth()) / 2;
    double y = (double) (fullHeight - image.getHeight()) / 2;
    g2.drawImage(image, (int) Math.round(x), (int) Math.round(y), image.getWidth(), image.getHeight(), null);
    g2.dispose();
    return scaledImage;
  }

  /**
   * Creates an image from a base64 encoded String
   *
   * @param encodedBase64 BufferedImage
   * @return BufferedImage
   * @throws IOException
   */
  public static BufferedImage createImageFromBase64(String encodedBase64) throws IOException {
    if (encodedBase64 == null) {
      return null;
    }
    return ImageIO.read(new ByteArrayInputStream(ExtBase64.decode(encodedBase64)));
  }

  /**
   * Creates an image from a base64 encoded char array
   *
   * @param encodedBase64 BufferedImage
   * @return BufferedImage
   * @throws IOException
   */
  public static BufferedImage createImageFromBase64(char[] encodedBase64) throws IOException {
    if (encodedBase64 == null) {
      return null;
    }
    return ImageIO.read(new ByteArrayInputStream(ExtBase64.decode(encodedBase64)));
  }
  
  public static Dimension getImageDimensions(URL imgURL) throws IOException {
    BufferedImage img= ImageIO.read(imgURL);
    return new Dimension(img.getWidth(), img.getHeight());
  }

  public static Dimension getImageDimensions(File imgFile) throws IOException {
    BufferedImage img= ImageIO.read(imgFile);
    return new Dimension(img.getWidth(), img.getHeight());
  }

  public static void main(String[] args) throws Exception {
    BufferedImage img = ImageIO.read(new File("c:/Development/JProjects2/ResearchGateway/WebContent/img/watson_report.png"));
    scale(img, 16, 0, 0);

    HttpServletRequest httpServletRequest = null; //The actual HttpServletRequest of course
    HttpURLConnection conn = (HttpURLConnection) new URL("...").openConnection();    
    //Transfer the SAML cookies
    StringBuilder authCookies = new StringBuilder();
    for (Cookie cookie : httpServletRequest.getCookies()) {
      if (cookie.getName().equals("SMSESSION") || cookie.getName().equals("SMIDENTITY")) {
        if (authCookies.length() > 0) {
          authCookies.append(";");
        }
        authCookies.append(cookie.getName() + "=" + cookie.getValue());
      }
    }
    //Set the SAML tokens in the connection
    if (authCookies.length() > 0) {
      conn.setRequestProperty("Cookie", authCookies.toString());
    }
    //Open the stream & read
    Image structure = ImageIO.read(conn.getInputStream());
    
    structure.flush();

  }
}
