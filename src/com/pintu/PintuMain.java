package com.pintu;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.pintu.activity.FullScreenActivity;
import com.pintu.task.GenericTask;
import com.pintu.task.SendTask;
import com.pintu.task.TaskAdapter;
import com.pintu.task.TaskListener;
import com.pintu.task.TaskParams;
import com.pintu.task.TaskResult;
import com.pintu.util.FileHelper;

public class PintuMain extends FullScreenActivity {

	private static final String TAG = "PintuMain";
	private static final String SIS_RUNNING_KEY = "running";

	private static final int REQUEST_IMAGE_CAPTURE = 2;
	private static final int REQUEST_PHOTO_LIBRARY = 3;
	private static final int MAX_BITMAP_SIZE = 400;

	// Header
	private Button mBackButton;
	private Button mTopSendButton;

	private EditText tagsEditText;
	private EditText descEditText;
	private CheckBox allowStory;
	private ImageButton chooseImagesButton;
	private ImageButton mCameraButton;
	private ProgressDialog dialog;

	// Picture
	private boolean withPic = false;
	private ImageView mPreview;

	private long startTime = -1;
	private long endTime = -1;

	// Task
	private GenericTask mSendTask;

	// the picture to send
	private File mFile;
	// temporal saved image
	private Uri mImageUri;

