using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using Emgu.CV;
using Emgu.CV.CvEnum;
using Emgu.CV.Util;
using Emgu.CV.Features2D;
using Emgu.CV.Flann;
using Emgu.CV.Structure;
using System.Drawing;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Models.OpenCV
{
    public static class FiducialRecognition
    {
        private static int k = 2;
        private static double uniquenessThreshold = 0.80;
        private static double FEATURE_THRESHOLD = 0.38;

        public static bool IsFiducial_Orb(Bitmap bmp)
        {
            var img = bmp.ToImage<Bgr, byte>();

            return IsFiducial_Orb(img.Mat);
        }

        public static bool IsFiducial_Orb(Mat observedImage)
        {
            VectorOfVectorOfDMatch matches = new VectorOfVectorOfDMatch();
            string m = HttpContext.Current.Request.MapPath("/bin/Models/OpenCV/images/armarker.png");
            using (Mat modelImage = CvInvoke.Imread(m, ImreadModes.Grayscale))
            {
                FindMatches_Orb(modelImage, observedImage, matches);
            }

            List<float> good = new List<float>();
            for (int i = 0; i < matches.Size; i++)
            {
                var arrayOfMatches = matches[i].ToArray();
                //if (mask.GetData(i)[0] == 0) continue;
                foreach (var match in arrayOfMatches)
                {
                    if (match.Distance < FEATURE_THRESHOLD)
                        good.Add(match.Distance);
                }
            }

            return good.Count > 20;
        }
        public static void FindMatches_Orb(Mat model, Mat observe, VectorOfVectorOfDMatch matches)
        {
            using (UMat uModelImage = model.GetUMat(AccessType.Read))
            using (UMat uObservedImage = observe.GetUMat(AccessType.Read))
            {
                Emgu.CV.Features2D.ORBDetector featureDetector = new ORBDetector();
                VectorOfKeyPoint modelKeyPoints = new VectorOfKeyPoint();
                VectorOfKeyPoint observedKeyPoints = new VectorOfKeyPoint();
                Mat mask;

                //extract features from the object image
                Mat modelDescriptors = new Mat();
                featureDetector.DetectAndCompute(uModelImage, null, modelKeyPoints, modelDescriptors, false);

                // extract features from the observed image
                Mat observedDescriptors = new Mat();
                featureDetector.DetectAndCompute(uObservedImage, null, observedKeyPoints, observedDescriptors, false);

                using (BFMatcher bf = new BFMatcher(DistanceType.Hamming2))
                {
                    bf.Add(modelDescriptors);
                    bf.KnnMatch(observedDescriptors, matches, k, null);

                    mask = new Mat(matches.Size, 1, DepthType.Cv8U, 1);
                    mask.SetTo(new MCvScalar(255));
                    Features2DToolbox.VoteForUniqueness(matches, uniquenessThreshold, mask);
                }
            }
        }

        public static bool IsFiducial(Bitmap bmp, IUploadImage image)
        {
            var img = bmp.ToImage<Bgr, byte>();
            //img._Mul()
            //img._EqualizeHist();
            //img.Save(@"C:\Users\Connor Dodd\Documents\ExportMedia\TestOutput\histimage.png");
            //img._GammaCorrect(1.9d);
            //img.Save(@"C:\Users\Connor\Pictures\Fiducials\gammaimage.png");

            return IsFiducial(img.Mat, image);
        }

        public static bool IsFiducial(Mat mat, IUploadImage image)
        {
            VectorOfVectorOfDMatch matches = new VectorOfVectorOfDMatch();
            string m = HttpContext.Current.Request.MapPath("/bin/Models/OpenCV/images/new_fiducial_1px.png");
            using (Mat modelImage = CvInvoke.Imread(m, ImreadModes.Grayscale))
            {
                FindMatches(modelImage, mat, matches, image);
            }

            List<float> good = new List<float>();
            for (int i = 0; i < matches.Size; i++)
            {
                var arrayOfMatches = matches[i].ToArray();
                //if (mask.GetData(i)[0] == 0) continue;
                foreach (var match in arrayOfMatches)
                {
                    if (match.Distance < FEATURE_THRESHOLD)
                        good.Add(match.Distance);
                }
            }

            return image.Homography != null; //good.Count > 20;
        }

        public static void FindMatches(Mat modelImage, Mat observedImage, VectorOfVectorOfDMatch matches, IUploadImage image)
        {
            Mat homography;
            VectorOfKeyPoint modelKeyPoints;
            VectorOfKeyPoint observedKeyPoints;
            Mat mask;
            FindMatch(modelImage, observedImage, out modelKeyPoints, out observedKeyPoints, matches,
                out mask, out homography);

            //Draw the matched keypoints
            //Mat result = new Mat();
            //Features2DToolbox.DrawMatches(modelImage, modelKeyPoints, observedImage, observedKeyPoints,
            //    matches, result, new MCvScalar(255, 255, 255), new MCvScalar(255, 255, 255), mask);

            #region draw the projected region on the image

            if (homography != null)
            {
                //draw a rectangle along the projected model
                Rectangle rect = new Rectangle(Point.Empty, modelImage.Size);
                PointF[] pts = new PointF[]
                {
                new PointF(rect.Left, rect.Bottom),
                new PointF(rect.Right, rect.Bottom),
                new PointF(rect.Right, rect.Top),
                new PointF(rect.Left, rect.Top)
                };
                pts = CvInvoke.PerspectiveTransform(pts, homography);

                image.Homography = new ImageHomography()
                {
                    BottomLeft = new DoublePoint(pts[0].X / observedImage.Size.Width, pts[0].Y / observedImage.Size.Height),
                    BottomRight = new DoublePoint(pts[1].X / observedImage.Size.Width, pts[1].Y / observedImage.Size.Height),
                    TopRight = new DoublePoint(pts[2].X / observedImage.Size.Width, pts[2].Y / observedImage.Size.Height),
                    TopLeft = new DoublePoint(pts[3].X / observedImage.Size.Width, pts[3].Y / observedImage.Size.Height),
                };


                //Point[] points = Array.ConvertAll<PointF, Point>(pts, Point.Round);
                //using (VectorOfPoint vp = new VectorOfPoint(points))
                //{
                //    CvInvoke.Polylines(result, vp, true, new MCvScalar(255, 0, 0, 255), 5);
                //}
            }
            else
                image.Homography = null;
            #endregion

            return;
        }

        public static void FindMatch(Mat modelImage, Mat observedImage, out VectorOfKeyPoint modelKeyPoints, out VectorOfKeyPoint observedKeyPoints, VectorOfVectorOfDMatch matches, out Mat mask, out Mat homography)
        {
            int k = 2;
            double uniquenessThreshold = 0.80;

            homography = null;

            modelKeyPoints = new VectorOfKeyPoint();
            observedKeyPoints = new VectorOfKeyPoint();

            using (UMat uModelImage = modelImage.GetUMat(AccessType.Read))
            using (UMat uObservedImage = observedImage.GetUMat(AccessType.Read))
            {
                KAZE featureDetector = new KAZE();

                //extract features from the object image
                Mat modelDescriptors = new Mat();
                featureDetector.DetectAndCompute(uModelImage, null, modelKeyPoints, modelDescriptors, false);

                // extract features from the observed image
                Mat observedDescriptors = new Mat();
                featureDetector.DetectAndCompute(uObservedImage, null, observedKeyPoints, observedDescriptors, false);

                // Bruteforce, slower but more accurate
                // You can use KDTree for faster matching with slight loss in accuracy
                using (Emgu.CV.Flann.LinearIndexParams ip = new Emgu.CV.Flann.LinearIndexParams())
                using (Emgu.CV.Flann.SearchParams sp = new SearchParams())
                using (DescriptorMatcher matcher = new FlannBasedMatcher(ip, sp))
                {
                    matcher.Add(modelDescriptors);

                    matcher.KnnMatch(observedDescriptors, matches, k, null);
                    mask = new Mat(matches.Size, 1, DepthType.Cv8U, 1);
                    mask.SetTo(new MCvScalar(255));
                    Features2DToolbox.VoteForUniqueness(matches, uniquenessThreshold, mask);

                    int nonZeroCount = CvInvoke.CountNonZero(mask);
                    if (nonZeroCount >= 4)
                    {
                        nonZeroCount = Features2DToolbox.VoteForSizeAndOrientation(modelKeyPoints, observedKeyPoints,
                            matches, mask, 1.5, 20);
                        if (nonZeroCount >= 4)
                            homography = Features2DToolbox.GetHomographyMatrixFromMatchedFeatures(modelKeyPoints,
                                observedKeyPoints, matches, mask, 2);
                    }
                }
            }
        }
    }
}