using Google.Cloud.Speech.V1;
using NAudio.Wave;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using System.Web;
using System.Web.Http;
using VISIDA_API.Helpers;
using VISIDA_API.Models;
using VISIDA_API.Models.InternalObjects;
using VISIDA_API.Models.OpenCV;

using Google.Cloud.Translation.V2;
using Newtonsoft.Json;
using System.IO.Compression;

namespace VISIDA_API.Controllers
{
    [AllowAnonymous]
    public class UploadController : ApiController
    {
        private VISIDA_APIContext db = new VISIDA_APIContext();

        //[Route("api/Upload/PostImageRecord")]
        //public async Task<HttpResponseMessage> PostImageRecord([FromUri]bool overwrite)
        //{
        //    string url = "";
        //    try
        //    {
        //        var httpRequest = HttpContext.Current.Request;

        //        foreach (string fileName in httpRequest.Files)
        //        {
        //            HttpPostedFile postedFile = httpRequest.Files[fileName];
        //            if (postedFile != null && postedFile.ContentLength > 0)
        //            {
        //                //Check image type here
        //                if (!postedFile.ContentType.Substring(0, postedFile.ContentType.IndexOf('/')).Equals("image"))
        //                    return Request.CreateResponse(HttpStatusCode.BadRequest, "Not a valid content type.");

        //                IUploadImage image = db.ImageRecords.FirstOrDefault(r => r.ImageName.Equals(postedFile.FileName));
        //                if (image == null)
        //                    image = db.CookRecipes.FirstOrDefault(r => r.ImageName.Equals(postedFile.FileName));
        //                if (image != null)
        //                {
        //                    if (image.ImageUrl != null && !overwrite)
        //                        return Request.CreateResponse(HttpStatusCode.OK, "Content already exists");


        //                    using (Bitmap pImage = new Bitmap(postedFile.InputStream))
        //                    {

        //                        PropertyItem pi;
        //                        Int16 rot;
        //                        if (pImage.PropertyIdList.Contains(0x0112))
        //                        {
        //                            pi = pImage.GetPropertyItem(0x0112);
        //                            if (pi.Type == 3 && pi.Value.Length >=2)
        //                            {
        //                                rot = BitConverter.ToInt16(pi.Value, 0);
        //                                switch (pi.Value[0])
        //                                {
        //                                    case 6:
        //                                        pImage.RotateFlip(RotateFlipType.Rotate90FlipNone);
        //                                        break;
        //                                    case 8:
        //                                        pImage.RotateFlip(RotateFlipType.Rotate270FlipNone);
        //                                        break;
        //                                    case 3:
        //                                        pImage.RotateFlip(RotateFlipType.Rotate180FlipNone);
        //                                        break;
        //                                }
        //                                pi.Value = BitConverter.GetBytes(1);
        //                                pImage.SetPropertyItem(pi);
        //                            }
        //                        }

        //                        url = CloudImage.UploadImage(postedFile.FileName, pImage);
        //                        if (url == null)
        //                            return Request.CreateResponse(HttpStatusCode.InternalServerError, "Could not upload image to cloud storage.");
        //                        image.ImageUrl = url;

        //                        var homography = FiducialFinder.FindImageHomography(pImage);
        //                        image.Homography = homography;

        //                        //float nSize = 1024;
        //                        //float scale = Math.Min(nSize / pImage.Width, nSize / pImage.Height);

        //                        //using (Bitmap bmp = new Bitmap((int)(scale * pImage.Width), (int)(scale * pImage.Height)))
        //                        //{
        //                        //    using (var graph = Graphics.FromImage(bmp))
        //                        //    {
        //                        //        var scaleWidth = (int)(pImage.Width * scale);
        //                        //        var scaleHeight = (int)(pImage.Height * scale);
        //                        //        //graph.DrawImage()
        //                        //        graph.DrawImage(pImage, new Rectangle(0, 0, scaleWidth, scaleHeight));
        //                        //        //bmp.Save("C:/Users/Connor/Pictures/Updated_fiducials/resizeTestImage.png");

        //                        //        image.IsFiducialPresent = FiducialRecognition.IsFiducial(bmp, image);
        //                        //    }
        //                        //}
        //                    }

        //                    db.SaveChanges();
        //                }
        //                else
        //                    return Request.CreateResponse(HttpStatusCode.BadRequest, "There is no record of an image with that name. It may have been deleted");
        //            }
        //        }

        //        return Request.CreateResponse(HttpStatusCode.OK, url);
        //    }
        //    catch (Exception ex)
        //    {
        //        return Request.CreateResponse(HttpStatusCode.BadRequest, "There was an error uploading the image.");
        //    }
        //}

