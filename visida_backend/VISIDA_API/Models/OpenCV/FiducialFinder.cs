using Emgu.CV;
using Emgu.CV.CvEnum;
using Emgu.CV.Structure;
using Emgu.CV.Util;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Models.OpenCV
{
    public class FiducialFinder
    {
        public static ImageHomography FindImageHomography(Bitmap bmp)
        {
            var points = FindFiducial(bmp.ToImage<Bgr, byte>(), bmp);
            if (points == null)
                return null;
            //var xes = points.OrderBy(x => x.X);

            double width = bmp.Width, height = bmp.Height;
            var np = new DoublePoint[4];
            np[0] = new DoublePoint(points[0].X / width, points[0].Y / height);
            np[1] = new DoublePoint(points[1].X / width, points[1].Y / height);
            np[2] = new DoublePoint(points[2].X / width, points[2].Y / height);
            np[3] = new DoublePoint(points[3].X / width, points[3].Y / height);
            var homography = new ImageHomography() { Points = np };
            return homography;
        }

        public static Point[] FindFiducial(Image<Bgr, byte> image, Bitmap bmp)
        {
            Mat fiducial = CvInvoke.Imread(HttpContext.Current.Request.MapPath("/bin/Models/OpenCV/images/new_fiducial_spaced.png"), ImreadModes.Grayscale);
            Rectangle bounds = FindBounds(image, fiducial);
            Point[] corners = null;

            // If there is bounds, try and find corners with the bounds
            if (!bounds.IsEmpty)
            {
                corners = FindCorners(image, bmp, bounds);
            }

            // If nothing found, try again with no bounds
            if (corners == null)
            {
                corners = FindCorners(image, bmp, new Rectangle());
            }

            return corners;
        }

        public static Rectangle FindBounds(Image<Bgr, byte> image, Mat fiducial) {
            // Homography
            Rectangle bounds = new Rectangle();
            var homography = Orb.GetHomography(fiducial, image.Mat);
            if (homography == null)
                // TODO - Skip some step
                return new Rectangle();

            bounds.X = Math.Min(homography[0].X, homography[3].X) - 25;
            bounds.Y = Math.Min(homography[2].Y, homography[3].Y) - 25;
            bounds.Width = Math.Max(homography[1].X, homography[2].X) - bounds.X + 50;
            bounds.Height = Math.Max(homography[0].Y, homography[1].Y) - bounds.Y + 50;


            if (bounds.X < 0 || bounds.Y < 0 || bounds.X + bounds.Width > image.Width || bounds.Y + bounds.Height > image.Height)
            {
                bounds = new Rectangle();
            }
            return bounds;
        }
        public static Point[] FindCorners(Image<Bgr, byte> image, Bitmap bmp, Rectangle bounds)
        {
            // Histogram
            var labImage = bmp.ToImage<Bgr, byte>();

            // If there are bounds
            if (!bounds.IsEmpty)
            {
                // Region of interest
                labImage.ROI = bounds;
            }

            var labPlanes = new VectorOfMat(3);
            CvInvoke.Split(labImage, labPlanes);

            Mat claheOut = new Mat();
            CvInvoke.CLAHE(labPlanes[0], 20, new Size(100, 100), claheOut);

            claheOut.CopyTo(labPlanes[0]);
            CvInvoke.Merge(labPlanes, labImage);

            var color = new Image<Bgr, byte>(labImage.Size);
            CvInvoke.CvtColor(labImage, color, ColorConversion.Lab2Bgr);
            image = color;


            // Black / White
            var gray = new Image<Gray, byte>(image.Size);
            CvInvoke.CvtColor(image, gray, ColorConversion.Bgr2Gray);
            // Save the bitmap as a JPEG file with quality level 25.
            // bmp.Save("c://Shapes025.bmp", ImageFormat.Bmp);
            CvInvoke.CvtColor(gray, image, ColorConversion.Gray2Bgr);


            // Blur
            CvInvoke.GaussianBlur(image, image, new Size(21, 21), 0);


            // Threshold
            gray = new Image<Gray, byte>(image.Size);
            CvInvoke.CvtColor(image, gray, ColorConversion.Bgr2Gray);

            // WAS 180 - shouldn't be this high anymore due to blur
            int threshold = 130;
            gray = gray.ThresholdBinary(new Gray(threshold), new Gray(255));

            CvInvoke.CvtColor(gray, image, ColorConversion.Gray2Bgr);


            // Contour
            List<Tuple<VectorOfPoint, VectorOfPointF>> contourList;
            var gray2 = new Image<Gray, byte>(image.Size);
            CvInvoke.CvtColor(image, gray2, ColorConversion.Bgr2Gray);
            VectorOfVectorOfPoint contours = new VectorOfVectorOfPoint();
            Mat heirarchy = new Mat();
            CvInvoke.FindContours(gray2, contours, heirarchy, RetrType.List, ChainApproxMethod.ChainApproxSimple);


            List<KeyValuePair<double, VectorOfPoint>> best = new List<KeyValuePair<double, VectorOfPoint>>();
            contourList = new List<Tuple<VectorOfPoint, VectorOfPointF>>();
            double cutoff = 15000;

            for (int i = 0; i < contours.Size; i++)
            {
                var contour = contours[i];

                var area = CvInvoke.ContourArea(contour);
                if (area <= cutoff)
                    continue;

                best.Add(new KeyValuePair<double, VectorOfPoint>(area, contour));
            }

            foreach (var kv in best)
            {
                var contour = kv.Value;
                double e = 0;
                VectorOfPointF p = new VectorOfPointF();
                do
                {
                    e += 5;
                    CvInvoke.ApproxPolyDP(contour, p, e, true);
                }
                while (p.Size > 4 && e < 30);

                if (p.Size == 4)
                {
                    contourList.Add(new Tuple<VectorOfPoint, VectorOfPointF>(contour, p));
                }
            }


            // Check Match
            gray = new Image<Gray, byte>(image.Size);
            CvInvoke.CvtColor(image, gray, ColorConversion.Bgr2Gray);

            PointF[] successMatch = null;
            foreach (var match in contourList)
            {
                var arr = match.Item2.ToArray();
                for (int i = 0; i < 3; i++)
                {
                    //Sort by Y
                    for (int j = i + 1; j < 4; j++)
                    {
                        if (arr[i].Y > arr[j].Y)
                        {
                            var temp = arr[i];
                            arr[i] = arr[j];
                            arr[j] = temp;
                        }
                    }
                }
                if (arr[0].X > arr[1].X)
                {
                    var temp = arr[0];
                    arr[0] = arr[1];
                    arr[1] = temp;
                }
                if (arr[3].X > arr[2].X)
                {
                    var temp = arr[2];
                    arr[2] = arr[3];
                    arr[3] = temp;
                }

                arr[0].X -= 1;
                arr[0].Y -= 1;

                arr[1].X += 1;
                arr[1].Y -= 1;

                arr[2].X += 1;
                arr[2].Y += 1;

                arr[3].X -= 1;
                arr[3].Y += 1;

                Image<Bgr, byte> fCheck = new Image<Bgr, byte>(new Size(255, 158));
                var fArr = new PointF[4] { new Point(0, 0), new Point(255, 0), new Point(255, 158), new Point(0, 158) };
                var transform = CvInvoke.GetPerspectiveTransform(arr, fArr);
                var color3 = bmp.ToImage<Bgr, byte>();
                CvInvoke.WarpPerspective(color3, fCheck, transform, fCheck.Size);

                fCheck.ROI = new Rectangle(8, 8, 38, 38);
                var avg = fCheck.GetAverage();
                if ((avg.Green + avg.Red) * 0.5 < avg.Blue)
                {
                    successMatch = arr;
                    continue;
                }

                fCheck.ROI = new Rectangle(210, 108, 246, 143);
                avg = fCheck.GetAverage();
                if ((avg.Green + avg.Red) * 0.5 < avg.Blue)
                {
                    successMatch = arr;
                    continue;
                }
            }
            // If we have found the fiducial
            if (successMatch != null)
            {
                Point[] response = new Point[4];

                for (int i = 0; i < 4; i++)
                {
                    response[i] = Point.Round(successMatch[i]);
                    // Add on the bounds coordinates in case we are only looking relative to a bounded rectangle
                    response[i].X += bounds.X;
                    response[i].Y += bounds.Y;
                }

                return response;
            }
            return null;
        }
    }
}