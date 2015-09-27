package com.example.android.happydays.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by usuario on 28/07/2015.
 */
public abstract class BaseFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }


//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_main, menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.item_clear_memory_cache:
//                ImageLoader.getInstance().clearMemoryCache();
//                return true;
//            case R.id.item_clear_disc_cache:
//                ImageLoader.getInstance().clearDiskCache();
//                return true;
//            default:
//                return false;
//        }
//    }
}
