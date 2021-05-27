package com.streamliners.galleryapp_improvments;

import com.streamliners.galleryapp_improvments.models.Item;

public abstract class ItemAdapterInterface {
     abstract void onItemDrag(int from, int to) ;
     abstract void onItemSwipe(int position) ;
}
