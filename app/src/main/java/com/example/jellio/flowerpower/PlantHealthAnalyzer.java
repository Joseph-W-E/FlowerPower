package com.example.jellio.flowerpower;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
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
    private TextView txtToggle;
    private double health;

    /****
     * Constructors
     ***/
    public PlantHealthAnalyzer(Bitmap image) {
        this.image = image;
        this.health = 0.0;
    }

    public void setImageDestination(ImageView imageDestination) {
        this.imgDestination = imageDestination;
    }

    public void setTextViewDestination(TextView textViewDestination) {
        this.txtDestination = textViewDestination;
    }

    public void setTextViewToggle(TextView textViewToggle) {
        this.txtToggle = textViewToggle;
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

        generatedBitmap = generateThresholdHeatMapBreadthFirstSearch();

        return generatedBitmap;
    }

    @Override
    protected void onProgressUpdate(Bitmap... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        if (imgDestination != null) {
            imgDestination.setImageBitmap(result);
        }
        if (txtDestination != null) {
            txtDestination.setText(txtDestination.getText().toString() +
                    " " + roundDoubleToTwoDecimalPlaces(health));
        }
        if (txtToggle != null) {
            txtToggle.setText(R.string.btn_analyze_hint);
        }
    }

    /****
     * Heat Map Methods
     ****/

    /**
     * Generates a heat-map of plant-detected pixels.
     * This does not modify the original image.
     * @return A copy of the original image with an overlaying heat-map.
     */
    private Bitmap generateThresholdHeatMapBreadthFirstSearch() {
        /*** How we will calculate the healthiness ***/
        long ChN = 0L, numPixels = 0L;
        /*** The new bitmap we are turning into the heatmap ***/
        Bitmap copy = image.copy(image.getConfig(), true);
        /*** Establish the starting position ***/
        int xInitial = copy.getWidth() / 2, yInitial = copy.getHeight() / 2;
        Pixel pixelInitial = new Pixel(copy.getPixel(xInitial, yInitial), xInitial, yInitial);

        /*** The start of the BFS algorithm ***/
        Queue<Pixel> queue = new LinkedList<>();
        queue.add(pixelInitial);

        ChN += getChNFromPixel(pixelInitial);
        numPixels++;
        copy.setPixel(xInitial, yInitial, Color.RED);

        while (!queue.isEmpty()) {
            Pixel pixel = queue.remove();
            for (Pixel neighbor : neighboringPixels(pixel, copy)) {
                ChN += getChNFromPixel(neighbor);
                numPixels++;
                copy.setPixel(neighbor.getX(), neighbor.getY(), Color.RED);
                queue.add(neighbor);
            }
        }

        health = ChN / numPixels;

        return copy;
    }

    /**
     * Gathers a list of valid neighboring pixels.
     * A pixel is considered "valid" if:
     * * The pixel is not out of bounds.
     * * The pixel is not a part of the heat-map.
     * @param pixel The center pixel.
     * @param copy The bitmap in which "pixel" resides.
     * @return A list of Pixel objects.
     */
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
                {238.0, 238.0, 0.0},   {205.0, 205.0, 0.0},   {152.0, 251.0, 152.0},
                {154.0, 255.0, 154.0}, {144.0, 238.0, 144.0}, {124.0, 205.0, 124.0},
                {84.0, 139.0, 84.0},   {50.0, 205.0, 50.0},   {34.0, 139.0, 34.0},
                {0.0, 255.0, 0.0},     {0.0, 238.0, 0.0},     {0.0, 205.0, 0.0},
                {0.0, 139.0, 0.0},     {0.0, 128.0, 0.0},     {0.0, 100.0, 0.0},
                {48.0, 128.0, 20.0},   {124.0, 252.0, 0.0},   {127.0, 255.0, 0.0},
                {118.0, 238.0, 0.0},   {102.0, 205.0, 0.0},   {69.0, 139.0, 0.0},
                {173.0, 255.0, 47.0},  {202.0, 255.0, 112.0}, {188.0, 238.0, 104.0},
                {162.0, 205.0, 90.0},  {110.0, 139.0, 61.0},  {85.0, 107.0, 47.0},
                {107.0, 142.0, 35.0},  {192.0, 255.0, 62.0},  {179.0, 238.0, 58.0},
                {154.0, 205.0, 50.0},  {105.0, 139.0, 34.0}
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

    /**
     * The formula for generating the Chlorophyll-Nitrogen content of a pixel.
     * Source: http://www.ipcbee.com/vol57/010-ICSEA2013-B1014.pdf
     * @param pixel The pixel to be evaluated.
     * @return The ChN value generated from the formula.
     */
    private long getChNFromPixel(Pixel pixel) {
        long ChN = Color.green(pixel.getRgb())
                - Color.red(pixel.getRgb()) / 2
                - Color.blue(pixel.getRgb()) / 2;
        return ChN;
    }

    /**
     * Formats the double to two decimal places.
     * @param val The double to be formatted.
     * @return A string representation of a two-decimal-place double.
     */
    private String roundDoubleToTwoDecimalPlaces(double val) {
        return String.format("%1$,.2f", val);
    }
}
