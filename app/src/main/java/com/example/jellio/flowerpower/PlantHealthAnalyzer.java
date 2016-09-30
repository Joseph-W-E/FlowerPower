package com.example.jellio.flowerpower;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Joseph Elliott on 9/16/16.
 */
public class PlantHealthAnalyzer extends AsyncTask<String, Bitmap, Bitmap> {

    /****
     * Instance variables
     ***/
    private Bitmap image;
    private ImageView imgDestination;
    private TextView txtDestination;
    private double health;

    /****
     * Constructors
     ***/
    public PlantHealthAnalyzer(Bitmap image, ImageView imgDestination, TextView txtDestination) {
        this.image = image;
        this.imgDestination = imgDestination;
        this.txtDestination = txtDestination;
        this.health = 0.0;
    }

    /****
     * Asynctask methods
     ****/

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap generatedBitmap;

        switch(params[0]) {
            case "quadratic":
                generatedBitmap = generateThresholdHeatMapQuadratic();
                break;
            case "jump":
                generatedBitmap = generateThresholdHeatMapJumpSearch();
                break;
            case "bfs":
                generatedBitmap = generateThresholdHeatMapBreadthFirstSearch();
                break;
            default:
                generatedBitmap = null;
        }

        return generatedBitmap;
    }

    @Override
    protected void onProgressUpdate(Bitmap... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        imgDestination.setImageBitmap(result);
        txtDestination.setText(txtDestination.getText().toString() + " " + generateHealthValue(health));
    }

    /****
     * Heat Map Methods
     ****/

    private Bitmap generateThresholdHeatMapBreadthFirstSearch() {
        Bitmap copy = image.copy(image.getConfig(), true);
        // Start in the middle of the image
        int x = copy.getWidth() / 2, y = copy.getHeight() / 2;

        Queue<Pixel> queue = new LinkedList<Pixel>();
        queue.add(new Pixel(copy.getPixel(x, y), x, y));
        copy.setPixel(x, y, Color.RED);
        while (!queue.isEmpty()) {
            Pixel pixel = queue.remove();
            for (Pixel neighbor : neighboringPixels(pixel, copy)) {
                copy.setPixel(neighbor.getX(), neighbor.getY(), Color.RED);
                queue.add(neighbor);
            }
        }

        return copy;
    }

    private ArrayList<Pixel> neighboringPixels(Pixel pixel, Bitmap copy) {
        ArrayList<Pixel> list = new ArrayList<>();

        int x = pixel.getX();
        int y = pixel.getY();

        // Top Left
        if (x - 1 > 0 && y + 1 < copy.getHeight() &&
                copy.getPixel(x - 1, y + 1) != Color.RED && withinThreshold(copy.getPixel(x - 1, y + 1))) {
            list.add(new Pixel(copy.getPixel(x - 1, y + 1), x - 1, y + 1));
        }
        // Top Middle
        if (y + 1 < copy.getHeight() &&
                copy.getPixel(x, y + 1) != Color.RED && withinThreshold(copy.getPixel(x, y + 1))) {
            list.add(new Pixel(copy.getPixel(x, y + 1), x, y + 1));
        }
        // Top Right
        if (x + 1 < copy.getWidth() && y + 1 < copy.getHeight() &&
                copy.getPixel(x + 1, y + 1) != Color.RED && withinThreshold(copy.getPixel(x + 1, y + 1))) {
            list.add(new Pixel(copy.getPixel(x + 1, y + 1), x + 1, y + 1));
        }
        // Middle Left
        if (x - 1 > 0 &&
                copy.getPixel(x - 1, y) != Color.RED && withinThreshold(copy.getPixel(x - 1, y))) {
            list.add(new Pixel(copy.getPixel(x - 1, y), x - 1, y));
        }
        // Middle Right
        if (x + 1 < copy.getWidth() &&
                copy.getPixel(x + 1, y) != Color.RED && withinThreshold(copy.getPixel(x + 1, y))) {
            list.add(new Pixel(copy.getPixel(x + 1, y), x + 1, y));
        }
        // Bottom Left
        if (x - 1 > 0 && y - 1 > 0 &&
                copy.getPixel(x - 1, y - 1) != Color.RED && withinThreshold(copy.getPixel(x - 1, y - 1))) {
            list.add(new Pixel(copy.getPixel(x - 1, y - 1), x - 1, y - 1));
        }
        // Bottom Middle
        if (y - 1 > 0 &&
                copy.getPixel(x, y - 1) != Color.RED && withinThreshold(copy.getPixel(x, y - 1))) {
            list.add(new Pixel(copy.getPixel(x, y - 1), x, y - 1));
        }
        // Bottom Right
        if (x + 1 < copy.getWidth() && y - 1 > 0 &&
                copy.getPixel(x + 1, y - 1) != Color.RED && withinThreshold(copy.getPixel(x + 1, y - 1))) {
            list.add(new Pixel(copy.getPixel(x + 1, y - 1), x + 1, y - 1));
        }

        return list;
    }

    /**
     * Generates a heap map of ChN-readable pixels using Jump method.
     *
     * @return A heat map in bitmap form.
     */
    private Bitmap generateThresholdHeatMapJumpSearch() {
        long ChN = 0L, numPixels = 0L;

        Bitmap copy = image.copy(image.getConfig(), true);
        int skipAmount = (int) Math.round((double) copy.getWidth() * 0.05);

        int xPlantFound, xEndOfPlantSegment;

        // Scan row by row
        outerLoop:
        for (int y = 0; y < image.getHeight(); y++) {
            int x = 0;

            // While we haven't run out of pixels in this row
            while (x < image.getWidth()) {
                // Get the first pixel.
                int pixel = image.getPixel(x, y);

                // Keep looking at pixels until we find a plant, or we go out of bounds.
                // If we go out of bounds, go to the next row.
                while (!withinThreshold(pixel)) {
                    x += skipAmount;
                    if (x >= image.getWidth()) continue outerLoop;
                    pixel = image.getPixel(x, y);
                }

                // Now that we found a plant, hold this point so we can come back to it later.
                xPlantFound = x;
                // Keep going right until we can no longer
                while (withinThreshold(pixel)) {
                    ChN += Color.green(pixel) - (Color.red(pixel) * 7f / 10f) - (Color.blue(pixel) / 2);
                    numPixels++;
                    copy.setPixel(x, y, Color.rgb(255, 0, 0));
                    x++;
                    if (x >= image.getWidth()) continue outerLoop;
                    pixel = image.getPixel(x, y);
                }

                // We can't go right any further, hold this point so we can come back to it later
                xEndOfPlantSegment = x;

                // Now return to that reference point from earlier and go left
                x = xPlantFound;
                while (withinThreshold(pixel)) {
                    ChN += Color.green(pixel) - (Color.red(pixel) * 7f / 10f) - (Color.blue(pixel) / 2);
                    numPixels++;
                    copy.setPixel(x, y, Color.rgb(255, 0, 0));
                    x--;
                    if (x < 0) continue outerLoop;
                    pixel = image.getPixel(x, y);
                }

                // We've scanned all of the plant segment, so now go back to the end of this segment.
                x = xEndOfPlantSegment;

                // Look for more plant segments.
            }

        }

        health = numPixels != 0.0 ? (double) ChN / (double) numPixels : 0.0;

        return copy;
    }

    /**
     * Generates a heap map of ChN-readable pixels using Quadratic method.
     *
     * @return A heat map in bitmap form.
     */
    private Bitmap generateThresholdHeatMapQuadratic() {
        Bitmap copy = image.copy(image.getConfig(), true);

        // Quadratic runtime. This is veeeery slow, and this needs to be optimized.
        for (int i = 0; i < copy.getWidth(); i++) {
            for (int j = 0; j < copy.getHeight(); j++) {

                int pixel = copy.getPixel(i, j);
                if (withinThreshold(pixel)) {
                    copy.setPixel(i, j, Color.rgb(255, 0, 0));
                }

            }
        }

        return copy;
    }

    /****
     * Helper methods
     ****/

    /**
     * Determines if the given RGB values are within a given threshold for Brown->Green.
     * @param pixel Integer representation of RGB value
     * @return true if RGB is within threshold, false otherwise.
     */
    private boolean withinThreshold(int pixel) {
        int r = Color.red(pixel), g = Color.green(pixel), b = Color.blue(pixel);
        double[][] colors = {
                {238.0, 238.0, 0.0},
                {205.0, 205.0, 0.0},
                {152.0, 251.0, 152.0},
                {154.0, 255.0, 154.0},
                {144.0, 238.0, 144.0},
                {124.0, 205.0, 124.0},
                {84.0, 139.0, 84.0},
                {50.0, 205.0, 50.0},
                {34.0, 139.0, 34.0},
                {0.0, 255.0, 0.0},
                {0.0, 238.0, 0.0},
                {0.0, 205.0, 0.0},
                {0.0, 139.0, 0.0},
                {0.0, 128.0, 0.0},
                {0.0, 100.0, 0.0},
                {48.0, 128.0, 20.0},
                {124.0, 252.0, 0.0},
                {127.0, 255.0, 0.0},
                {118.0, 238.0, 0.0},
                {102.0, 205.0, 0.0},
                {69.0, 139.0, 0.0},
                {173.0, 255.0, 47.0},
                {202.0, 255.0, 112.0},
                {188.0, 238.0, 104.0},
                {162.0, 205.0, 90.0},
                {110.0, 139.0, 61.0},
                {85.0, 107.0, 47.0},
                {107.0, 142.0, 35.0},
                {192.0, 255.0, 62.0},
                {179.0, 238.0, 58.0},
                {154.0, 205.0, 50.0},
                {105.0, 139.0, 34.0}
        };

        int tallyFor = 0, tallyAgainst = 0;

        for (double[] color : colors) {
            double distance = Math.sqrt(
                    Math.pow((double) r - color[0], 2)
                    + Math.pow((double) g - color[1], 2)
                    + Math.pow((double) b - color[2], 2)
            );

            if (180 - distance >= 0) {
                tallyFor++;
            } else {
                tallyAgainst++;
            }
        }

        return tallyFor > tallyAgainst - 5;
    }

    private String generateHealthValue(double val) {
        return String.format("%1$,.2f", val);
    }
}
