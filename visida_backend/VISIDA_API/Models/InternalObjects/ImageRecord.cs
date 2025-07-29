using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects.Portion;
using VISIDA_API.Models.User;

namespace VISIDA_API.Models.InternalObjects
{
    public class ImageRecord : IUploadImage, IUploadAudio
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        public virtual Household Household { get; set; }
        public virtual HouseholdMeal Meal { get; set; }
        public virtual HouseholdGuestInfo GuestInfo { get; set; }
        public DateTime CaptureTime { get; set; }

        //public virtual ICollection<EatRecord> EatRecords{ get; set; }

        public enum RecordTypes { EatRecord, Ingredient, Leftovers, Test }
        public RecordTypes RecordType { get; set; }

        public bool Hidden { get; set; } = false;

        [Column(TypeName = "VARCHAR"), StringLength(256), Index(IsUnique = false)]
        public string ImageName { get; set; }
        [Column(TypeName = "VARCHAR"), StringLength(256)]
        public string ImageUrl { get; set; }
        [Column(TypeName = "VARCHAR"), StringLength(256)]
        public string ImageThumbUrl { get; set; }
        [Column(TypeName = "VARCHAR"), StringLength(256)]
        public string ImageUrlUpdated { get; set; }

        [Column(TypeName = "VARCHAR"), StringLength(256)]
        public string AudioName { get; set; }
        [Column(TypeName = "VARCHAR"), StringLength(256)]
        public string AudioUrl { get; set; }
        public string AudioUrlUpdated { get; set; }

        [Column(TypeName = "NVARCHAR"), StringLength(1024)]
        public string NTranscript { get; set; } //Initial speech-to-text transcription
        [Column(TypeName = "VARCHAR"), StringLength(1024)]
        public string Transcript { get; set; } //Initial english translation of NTranscript
        [Column(TypeName = "NVARCHAR"), StringLength(1024)]
        public string TextDescription { get; set; } //Manually updated english description

        //Test stuff
        [Column(TypeName = "NVARCHAR"), StringLength(1024)]
        public string ManualTranscript { get; set; } //Manually updated native transcript
        [Column(TypeName = "VARCHAR"), StringLength(1024)]
        public string Translation { get; set; }

        public virtual ImageHomography Homography { get; set; }
        public bool IsFiducialPresent { get; set; }

        public virtual ICollection<FoodItem> FoodItems { get; set; }
        public virtual ICollection<Comment> Comments { get; set; }
        //public virtual ICollection<PortionUpdate> PortionUpdates { get; set; }
        public virtual ICollection<RecordHistory> Updates { get; set; }
        public virtual ICollection<CookRecipe> Recipes { get; set; }
        [InverseProperty("ImageRecord")]
        public virtual ICollection<EatRecord> EatRecords { get; set; }

        public bool IsCompleted { get; set; }
        public bool IsLeftovers { get; set; }
        public enum AnnotationStatuses { None, Updated, Created };
        public AnnotationStatuses AnnotationStatus { get; set; }
        public AnnotatedStatuses AnnotatedStatus { get; set; }

        //public int Participants { get; set; }

        public LoginUser LockedBy { get; set; }
        public DateTime? LockTimestamp { get; set; }
        public bool Is24HR { get; set; }

        //Used to link matching annotated records when parsing into image records.
        [NotMapped]
        public int GuestInfoId { get; set; }

        [NotMapped]
        public int ParticipantCount
        {
            get
            {
                return GuestInfo?.TotalParticipants ?? (RecordType == RecordTypes.EatRecord ? 1 : 0);
            }
        }


//Id
//Household
//Meal
//GuestInfo
//CaptureTime
//RecordType
//Hidden
//ImageName
//ImageUrl
//ImageThumbUrl
//AudioName
//AudioUrl
//NTranscript
//Transcript
//TextDescription
//ManualTranscript
//Translation
//Homography
//IsFiducialPresent
//FoodItems
//Comments
//Updates
//Recipes
//IsCompleted
//IsLeftovers
//AnnotationStatuses
//AnnotationStatus
//AnnotatedStatus
//Participants
//LockedBy
//LockTimestamp
//GuestInfoId
    }
}