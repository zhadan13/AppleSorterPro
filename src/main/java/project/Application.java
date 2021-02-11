package project;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.awt.image.BufferedImage;

public class Application {

    private static BufferedImage image;
    protected static volatile int result;

    public static void main(String[] args) {

    }

    private static void setImage() {
        image = ApplicationGui.grabbedImage();
    }

    protected static int result() {
        setImage();

        int height = image.getHeight();
        int width = image.getWidth();

        Map<Integer, Integer> map = new HashMap<>();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = image.getRGB(i, j);
                int[] rgbArr = getRGBArr(rgb);
                if (!isGray(rgbArr)) {
                    Integer counter = map.get(rgb);
                    if (counter == null) {
                        counter = 0;
                    }
                    counter++;
                    map.put(rgb, counter);
                }
            }
        }

        int[] rgbColor = getMostCommonColor(map);
        int[] labColor = rgb2lab(rgbColor[0], rgbColor[1], rgbColor[2]);
        result = what(labColor[1], labColor[2]);

        return result;
    }

    private static int[] getMostCommonColor(Map<Integer, Integer> map) {
        if (map.isEmpty()) {
            return new int[] {0, 0, 0};
        }
        LinkedList<Map.Entry<Integer, Integer>> colorList = new LinkedList<>(map.entrySet());
        colorList.sort((o1, o2) -> ((Comparable<Integer>) o1.getValue()).compareTo(o2.getValue()));
        Map.Entry<Integer, Integer> mapEntry = colorList.get(colorList.size() - 1);
        int[] rgb = getRGBArr(mapEntry.getKey());
        return new int[] {rgb[0], rgb[1], rgb[2]};
    }

    private static int[] getRGBArr(final int pixel) {
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
        return new int[] {red, green, blue};
    }

    private static boolean isGray(final int[] rgbArr) {
        int rgDiff = Math.abs(rgbArr[0] - rgbArr[1]);
        int rbDiff = Math.abs(rgbArr[0] - rgbArr[2]);
        int gbDiff = Math.abs(rgbArr[1] - rgbArr[2]);
        int tolerance = 30;
        return rgDiff <= tolerance && rbDiff <= tolerance && gbDiff <= tolerance;
    }

    private static int[] rgb2lab(final int R, final int G, final int B) {
        double r, g, b, X, Y, Z, xr, yr, zr, Ls, as, bs;
        r = R / 255.0;
        g = G / 255.0;
        b = B / 255.0;
        if (r <= 0.04045) {
            r = r / 12.92;
        }
        else {
            r = Math.pow((r + 0.055) / 1.055, 2.4);
        }
        if (g <= 0.04045) {
            g = g / 12.92;
        }
        else {
            g = Math.pow((g + 0.055) / 1.055, 2.4);
        }
        if (b <= 0.04045) {
            b = b / 12.92;
        }
        else {
            b = Math.pow((b + 0.055) / 1.055, 2.4);
        }
        r *= 100.;
        g *= 100.;
        b *= 100.;
        X = 0.4124 * r + 0.3576 * g + 0.1805 * b;
        Y = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        Z = 0.0193 * r + 0.1192 * g + 0.9505 * b;
        xr = X / 95.047;
        yr = Y / 100.000;
        zr = Z / 108.883;
        if (xr > 0.008856) {
            xr = Math.pow(xr, (1. / 3.));
        }
        else {
            xr = (7.787 * xr) + (16. / 116.);
        }
        if (yr > 0.008856) {
            yr = Math.pow(yr, (1. / 3.));
        }
        else {
            yr = (7.787 * yr) + (16. / 116.);
        }
        if (zr > 0.008856) {
            zr = Math.pow(zr, (1. / 3.));
        }
        else {
            zr = (7.787 * zr) + (16. / 116.);
        }
        Ls = (116. * yr) - 16.;
        as = 500. * (xr - yr);
        bs = 200. * (yr - zr);

        return new int[] {(int) Ls, (int) as, (int) bs};
    }

    private static int what(final int A, final int B) {
        if (B < 0 || A * A + B * B < 400) {
            return 0;
        }
        if (A <= 0) {
            return -1;
        }
        return 1;
    }

}
