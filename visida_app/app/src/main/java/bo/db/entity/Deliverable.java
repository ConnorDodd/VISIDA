package bo.db.entity;

import android.content.Context;
import android.content.Intent;

public interface Deliverable{

    //The actual id for the notification
    int getNotificationId();

    //The identifier of who the notification is for (getPPid() for ReviewNotification)
    String getId();

    //Get the message to be displayed in the drop down menu.
    //Pass context in so we can use string resources.
    String getMessage(Context context);

    boolean isSeen();

    void setSeen(boolean seen);

    Intent getIntent(Context packageContext);
}