	// -------------------- Construction UI Logic
	// -----------------------------------------

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.postpic);

		getViews();

		addEventListeners();

	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy.");

		if (mSendTask != null
				&& mSendTask.getStatus() == GenericTask.Status.RUNNING) {
			// Doesn't really cancel execution (we let it continue running).
			// See the SendTask code for more details.
			mSendTask.cancel(true);
		}

		// Don't need to cancel FollowersTask (assuming it ends properly).

		if (dialog != null) {
			dialog.dismiss();
		}
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mSendTask != null
				&& mSendTask.getStatus() == GenericTask.Status.RUNNING) {
			outState.putBoolean(SIS_RUNNING_KEY, true);
		}
	}

	private void getViews() {
		mBackButton = (Button) this.findViewById(R.id.top_back);
		mTopSendButton = (Button) this.findViewById(R.id.top_send_btn);

		mPreview = (ImageView) findViewById(R.id.preview);
		tagsEditText = (EditText) findViewById(R.id.tags);
		descEditText = (EditText) findViewById(R.id.description);
		allowStory = (CheckBox) findViewById(R.id.allowstory);

		mCameraButton = (ImageButton) findViewById(R.id.camera_button);
		chooseImagesButton = (ImageButton) findViewById(R.id.choose_images_button);

	}

	private void addEventListeners() {
		mBackButton.setOnClickListener(mGoListener);
		mTopSendButton.setOnClickListener(sendListener);
		chooseImagesButton.setOnClickListener(insertImgListener);
		mCameraButton.setOnClickListener(takeShotListener);

	}

	private void getPic(Uri uri) {

		withPic = true;

		mFile = null;

		if (uri.getScheme().equals("content")) {
			mFile = new File(getRealPathFromURI(uri));
		} else {
			mFile = new File(uri.getPath());
		}

		Bitmap thumbnail = createThumbnailBitmap(uri, MAX_BITMAP_SIZE);
		mPreview.setImageBitmap(thumbnail);

		if (mFile == null) {
			updateProgress("Could not locate picture file. Sorry!");
			disableEntry();
		}

	}

	public static Bitmap createFitinBitmap(String path, int fitinWidth,
			int fitinHeight) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);
		int sampleSize1 = opts.outWidth / fitinWidth;
		int sampleSize2 = opts.outHeight / fitinHeight;
		opts.inSampleSize = sampleSize1 > sampleSize2 ? sampleSize1
				: sampleSize2;
		opts.inJustDecodeBounds = false;
		opts.inDither = false;
		opts.inPreferredConfig = Bitmap.Config.RGB_565;
		return BitmapFactory.decodeFile(path, opts);
	}

	private String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaColumns.DATA };
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	/**
	 * 
	 * @param uri
	 * @param size
	 * @return
	 */
	private Bitmap createThumbnailBitmap(Uri uri, int size) {
		InputStream input = null;

		try {
			input = getContentResolver().openInputStream(uri);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(input, null, options);
			input.close();

			// Compute the scale.
			int scale = 1;
			while ((options.outWidth / scale > size)
					|| (options.outHeight / scale > size)) {
				scale *= 2;
			}

			options.inJustDecodeBounds = false;
			options.inSampleSize = scale;

			Log.d(TAG, "Create Thumbnail, SampleSize: " + scale);

			input = getContentResolver().openInputStream(uri);
			return BitmapFactory.decodeStream(input, null, options);

		} catch (IOException e) {
			Log.w(TAG, e);

			return null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					Log.w(TAG, e);
				}
			}
		}
	}

	private OnClickListener mGoListener = new OnClickListener() {
		public void onClick(View v) {
			finish();
		}
	};

	private OnClickListener sendListener = new OnClickListener() {
		public void onClick(View v) {
			doSend();
		}
	};

	private OnClickListener insertImgListener = new OnClickListener() {
		public void onClick(View v) {
			Log.d(TAG, "chooseImagesButton onClick");
			openPhotoLibraryMenu();
		}
	};

	private OnClickListener takeShotListener = new OnClickListener() {
		public void onClick(View v) {
			Log.d(TAG, "chooseImagesButton onClick");
			openImageCaptureMenu();
		}
	};

	private TaskListener mSendTaskListener = new TaskAdapter() {
		@Override
		public void onPreExecute(GenericTask task) {
			onSendBegin();
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			endTime = System.currentTimeMillis();
			Log.d("LDS", "Sended a status in " + (endTime - startTime));

			if (result == TaskResult.AUTH_ERROR) {
				logout();
			} else if (result == TaskResult.OK) {// 成功发送
				onSendSuccess();
			} else if (result == TaskResult.IO_ERROR) {
				onSendFailure();
			}
		}

		@Override
		public String getName() {
			return "SendTask";
		}
	};

	private void doSend() {
		Log.d(TAG, "dosend  " + withPic);
		startTime = System.currentTimeMillis();

		if (mSendTask != null
				&& mSendTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {

			String description = descEditText.getText().toString();
			String tags = tagsEditText.getText().toString();
			boolean storyable = allowStory.isChecked();

			if (withPic || mFile != null) {

				int mode = SendTask.TYPE_NORMAL;
				if (withPic)
					mode = SendTask.TYPE_PHOTO;

				mSendTask = new SendTask();
				mSendTask.setListener(mSendTaskListener);

				TaskParams params = new TaskParams();
				params.put("mode", mode);
				params.put("file", mFile);
				params.put("description", description);
				params.put("tags", tags);
				params.put("allowStory", storyable ? "1" : "0");
				params.put("api", PintuApp.mApi);
				mSendTask.execute(params);

			} else {
				updateProgress(getString(R.string.page_text_is_null));
			}
		}
	}

	private void onSendBegin() {
		disableEntry();
		dialog = ProgressDialog.show(this, "",
				getString(R.string.page_status_updating), true);
		if (dialog != null) {
			dialog.setCancelable(false);
		}
	}

	private void logout() {
		// do nothing temporally
	}

	private void onSendSuccess() {
		if (dialog != null) {
			dialog.setMessage(getString(R.string.page_status_update_success));
			dialog.dismiss();
		}

		updateProgress(getString(R.string.page_status_update_success));
		enableEntry();

		finish();

	}

	private void updateProgress(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	private void onSendFailure() {
		dialog.setMessage(getString(R.string.page_status_unable_to_update));
		dialog.dismiss();
		updateProgress(getString(R.string.page_status_unable_to_update));
		enableEntry();
	}

	private void enableEntry() {
		descEditText.setEnabled(true);
		chooseImagesButton.setEnabled(true);
	}

	private void disableEntry() {
		descEditText.setEnabled(false);
		chooseImagesButton.setEnabled(false);
	}

	private void openPhotoLibraryMenu() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, REQUEST_PHOTO_LIBRARY);
	}

	private void openImageCaptureMenu() {
		try {
			// TODO: API < 1.6, images size too small
			File mImageFile = new File(FileHelper.getBasePath(), "upload.jpg");
			mImageUri = Uri.fromFile(mImageFile);
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
			startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	/**
	 * take picture or select from album, then reach to this method, which is a
	 * callback of startActivityForResult
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log.d(TAG, ">>> onActivityResult... to getPic");

		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			// get the shotted image, from previously saved uri
			getPic(mImageUri);
		} else if (requestCode == REQUEST_PHOTO_LIBRARY
				&& resultCode == RESULT_OK) {
			getPic(data.getData());
		}
	}

} // end of class