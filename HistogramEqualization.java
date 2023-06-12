package PROJECT;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class HistogramEqualization {
    private static final int NUM_BINS = 256;

    public static void main(String[] args) {
        String imagePath = "C:\\Users\\hp\\Downloads\\Rain_Tree.jpg";
        BufferedImage image = loadImage(imagePath);
        if (image == null) {
            System.out.println("Failed to load the image: " + imagePath);
            return;
        }

        // Single-threaded implementation
        long startTime = System.currentTimeMillis();
        BufferedImage resultSingle = equalizeHistogramSingleThread(image);
        long endTime = System.currentTimeMillis();
        System.out.println("Single-threaded execution time: " + (endTime - startTime) + " ms");

        // Multi-threaded implementation
        int numOfThreads = 4; // Number of threads to use
        startTime = System.currentTimeMillis();
        BufferedImage resultMulti = equalizeHistogramMultiThread(image, numOfThreads);
        endTime = System.currentTimeMillis();
        System.out.println("Multi-threaded execution time (numOfThreads = " + numOfThreads + "): "
                + (endTime - startTime) + " ms");

        // Save the results
        saveImage(resultSingle, "Rain_Tree_single.jpg");
        saveImage(resultMulti, "Rain_Tree_multi.jpg");
    }

    private static BufferedImage loadImage(String imagePath) {
        try {
            File file = new File(imagePath);
            if (!file.exists()) {
                System.out.println("Image file not found: " + imagePath);
                return null;
            }
            return ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void saveImage(BufferedImage image, String outputPath) {
        try {
            ImageIO.write(image, "jpg", new File(outputPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage equalizeHistogramSingleThread(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] histogram = new int[NUM_BINS];
        int[] cumHistogram = new int[NUM_BINS];

        // Compute histogram
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int gray = (int) (0.299 * ((rgb >> 16) & 0xFF) + 0.587 * ((rgb >> 8) & 0xFF)
                        + 0.114 * (rgb & 0xFF));
                histogram[gray]++;
            }
        }

        // Compute cumulative histogram
        int totalPixels = width * height;
        int sum = 0;
        for (int i = 0; i < NUM_BINS; i++) {
            sum += histogram[i];
            cumHistogram[i] = sum * (NUM_BINS - 1) / totalPixels;
        }

        // Equalize image
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int gray = (int) (0.299 * ((rgb >> 16) & 0xFF) + 0.587 * ((rgb >> 8) & 0xFF)
                        + 0.114 * (rgb & 0xFF));
                int newGray = cumHistogram[gray];
                result.setRGB(x, y, new Color(newGray, newGray, newGray).getRGB());
            }
        }

        return result;
    }

    private static BufferedImage equalizeHistogramMultiThread(BufferedImage image, int numOfThreads) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] histogram = new int[NUM_BINS];
        int[] cumHistogram = new int[NUM_BINS];

        // Compute histogram using multiple threads
        HistogramThread[] threads = new HistogramThread[numOfThreads];
        int startY = 0;
        int step = height / numOfThreads;
        for (int i = 0; i < numOfThreads; i++) {
            int endY = (i == numOfThreads - 1) ? height : startY + step;
            threads[i] = new HistogramThread(image, startY, endY, histogram);
            threads[i].start();
            startY += step;
        }

        // Wait for all threads to finish
        for (HistogramThread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Compute cumulative histogram
        int totalPixels = width * height;
        int sum = 0;
        for (int i = 0; i < NUM_BINS; i++) {
            sum += histogram[i];
            cumHistogram[i] = sum * (NUM_BINS - 1) / totalPixels;
        }

        // Equalize image using multiple threads
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        HistogramEqualizationThread[] eqThreads = new HistogramEqualizationThread[numOfThreads];
        startY = 0;
        for (int i = 0; i < numOfThreads; i++) {
            int endY = (i == numOfThreads - 1) ? height : startY + step;
            eqThreads[i] = new HistogramEqualizationThread(image, startY, endY, cumHistogram, result);
            eqThreads[i].start();
            startY += step;
        }

        // Wait for all threads to finish
        for (HistogramEqualizationThread thread : eqThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private static class HistogramThread extends Thread {
        private final BufferedImage image;
        private final int startY;
        private final int endY;
        private final int[] histogram;

        public HistogramThread(BufferedImage image, int startY, int endY, int[] histogram) {
            this.image = image;
            this.startY = startY;
            this.endY = endY;
            this.histogram = histogram;
        }

        @Override
        public void run() {
            int width = image.getWidth();
            int height = image.getHeight();

            Arrays.fill(histogram, 0);

            for (int y = startY; y < endY; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    int gray = (int) (0.299 * ((rgb >> 16) & 0xFF) + 0.587 * ((rgb >> 8) & 0xFF)
                            + 0.114 * (rgb & 0xFF));
                    histogram[gray]++;
                }
            }
        }
    }

    private static class HistogramEqualizationThread extends Thread {
        private final BufferedImage image;
        private final int startY;
        private final int endY;
        private final int[] cumHistogram;
        private final BufferedImage result;

        public HistogramEqualizationThread(BufferedImage image, int startY, int endY, int[] cumHistogram,
                                           BufferedImage result) {
            this.image = image;
            this.startY = startY;
            this.endY = endY;
            this.cumHistogram = cumHistogram;
            this.result = result;
        }

        @Override
        public void run() {
            int width = image.getWidth();
            int height = image.getHeight();

            for (int y = startY; y < endY; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    int gray = (int) (0.299 * ((rgb >> 16) & 0xFF) + 0.587 * ((rgb >> 8) & 0xFF)
                            + 0.114 * (rgb & 0xFF));
                    int newGray = cumHistogram[gray];
                    result.setRGB(x, y, new Color(newGray, newGray, newGray).getRGB());
                }
            }
        }
    }
}