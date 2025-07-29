using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Drawing;
using System.Linq;
using System.Web;
using VISIDA_API.Models.OpenCV;

namespace VISIDA_API.Models.InternalObjects
{
    public class ImageHomography
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        public bool Improved { get; set; }

        [NotMapped]
        public DoublePoint[] Points
        {
            get
            {
                DoublePoint[] points = new DoublePoint[4];
                points[0] = TopLeft;
                points[1] = TopRight;
                points[2] = BottomRight;
                points[3] = BottomLeft;

                return points;
            }
            set
            {
                if (value.Length < 4)
                    throw new InvalidOperationException();

                
                for (int i = 0; i < value.Length; i++)
                {
                    for (int j = i; j < value.Length; j++)
                    {
                        if (value[j].Y < value[i].Y)
                        {
                            var temp = value[i];
                            value[i] = value[j];
                            value[j] = temp;
                        }
                    }
                }

                if (value[1].X < value[0].X)
                {
                    var temp = value[0];
                    value[0] = value[1];
                    value[1] = temp;
                }
                if (value[2].X < value[3].X)
                {
                    var temp = value[2];
                    value[2] = value[3];
                    value[3] = temp;
                }

                TopLeft = value[0];
                TopRight = value[1];
                BottomRight = value[2];
                BottomLeft = value[3];
            }
        }

        //TopLeft
        [NotMapped]
        public DoublePoint TopLeft
        {
            get
            {
                return new DoublePoint(TopLeftX, TopLeftY);
            }
            set
            {
                TopLeftX = value.X;
                TopLeftY = value.Y;
            }
        }

        public double TopLeftX { get; set; }
        public double TopLeftY { get; set; }

        //TopRight
        [NotMapped]
        public DoublePoint TopRight
        {
            get
            {
                return new DoublePoint(TopRightX, TopRightY);
            }
            set
            {
                TopRightX = value.X;
                TopRightY = value.Y;
            }
        }

        public double TopRightX { get; set; }
        public double TopRightY { get; set; }

        //BottomLeft
        [NotMapped]
        public DoublePoint BottomLeft
        {
            get
            {
                return new DoublePoint(BottomLeftX, BottomLeftY);
            }
            set
            {
                BottomLeftX = value.X;
                BottomLeftY = value.Y;
            }
        }

        public double BottomLeftX { get; set; }
        public double BottomLeftY { get; set; }

        //BottomRight
        [NotMapped]
        public DoublePoint BottomRight
        {
            get
            {
                return new DoublePoint(BottomRightX, BottomRightY);
            }
            set
            {
                BottomRightX = value.X;
                BottomRightY = value.Y;
            }
        }

        public double BottomRightX { get; set; }
        public double BottomRightY { get; set; }

        public ImageHomography()
        {
        }

        public ImageHomography(ImageHomography copy)
        {
            Points = copy.Points;
        }
    }
}