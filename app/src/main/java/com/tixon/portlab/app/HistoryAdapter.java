package com.tixon.portlab.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private ArrayList<HistoryItem> items;
    private int rowLayout;
    private Context mContext;

    public HistoryAdapter(ArrayList<HistoryItem> items, int rowLayout, Context mContext) {
        this.items = items;
        this.rowLayout = rowLayout;
        this.mContext = mContext;
    }

    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(HistoryAdapter.ViewHolder holder, final int position) {
        final HistoryItem item = items.get(position);
        holder.tvExpression.setText(item.expression);
        holder.tvResult.setText(item.result);
        holder.tvExpression.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("myLogs", "expression");
            }
        });
        holder.tvResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("myLogs", "result");
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvExpression, tvResult;

        public ViewHolder(View itemView) {
            super(itemView);
            tvExpression = (TextView) itemView.findViewById(R.id.tvHistoryItemExpression);
            tvResult = (TextView) itemView.findViewById(R.id.tvHistoryItemResult);
        }
    }
}
