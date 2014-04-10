package icom5047.aerobal.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.widget.LinearLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import icom5047.aerobal.resources.Keys;


public class GraphActivity extends Activity {

    private String xAxisTitle;
    private String yAxisTitle;
    private double[] xAxisValues;
    private double[] yAxisValues;


    private GraphicalView mChart;

    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();

    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

    private XYSeries mCurrentSeries;

    private XYSeriesRenderer mCurrentRenderer;

    private void initChart() {
        mCurrentSeries = new XYSeries("Experiment Data");
        //Show Grid and make White

        mRenderer.setBackgroundColor(getResources().getColor(android.R.color.white)); //BG White
        mRenderer.setMargins(new int[]{10,170,50,10}); //Margins

        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
        mRenderer.setTextTypeface(face);
        mRenderer.setYLabelsPadding(75);
        //mRenderer.setXLabelsPadding(20);
        mRenderer.setAxisTitleTextSize(50);

        //Set Titles
        mRenderer.setXTitle(xAxisTitle);
        mRenderer.setYTitle(yAxisTitle);

        //Remove Legend
        mRenderer.setShowLegend(false);
        mRenderer.setShowGrid(true); //Show Grid
        mRenderer.setGridColor(getResources().getColor(android.R.color.black)); //SetGridColor

        //Set Line Color

        //Set Render size
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float val = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, metrics);
        mRenderer.setLabelsTextSize(val);


        mDataset.addSeries(mCurrentSeries);
        mCurrentRenderer = new XYSeriesRenderer();
        mCurrentRenderer.setLineWidth(10);
        mCurrentRenderer.setColor(getResources().getColor(android.R.color.holo_blue_bright));
        mRenderer.addSeriesRenderer(mCurrentRenderer);
    }

    private void addSampleData(double[] x, double[] y, int n) {
       for(int i=0; i<n; i++){

           Log.v("Tag", x[i]+" ,"+y[i]);
            mCurrentSeries.add(x[i], y[i]);
        }


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        final ActionBar ab = this.getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        Bundle bnd = this.getIntent().getExtras();

        if(bnd != null){
            xAxisTitle = bnd.getString(Keys.BundleKeys.XAxisTitle);
            yAxisTitle = bnd.getString(Keys.BundleKeys.YAxisTitle);

            xAxisValues = bnd.getDoubleArray(Keys.BundleKeys.XAxis);
            yAxisValues = bnd.getDoubleArray(Keys.BundleKeys.YAxis);


        }

        ab.setTitle(yAxisTitle+" vs "+xAxisTitle);



    }

    @Override
    protected void onResume() {
        super.onResume();
        LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
        if (mChart == null) {
            initChart();
            addSampleData(xAxisValues, yAxisValues, xAxisValues.length);
            mChart = ChartFactory.getLineChartView(this, mDataset, mRenderer);
            layout.addView(mChart);
        } else {
            mChart.repaint();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();


        }
        return true;
    }

}
