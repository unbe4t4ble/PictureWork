package kidzania.picturework.ListImage;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import kidzania.picturework.R;

/**
 * Created by mubarik on 20/11/2017.
 */

public class ListViewHolder extends RecyclerView.ViewHolder {
    public ImageView imgView;
    public TextView txtfileName;
    public CheckBox cbChoiceImage;

    public ListViewHolder(View itemView) {
        super(itemView);

        imgView = itemView.findViewById(R.id.imgView);
        txtfileName = itemView.findViewById(R.id.txtfileName);
        cbChoiceImage = itemView.findViewById(R.id.cbChoiceImage);
    }
}
