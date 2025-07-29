using Microsoft.WindowsAzure.Storage;
using Microsoft.WindowsAzure.Storage.Auth;
using Microsoft.WindowsAzure.Storage.Blob;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using System.Web;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Helpers
{
    public class StorageHelper
    {
        private static AzureStorageConfig config;
        public const string IMAGE_CONTAINER = "image-container", THUMBNAIL_CONTAINER = "thumb-container", AUDIO_CONTAINER = "audio-container", MEASURE_CONTAINER = "measure-container";

        static StorageHelper()
        {
            config = new AzureStorageConfig()
            {
                AccountKey = "VJAD8l5HE/MpQltwSS2yN7UH0BSAxVz5ahzsNVNexHMGyBl1zcUZHXPIvlq8mY67U0NTXa4NAxsUSsCov7Ch0Q==",
                AccountName = "visidamedia"
            };
        }

        private static CloudBlobClient _blobClient;
        private static CloudBlobClient BlobClient
        {
            get
            {
                if (_blobClient == null)
                {
                    StorageCredentials storageCredentials = new StorageCredentials(config.AccountName, config.AccountKey);
                    CloudStorageAccount storageAccount = new CloudStorageAccount(storageCredentials, true);
                    _blobClient = storageAccount.CreateCloudBlobClient();
                }
                return _blobClient;
            }
        }

        private static CloudBlobContainer GetBlobContainer(string container)
        {
            return BlobClient.GetContainerReference(container);
        }

        public static bool IsImage(HttpPostedFile file)
        {
            if (file.ContentType.Contains("image"))
            {
                return true;
            }

            string[] formats = new string[] { ".jpg", ".png", ".gif", ".jpeg" };

            return formats.Any(item => file.FileName.EndsWith(item, StringComparison.OrdinalIgnoreCase));
        }

        public static bool IsAudio(HttpPostedFile file)
        {
            if (file.ContentType.Contains("audio"))
            {
                return true;
            }

            string[] formats = new string[] { ".mp3", ".ogg" };

            return formats.Any(item => file.FileName.EndsWith(item, StringComparison.OrdinalIgnoreCase));
        }

        public static async Task UploadFileToStorage(Bitmap bmp, string fileName, IUploadImage record)
        {
            using (MemoryStream ms = new MemoryStream())
            {
                bmp.Save(ms, System.Drawing.Imaging.ImageFormat.Jpeg);
                ms.Position = 0;

                record.ImageUrl = await UploadFileToStorage(ms, fileName, IMAGE_CONTAINER);
            }

            float nSize = 256;
            float scale = Math.Min(nSize / bmp.Width, nSize / bmp.Height);

            using (Bitmap thumb = new Bitmap((int)(scale * bmp.Width), (int)(scale * bmp.Height)))
            {
                using (var graph = Graphics.FromImage(thumb))
                {
                    var scaleWidth = (int)(bmp.Width * scale);
                    var scaleHeight = (int)(bmp.Height * scale);
                    graph.DrawImage(bmp, new Rectangle(0, 0, scaleWidth, scaleHeight));
                }
                using (MemoryStream ms = new MemoryStream())
                {
                    thumb.Save(ms, System.Drawing.Imaging.ImageFormat.Jpeg);
                    ms.Position = 0;

                    record.ImageThumbUrl = await UploadFileToStorage(ms, fileName, THUMBNAIL_CONTAINER);
                }
            }
        }

        public static async Task<string> UploadFileToStorage(Stream fileStream, string fileName, string configContainer)
        {
            // Create storagecredentials object by reading the values from the configuration (appsettings.json)
            StorageCredentials storageCredentials = new StorageCredentials(config.AccountName, config.AccountKey);

            // Create cloudstorage account by passing the storagecredentials
            CloudStorageAccount storageAccount = new CloudStorageAccount(storageCredentials, true);

            // Create the blob client.
            CloudBlobClient blobClient = storageAccount.CreateCloudBlobClient();

            // Get reference to the blob container by passing the name by reading the value from the configuration (appsettings.json)
            CloudBlobContainer container = blobClient.GetContainerReference(configContainer);

            // Get the reference to the block blob from the container
            CloudBlockBlob blockBlob = container.GetBlockBlobReference(fileName);

            // Upload the file
            await blockBlob.UploadFromStreamAsync(fileStream);

            return await Task.FromResult(blockBlob.StorageUri.PrimaryUri.AbsoluteUri);
        }

        internal static async Task DeleteFile(string url, string configContainer)
        {
            try
            {
                string fileName = Path.GetFileName(url);
                //fileName = Uri.UnescapeDataString(fileName);
                fileName = Uri.UnescapeDataString(fileName); //Have to do it twice cause entity encodes while putting it in and taking it out :/

                CloudBlobContainer container = GetBlobContainer(configContainer);
                CloudBlockBlob blockBlob = container.GetBlockBlobReference(fileName);
                await blockBlob.DeleteAsync();

            }
            catch (Exception e)
            {
                throw e;
            }

            return;
        }
    }
}