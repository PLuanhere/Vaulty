package com.example.vaultyapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.ViewHolder> {
    private List<AccountItem> items;

    public AccountAdapter(List<AccountItem> items) {
        this.items = items;
    }

    public void updateData(List<AccountItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AccountAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountAdapter.ViewHolder holder, int position) {
        AccountItem item = items.get(position);
        holder.tvAppname.setText(item.getAppname());
        holder.tvUsername.setText(item.getUsername());
        holder.tvPassword.setText(item.getPassword());
        holder.tvContent.setText(item.getContent());
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAppname, tvUsername, tvPassword, tvContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAppname = itemView.findViewById(R.id.tvAppname);
            tvUsername = itemView.findViewById(R.id.tvUsername);
        }
    }
}