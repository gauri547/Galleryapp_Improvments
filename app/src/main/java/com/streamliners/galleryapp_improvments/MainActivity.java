package com.streamliners.galleryapp_improvments;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.streamliners.galleryapp_improvments.databinding.ActivityMainBinding;
import com.streamliners.galleryapp_improvments.databinding.ItemCardBinding;
import com.streamliners.galleryapp_improvments.models.Item;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding b;
    Gson gson = new Gson();

    private boolean isSorted;
    ItemTouchHelper itemTouchHelper;
    ItemAdapter adapter;

    // Shared preferences
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        //Load data from sharedPreferences
        loadSharedPreferenceData();


    }

    /**
     * Load data from sharedPreferences
     * Fetch Images from caches
     */
    private void loadSharedPreferenceData() {
        b.noItemTv.setVisibility(View.GONE);
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String json = preferences.getString(Constants.ALL_ITEMS, null);
        allItem = gson.fromJson(json, new TypeToken<List<Item>>() {
        }.getType());
        if (allItem != null) {
            setUpRecyclerView();
        } else {
            allItem = new ArrayList<>();
        }
    }


    /**
     * This method contains the saved instance data and it will prevent loss of data when the screen
     * is rotated.
     *
     * @param savedInstanceState
     */
    private void savedInstance(Bundle savedInstanceState) {
        b.noItemsTV.setVisibility(View.GONE);
        String json = savedInstanceState.getString(Constants.ALL_ITEMS, null);
        allItem = gson.fromJson(json, new TypeToken<List<Item>>() {
        }.getType());
        if (allItem != null) {
            /*for(Item item : allItems){
                //Bind Data
                *//*ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());
                if(item.imageRedirectedUrl != null){
                    Glide.with(this)
                            .asBitmap()
                            .load(item.imageRedirectedUrl)
                            .into(binding.imageView);
                }
                else{
                    Glide.with(this)
                            .asBitmap()
                            .load(Uri.parse(item.uri))
                            .into(binding.imageView);
                }
                //binding.imageView.setImageBitmap(bitmapFromString);
                binding.title.setText(item.label);
                binding.title.setBackgroundColor(item.color);
                b.list.addView(binding.getRoot());*/
            setUpRecyclerView();
        } else {
            allItems = new ArrayList<>();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery_app, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ItemAdapter adapter = new ItemAdapter(MainActivity.this);
                adapter.filter(query, allItems);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                /**
                 * We are creating new reference of adapter everytime so that whenever user removes
                 * the filter search, the lists should not be pointing to the same reference and this
                 * was the main cause of the bug in the code(Resolved).
                 */
                ItemAdapter adapter = new ItemAdapter(MainActivity.this);
                adapter.filter(newText, allItems);
                b.list.setAdapter(adapter);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addImage) {
            showAddImageDialog();
            return true;
        }
        if (item.getItemId() == R.id.sort) {
            sortList();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery_app, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        /**
         * Listener for SearchView
         */
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ItemAdapter adapter = new ItemAdapter(MainActivity.this);
                adapter.filter(query, allItems);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                /**
                 * We are creating new reference of adapter everytime so that whenever user removes
                 * the filter search, the lists should not be pointing to the same reference and this
                 * was the main cause of the bug in the code(Resolved).
                 */
                ItemAdapter adapter = new ItemAdapter(MainActivity.this);
                adapter.filter(newText, allItems);
                b.list.setAdapter(adapter);
                return true;
            }
        });
        return true;
    }


    /**
     * This function will sort the list alphabetically.
     */
    private void sortList() {
        if (!isSorted) {
            isSorted = true;
            List<Item> sortedItems = new ArrayList<>(allItems);
            Collections.sort(sortedItems, (p1, p2) -> p1.label.compareTo(p2.label));
            if (adapter != null) {
                adapter.requiredNewItemList = sortedItems;
                adapter.showSortedItems();
                b.list.setAdapter(adapter);
            }
        } else {
            isSorted = false;
            if (adapter != null) {
                adapter.requiredNewItemList = allItems;
                adapter.showSortedItems();
                b.list.setAdapter(adapter);
            }

        }
    }

    /**
     * To show the dialog to add image
     */
    private void showAddImageDialog() {
        new AddImageDialog()
                .show(this, new AddImageDialog.OnCompleteListener() {
                    @Override
                    public void onImageAdded(Item item) {
                        allItems.add(item);
                        setUpRecyclerView();
                    }

                    @Override
                    public void onError(String error) {
                        new MaterialAlertDialogBuilder(MainActivity.this)
                                .setTitle("Error")
                                .setMessage(error)
                                .show();
                    }
                });
    }


    private void setUpRecyclerView() {

        if (adapter == null) {
            adapter = new ItemAdapter(this, allItems);
        } else {
            adapter.itemsList = allItems;
        }
        b.list.setLayoutManager(new LinearLayoutManager(this));
        b.list.setAdapter(adapter);
        itemRemove();
    }

    /**
     * Below code is the code for swipe functionality that will remove the card from the
     * recycler view.
     */
    private void itemRemove() {
        itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallBack(adapter));
        adapter.notifyDataSetChanged();
        itemTouchHelper.attachToRecyclerView(b.list);
    }

    /**
     * This method will save the item card so that when the screen is rotated the data is not lost.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        String json = gson.toJson(allItems);
        outState.putString(Constants.ALL_ITEMS, json);
    }


    /**
     * This method will save the item card in shared preferences
     */
    @Override
    protected void onPause() {
        super.onPause();
        String json = gson.toJson(allItems);
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        preferences.edit()
                .putString(Constants.ALL_ITEMS, json)
                .apply();
    }

    /**
     * All these below methods are the callbacks from the itemHelper and the galleryImageUploader
     * classes.

     * @param colors
     * @param labels
     */


    @Override
    public void onFetched(String redirectedUrl, Set<Integer> colors, List<String> labels) {

    }

    @Override
    public void onImageAdded(Item item) {

        allItems.add(item);
        setUpRecyclerView();
    }

    @Override
    public void onError(String error) {

    }
}





























