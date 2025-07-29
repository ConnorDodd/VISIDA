using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.FCT;
using VISIDA_API.Models.InternalObjects;
using static VISIDA_API.Models.InternalObjects.WorkAssignation;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EStudy
    {
        public int Id { get; set; }
        public string Name { get; set; }
        public List<EWorkAssignation> Analysts { get; set; }
        public List<EHousehold> Households { get; set; }
        public EFoodCompositionTable FoodCompositionTable { get; set; }
        public DateTime? DeletedTime { get; set; }

        public string CountryCode { get; set; }
        public bool Transcribe { get; set; }
        public bool Translate { get; set; }
        public bool Gestalt { get; set; }
        public int GestaltMax { get; set; }

        public List<ETiming> WorkDone { get; set; }
        public int? RDAModelId { get; set; }
        public ERDAModel RDAModel{ get; set; }

        [JsonConverter(typeof(StringEnumConverter))]
        public AccessLevels AccessLevel { get; set; }

        public static EStudy ToShallowEStudy(Study s)
        {
            EStudy e = new EStudy()
            {
                Id = s.Id,
                Name = s.Name,
                Analysts = s.Assignees.Select(x => (EWorkAssignation)x).ToList(),// Select(x => x.LoginUser).Select(x => new ELoginUser() { Id = x.Id, UserName = x.UserName, x. }).ToList(),
                Households = s.Households.Select(x => (EHousehold)x).ToList(),
                FoodCompositionTable = s.FoodCompositionTable == null ? null : new EFoodCompositionTable()
                {
                    Id = s.FoodCompositionTable.Id,
                    Name = s.FoodCompositionTable.Name
                },
                DeletedTime = s.DeletedTime,
                CountryCode = s.CountryCode,
                Transcribe = s.Transcribe,
                Translate = s.Translate,
                Gestalt = s.Gestalt,
                GestaltMax = s.GestaltMax,
                RDAModel = s.RDAModel == null ? null : new ERDAModel()
                {
                    Description = s.RDAModel.Description,
                    Id = s.RDAModel.Id,
                    FieldData = s.RDAModel.FieldData,
                    Name = s.RDAModel.Name,
                },
                RDAModelId = s.RDAModel?.Id
            };

            return e;
        }

        public static implicit operator EStudy(Study s)
        {
            EStudy e = new EStudy()
            {
                Id = s.Id,
                Name = s.Name,
                Analysts = s.Assignees.Select(x => (EWorkAssignation)x).ToList(),// Select(x => x.LoginUser).Select(x => new ELoginUser() { Id = x.Id, UserName = x.UserName, x. }).ToList(),
                Households = s.Households.Select(x => (EHousehold)x).ToList(),
                FoodCompositionTable = s.FoodCompositionTable == null ? null : new EFoodCompositionTable()
                {
                    Id = s.FoodCompositionTable.Id,
                    Name = s.FoodCompositionTable.Name },
                DeletedTime = s.DeletedTime,
                CountryCode = s.CountryCode,
                Transcribe = s.Transcribe,
                Translate = s.Translate,
                Gestalt = s.Gestalt,
                GestaltMax = s.GestaltMax,
                RDAModel = s.RDAModel,
                RDAModelId = s.RDAModel?.Id
            };

            return e;
        }
    }
}