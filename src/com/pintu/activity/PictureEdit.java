package com.pintu.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.inputmethod.*;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.activity.base.FullScreenActivity;
import com.pintu.task.GenericTask;
import com.pintu.task.SendTask;
import com.pintu.task.TaskAdapter;
import com.pintu.task.TaskListener;
import com.pintu.task.TaskParams;
import com.pintu.task.TaskResult;
import com.pintu.util.FileHelper;
import com.pintu.util.Preferences;

public class PictureEdit extends FullScreenActivity {

	private static final String TAG = "PictureEdit";
	private static final String SIS_RUNNING_KEY = "running";

	private static final int REQUEST_IMAGE_CAPTURE = 2;
	private static final int REQUEST_PHOTO_LIBRARY = 3;
	private static final int MAX_BITMAP_SIZE = 400;

	// Header
	private ImageButton mBackButton;
	private Button mTopSendButton;

	private EditText tagsEditText;
	private EditText descEditText;
	private CheckBox isOriginal;
	private ImageButton chooseImagesButton;
	private ImageButton mCameraButton;
	private ProgressDialog dialog;

	// Picture
	private ImageView mPreview;

	private long startTime = -1;
	private long endTime = -1;

	// Task
	private GenericTask mSendTask;

	// the picture to send
	private File mFile;
	// temporal saved image
	private Uri mImageUri;

