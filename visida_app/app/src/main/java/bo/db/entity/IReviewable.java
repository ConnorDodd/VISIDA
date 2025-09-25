package bo.db.entity;

import android.content.Context;

import java.util.List;

/**
 * Interface to implement in the entities which can be reviewed.
 * Mainly to be used by the {@link ui.RecordReviewAdapter}
 */
public interface IReviewable {
    String getHeaderString();
    boolean isValid();
    List<? extends ImageAudioProvider> getReviewItems();
    long getId();
}
