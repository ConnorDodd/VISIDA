namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class AddGuestInfo : DbMigration
    {
        public override void Up()
        {
            CreateTable(
                "dbo.HouseholdGuestInfoes",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        AdultFemaleGuests = c.Int(nullable: false),
                        AdultMaleGuests = c.Int(nullable: false),
                        ChildGuests = c.Int(nullable: false),
                        ModAdultFemaleGuests = c.Int(),
                        ModAdultMaleGuests = c.Int(),
                        ModChildGuests = c.Int(),
                        Household_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.Households", t => t.Household_Id)
                .Index(t => t.Household_Id);
            
            AddColumn("dbo.ImageRecords", "GuestInfo_Id", c => c.Int());
            CreateIndex("dbo.ImageRecords", "GuestInfo_Id");
            AddForeignKey("dbo.ImageRecords", "GuestInfo_Id", "dbo.HouseholdGuestInfoes", "Id");
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.HouseholdGuestInfoes", "Household_Id", "dbo.Households");
            DropForeignKey("dbo.ImageRecords", "GuestInfo_Id", "dbo.HouseholdGuestInfoes");
            DropIndex("dbo.ImageRecords", new[] { "GuestInfo_Id" });
            DropIndex("dbo.HouseholdGuestInfoes", new[] { "Household_Id" });
            DropColumn("dbo.ImageRecords", "GuestInfo_Id");
            DropTable("dbo.HouseholdGuestInfoes");
        }
    }
}
