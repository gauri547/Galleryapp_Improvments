package com.streamliners.galleryapp_improvments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.streamliners.galleryapp_improvments.databinding.ActivityMainBinding;
import com.streamliners.galleryapp_improvments.databinding.ItemCardBinding;
import com.streamliners.galleryapp_improvments.models.Item;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 0;
    ActivityMainBinding b;
    SharedPreferences preferences;
    List<Item> items = new ArrayList<>();

    private String imageUrl;
    ItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        preferences = getPreferences(MODE_PRIVATE);
        inflateDataFromSharedPreferences();

        if (!items.isEmpty())
            showItems(items);

    }

    //Functions for Context Menu

    /**
     * Handle Menu Item Selection
     *
     * @param item
     * @return
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        imageUrl = adapter.imageUrl;    //Image Url of Parent of Context Menu
        int index = adapter.index;      //Index of item for context menu
        ItemCardBinding binding = adapter.itemCardBinding;      //Binding of parent of context menu
        if (item.getItemId() == R.id.editMenuItem) {
            new EditImageDialog()
                    .show(this, imageUrl, new EditImageDialog.onCompleteListener() {
                        @Override
                        public void onEditCompleted(Item item) {
//                            int index = b.list.indexOfChild(bindingToRemove.getRoot()) - 1;
                            items.set(index, item);
                            //Inflate Layout
                            adapter.notifyDataSetChanged();
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
        if (item.getItemId() == R.id.shareImage)
            shareImage(binding);
        return true;
    }

    private void shareImage(ItemCardBinding binding) {
        Bitmap bitmap = getBitmapFromView(binding.getRoot());
        String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "palette", "share palette");
        Uri bitmapUri = Uri.parse(bitmapPath);
        //Intent to send image
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        startActivity(Intent.createChooser(intent, "Share"));
    }

    /**
     * Returns Bitmap from a View
     *
     * @param view
     * @return
     */
    public static Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    /**
     * Gives Add Image Option in menu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery_app, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        // SearchView on query text listener to add search function of adapter
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
        return true;
    }


    /**
     * Shows add image dialog on clicking icon in menu
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addImage) {
            showAddImageDialog();
            return true;
        }
        if (item.getItemId() == R.id.addFromGallery) {
            addFromGallery();
        }
        if (item.getItemId() == R.id.sortLabels) {
            adapter.sortAlphabetically();
        }
        return false;
    }


    /**
     * Send Intent to get image from gallery
     */
    private void addFromGallery() {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }


    /**
     * Shows Image Dialog Box
     */
    private void showAddImageDialog() {
        if (this.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
//            isDialogBoxShowed = true;
            // To set the screen orientation in portrait mode only
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        new AddImageDialog()
                .show(this, new AddImageDialog.onCompleteListener() {
                    @Override
                    public void onImageAdded(Item item) {
                        items.add(item);
                        showItems(items);

//                        b.noItemTV.setVisibility(View.GONE);
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

    // Callback for swipe action
    ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            items.remove(viewHolder.getAdapterPosition());
            adapter.notifyDataSetChanged();
        }
    };

    /**
     * Pass items list to ItemAdapter and add additional callbacks
     *
     * @param items
     */
    public void showItems(List<Item> items) {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        adapter = new ItemAdapter(this, items);
        b.list.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        adapter.setItemAdapterHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(b.list);
        ItemTouchHelper.Callback callback2 = new ItemAdapterHelper(adapter);
        ItemTouchHelper itemTouchHelper1 = new ItemTouchHelper(callback2);
        adapter.setItemAdapterHelper(itemTouchHelper1);
        itemTouchHelper1.attachToRecyclerView(b.list);
        b.list.setAdapter(adapter);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

    }


    /**
     * OverRide onPause method to save shared preferences
     */
    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor myEdit = preferences.edit();

        int numOfImg = items.size();
        myEdit.putInt(Constants.NUMOFIMG, numOfImg).apply();

        int counter = 0;
        for (Item item : items) {
            myEdit.putInt(Constants.COLOR + counter, item.color)
                    .putString(Constants.LABEL + counter, item.label)
                    .putString(Constants.IMAGE + counter, item.imageUrl)
                    .apply();
            counter++;
        }
        myEdit.commit();
    }

    /**
     * Inflate data from shared preferences
     */
    private void inflateDataFromSharedPreferences() {
        int itemCount = preferences.getInt(Constants.NUMOFIMG, 0);
//        if (itemCount!=0) b.noItemTV.setVisibility(View.GONE);
        // Inflate all items from shared preferences
        for (int i = 0; i < itemCount; i++) {

            Item item = new Item(preferences.getString(Constants.IMAGE + i, "")
                    , preferences.getInt(Constants.COLOR + i, 0)
                    , preferences.getString(Constants.LABEL + i, ""));

            items.add(item);
        }
        showItems(items);
    }

    /**
     * Fetch image from gallery
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            String uri = selectedImage.toString();

            new AddFromGallery().show(this, uri, new AddFromGallery.onCompleteListener() {
                @Override
                public void onAddCompleted(Item item) {
                    items.add(item);
                    showItems(items);
//                   b.noItemTV.setVisibility(View.GONE);
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

    }
}






