using CloudinaryDotNet;
using CloudinaryDotNet.Actions;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models
{
    public class CloudImage
    {
        private const string CloudName = "visidaapp";
        private const string ApiKey = "558339539811552";
        private const string ApiSecret = "UH0QKOHgOju7OEN9H-GO47YF4Qg";
        private const string EnvironmentVariable = "cloudinary://558339539811552:UH0QKOHgOju7OEN9H-GO47YF4Qg@visidaapp/";

        private static Cloudinary _cloud;
        public static Cloudinary Cloud
        {
            get
            {
                if (_cloud != null)
                    return _cloud;

                _cloud = new Cloudinary(new Account(CloudName, ApiKey, ApiSecret));
                return _cloud;
            }
        }

        public static string UploadImage(string imageName, HttpPostedFile file)
        {
            var uploadParams = new ImageUploadParams()
            {
                UniqueFilename = false,
                UseFilename = true,
                DiscardOriginalFilename = false,
                File = new FileDescription(imageName, file.InputStream)
            };
            var uploadResult = Cloud.Upload(uploadParams);

            return uploadResult.StatusCode == System.Net.HttpStatusCode.OK ? uploadResult.Uri.AbsoluteUri : null;
        }

        public static string UploadImage(string imageName, Bitmap file)
        {
            using (MemoryStream ms = new MemoryStream())
            {
                file.Save(ms, System.Drawing.Imaging.ImageFormat.Jpeg);
                ms.Position = 0;

                var uploadParams = new ImageUploadParams()
                {
                    UniqueFilename = false,
                    UseFilename = true,
                    DiscardOriginalFilename = false,
                    File = new FileDescription(imageName, ms)
                };
                var uploadResult = Cloud.Upload(uploadParams);

                return uploadResult.StatusCode == System.Net.HttpStatusCode.OK ? uploadResult.Uri.AbsoluteUri : null;
            }
        }

        public static string UploadAudio(string audioName, HttpPostedFile file)
        {
            RawUploadParams uploadParams = new RawUploadParams()
            {
                DiscardOriginalFilename = false,
                UniqueFilename = false,
                UseFilename = true,
                File = new FileDescription(audioName, file.InputStream)
            };
            var uploadResult = Cloud.Upload(uploadParams);

            return uploadResult.StatusCode == System.Net.HttpStatusCode.OK ? uploadResult.Uri.AbsoluteUri : null;
        }
    }
}