using Newtonsoft.Json;
using Newtonsoft.Json.Serialization;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Web;

namespace VISIDA_API.Providers
{
    public class DepthLimitedContractResolver : DefaultContractResolver
    {
        private Func<bool> _includeProperty;

        public Func<bool> IncludeProperty
        {
            set
            {
                _includeProperty = value;
            }
        }

        protected override JsonProperty CreateProperty(
            MemberInfo member, MemberSerialization memberSerialization)
        {
            var property = base.CreateProperty(member, memberSerialization);
            var shouldSerialize = property.ShouldSerialize;
            property.ShouldSerialize = obj => _includeProperty() &&
                                              (shouldSerialize == null ||
                                               shouldSerialize(obj));
            return property;
        }
    }
}