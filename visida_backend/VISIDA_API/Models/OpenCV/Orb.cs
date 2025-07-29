using Emgu.CV;
using Emgu.CV.CvEnum;
using Emgu.CV.Features2D;
using Emgu.CV.Flann;
using Emgu.CV.Structure;
using Emgu.CV.Util;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.OpenCV
{
    public class Orb
    {
        public static int k = 4;
        public static double uniquenessThreshold = 4;
        public static double scaleIncrement = 1.1;
        public static int rotationBins = 20;
        public static double ransacReprojThreshold = 2.0;

        public static Mat Draw(Mat modelImage, Mat observedImage)
        {
            Mat homography;
            VectorOfKeyPoint modelKeyPoints;
            VectorOfKeyPoint observedKeyPoints;
            using (VectorOfVectorOfDMatch matches = new VectorOfVectorOfDMatch())
            {
                Mat mask;
                FindMatch2(modelImage, observedImage, out modelKeyPoints, out observedKeyPoints, matches, out mask, out homography);

                //Draw the matched keypoints
                Mat result = new Mat();
                Features2DToolbox.DrawMatches(modelImage, modelKeyPoints, observedImage, observedKeyPoints, matches, result, new MCvScalar(255, 255, 255), new MCvScalar(255, 255, 255), mask);

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

                    Point[] points = Array.ConvertAll<PointF, Point>(pts, Point.Round);
                    using (VectorOfPoint vp = new VectorOfPoint(points))
                    {
                        CvInvoke.Polylines(result, vp, true, new MCvScalar(255, 0, 0, 255), 5);
                    }

                }

                #endregion

                return result;

            }
        }

        public static Point[] GetHomography(Mat modelImage, Mat observedImage)
        {
            Mat homography;
            VectorOfKeyPoint modelKeyPoints;
            VectorOfKeyPoint observedKeyPoints;
            using (VectorOfVectorOfDMatch matches = new VectorOfVectorOfDMatch())
            {
                Mat mask;
                FindMatch2(modelImage, observedImage, out modelKeyPoints, out observedKeyPoints, matches, out mask, out homography);

                if (homography != null)
                {
                    Rectangle rect = new Rectangle(Point.Empty, modelImage.Size);
                    PointF[] pts = new PointF[]
                    {
                        new PointF(rect.Left, rect.Bottom),
                        new PointF(rect.Right, rect.Bottom),
                        new PointF(rect.Right, rect.Top),
                        new PointF(rect.Left, rect.Top)
                    };
                    pts = CvInvoke.PerspectiveTransform(pts, homography);
                    return Array.ConvertAll<PointF, Point>(pts, Point.Round);
                }
                return null;
            }
        }

        public static void FindMatch(Mat modelImage, Mat observedImage, out VectorOfKeyPoint modelKeyPoints, out VectorOfKeyPoint observedKeyPoints, VectorOfVectorOfDMatch matches, out Mat mask, out Mat homography)
        {
            homography = null;

            List<long> timings = new List<long>();

            modelKeyPoints = new VectorOfKeyPoint();
            observedKeyPoints = new VectorOfKeyPoint();
            using (UMat uModelImage = modelImage.GetUMat(AccessType.Read))
            using (UMat uObservedImage = observedImage.GetUMat(AccessType.Read))
            {
                ORBDetector orb = new ORBDetector();
                //extract features from the object image
                UMat modelDescriptors = new UMat();
                orb.DetectAndCompute(uModelImage, null, modelKeyPoints, modelDescriptors, false);

                // extract features from the observed image
                UMat observedDescriptors = new UMat();
                orb.DetectAndCompute(uObservedImage, null, observedKeyPoints, observedDescriptors, false);
                BFMatcher matcher = new BFMatcher(DistanceType.L2);
                matcher.Add(modelDescriptors);

                matcher.KnnMatch(observedDescriptors, matches, k, null);

                mask = new Mat(matches.Size, 1, DepthType.Cv8U, 1);
                mask.SetTo(new MCvScalar(255));
                Features2DToolbox.VoteForUniqueness(matches, uniquenessThreshold, mask);

                int nonZeroCount = CvInvoke.CountNonZero(mask);
                if (nonZeroCount >= 4)
                {
                    //nonZeroCount = Features2DToolbox.VoteForSizeAndOrientation(modelKeyPoints, observedKeyPoints,
                    //    matches, mask, scaleIncrement, rotationBins);
                    if (nonZeroCount >= 4)
                        homography = Features2DToolbox.GetHomographyMatrixFromMatchedFeatures(modelKeyPoints,
                            observedKeyPoints, matches, mask, ransacReprojThreshold);
                }

                return;
            }
        }

        public static void FindMatch2(Mat modelImage, Mat observedImage, out VectorOfKeyPoint modelKeyPoints, out VectorOfKeyPoint observedKeyPoints, VectorOfVectorOfDMatch matches, out Mat mask, out Mat homography)
        {
            homography = null;

            modelKeyPoints = new VectorOfKeyPoint();
            observedKeyPoints = new VectorOfKeyPoint();
            using (UMat uModelImage = modelImage.GetUMat(AccessType.Read))
            using (UMat uObservedImage = observedImage.GetUMat(AccessType.Read))
            {
                ORBDetector orb = new ORBDetector();

                Mat modelDescriptors = new Mat();
                orb.DetectAndCompute(uModelImage, null, modelKeyPoints, modelDescriptors, false);

                Mat observedDescriptors = new Mat();
                orb.DetectAndCompute(uObservedImage, null, observedKeyPoints, observedDescriptors, false);

                using (LshIndexParams lp = new LshIndexParams(20, 10, 2))
                using (SearchParams sp = new SearchParams())
                using (DescriptorMatcher matcher = new FlannBasedMatcher(lp, sp))
                {
                    matcher.Add(modelDescriptors);

                    matcher.KnnMatch(observedDescriptors, matches, k, null);

                    mask = new Mat(matches.Size, 1, DepthType.Cv8U, 1);
                    mask.SetTo(new MCvScalar(255));

                    mask = new Mat(matches.Size, 1, DepthType.Cv8U, 1);
                    mask.SetTo(new MCvScalar(255));
                    Features2DToolbox.VoteForUniqueness(matches, uniquenessThreshold, mask);

                    int nonZeroCount = CvInvoke.CountNonZero(mask);
                    if (nonZeroCount >= 4)
                    {
                        nonZeroCount = Features2DToolbox.VoteForSizeAndOrientation(modelKeyPoints, observedKeyPoints,
                            matches, mask, scaleIncrement, 20);
                        if (nonZeroCount >= 4)
                            homography = Features2DToolbox.GetHomographyMatrixFromMatchedFeatures(modelKeyPoints,
                                observedKeyPoints, matches, mask, 2);
                    }
                }
            }
        }
    }
}