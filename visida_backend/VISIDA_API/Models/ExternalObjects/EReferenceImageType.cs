using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EReferenceImageType
    {
        public int Id { get; set; }
        public string Name { get; set; }
        public virtual ICollection<EReferenceImage> Images { get; set; } = new List<EReferenceImage>();

        public static implicit operator EReferenceImageType(FCT.ReferenceImageType r)
        {
            var ret = new EReferenceImageType()
            {
                Id = r.Id,
                Name = r.Name
            };

            foreach (var i in r.Images)
            {
                ret.Images.Add(new EReferenceImage()
                {
                    Id = i.Id,
                    Description = i.Description,
                    ImageUrl = i.ImageUrl,
                    SizeGrams = i.SizeGrams,
                    OriginId = i.OriginId
                });
            }
            ret.Images = ret.Images.OrderBy(x => x.SizeGrams).ToList();

            return ret;
        }
    }
}