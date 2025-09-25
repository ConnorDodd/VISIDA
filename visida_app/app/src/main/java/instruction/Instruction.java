package instruction;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jnc985 on 18-Apr-18.
 * Holds an int ID reference to a Typed array of drawable references
 * and string array. Its up to the developer to make sure the media
 * items match the text descriptions.
 */

public class Instruction implements Parcelable {
    private int text;
    private int media;
    private int audioFile; //https://stackoverflow.com/a/46991441/4960314
    //https://stackoverflow.com/a/9633306/4960314

    /**
     * Public constructor for instructions.
     * @param imgId: The id of the Resource Array of drawable ID's of Media file. R.array.inst_media_main
     * @param text: The id of the String array to be presented in the instruction dialog R.array.instruction_mainactivity
     */
    public Instruction(int imgId, int text, int audioId){
        this.media = imgId;
        this.text = text;
        this.audioFile = audioId;
    }

    protected Instruction(Parcel in){
        this.text = in.readInt();
        this.media = in.readInt();
        this.audioFile = in.readInt();
    }

    public int getText() {
        return text;
    }

    public void setText(int text) {
        this.text = text;
    }

    public int getMedia() {
        return media;
    }

    public void setMedia(int media) {
        this.media = media;
    }

    public int getAudio() {
        return audioFile;
    }

    public void setAudioFile(int audioFile) {
        this.audioFile = audioFile;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Instruction> CREATOR = new Creator<Instruction>() {
        @Override
        public Instruction createFromParcel(Parcel in) {
            return new Instruction(in);
        }

        @Override
        public Instruction[] newArray(int size) {
            return new Instruction[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.text);
        dest.writeInt(this.media);
        dest.writeInt(this.audioFile);
    }
}
