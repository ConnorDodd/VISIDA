using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Http.Formatting;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;
using System.Web;
using Newtonsoft.Json;

namespace VISIDA_API.Providers
{
    public class DepthLimitedJsonFormatter : JsonMediaTypeFormatter
    {
        private DepthLimitedContractResolver _contractResolver;

        public DepthLimitedJsonFormatter(DepthLimitedContractResolver contractResolver)
        {
            _contractResolver = contractResolver;
            SerializerSettings.ContractResolver = _contractResolver;
        }

        public override JsonWriter CreateJsonWriter(Type type, Stream writeStream, Encoding effectiveEncoding)
        {
            //return base.CreateJsonWriter(type, writeStream, effectiveEncoding);
            var writer = new DepthLimitedJsonWriter(new StreamWriter(writeStream, effectiveEncoding));
            Func<bool> include = () => writer.CurrentDepth <= 5;
            _contractResolver.IncludeProperty = include;
            return writer;
        }
    }
}