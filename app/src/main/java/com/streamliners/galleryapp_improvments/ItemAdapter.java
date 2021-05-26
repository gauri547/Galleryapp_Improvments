package com.streamliners.galleryapp_improvments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.streamliners.galleryapp_improvments.databinding.ItemCardBinding;
import com.streamliners.galleryapp_improvments.models.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    private Context context;
    String finalUrl = " ";
    List<Item> itemList, requiredNewItemList;

    /**
     * To avoid reference issues during filtering
     * @param context
     */
    public ItemAdapter(Context context){
        this.context=context;
        itemList = new ArrayList<>();
        requiredNewItemList = new ArrayList<>();
    }
    /**
     * To fetch new image
     */
    public ItemAdapter(Context context, List<Item> itemList){
        this.context=context;
        this.itemList = itemList;
        requiredNewItemList = itemList;
    }

    @NonNull
    @Override
    public ItemAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCardBinding b = ItemCardBinding.inflate(LayoutInflater.from(context),parent, false);
        return new ItemViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ItemViewHolder holder, int position, @NonNull List<Object> payloads) {

        Item item=requiredNewItemList.get(position);
        finalUrl=checkUrl(item);
        Glide.with(context).load(finalUrl).into(holder.b.imageView);
        holder.b.title.setText(item.label);
        holder.b.title.setBackgroundColor(item.color);
    }
    private String checkUrl(Item item){
        if(item.imageRedirectedUrl !=null){
            return item.imageRedirectedUrl;
        }
        return item.uri;
    }

    @Override
    public int getItemCount() {
        return requiredNewItemList.size();
    }

    /**
     * To filter the particular card
     * @param query
     */
    public void filter(String query, List<Item> itemList){

        if (query.trim().isEmpty()){
            requiredNewItemList = itemList;
            return;
        }
        query = query.trim().toLowerCase();
        requiredNewItemList.clear();
        for(Item item : itemList){
            if(item.label.toLowerCase().contains(query)){
                requiredNewItemList.add(item);
            }
        }
        notifyDataSetChanged();
    }
    public void onItemMove(int fromPosition, int toPosition){
        if(fromPosition<toPosition){
            for(int i= fromPosition; i<toPosition;i++){
                Collections.swap(requiredNewItemList,i,i+1);
            }
        }
        else{
            for (int i = fromPosition; i>toPosition;i--){
                Collections.swap(requiredNewItemList,i,i-1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }
    static class ItemViewHolder extends RecyclerView.ViewHolder{
        ItemCardBinding b;
        public ItemViewHolder(ItemCardBinding b){
            super(b.getRoot());
            this.b=b;
        }
    }
}
