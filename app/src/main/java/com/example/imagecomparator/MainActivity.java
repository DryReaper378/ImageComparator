package com.example.imagecomparator;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FastFeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.ORB;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static ImageView iv1, iv2,iv3;
    private static final int SELECT_PHOTO = 100;
    private static int imgNo = 0;
    private static Uri selectedImage;
    private static InputStream imageStream;
    private static Bitmap bmp, yourSelectedImage, bmpimg1, bmpimg2;
    private static String path1, path2;
    private static Button runBtn;
    private static MatOfKeyPoint keypoints= new MatOfKeyPoint(), dupKeypoints= new MatOfKeyPoint();
    private static MatOfDMatch matches= new MatOfDMatch();
    private static Mat descriptors= new Mat(), dupDescriptors= new Mat();
    final static ORB detector  = ORB.create();
    final static DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv1 = findViewById(R.id.iv_takePhoto);
        iv2 = findViewById(R.id.iv_takePhoto2);
        iv3 = findViewById(R.id.iv_takePhoto3);
        runBtn = findViewById(R.id.button);
        iv3.setVisibility(View.GONE);
        if (!OpenCVLoader.initDebug()) {
            Log.i("ImageComparator", "Error in image Comparator");

        }
       // run();


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    selectedImage = imageReturnedIntent.getData();
                    try {
                        imageStream = getContentResolver().openInputStream(
                                selectedImage);
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                    if (imgNo == 1) {
                        iv1.setImageBitmap(yourSelectedImage);
                        path1 = selectedImage.getPath();
                        bmpimg1 = yourSelectedImage;
                        iv1.invalidate();
                    } else if (imgNo == 2) {
                        iv2.setImageBitmap(yourSelectedImage);
                        path2 = selectedImage.getPath();
                        bmpimg2 = yourSelectedImage;
                        iv2.invalidate();
                    }
                }
        }
    }

    public void run(){
        runBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compare();
            }
        });
        iv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                imgNo = 1;

            }
        });
        iv2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                imgNo = 2;
            }
        });
    }
    public void compare(){
        Mat img1 = Imgcodecs.imread(path1, Imgcodecs.IMREAD_COLOR);
        Mat img2 = Imgcodecs.imread(path2, Imgcodecs.IMREAD_COLOR);

        analyze(img1,keypoints,descriptors);
        analyze(img2,dupKeypoints,dupDescriptors);
        matches = match(descriptors,dupDescriptors);
        matches = filterMatchesByDistance(matches);
        matches = filterMatchesByHomography(keypoints,dupKeypoints,matches);
        List<DMatch> matchesList = matches.toList();
        Toast.makeText(this, String.valueOf(matchesList.size()), Toast.LENGTH_SHORT).show();
        iv3.setVisibility(View.VISIBLE);
        iv3.setImageBitmap(drawMatches(img1,keypoints,img2,dupKeypoints,matches,false));
        //Toast.makeText(this, String.valueOf(matches_final.size()), Toast.LENGTH_SHORT).show();




    }
    /*private void compare1( ) {
        try {
            Mat img1 = Imgcodecs.imread(path1, Imgcodecs.IMREAD_COLOR);
            Mat img2 = Imgcodecs.imread(path2, Imgcodecs.IMREAD_COLOR);
            ORB detector = ORB.create();
            //DescExtractor = DescExtractor;
            //DescExtractor = DescriptorExtractor.create(descriptor);
            matcher = DescriptorMatcher
                    .create(DescriptorMatcher.BRUTEFORCE_HAMMING);

            keypoints = new MatOfKeyPoint();
            dupKeypoints = new MatOfKeyPoint();
            descriptors = new Mat();
            dupDescriptors = new Mat();
            matches = new MatOfDMatch();
            detector.detect(img1, keypoints);
            detector.detect(img2, dupKeypoints);
            // Descript keypoints
            detector.compute(img1, keypoints, descriptors);
            detector.compute(img2, dupKeypoints, dupDescriptors);
            // matching descriptors
            matcher.match(descriptors, dupDescriptors, matches);
                        // New method of finding best matches
            List<DMatch> matchesList = matches.toList();
            List<DMatch> matches_final = new ArrayList<DMatch>();
            Toast.makeText(this, String.valueOf(matchesList.size()), Toast.LENGTH_SHORT).show();
            for (int i = 0; i < matchesList.size(); i++) {
                if (matchesList.get(i).distance <= 10) {
                    matches_final.add(matches.toList().get(i));
                }
            }
            //Toast.makeText(this, String.valueOf(matches_final.size()), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }*/

    static void analyze(Mat image, MatOfKeyPoint keypoints, Mat descriptors){
        detector.detect(image, keypoints);
        detector.compute(image, keypoints, descriptors);
    }

    static MatOfDMatch match(Mat desc1, Mat desc2){
        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(desc1, desc2, matches);
        return matches;
    }

    static MatOfDMatch filterMatchesByDistance(MatOfDMatch matches){
        List<DMatch> matches_original = matches.toList();
        List<DMatch> matches_filtered = new ArrayList<DMatch>();

        int DIST_LIMIT = 30;
        // Check all the matches distance and if it passes add to list of filtered matches
        for (int i = 0; i < matches_original.size(); i++) {
            DMatch d = matches_original.get(i);
            if (Math.abs(d.distance) <= DIST_LIMIT) {
                matches_filtered.add(d);
            }
        }
        MatOfDMatch mat = new MatOfDMatch();
        mat.fromList(matches_filtered);
        return mat;
    }

    static MatOfDMatch filterMatchesByHomography(MatOfKeyPoint keypoints1, MatOfKeyPoint keypoints2, MatOfDMatch matches){
        List<Point> lp1 = new ArrayList<Point>(500);
        List<Point> lp2 = new ArrayList<Point>(500);

        KeyPoint[] k1 = keypoints1.toArray();
        KeyPoint[] k2 = keypoints2.toArray();


        List<DMatch> matches_original = matches.toList();

        if (matches_original.size() < 4){
            MatOfDMatch mat = new MatOfDMatch();
            return mat;
        }

        // Add matches keypoints to new list to apply homography
        for(DMatch match : matches_original){
            Point kk1 = k1[match.queryIdx].pt;
            Point kk2 = k2[match.trainIdx].pt;
            lp1.add(kk1);
            lp2.add(kk2);
        }

        MatOfPoint2f srcPoints = new MatOfPoint2f(lp1.toArray(new Point[0]));
        MatOfPoint2f dstPoints  = new MatOfPoint2f(lp2.toArray(new Point[0]));

        Mat mask = new Mat();
        Mat homography = Calib3d.findHomography(srcPoints, dstPoints, Calib3d.LMEDS, 0.2, mask);
        List<DMatch> matches_homo = new ArrayList<DMatch>();
        int size = (int) mask.size().height;
        for(int i = 0; i < size; i++){
            if ( mask.get(i, 0)[0] == 1){
                DMatch d = matches_original.get(i);
                matches_homo.add(d);
            }
        }

        MatOfDMatch mat = new MatOfDMatch();
        mat.fromList(matches_homo);
        return mat;
    }
    static Bitmap drawMatches(Mat img1, MatOfKeyPoint key1, Mat img2, MatOfKeyPoint key2, MatOfDMatch matches, boolean imageOnly){
        Mat out = new Mat();
        Mat im1 = new Mat();
        Mat im2 = new Mat();
        Imgproc.cvtColor(img1, im1, Imgproc.COLOR_BGR2RGB);
        Imgproc.cvtColor(img2, im2, Imgproc.COLOR_BGR2RGB);
        if ( imageOnly){
            MatOfDMatch emptyMatch = new MatOfDMatch();
            MatOfKeyPoint emptyKey1 = new MatOfKeyPoint();
            MatOfKeyPoint emptyKey2 = new MatOfKeyPoint();
            Features2d.drawMatches(im1, emptyKey1, im2, emptyKey2, emptyMatch, out);
        } else {
            Features2d.drawMatches(im1, key1, im2, key2, matches, out);
        }
        Bitmap bmp = Bitmap.createBitmap(out.cols(), out.rows(), Bitmap.Config.ARGB_8888);
        Imgproc.cvtColor(out, out, Imgproc.COLOR_BGR2RGB);
        Utils.matToBitmap(out, bmp);
        return bmp;
    }

}
