package com.demo.simple;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.demo.simple.databinding.ItemLayoutBinding;
import com.demo.simple.databinding.MyFragmentLayoutBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Lisen.Liu on 2018/11/2.
 */

public class MyFragment extends Fragment implements DragLayerLayout.IDragDataCallback<DataBean> {
    private static Handler handler = new Handler();
    private MyFragmentLayoutBinding mBinding;

    private DragLayerLayout<DataBean> mDragLayerLayout;
    private String mPrefixName;
    private int count;

    public static MyFragment getInstance(DragLayerLayout dragLayerLayout, int id, int count) {
        MyFragment fragment = new MyFragment();
        fragment.mDragLayerLayout = dragLayerLayout;
        fragment.mPrefixName = "G" + id + "-";
        fragment.count = count;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.my_fragment_layout, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private RecyclerViewAdapter mAdapter = null;
    private GridLayoutManager mGridLayoutManager;

    private void init() {
        Activity activity = getActivity();
        mGridLayoutManager = new GridLayoutManager(activity, 3);
        mBinding.recyclerView.setLayoutManager(mGridLayoutManager);
        List<DataBean> data = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            data.add(new DataBean(mPrefixName + i));
        }
        mAdapter = new RecyclerViewAdapter(activity, data);
        mBinding.recyclerView.setAdapter(mAdapter);
    }


    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.VH> {
        private Context mContext;
        private List<DataBean> mData;

        private void addItem(DataBean item, RectF rectF) {
            View view = mBinding.recyclerView.findChildViewUnder(rectF.centerX(), rectF.centerY());
            if (view != null) {
                RecyclerView.ViewHolder viewHolder = mBinding.recyclerView.findContainingViewHolder(view);
                int position = viewHolder.getLayoutPosition();
                mData.add(position, item);
                notifyItemInserted(position);
            } else {
                mData.add(item);
                notifyItemInserted(mData.size() - 1);
                //mGridLayoutManager.scrollToPositionWithOffset(mData.size() - 1, 0);
                mBinding.recyclerView.smoothScrollToPosition(mData.size() - 1);
            }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            }, 800);

        }

        private void removeItem(DataBean item) {
            int index = mData.lastIndexOf(item);
            if (index >= 0) {
                mData.remove(index);
                notifyItemRemoved(index);
            }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            }, 800);

        }

        /**
         * 手指滑动没有放下的交换效果
         */
        private void flipItem() {

        }


        private void swipItem(int from, RectF rectF) {
            View view = mBinding.recyclerView.findChildViewUnder(rectF.centerX(), rectF.centerY());
            if (view != null) {
                RecyclerView.ViewHolder viewHolder = mBinding.recyclerView.findContainingViewHolder(view);
                int to = viewHolder.getLayoutPosition();
                synchronized (this) {
                    if (from > to) {
                        int count = from - to;
                        for (int i = 0; i < count; i++) {
                            Collections.swap(mData, from - i, from - i - 1);
                        }
                    }
                    if (from < to) {
                        int count = to - from;
                        for (int i = 0; i < count; i++) {
                            Collections.swap(mData, from + i, from + i + 1);
                        }
                    }
                    mAdapter.notifyItemMoved(from, to);

                }
            } else {
                Collections.swap(mData, from, mData.size() - 1);
                notifyItemMoved(from, mData.size() - 1);
                //mGridLayoutManager.scrollToPositionWithOffset(mData.size() - 1, 0);
                mBinding.recyclerView.smoothScrollToPosition(mData.size() - 1);
            }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            }, 800);

        }

        private void onMove(View originView, int from, RectF rectF) {
            float x = rectF.centerX();
            float y = rectF.centerY();
            Log.e("onTouchEvent", "x===============" + x + "                y=========" + y);
            View view = mBinding.recyclerView.findChildViewUnder(x, y);
            if (view != null && view != originView) {
                RecyclerView.ViewHolder viewHolder = mBinding.recyclerView.findContainingViewHolder(view);
                int to = viewHolder.getLayoutPosition();
                if (from == to) {
                    return;
                }
                synchronized (this) {
                    if (from > to) {
                        int count = from - to;
                        for (int i = 0; i < count; i++) {
                            Collections.swap(mData, from - i, from - i - 1);
                        }
                    }
                    if (from < to) {
                        int count = to - from;
                        for (int i = 0; i < count; i++) {
                            Collections.swap(mData, from + i, from + i + 1);
                        }
                    }
                    mAdapter.notifyItemMoved(from, to);

                }
            } else {
                Collections.swap(mData, from, mData.size() - 1);
                notifyItemMoved(from, mData.size() - 1);
                //mGridLayoutManager.scrollToPositionWithOffset(mData.size() - 1, 0);
                mBinding.recyclerView.smoothScrollToPosition(mData.size() - 1);
            }
        }


        public RecyclerViewAdapter(Context context, List<DataBean> data) {
            mContext = context;
            mData = data;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_layout, parent, false);
            return new VH(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.bindViewInfo(position);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        class VH extends RecyclerView.ViewHolder implements View.OnLongClickListener {
            private ItemLayoutBinding binding;
            private DataBean data;
            private int position;

            public VH(ItemLayoutBinding binding) {
                super(binding.getRoot());
                binding.getRoot().setOnLongClickListener(this);
                this.binding = binding;
            }

            private void bindViewInfo(int position) {
                this.position = position;
                data = mData.get(position);
                binding.icon.setImageResource(data.resId);
                binding.name.setText(data.name);
            }

            @Override
            public boolean onLongClick(View v) {
                mDragLayerLayout.startDrag(v, data, position);
                return true;
            }
        }

    }

    @Override
    public void addItem(DataBean data, RectF rectF) {
        mAdapter.addItem(data, rectF);
    }

    @Override
    public void removeItem(DataBean data) {
        mAdapter.removeItem(data);
    }

    @Override
    public void swipItem(int fromPosition, RectF toRectF) {
        mAdapter.swipItem(fromPosition, toRectF);
    }

    @Override
    public void onMove(View originView, int fromPosition, RectF toRectF) {
        mAdapter.onMove(originView, fromPosition, toRectF);
    }
}
