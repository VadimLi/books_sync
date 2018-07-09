package com.example.vadim.books_sync.views.adapter;


import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;

abstract public class DataBoundAdapter<T extends ViewDataBinding> extends
        BaseDataBoundAdapter<T> {

    @LayoutRes
    private final int mLayoutId;

    /**
     * Creates a DataBoundAdapter with the given item layout
     *
     * @param layoutId The layout to be used for items. It must use data binding.
     */
    public DataBoundAdapter(@LayoutRes int layoutId) {
        mLayoutId = layoutId;
    }

    @Override
    public int getItemLayoutId(int position) {
        return mLayoutId;
    }
}