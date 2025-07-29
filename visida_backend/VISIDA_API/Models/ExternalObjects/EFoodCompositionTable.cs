using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EFoodCompositionTable
    {
        public int Id { get; set; }
        public string Name { get; set; }
        public bool IsPublic { get; set; }
        public EFoodCompositionTable()
        {

        }
        public EFoodCompositionTable(int id, string name, bool isPublic)
        {
            this.Id = id;
            this.Name = name;
            this.IsPublic = isPublic;
        }
    }
}