	// -------------------- Construction UI Logic -------------------------

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, ">>> onCreate... to create pic edit...");

		// 要先判断是否已登录
		// 因为通过画廊的分享功能
		// 可以直接打开该活动
		shouldEdit();

		setContentView(R.layout.postpic);
		// 获取组件引用
		getViews();
		// 添加事件监听
		addEventListeners();

		// 该活动有可能从画廊的分享菜单打开
		// 从相册中分享传送过来的图片
		sharePicFromGallery();
				
	}
	
	/**
	 * 手机拍照统一处理逻辑
	 * 用拍照时间来判断发生的动作，是第一次进来还是刚才拍过照 
	 * 2011/12/06
	 */
	public void onResume(){
		super.onResume();

		Log.d(TAG, "on Resume...");		

		Boolean shotFlag = false;
		Long shotTime =  getSharedPreferences(TAG, 0).getLong("shottTime", 0);
		Long shotDiff = new Date().getTime() - shotTime;
		
		Log.d(TAG, "shotDiff: "+shotDiff);
		
		//FIXME, 判断结果返回时间是否为最近，最近的拍照才给显示
		//否则就是时间久远了，表示第一次进来，所以不显示照片
		int durationSec = 2;				
		if(shotDiff>durationSec*1000){
			//这是在刚进来准备发图，不往下走了
			return;
		}else{
			//这是从相机返回了
			shotFlag = true;
		}
		//从偏好对象中找回拍照时记录的文件路径
		String capturePath = getLastCaptureFile();
		
		if(capturePath==null){
			this.updateProgress("OH NO, last capture file not found!");
		}
		if(capturePath!=null && shotFlag){
			Uri tempImg = Uri.parse(capturePath);
			Bitmap bm = createThumbnailBitmap(tempImg, MAX_BITMAP_SIZE);
			//终于可以显示了
			centerDisplayThumbnail(bm);
			
			String filePath = getRealPathFromURI(tempImg);
			Log.d(TAG, "filePath: "+filePath);
			//保存该文件，以备上传
			mFile = new File(filePath);			
		}
	}

	private void sharePicFromGallery() {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		Uri uri = null;
		if (extras != null) {
			uri = (Uri) (extras.get(Intent.EXTRA_STREAM));
		}
		if (Intent.ACTION_SEND.equals(intent.getAction()) && uri != null) {
			getPic(uri);
		}
	}

	private void shouldEdit() {
		boolean userLogged = PintuApp.isLoggedin();
		if (!userLogged) {
			Intent it = new Intent();
			it.setClass(this, LogonSys.class);
			// 启动登录
			startActivity(it);
			// 关闭当前
			finish();
		}
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

		mSendTask = null;

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
		mBackButton = (ImageButton) this.findViewById(R.id.top_back);
		mTopSendButton = (Button) this.findViewById(R.id.top_send_btn);

		mPreview = (ImageView) findViewById(R.id.preview);
		tagsEditText = (EditText) findViewById(R.id.tags);
		descEditText = (EditText) findViewById(R.id.description);

		isOriginal = (CheckBox) findViewById(R.id.isOriginal);

		mCameraButton = (ImageButton) findViewById(R.id.camera_button);
		chooseImagesButton = (ImageButton) findViewById(R.id.choose_images_button);

	}

	private void addEventListeners() {
		mBackButton.setOnClickListener(mGoListener);
		mTopSendButton.setOnClickListener(sendListener);
		chooseImagesButton.setOnClickListener(insertImgListener);
		mCameraButton.setOnClickListener(takeShotListener);

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
			Log.d(TAG, "chooseImage Button onClick");
			openPhotoLibraryMenu();
		}
	};

	private OnClickListener takeShotListener = new OnClickListener() {
		public void onClick(View v) {
			Log.d(TAG, "takeShot Button onClick");			
			takeShotToMediaLib();
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
			} else if (result == TaskResult.FAILED) {
				onSendFailure();
			} else if (result == TaskResult.IO_ERROR) {
				onSendFailure();
			}
		}

		public void deliverResponseString(String response) {
			// 测过了中文没问题
			// if(response!=null) PictureEdit.this.updateProgress(response);
		}

	};

	private void doSend() {
		
		if (mSendTask != null
				&& mSendTask.getStatus() == GenericTask.Status.RUNNING)
			return;

		String description = descEditText.getText().toString();
		String tags = tagsEditText.getText().toString();
		boolean original = isOriginal.isChecked();

		if (mFile != null) {

			int mode =SendTask.TYPE_PHOTO;					

			mSendTask = new SendTask();
			mSendTask.setListener(mSendTaskListener);

			TaskParams params = new TaskParams();
			params.put("mode", mode);
			params.put("file", mFile);
			params.put("description", description);
			params.put("tags", tags);
			params.put("isOriginal", original ? "1" : "0");
			mSendTask.execute(params);

		} else {
			updateProgress(getString(R.string.page_text_is_null));
		}

	}

	private void onSendBegin() {
		disableEntry();
		dialog = ProgressDialog.show(this, "",
				getString(R.string.page_status_updating), true);
		if (dialog != null) {
			dialog.setCancelable(true);
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
		// 关闭当前活动，回画廊
		finish();
	}

	private void updateProgress(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	private void onSendFailure() {
		dialog.setMessage(getString(R.string.page_status_unable_to_update));
		dialog.dismiss();
		updateProgress(getString(R.string.page_status_unable_to_update));
		enableEntry();
	}

	private void enableEntry() {
		tagsEditText.setEnabled(true);
		descEditText.setEnabled(true);
		chooseImagesButton.setEnabled(true);
	}

	private void disableEntry() {
		tagsEditText.setEnabled(false);
		descEditText.setEnabled(false);
		chooseImagesButton.setEnabled(false);
	}
	
	/**
	 * 使用媒体库的拍照方式
	 * 2011/12/5
	 */
	private void takeShotToMediaLib(){
		//在这里启动Camera:
		
		//Camera中定义了一个Intent-Filter，其中Action是android.media.action.IMAGE_CAPTURE   
		//我们使用的时候，最好不要直接使用这个，而是用MediaStore中的常量ACTION_IMAGE_CAPTURE.   
		//这个常量就是对应的上面的action   
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
		
		//这里我们插入一条数据，ContentValues是我们希望这条记录被创建时包含的数据信息   
		//这些数据的名称已经作为常量在MediaStore.Images.Media中,有的存储在MediaStore.MediaColumn中了   		
		ContentValues values = new ContentValues(3);
		String fileName =  getPhotoFilename(new Date());
		values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);   
		values.put(MediaStore.Images.Media.DESCRIPTION, "this is description");   
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); 
		
		mImageUri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);   
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri); 
		
		//记下来，返回到发送界面时使用
		String imgUri = mImageUri.toString();
		rememberCaptureFile(imgUri);
		
