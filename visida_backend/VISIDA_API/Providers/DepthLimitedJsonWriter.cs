using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Web;

namespace VISIDA_API.Providers
{
    public class DepthLimitedJsonWriter : JsonTextWriter
    {
        public int? MaxDepth { get; set; }
        public int MaxObservedDepth { get; private set; }

        public DepthLimitedJsonWriter(TextWriter writer) : base(writer) { }

        public int CurrentDepth { get; private set; }

        public override void WriteStartObject()
        {
            CurrentDepth++;
            base.WriteStartObject();
        }

        public override void WriteEndObject()
        {
            CurrentDepth--;
            base.WriteEndObject();
        }
    }
}