        [Route("api/Upload/PostImageRecord")]
        public async Task<HttpResponseMessage> PostImageRecord([FromUri]bool overwrite)
        {
            string url = "";
            try
            {
                var httpRequest = HttpContext.Current.Request;

                if (httpRequest.Files.Count == 0)
                    return Request.CreateResponse(HttpStatusCode.BadRequest, "No files were found");
                foreach (string fileName in httpRequest.Files)
                {
                    HttpPostedFile postedFile = httpRequest.Files[fileName];
                    if (postedFile != null && postedFile.ContentLength > 0)
                    {
                        //Check image type here
                        if (!StorageHelper.IsImage(postedFile))
                            return Request.CreateResponse(HttpStatusCode.BadRequest, "Not a valid content type.");

                        List<IUploadImage> records = new List<IUploadImage>();
                        var images = db.ImageRecords.Where(r => postedFile.FileName.Equals(r.ImageName)).ToList();
                        records.AddRange(images);
                        var recipes = db.CookRecipes.Where(r => postedFile.FileName.Equals(r.ImageName)).ToList();
                        records.AddRange(recipes);
                        //IUploadImage image = db.ImageRecords.FirstOrDefault(r => r.ImageName.Equals(postedFile.FileName));
                        //if (image == null)
                            //image = db.CookRecipes.FirstOrDefault(r => r.ImageName.Equals(postedFile.FileName));
                        foreach (var record in records)
                        //if (image != null)
                        {
                            if (record.ImageUrl != null && !overwrite)
                                return Request.CreateResponse(HttpStatusCode.OK, "Content already exists");

                            try
                            {
                                using (Bitmap pImage = new Bitmap(postedFile.InputStream))
                                {

                                    PropertyItem pi;
                                    Int16 rot;
                                    if (pImage.PropertyIdList.Contains(0x0112))
                                    {
                                        pi = pImage.GetPropertyItem(0x0112);
                                        if (pi.Type == 3 && pi.Value.Length >= 2)
                                        {
                                            rot = BitConverter.ToInt16(pi.Value, 0);
                                            switch (pi.Value[0])
                                            {
                                                case 6:
                                                    pImage.RotateFlip(RotateFlipType.Rotate90FlipNone);
                                                    break;
                                                case 8:
                                                    pImage.RotateFlip(RotateFlipType.Rotate270FlipNone);
                                                    break;
                                                case 3:
                                                    pImage.RotateFlip(RotateFlipType.Rotate180FlipNone);
                                                    break;
                                            }
                                            pi.Value = BitConverter.GetBytes(1);
                                            pImage.SetPropertyItem(pi);
                                        }
                                    }

                                    string newName = string.Format("{0}_{1}", Guid.NewGuid().ToString().Substring(0, 8), postedFile.FileName);
                                    if (record.ImageUrl != null && overwrite)
                                        newName = record.ImageUrl.Substring(record.ImageUrl.LastIndexOf('/')+1);
                                    await StorageHelper.UploadFileToStorage(pImage, newName, record);

                                    if (record.ImageUrl == null)
                                        return Request.CreateResponse(HttpStatusCode.InternalServerError, "Could not upload image to cloud storage.");
                                    url = record.ImageUrl;

                                    try
                                    {
                                        var homography = FiducialFinder.FindImageHomography(pImage);
                                        record.Homography = homography;
                                        record.IsFiducialPresent = homography != null;
                                    }
                                    catch (Exception e)
                                    {
                                        record.Homography = null;
                                    }
                                }

                                db.SaveChanges();
                            }
                            catch (Exception e)
                            {
                                return Request.CreateResponse(HttpStatusCode.BadRequest, "Could not upload");
                            }
                        }
                    }
                }
                return Request.CreateResponse(HttpStatusCode.OK, url);
            }
            catch (Exception ex)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, "There was an error uploading the image.");
            }
        }

        [Route("api/Upload/PostAudioRecord")]
        public async Task<HttpResponseMessage> PostAudioRecord([FromUri]bool overwrite)
        {
            string url = "";
            string localFile = "";
            try
            {
                var httpRequest = HttpContext.Current.Request;

                foreach (string fileName in httpRequest.Files)
                {
                    HttpPostedFile postedFile = httpRequest.Files[fileName];
                    localFile = Path.GetTempFileName();//Path.Combine(Path.GetTempFileName, "/App_Data/Audio/" + postedFile.FileName);
                    try
                    {
                        postedFile.SaveAs(localFile);
                    }
                    catch (Exception e)
                    {
                        if (!File.Exists(localFile))
                            return Request.CreateErrorResponse(HttpStatusCode.InternalServerError, e);
                    }

                    if (postedFile != null && postedFile.ContentLength > 0)
                    {
                        //Check file type here
                        if (!StorageHelper.IsAudio(postedFile))
                            return Request.CreateResponse(HttpStatusCode.BadRequest, "Not a valid content type.");

                        //IUploadAudio record = db.ImageRecords.Include(x => x.Household).Include(x => x.Household.Study).FirstOrDefault(r => r.AudioName.Equals(postedFile.FileName));
                        //if (record == null)
                        //    record = db.CookRecipes.FirstOrDefault(r => r.AudioName.Equals(postedFile.FileName));
                        List<IUploadAudio> records = new List<IUploadAudio>();
                        var images = db.ImageRecords.Where(r => postedFile.FileName.Equals(r.AudioName)).ToList();
                        records.AddRange(images);
                        var recipes = db.CookRecipes.Where(r => postedFile.FileName.Equals(r.AudioName)).ToList();
                        records.AddRange(recipes);
                        //if (record != null)
                        if (records.Count == 0)
                            return Request.CreateResponse(HttpStatusCode.BadRequest, "There is no record of audio with that name.");
                        foreach (var record in records)
                        {
                            if (record.AudioUrl != null)
                            {
                                if (!overwrite)
                                    return Request.CreateResponse(HttpStatusCode.OK, "Content already exists");
                                else
                                    await StorageHelper.DeleteFile(record.AudioUrl, StorageHelper.AUDIO_CONTAINER);
                            }

                            string newName = string.Format("{0}_{1}", Guid.NewGuid().ToString().Substring(0, 8), postedFile.FileName);
                            //if (record.AudioUrl != null && overwrite)
                                //newName = record.AudioUrl.Substring(record.AudioUrl.LastIndexOf('/') + 1);
                            url = await StorageHelper.UploadFileToStorage(postedFile.InputStream, newName, StorageHelper.AUDIO_CONTAINER);
                            if (url != null)
                            {
                                record.AudioUrl = url;

                                var study = record.Household.Study;
                                if (study != null && study.Transcribe && !String.IsNullOrEmpty(study.CountryCode))
                                {
                                    int sampleRate = 0;
                                    MemoryStream outStream = new MemoryStream();

                                    using (MediaFoundationReader reader = new MediaFoundationReader(localFile))
                                    {
                                        WaveFileWriter.WriteWavFileToStream(outStream, reader);

                                        sampleRate = reader.WaveFormat.SampleRate;
                                    }

                                    var speech = SpeechClient.Create();
                                    var response = speech.Recognize(new RecognitionConfig()
                                    {
                                        Encoding = RecognitionConfig.Types.AudioEncoding.Linear16,
                                        SampleRateHertz = sampleRate,
                                        LanguageCode = study.CountryCode,
                                        MaxAlternatives = 1
                                    }, RecognitionAudio.FromStream(outStream));
                                    if (response.Results.Count > 0)
                                    {
                                        var result = response.Results[0];
                                        if (result.Alternatives.Count > 0)
                                        {
                                            var alternative = result.Alternatives[0];
                                            if (study.CountryCode.Equals("en-AU"))
                                                record.Transcript = alternative.Transcript;
                                            else
                                                record.NTranscript = alternative.Transcript;
                                        }
                                    }
                                    outStream.Dispose();
                                }
                                if (study != null && study.Translate && record.NTranscript != null)
                                {
                                    TranslationResult result = null;
                                    var lc = study.CountryCode.Substring(0, 2);
                                    TranslationClient client = TranslationClient.Create();
                                    result = client.TranslateText(
                                        record.NTranscript,
                                        targetLanguage: "en",
                                        sourceLanguage: lc,
                                        model: TranslationModel.NeuralMachineTranslation
                                    );

                                    if (result != null)
                                        record.Transcript = result.TranslatedText;
                                }
                                db.SaveChanges();
                            }
                            else
                                return Request.CreateResponse(HttpStatusCode.InternalServerError, "Could not upload audio to cloud storage.");
                        }
                    }
                }

                return Request.CreateResponse(HttpStatusCode.OK, url);
            }
            catch (Exception e)
            {
                 return Request.CreateResponse(HttpStatusCode.BadRequest, "There was an error uploading the audio. " + e.Message);
            }
            finally
            {
                try
                {
                    if (File.Exists(localFile))
                    {
                        File.Delete(localFile);
                    }
                }
                catch (Exception ex)
                {

                }
            }
        }
        
        [Route("api/Upload/PostZip")]
        public async Task<HttpResponseMessage> PostZip([FromUri] bool overwrite)
        {
            try
            {
                var httpRequest = HttpContext.Current.Request;

                if (httpRequest.Files.Count == 0)
                {
                    // return bad request
                }
                HttpPostedFile postedZip = httpRequest.Files[0];
                ZipArchive zipArchive = new ZipArchive(postedZip.InputStream);
                ZipArchiveEntry jsonFile = zipArchive.Entries.FirstOrDefault(x => x.Name.Contains(".json"));

                System.Diagnostics.Trace.WriteLine(DeserializeFromStream(jsonFile.Open()));

                Household household = (Household) DeserializeFromStream(jsonFile.Open());
                return Request.CreateResponse(HttpStatusCode.OK, household);

            } catch (Exception e)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, "caugh");
            }
            
        }
        public static object DeserializeFromStream(Stream stream)
        {
            var serializer = new JsonSerializer();

            using (var sr = new StreamReader(stream))
            using (var jsonTextReader = new JsonTextReader(sr))
            {
                return serializer.Deserialize(jsonTextReader);
            }
        }


        [Route("api/Upload/PostLogFile")]
        public IHttpActionResult PostLogFile()
        {
            string guid, timeStr, log = null;
            DateTime time;
            try
            {
                Regex rgx = new Regex("([a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12})");
                log = Request.Content.ReadAsStringAsync().Result;
                guid = rgx.Match(log).Value;

                Regex timeRgx = new Regex("([0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3})");
                timeStr = timeRgx.Match(log).Value;
                time = DateTime.ParseExact(timeStr, "MM-dd HH:mm:ss.fff", CultureInfo.InvariantCulture);
            }
            catch (Exception e)
            {
                if (log != null && log.IndexOf("About.txt") >= 0)
                    return Ok("About.txt file is unused for now");
                return BadRequest("Log file has incorrect format");
            }

            var hh = db.Households.FirstOrDefault(x => x.Guid.Equals(guid));
            if (hh == null)
                return BadRequest("This log does not correspond to any uploaded household");
            hh.UsageLog = new UsageLogFile
            {
                RawData = log,
                LogCreationTime = time
            };

            db.SaveChanges();

            return Ok();
        }

        public static Stream FormatAudio(HttpPostedFile postedFile)
        {
            MemoryStream output = new MemoryStream();
            //using (MediaFoundationReader reader = new MediaFoundationReader(postedFile.InputStream))
            postedFile.InputStream.Position = 0;
            using (var provider = new WaveFormatConversionProvider(new WaveFormat(48000, 1), new WaveFileReader(postedFile.InputStream)))
            {
                WaveFileWriter.WriteWavFileToStream(output, provider);
            }
            //using (WaveStream wav = WaveFormatConversionStream.CreatePcmStream(new WaveFileReader(postedFile.InputStream)))
            //{
            //    WaveFileWriter.WriteWavFileToStream(output, new StereoToMonoProvider16())
            //}
            output.Position = 0;
            return output;
        }

        public static Stream Mp3ToWav(Stream input)
        {
            MemoryStream output = new MemoryStream();
            input.Position = 0;
            using (Mp3FileReader reader = new Mp3FileReader(input))
            {
                reader.Position = 0;


                using (WaveStream pcmStream = WaveFormatConversionStream.CreatePcmStream(reader))
                {
                    pcmStream.Position = 0;
                    var stereo = new StereoToMonoProvider16(pcmStream);
                    WaveFileWriter.WriteWavFileToStream(output, stereo);
                }
            }

            output.Position = 0;
            return output;
        }

        public static Stream M4AToWav(string input)
        {
            using (MediaFoundationReader reader = new MediaFoundationReader(input))
            {
                //using (WaveStream pcmStream = WaveFormatConversionStream.CreatePcmStream(reader))
                //{
                //    WaveFileWriter.WriteWavFileToStream(output, pcmStream);
                //}
                reader.Position = 0;
                var waveFormat = new WaveFormat(16000, 16, 1);
                var output = new RawSourceWaveStream(reader, waveFormat);
                //WaveFileWriter.WriteWavFileToStream(output, reader);

                return output;
            }
        }

        [Route("api/RetranscribeEverything"), HttpGet]
        public IHttpActionResult Retranscribe()
        {
            var records = db.ImageRecords.Where(x => !string.IsNullOrEmpty(x.AudioUrl) && x.NTranscript == null && (x.Household.Study_Id == 37 || x.Household.Study_Id == 38)).OrderBy(x => x.Id).ToList();
            List<TranscriptionRequest> transcriptions = new List<TranscriptionRequest>();
            string jsonSoFar = null;
            if (File.Exists("D:/VISIDA/visida_backend/transcription.json"))
                jsonSoFar = File.ReadAllText("D:/VISIDA/visida_backend/transcription.json");
            if (!string.IsNullOrEmpty(jsonSoFar))
                transcriptions = JsonConvert.DeserializeObject<List<TranscriptionRequest>>(jsonSoFar);
            string json;


            using (var client = new WebClient())
            {
                var count = 0;
                foreach(var record in records)
                {
                    try
                    {
                    //var path = Path.Combine(Application Server.MapPath("~/Content/Upload"), fileName);
                    client.DownloadFile(record.AudioUrl, "temp.mp3");

                    int sampleRate = 0;
                    MemoryStream outStream = new MemoryStream();

                    using (MediaFoundationReader reader = new MediaFoundationReader("temp.mp3"))
                    {
                        WaveFileWriter.WriteWavFileToStream(outStream, reader);
                        sampleRate = reader.WaveFormat.SampleRate;

                        var speech = SpeechClient.Create();
                        var response = speech.Recognize(new RecognitionConfig()
                        {
                            Encoding = RecognitionConfig.Types.AudioEncoding.Linear16,
                            SampleRateHertz = sampleRate,
                            LanguageCode = "km-KH",
                            MaxAlternatives = 5
                        }, RecognitionAudio.FromStream(outStream));

                        transcriptions.Add(new TranscriptionRequest()
                        {
                            RecordId = record.Id,
                            Response = response,
                            SampleRate = sampleRate
                        });

                        string transcription = null;
                        foreach (var result in response.Results)
                        {
                            transcription += result.Alternatives[0].Transcript;
                        }
                            record.NTranscript = transcription ?? "failed" ;
                    }

                    outStream.Dispose();
                    File.Delete("temp.mp3");

                    if (++count % 10 == 0)
                    {
                        db.SaveChanges();
                        json = JsonConvert.SerializeObject(transcriptions, Formatting.Indented);
                        File.WriteAllText("D:/VISIDA/visida_backend/transcription.json", json);
                    }
                    } catch (System.Runtime.InteropServices.COMException e)
                    {
                        record.NTranscript = "exception";
                        transcriptions.Add(new TranscriptionRequest()
                        {
                            RecordId = record.Id,
                            Response = new RecognizeResponse(),
                            SampleRate = -1
                        });
                    } catch (Exception e)
                    {
                        return InternalServerError(e);
                    }
                }
            }

            db.SaveChanges();
            json = JsonConvert.SerializeObject(transcriptions, Formatting.Indented);
            File.WriteAllText("D:/VISIDA/visida_backend/transcription.json", json);

            return Ok();
        }

        class TranscriptionRequest
    {
        public int RecordId { get; set; }
        public int SampleRate { get; set; }
        public RecognizeResponse Response { get; set; }
    }

        [Route("api/Upload/PostAPK"), AllowAnonymous]
        public IHttpActionResult PostAPK()
        {
            var httpRequest = HttpContext.Current.Request;

            foreach (string fileName in httpRequest.Files)
            {
                HttpPostedFile postedFile = httpRequest.Files[fileName];
                if (postedFile != null && postedFile.ContentLength > 0 && postedFile.FileName.EndsWith(".apk"))
                {
                    var isKhmer = postedFile.FileName.ToLower().Contains("khmer");
                    var filePath = HttpContext.Current.Server.MapPath("~/" + (isKhmer ? "khmer_apk.apk" : "english_apk.apk"));
                    postedFile.SaveAs(filePath);
                }
                else
                {
                    return BadRequest();
                }
            }
            return Ok();
        }

        [Route("api/Upload/GetAPK")]
        public HttpResponseMessage GetAPK([FromUri] bool isKhmer)
        {
            string fileName = (isKhmer ? "khmer_apk.apk" : "english_apk.apk");
            string filePath = HttpContext.Current.Server.MapPath("~/" + fileName);

            var dataBytes = File.ReadAllBytes(filePath);
            var dataStream = new MemoryStream(dataBytes);

            HttpResponseMessage httpResponseMessage = Request.CreateResponse(HttpStatusCode.OK);
            httpResponseMessage.Content = new StreamContent(dataStream);
            httpResponseMessage.Content.Headers.ContentDisposition = new System.Net.Http.Headers.ContentDispositionHeaderValue("attachment");
            httpResponseMessage.Content.Headers.ContentDisposition.FileName = fileName;
            httpResponseMessage.Content.Headers.ContentType = new System.Net.Http.Headers.MediaTypeHeaderValue("application/octet-stream");

            return httpResponseMessage;
        }
    }
}
