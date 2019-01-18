//package mesh;
//
//
//import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.highgui.HighGui;
//import org.opencv.core.Mat;
//import org.opencv.core.Core;
//import org.opencv.imgproc.Imgproc;
//
//public class CannyEdgeDetection {
//    // Compulsory
//    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
//
//    public static String path = "res/flower_contrast.png";
//
//    public void detectEdges() {
//        //read the RGB image
//        Mat rgbImage = Imgcodecs.imread(path);
//        //mat gray image holder
//        Mat imageGray = new Mat();
//        //mat canny image
//        Mat imageCny = new Mat();
//        //Show the RGB Image
//
//
//        Imgproc.cvtColor(rgbImage, imageGray, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.Canny(imageGray, imageCny, 300, 600, 3, true);
//        Imgcodecs.imwrite("res/output.png", imageCny);
//    }
//    public static void main(String[] args) {
//        CannyEdgeDetection edgeDetection = new CannyEdgeDetection();
//        edgeDetection.detectEdges();
//    }
//}