using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.FCT;

namespace VISIDA_API.Models.ExternalObjects
{
    public class ERDAModel
    {
        public int Id { get; set; }
        public string Name { get; set; }
        public string Description { get; set; }

        public string FieldData { get; set; }

        public virtual ICollection<ERDA> RDAs { get; set; }

        public static implicit operator ERDAModel(RDAModel m)
        {
            if (m == null)
                return null;
            return new ERDAModel()
            {
                Id = m.Id,
                Name = m.Name,
                Description = m.Description,
                FieldData = m.FieldData ?? "",
                RDAs = m.RDAs.Select(x => (ERDA)x).ToList()
            };
        }
    }
}