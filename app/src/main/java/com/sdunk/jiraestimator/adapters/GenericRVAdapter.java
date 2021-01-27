package com.sdunk.jiraestimator.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

public abstract class GenericRVAdapter<T, D> extends RecyclerView.Adapter<GenericRVAdapter<T, D>.ItemViewHolder> {

    private final ArrayList<T> values;

    public GenericRVAdapter(ArrayList<T> values) {
        this.values = values;
    }

    public abstract int getLayoutResId();

    public abstract void onBindData(T model, D dataBinding);

    public abstract void onItemClick(T model);

    public void setValues(ArrayList<T> values) {
        this.values.clear();
        this.values.addAll(values);
        this.notifyDataSetChanged();
    }

    @NotNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        ViewDataBinding dataBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), getLayoutResId(), parent, false);
        return new ItemViewHolder(dataBinding);
    }

    @Override
    public void onBindViewHolder(@NotNull ItemViewHolder holder, final int position) {
        onBindData(values.get(position), holder.dataBinding);

        ((ViewDataBinding) holder.dataBinding).getRoot().setOnClickListener(view -> onItemClick(values.get(position)));
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        protected final D dataBinding;

        @SuppressWarnings("unchecked")
        public ItemViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            dataBinding = (D) binding;
        }
    }
}