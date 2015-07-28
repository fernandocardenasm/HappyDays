package com.example.android.happydays.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.happydays.ParseConstants;
import com.example.android.happydays.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by usuario on 28/07/2015.
 */
public class ImageGridFragment extends AbsListViewBaseFragment {
    public static final int INDEX = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_main, container, false);
        listView = (GridView) rootView.findViewById(R.id.gridView);

        ParseQuery<ParseObject> query = new ParseQuery<>(ParseConstants.CLASS_MOMENTS);
        query.whereEqualTo(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        query.addAscendingOrder(ParseConstants.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> moments, ParseException e) {

                //Validating if the SwipeRefresher is being used

//                if (mSwipeRefreshLayout.isRefreshing()){
//                    mSwipeRefreshLayout.setRefreshing(false);
//                }
//
//                if (e == null){
//                    //We found moments
//                    mMoments = moments;
//                    if (mGridView.getAdapter() == null){
//                        GridViewAdapter mGridViewAdapter = new GridViewAdapter(MainActivity.this, R.layout.grid_item_layout, moments);
//                        mGridView.setAdapter(mGridViewAdapter);
//                    }
//                    else{
//                        ((GridViewAdapter)mGridView.getAdapter()).refill(mMoments);
//                    }
//                    mGridView.setOnItemClickListener(mOnItemClickListener);
//                }

                int i = 0;
//                String[] momentsArray = new String[moments.size()];
//                for (i = 0; i < moments.size(); i++){
//                    ParseObject moment = moments.get(i);
//                    ParseFile file = moment.getParseFile(ParseConstants.KEY_FILE);
//                    momentsArray[i] = Uri.parse(file.getUrl()).toString();
//                    Log.d("Link", "moment:" + momentsArray[i]);
//                }

                if (e == null){
                    ((GridView) listView).setAdapter(new ImageAdapter(getActivity(),moments));
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                startImagePagerActivity(position);
//            }
//        });
                }
            }
        });


        return rootView;
    }

    private static class ImageAdapter extends BaseAdapter {

        private List<ParseObject> mMoments;

        private LayoutInflater inflater;

        private DisplayImageOptions options;


        ImageAdapter(Context context, List<ParseObject> moments) {
            inflater = LayoutInflater.from(context);
            mMoments = moments;

            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.ic_stub)
                    .showImageForEmptyUri(R.drawable.ic_empty)
                    .showImageOnFail(R.drawable.ic_error)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
        }

        @Override
        public int getCount() {
            return mMoments.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.item_grid_image, parent, false);
                holder = new ViewHolder();
                assert view != null;
                holder.imageView = (ImageView) view.findViewById(R.id.image);
                holder.imageTitle = (TextView) view.findViewById(R.id.text);
                holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            ParseObject moment = mMoments.get(position);
            ParseFile file = moment.getParseFile(ParseConstants.KEY_FILE);
            String uriFile = Uri.parse(file.getUrl()).toString();

            if (moment.getString(ParseConstants.KEY_MOMENT_TEXT).equals(ParseConstants.EMPTY_FIELD)){
                holder.imageTitle.setText("Happy moment!");
            }
            else{
                holder.imageTitle.setText(moment.getString(ParseConstants.KEY_MOMENT_TEXT));
            }

//                    momentsArray[i] = Uri.parse(file.getUrl()).toString();

            ImageLoader.getInstance()
                    .displayImage(uriFile, holder.imageView, options, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            holder.progressBar.setProgress(0);
                            holder.progressBar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            holder.progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            holder.progressBar.setVisibility(View.GONE);
                        }
                    }, new ImageLoadingProgressListener() {
                        @Override
                        public void onProgressUpdate(String imageUri, View view, int current, int total) {
                            holder.progressBar.setProgress(Math.round(100.0f * current / total));
                        }
                    });

            return view;
        }
    }

    static class ViewHolder {
        ImageView imageView;
        ProgressBar progressBar;
        TextView imageTitle;
    }
}