package kidzania.picturework.ListImage;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kidzania.picturework.R;

/**
 * Created by mubarik on 20/11/2017.
 */

public class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ImageModel> dataModel = Collections.emptyList();
    private Context context;

    public static ArrayList<String> ArrLocationImage = new ArrayList<>();
    public static ArrayList<String> ArrFileName = new ArrayList<>();

    public static ArrayList<String> ArrLocationImageCompress = new ArrayList<>();

    public ListAdapter(Context con, List<ImageModel> Model){
        this.context = con;
        this.dataModel = Model;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder xView = null;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        xView = new ListViewHolder(view);
        return xView;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ListViewHolder) {
            final ListViewHolder myHolder = (ListViewHolder) holder;
            final ImageModel current = dataModel.get(position);
            myHolder.txtfileName.setText(current.get_fileName());

            Glide.with(context)
                    .load(new File(current.get_locationImage()))
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(myHolder.imgView);

            myHolder.cbChoiceImage.setChecked(false);
            myHolder.cbChoiceImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (myHolder.cbChoiceImage.isChecked()) {
                        ArrLocationImage.add(current.get_locationImage());
                        ArrFileName.add(current.get_fileName());
                        //Toast.makeText(context, String.valueOf(ArrLocationImage.size()), Toast.LENGTH_SHORT).show();
                    }else{
                        ArrLocationImage.remove(current.get_locationImage());
                        ArrFileName.remove(current.get_fileName());
                        //Toast.makeText(context, String.valueOf(ArrLocationImage.size()), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            myHolder.imgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShowImage(current.get_locationImage());
                }
            });
        }
    }

    private void ShowImage(String locationImage){
        AlertDialog.Builder sayWindows = new AlertDialog.Builder(context);
        View mView = LayoutInflater.from(context).inflate(R.layout.pop_up_image, null);
        final ImageView imageView = mView.findViewById(R.id.imgPrw);
        sayWindows.setView(mView);

        Glide.with(context)
                .load(new File(locationImage))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);

        final AlertDialog mAlertDialog = sayWindows.create();

        mAlertDialog.show();
    }

    @Override
    public int getItemCount() {
        return dataModel.size();
    }
}