//		this.updateProgress("Captured in: "+imgUri);
		
		//这样就将文件的存储方式和uri指定到了Camera应用中   
		//由于我们需要调用完Camera后，可以返回Camera获取到的图片，   
		//所以，我们使用startActivityForResult来启动Camera   
		startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);    
	}

	/**
	 * @deprecated 这个方法在三星手机上拍照会产生重复文件生成
	 * 所以要废弃掉，换用媒体库方式存储
	 */
	private void openImageCaptureMenu() {
		try {
			String filename = getPhotoFilename(new Date());
			// FIXME, 记下来返回时方便获取
			rememberCaptureFile(filename);
			mFile = new File(FileHelper.getBasePath(), filename);
			mImageUri = Uri.fromFile(mFile);
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
			startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	private String getPhotoFilename(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		return dateFormat.format(date) + ".jpg";
	}
	
	private void openPhotoLibraryMenu() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		// intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
		// "image/*");
		startActivityForResult(intent, REQUEST_PHOTO_LIBRARY);
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
			//FIXME, 拍过照了，记下来时间以便显示时判断
			//在onResume方法中处理照片压缩显示并准备发送
			Long shotTime = new Date().getTime();
			this.getSharedPreferences(TAG, 0).edit().putLong("shottTime", shotTime).commit();		
			Log.d(TAG, "shotTime: "+shotTime);
			
		} else if (requestCode == REQUEST_PHOTO_LIBRARY
				&& resultCode == RESULT_OK) {
			getPic(data.getData());
		}
	}

	/**
	 * 这个方法只为从画廊取图而使用，拍照取图统一走onResume方法中处理
	 * @param uri
	 */
	private void getPic(Uri uri) {
		Uri tempImg = uri;		

		if (tempImg.getScheme().equals("content")) {// 从画廊获取
			//要存下来，发送数据时需要
			mFile = new File(getRealPathFromURI(tempImg));					
		}else{
			return;
		}
		
		Bitmap thumbnail = createThumbnailBitmap(tempImg, MAX_BITMAP_SIZE);
		//可以显示了
		centerDisplayThumbnail(thumbnail);
		
	}
	
	/**
	 * 这里必须编码设置一下布局，否则无法居中
	 * xml布局文件中无法达到这种效果，老跑偏
	 *  lwz7512 @ 2011/08/18
	 * @param thumbnail
	 */
	private void centerDisplayThumbnail(Bitmap thumbnail){
		int picWidth = thumbnail.getWidth();
		int picHeight = thumbnail.getHeight();
		LinearLayout.LayoutParams layouts = new LinearLayout.LayoutParams(
				picWidth, picHeight);
		layouts.bottomMargin = 10;
		layouts.topMargin = 10;
		layouts.gravity = Gravity.CENTER;

		mPreview.setLayoutParams(layouts);				
		mPreview.setImageBitmap(thumbnail);
	}
	
	/**
	 * @deprecated 不从SD卡上找照片了
	 * @return SD卡上文件的地址
	 */
	private Uri findImageByName(){
		Uri uri = null;
		try {
			String fileName = getLastCaptureFile();
			if (fileName == null) {
				updateProgress("Caputured image file name is null!");
				return null;
			}
			mFile = new File(FileHelper.getBasePath(), fileName);

			if (!mFile.exists()) {
				this.updateProgress("the file doesnot exist: "
						+ mFile.getAbsolutePath());
			}
			uri = Uri.fromFile(mFile);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return uri;
	}


	/**
	 * 记录照片在媒体库的Uri路径
	 * @param fileName
	 */
	private void rememberCaptureFile(String uriPath) {
		Log.d(TAG, "uriPath: "+uriPath);
		getPreferences().edit()
				.putString(Preferences.LAST_CAPTURE_FILE, uriPath).commit();
	}

	private String getLastCaptureFile() {
		String captureImg = getPreferences().getString(
				Preferences.LAST_CAPTURE_FILE, null);
		if (captureImg != null) {
			return captureImg;
		} else {
			this.updateProgress("captured picture name has losted!!");
		}
		return null;
	}

	/**
	 * 根据内容uri得到文件的绝对路径
	 * @param contentUri
	 * @return 以/开头的文件绝对路径
	 */
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
			// 这段很关键啊，能让图片变小
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

//			Log.d(TAG, "Create Thumbnail, SampleSize: " + scale);

			input = getContentResolver().openInputStream(uri);
			return BitmapFactory.decodeStream(input, null, options);

		} catch (IOException e) {
			Log.w(TAG, e);
			this.updateProgress("file not found while create thumbnail!!!");
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

	public static File bitmapToFile(Bitmap bitmap) {
		try {
			File file = new File(FileHelper.getBasePath(), "upload.jpg");
			FileOutputStream out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
				out.flush();
				out.close();
			}
			return file;
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Sorry, the file can not be created. " + e.getMessage());
			return null;
		} catch (IOException e) {
			Log.e(TAG,
					"IOException occurred when save upload file. "
							+ e.getMessage());
			return null;
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

} // end of class
