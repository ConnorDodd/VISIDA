package bo.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import bo.db.entity.HouseholdMember;

/**
 * Created by jnc985 on 14-Dec-17.
 */

@Dao
public interface HouseholdMemberDao {
    @Query("SELECT * FROM householdmember")
    LiveData<List<HouseholdMember>> getAll();

    @Query("SELECT * FROM HouseholdMember WHERE uid IN (:ids)")
    List<HouseholdMember> getHouseholdMembers(List<Long> ids);

    @Query("SELECT * FROM householdmember")
    List<HouseholdMember> getHouseholdMembers();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long[] insert(HouseholdMember... householdMembers);

    @Query("SELECT * from householdmember WHERE uid IS :householdMemberId LIMIT 1")
    HouseholdMember getHouseholdMember(long householdMemberId);

    @Query("SELECT * FROM HouseholdMember WHERE participantHouseholdMemberId IS :ppid LIMIT 1")
    HouseholdMember getHouseholdMember(String ppid);

    @Update
    void update(HouseholdMember... hms);

    @Delete
    void delete(HouseholdMember... hms);

    @Query("SELECT CASE WHEN EXISTS(" +
                "SELECT participantHouseholdMemberId " +
                "FROM HouseholdMember " +
                "WHERE participantHouseholdMemberId LIKE :ppid) " +
            "THEN CAST(1 as BIT) ELSE CAST(0 as BIT) END;")
    Integer participantIdExists(String ppid);

    @Query("SELECT avatar from HouseholdMember where uid is :hmId")
    String getHouseholdMemberImagePath(Long hmId);

    @Query("SELECT * FROM HouseholdMember WHERE age < 5 LIMIT 1")
    HouseholdMember childExists();

    @Query("SELECT count(*) from HouseholdMember WHERE isBreastfed IS 1;")
    Integer countBreastfed();


    //@Insert
    //void insert(List<HouseholdMember> householdMembers);
}
