package com.sdunk.jiraestimator.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

public abstract class GenericRVAdapter<T, D> extends RecyclerView.Adapter<GenericRVAdapter<T, D>.ItemViewHolder> {

    private Context context;
    private ArrayList<T> values;

    public GenericRVAdapter(Context context, ArrayList<T> values) {
        this.context = context;
        this.values = values;
    }

    public abstract int getLayoutResId();

    public abstract void onBindData(T model, int position, D dataBinding);

    public abstract void onItemClick(T model, int position);

    @NotNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        ViewDataBinding dataBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), getLayoutResId(), parent, false);
        return new ItemViewHolder(dataBinding);
    }

    @Override
    public void onBindViewHolder(@NotNull ItemViewHolder holder, final int position) {
        onBindData(values.get(position), position, holder.dataBinding);

        ((ViewDataBinding) holder.dataBinding).getRoot().setOnClickListener(view -> onItemClick(values.get(position), position));
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public void addItems(ArrayList<T> arrayList) {
        values = arrayList;
        this.notifyDataSetChanged();
    }

    public T getItem(int position) {
        return values.get(position);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        protected D dataBinding;

        public ItemViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            dataBinding = (D) binding;
        }
    }
}