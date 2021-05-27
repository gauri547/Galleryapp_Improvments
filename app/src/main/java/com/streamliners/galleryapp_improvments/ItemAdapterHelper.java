package com.streamliners.galleryapp_improvments;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public class ItemAdapterHelper extends ItemTouchHelper.Callback {

    ItemAdapter itemTouchHelperAdapter;

    /**
     * Parameterised Constructor for ItemAdapterHelper
     *
     * @param itemTouchHelperAdapter
     */
    public ItemAdapterHelper(ItemAdapter itemTouchHelperAdapter) {
        this.itemTouchHelperAdapter = itemTouchHelperAdapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags( @NotNull @NonNull RecyclerView recyclerView,@NotNull @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlags,swipeFlags);
    }
    /**
     * On Move Callback
     * @param recyclerView
     * @param viewHolder
     * @param target
     * @return
     */
    @Override
    public boolean onMove(@NotNull @NonNull RecyclerView recyclerView,@NotNull @NonNull RecyclerView.ViewHolder viewHolder,@NotNull @NonNull RecyclerView.ViewHolder target) {
        itemTouchHelperAdapter.onItemDrag(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }
    /**
     * On Swipe Callback
     * @param viewHolder
     * @param direction
     */
    @Override
    public void onSwiped(@NotNull @NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        itemTouchHelperAdapter.onItemSwipe(viewHolder.getAdapterPosition());
    }
}





