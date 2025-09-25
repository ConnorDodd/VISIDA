package instruction;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import au.edu.newcastle.jnc985.visida.R;
import bo.State;
import bo.Utilities;

/**
 * Created by jnc985 on 16-Apr-18.
 * Handler for creating instruction Dialogs.
 */

public class InstructionHandler {

    public static final String INSTRUCTION = "instruction";
    // Handling of instruction videos that are on the select household member finalize section only
    // Select household member is reused, so we need to consider the use in finalize as different
    public static final String SELECT_HOUSEHOLD_MEMBER_FINALIZE = "SELECT_HOUSEHOLD_MEMBER_FINALIZE";
    public static final String SELECT_HOUSEHOLD_MEMBER_BREASTFEED = "SELECT_HOUSEHOLD_MEMBER_BREASTFEED";
    public static final String SELECT_HOUSEHOLD_MEMBER_EAT = "SELECT_HOUSEHOLD_MEMBER_EAT";
    //https://ezgif.com/optimize/ezgif-1-d4ceefcc4e.gif
    private static Map<String, Instruction> instructions = new HashMap<String, Instruction>(){{
        put(au.edu.newcastle.jnc985.visida.activity.AudioActivity.class.getName(), new Instruction(R.array.inst_media_record_audio, R.array.instruction_audioactivity, R.array.inst_media_record_audio_audio));
        put(au.edu.newcastle.jnc985.visida.activity.BreastfeedActivity.class.getName(), new Instruction(R.array.inst_media_breastfeed, R.array.instruction_breastfeedactivity, R.array.inst_media_breastfeed_audio));
        put(au.edu.newcastle.jnc985.visida.activity.CameraActivity.class.getName(), new Instruction(R.array.inst_media_camera, R.array.instruction_cameraactivity, R.array.inst_media_camera_audio));
        put(au.edu.newcastle.jnc985.visida.activity.CreateHouseholdMemberActivity.class.getName(), new Instruction(R.array.inst_media_ph, R.array.instruction_createhouseholdmember, R.array.inst_media_ph_audio));
        put(au.edu.newcastle.jnc985.visida.activity.CreateRecipeActivity.class.getName(), new Instruction(R.array.inst_create_recipe, R.array.instruction_createrecipe, R.array.inst_create_recipe_audio));
        put(au.edu.newcastle.jnc985.visida.activity.EatingOccasionActivity.class.getName(), new Instruction(R.array.inst_eating_occasion, R.array.instruction_eatingoccasionactivity, R.array.inst_eating_occasion_audio));
        put(au.edu.newcastle.jnc985.visida.activity.FinalizeFoodItemActivity.class.getName(), new Instruction(R.array.inst_media_finalize, R.array.instruction_finalizefooditemactivity, R.array.inst_media_finalize_audio));
        put(au.edu.newcastle.jnc985.visida.activity.ListRecipesActivity.class.getName(), new Instruction(R.array.inst_media_list_recipe, R.array.instruction_listrecipeactivity, R.array.inst_media_list_recipe_audio));
        put(au.edu.newcastle.jnc985.visida.activity.MainActivity.class.getName(), new Instruction(R.array.inst_media_main, R.array.instruction_mainactivity, R.array.inst_media_main_audio));
        put(au.edu.newcastle.jnc985.visida.activity.MealActivity.class.getName(), new Instruction(R.array.inst_media_meal, R.array.instruction_mealactivity, R.array.inst_media_meal_audio));
        put(au.edu.newcastle.jnc985.visida.activity.RecordReviewActivity.class.getName(), new Instruction(R.array.inst_record_review, R.array.instruction_recordreviewactivity, R.array.inst_record_review_audio));
        put(au.edu.newcastle.jnc985.visida.activity.SelectEatingOccasionActivity.class.getName(), new Instruction(R.array.inst_media_select_eating_occasion, R.array.instruction_selecteatingoccasionactivity, R.array.inst_media_select_eating_occasion_audio));
        put(SELECT_HOUSEHOLD_MEMBER_FINALIZE, new Instruction(R.array.inst_media_select_household_member_finalize, R.array.instruction_selecthouseholdmemberactivity_finalize, R.array.inst_media_select_household_member_audio_finalize));
        put(SELECT_HOUSEHOLD_MEMBER_BREASTFEED, new Instruction(R.array.inst_media_select_household_member_breastfeed, R.array.instruction_selecthouseholdmemberactivity_breastfeed, R.array.inst_media_select_household_member_audio_breastfeed));
        put(SELECT_HOUSEHOLD_MEMBER_EAT, new Instruction(R.array.inst_media_select_household_member_eat, R.array.instruction_selecthouseholdmemberactivity_eat, R.array.inst_media_select_household_member_audio_eat));
        put(au.edu.newcastle.jnc985.visida.activity.SelectHouseholdMemberActivity.class.getName(), new Instruction(R.array.inst_media_select_household_member, R.array.instruction_selecthouseholdmemberactivity, R.array.inst_media_select_household_member_audio));
        put(au.edu.newcastle.jnc985.visida.activity.SettingsActivity.class.getName(), new Instruction(R.array.inst_media_ph, R.array.instruction_settingsactivity, R.array.inst_media_ph_audio));
        put(au.edu.newcastle.jnc985.visida.activity.SetupHouseholdActivity.class.getName(), new Instruction(R.array.inst_media_ph, R.array.instruction_setuphouseholdctivity, R.array.inst_media_ph_audio));
        put(recordverification.RecordVerificationActivity.class.getName(), new Instruction(R.array.inst_media_ph, R.array.instruction_verify, R.array.inst_media_ph_audio));
    }};

    public static void playInstruction(AppCompatActivity activity) {
        Instruction instruction;
        if(activity.getClass().getName().equals(au.edu.newcastle.jnc985.visida.activity.SelectHouseholdMemberActivity.class.getName()))
        {
            if(Utilities.getState(activity.getApplicationContext()) == State.FINALIZE) {
                instruction = instructions.get(SELECT_HOUSEHOLD_MEMBER_FINALIZE);
            }
            else if (Utilities.getState(activity.getApplicationContext()) == State.BREASTFEED) {
                instruction = instructions.get(SELECT_HOUSEHOLD_MEMBER_BREASTFEED);
            }
            else if (Utilities.getState(activity.getApplicationContext()) == State.EAT) {
                instruction = instructions.get(SELECT_HOUSEHOLD_MEMBER_EAT);
            }
            // Otherwise just the generic finalize instructions
            else {
                instruction = instructions.get(activity.getClass().getName());
            }
        }
        else {
            instruction = instructions.get(activity.getClass().getName());
        }
        Intent intent = new Intent(activity, InstructionActivity.class);
        intent.putExtra(INSTRUCTION, instruction);
        activity.startActivity(intent);
    }
}
