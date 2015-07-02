package com.codepath.apps.tumblrsnap.fragments;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.codepath.apps.tumblrsnap.PhotosAdapter;
import com.codepath.apps.tumblrsnap.R;
import com.codepath.apps.tumblrsnap.TumblrClient;
import com.codepath.apps.tumblrsnap.models.Photo;
import com.loopj.android.http.JsonHttpResponseHandler;

public class PhotosFragment extends Fragment {
	private static final int TAKE_PHOTO_CODE = 1;
	private static final int PICK_PHOTO_CODE = 2;

	private Bitmap photoBitmap;
	
	TumblrClient client;
	ArrayList<Photo> photos;
	PhotosAdapter photosAdapter;
	ListView lvPhotos;
	FloatingActionButton actions;

	
	@Override 
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_photos, container, false);
		setHasOptionsMenu(true);
		return view;
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		client = ((TumblrClient) TumblrClient.getInstance(
				TumblrClient.class, getActivity()));
		photos = new ArrayList<Photo>();
		photosAdapter = new PhotosAdapter(getActivity(), photos);
		lvPhotos = (ListView) getView().findViewById(R.id.lvPhotos);
		lvPhotos.setAdapter(photosAdapter);

		actions = (FloatingActionButton) getView().findViewById(R.id.fabPhoto);

		actions.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectImage();
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		reloadPhotos();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.settings, menu);
	}

	private void selectImage() {
		final CharSequence[] items = { "Take Photo", "Choose from Library",
				"Cancel" };

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Add Photo!");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Take Photo")) {
					// Take the user to the camera app
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					File f = new File(android.os.Environment
							.getExternalStorageDirectory(), "temp.jpg");
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
					startActivityForResult(intent, TAKE_PHOTO_CODE);
				} else if (items[item].equals("Choose from Library")) {
					// Take the user to the gallery app
					Intent intent = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					intent.setType("image/*");
					startActivityForResult(
							Intent.createChooser(intent, "Select File"), PICK_PHOTO_CODE);
				} else if (items[item].equals("Cancel")) {
					dialog.dismiss();
				}
			}
		});
		builder.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == TAKE_PHOTO_CODE) {
				// Extract the photo that was just taken by the camera
				getPicFromCamera();
			} else if (requestCode == PICK_PHOTO_CODE) {
				// Extract the photo that was just picked from the gallery
				getPicFromGallery(data);
			}
		}
	}

	private void getPicFromGallery(Intent data) {
		Uri selectedImageUri = data.getData();

		String tempPath = getPath(selectedImageUri, getActivity());
		Bitmap bm;
		BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
		bm = BitmapFactory.decodeFile(tempPath, btmapOptions);
		photoBitmap = bm;
		postPhotoToAccount();
	}

	public String getPath(Uri uri, Activity activity) {
		String[] projection = { MediaStore.MediaColumns.DATA };
		Cursor cursor = activity
				.managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	private void getPicFromCamera() {
		File f = new File(Environment.getExternalStorageDirectory()
				.toString());
		if(f.listFiles() != null)
		{
			for (File temp : f.listFiles()) {
				if (temp.getName().equals("temp.jpg")) {
					f = temp;
					break;
				}
			}
		}
		try
		{
			Bitmap bm;
			BitmapFactory.Options btmapOptions = new BitmapFactory.Options();

			bm = BitmapFactory.decodeFile(f.getAbsolutePath(),
					btmapOptions);

			// bm = Bitmap.createScaledBitmap(bm, 70, 70, true);
			photoBitmap = bm;

			String path = getActivity().getFilesDir().getPath()+ "/codepath/tumblr";
			f.delete();
			OutputStream fOut = null;
			File file = new File(path, String.valueOf(System
					.currentTimeMillis()) + ".jpg");
			try {
				fOut = new FileOutputStream(file);
				bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
				fOut.flush();
				fOut.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			postPhotoToAccount();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void postPhotoToAccount()
	{
		//TODO start Service to post Photos
	}

	private void reloadPhotos()
	{
		client.getTaggedPhotos(new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int code, JSONObject response) {
				try {
					JSONArray photosJson = response.getJSONArray("response");
					photosAdapter.clear();
					photosAdapter.addAll(Photo.fromJson(photosJson));
					mergeUserPhotos(); // bring in user photos
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(Throwable arg0) {
				Log.d("DEBUG", arg0.toString());
			}
		});
	}

	// Loads feed of users photos and merges them with the tagged photos
	// Used to avoid an API limitation where user photos arent returned in tagged
	private void mergeUserPhotos() {
		client.getUserPhotos(new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int code, JSONObject response) {
				try {
					JSONArray photosJson = response.getJSONObject("response").getJSONArray("posts");
					for (Photo p : Photo.fromJson(photosJson)) {
						if (p.isSnap()) { photosAdapter.add(p); }
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				photosAdapter.sort(new Comparator<Photo>() {
					@Override
					public int compare(Photo a, Photo b) {
						return Long.valueOf(b.getTimestamp()).compareTo(a.getTimestamp());
					}
				});
			}

			@Override
			public void onFailure(Throwable arg0) {
				Log.d("DEBUG", arg0.toString());
			}
		});
	}
}
