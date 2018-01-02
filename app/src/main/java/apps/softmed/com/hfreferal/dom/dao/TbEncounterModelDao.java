package apps.softmed.com.hfreferal.dom.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import apps.softmed.com.hfreferal.dom.objects.TbEncounters;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by issy on 12/30/17.
 *
 * @issyzac issyzac.iz@gmail.com
 * On Project HFReferralApp
 */

@Dao
public interface TbEncounterModelDao {

    @Query("select * from TbEncounters where tbPatientID = :tbPatientId")
    List<TbEncounters> getEncounterByPatientID(long tbPatientId);

    @Query("select * from TbEncounters")
    LiveData<List<TbEncounters>> getAllEncounters();

    @Query("select * from TbEncounters where tbPatientID = :tbPatientTempId")
    List<TbEncounters> getAllEncountersList(String tbPatientTempId);

    @Insert(onConflict = REPLACE)
    void addEncounter(TbEncounters tbEncounters);

    @Query("select * from TbEncounters where encounterMonth = :encounterMonth and tbPatientID =:tbPatientID")
    List<TbEncounters> getMonthEncounter(String encounterMonth, String tbPatientID);

    @Update
    void updatePreviousMonthMedicationStatus(TbEncounters tbEncounters);

    @Delete
    void deleteAnEncounter(TbEncounters encounters);

}
