package me.kristoprifti.android.tumbasolutions.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import me.kristoprifti.android.tumbasolutions.models.Picture;
import me.kristoprifti.android.tumbasolutions.R;

/**
 * Created by Kristi on 1/4/2017.
 */

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.ViewHolder>{

    private ArrayList<Picture> mPicturesList;
    private Context mContext;
    private OnItemClickListener mItemClickListener;

    public PictureAdapter(ArrayList<Picture> picturesList, Context context) {
        this.mPicturesList = picturesList;
        this.mContext = context;
    }

    @Override
    public PictureAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.picture_row_layout, parent, false);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Picasso.with(mContext)
                .load(Uri.parse(mPicturesList.get(position).getPictureUrl()))
                .noPlaceholder()
                .error(R.mipmap.ic_launcher)
                .into(viewHolder.pictureImageView);
    }

    @Override
    public int getItemCount() {
        return mPicturesList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView pictureImageView;

        ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            pictureImageView = (ImageView) itemLayoutView.findViewById(R.id.pictureImageView);

            pictureImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(itemView, getAdapterPosition());
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}