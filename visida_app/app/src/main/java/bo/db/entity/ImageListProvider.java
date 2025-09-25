package bo.db.entity;

import android.content.Context;

import java.util.List;

/**
 * Created by Josh on 09-Mar-18.
 */

public interface ImageListProvider {
    long getId();

    /**
     * Return the NAME of the images. This does not include the
     * full path of the image as this is not stored in the database
     * It is asusmed all images are in the media directory returned
     * from {@link bo.Utilities#getMediaDirectory(Context)}}
     * @return
     */
    List<String> getImageNames();
}
