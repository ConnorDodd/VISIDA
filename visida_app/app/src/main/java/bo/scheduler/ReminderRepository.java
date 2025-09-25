package bo.scheduler;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;
import java.util.concurrent.ExecutionException;

import bo.db.AppDatabase;
import bo.db.dao.ReminderDao;
import bo.db.entity.Reminder;

public class ReminderRepository {

    private ReminderDao mReminderDao;
    public ReminderRepository(Application app){
        this.mReminderDao = AppDatabase.getInstance(app).getReminderDao();
    }

    public List<Reminder> getReminders(){
        try{
            return new GetRemindersAsync(mReminderDao).execute().get();
        }
        catch(InterruptedException | ExecutionException ex){
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Inserts the reminder into the database
     * @param r
     */
    public void addReminder(Reminder r) {
        new InsertRemindersAsync(mReminderDao).execute(r);
    }

    private static class GetRemindersAsync extends AsyncTask<Void, Void, List<Reminder>>{
        private ReminderDao mDao;
        GetRemindersAsync(ReminderDao dao){this.mDao = dao;}


        @Override
        protected List<Reminder> doInBackground(Void... voids) {
            return mDao.getReminders();
        }
    }

    private static class InsertRemindersAsync extends AsyncTask<Reminder, Void, Void>{
        private ReminderDao mDao;
        InsertRemindersAsync(ReminderDao dao){this.mDao = dao;}


        @Override
        protected Void doInBackground(Reminder... reminders) {
            mDao.insert(reminders);
            return null;
        }
    }
}
