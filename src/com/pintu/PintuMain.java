package com.pintu;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pintu.task.GenericTask;
import com.pintu.task.SendTask;
import com.pintu.task.TaskAdapter;
import com.pintu.task.TaskListener;
import com.pintu.task.TaskParams;
import com.pintu.task.TaskResult;
import com.pintu.util.FileHelper;
import com.pintu.widget.TweetEdit;

public class PintuMain extends Activity {


    private static final String TAG = "PintuMain";
    private static final String SIS_RUNNING_KEY = "running";

    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_PHOTO_LIBRARY = 3;
    private static final int MAX_BITMAP_SIZE = 400;

    //Header
    private Button mBackButton;
    private Button mTopSendButton;

    
    // View
    private TweetEdit mTweetEdit;
    private EditText mTweetEditText;
    private TextView mProgressText;
    private ImageButton chooseImagesButton;
    private ImageButton mCameraButton;
    private ProgressDialog dialog;
    

    // Picture
    private boolean withPic=false ;
    private ImageView mPreview;
    private ImageView imageDelete;
    
    
    private long startTime = -1;
    private long endTime = -1;

    // Task
    private GenericTask mSendTask;
	
    //Ҫ���͵�ͼƬ�ļ�
    private File mFile;
	//�ݴ��ͼƬ��ַ
    private Uri mImageUri;
    
    
    
    
    
    
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //���ò���
        setContentView(R.layout.write);
        
        //��ȡ�����е��Ӿ�����
        getViews();
        
        //Ϊ�����Ӿ���������¼���������
        addEventListeners();
        
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause.");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume.");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop.");
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
    
    
    private void getViews(){
        //���ذ�ť
        mBackButton = (Button) this.findViewById(R.id.top_back);
        //����
        mTopSendButton = (Button) this.findViewById(R.id.top_send_btn);
        
        //��������
        mProgressText = (TextView) findViewById(R.id.progress_text);
        //���������
        mTweetEditText = (EditText) findViewById(R.id.tweet_edit);
        //����
        mCameraButton = (ImageButton) findViewById(R.id.camera_button);
        //����ͼƬ
        chooseImagesButton = (ImageButton) findViewById(R.id.choose_images_button);
        
        //Ԥ��ͼƬ
        mPreview = (ImageView) findViewById(R.id.preview);
        //ɾ��ͼƬ
        imageDelete = (ImageView) findViewById(R.id.image_delete);
        
        //��������
        mTweetEdit = new TweetEdit(mTweetEditText,
                (TextView) findViewById(R.id.chars_text));

        //TODO, ���������...    	
    }
    
    
    private void addEventListeners(){
    	//����
        mBackButton.setOnClickListener(mGoListener);
        //����
        mTopSendButton.setOnClickListener(sendListener);        
        //����ͼƬ
        chooseImagesButton.setOnClickListener(insertImgListener);
    	//���õ���������Ӧ������
        mCameraButton.setOnClickListener(takeShotListener);
        //ɾ����ѡͼƬ
        imageDelete.setOnClickListener(deleteListener);
        
        //TODO, ...
        
    }
    
    
    private void getPic(Uri uri) {

        // layout for picture mode
        changeStyleWithPic();

        withPic = true;
        
        //����Ҫ���͵��ļ�����
        mFile = null;

        if (uri.getScheme().equals("content")) {
            mFile = new File(getRealPathFromURI(uri));
        } else {
            mFile = new File(uri.getPath());
        }

        
        mPreview.setImageBitmap(createThumbnailBitmap(uri,MAX_BITMAP_SIZE));

        if (mFile == null) {
            updateProgress("Could not locate picture file. Sorry!");
            disableEntry();
        }

    }
    
    private void changeStyleWithPic() {
        // �޸Ĳ��� ����ǰ ͼƬ���У����������
        // mPreview.setLayoutParams(
        // new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
        // LayoutParams.FILL_PARENT)
        // );
        mPreview.setVisibility(View.VISIBLE);
        imageDelete.setVisibility(View.VISIBLE);
        mTweetEditText.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 2f));
    }
    
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaColumns.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    /**
     * ����΢��ͼ
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
            
            Log.d(TAG, "Create Thumbnail, SampleSize: "+scale);
            
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
    
    
    private OnClickListener mGoListener = new OnClickListener(){
        public void onClick(View v){
            finish();
        }
    };
	
    private OnClickListener sendListener = new OnClickListener(){
        public void onClick(View v){
        	doSend();
        }
    };
    
    private OnClickListener insertImgListener = new OnClickListener(){
        public void onClick(View v) {
            Log.d(TAG, "chooseImagesButton onClick");
            openPhotoLibraryMenu();
        }    	
    };
    
    private OnClickListener takeShotListener = new OnClickListener(){
        public void onClick(View v) {
            Log.d(TAG, "chooseImagesButton onClick");
            openImageCaptureMenu();
        }    	
    }; 
    
    private OnClickListener deleteListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = getIntent();
            intent.setAction(null);
            withPic = false;
            mPreview.setVisibility(View.INVISIBLE);
            imageDelete.setVisibility(View.INVISIBLE);
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
            } else if (result == TaskResult.OK) {//�ɹ�����
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
        Log.d(TAG, "dosend  "+withPic);
        startTime = System.currentTimeMillis();

        if (mSendTask != null
                && mSendTask.getStatus() == GenericTask.Status.RUNNING) {
            return;
        } else {
            String status = mTweetEdit.getText().toString();

            if (!TextUtils.isEmpty(status) || withPic) {
                int mode = SendTask.TYPE_NORMAL;

                if (withPic) {
                    mode = SendTask.TYPE_PHOTO;
                }

                mSendTask = new SendTask();
                mSendTask.setListener(mSendTaskListener);

                TaskParams params = new TaskParams();
                //����ģʽ
                params.put("mode", mode);
                //�����ļ�
                params.put("file", mFile);
                //��������
                params.put("status", status);
                //��APIҲ�Ž�ȥ
                params.put("api", PintuApp.mApi);
                //ִ�з�������
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
        updateProgress(getString(R.string.page_status_updating));
    }
    
    private void logout() {
    	//������Բ��Ӷ�����������Ϊ�������û����鲻�ã�
    }
    
    private void onSendSuccess(){
        if (dialog != null) {
            dialog.setMessage(getString(R.string.page_status_update_success));
            dialog.dismiss();
        }
        
        updateProgress(getString(R.string.page_status_update_success));
        enableEntry();

        updateProgress("");

        // ���ͳɹ����Զ��رս���
        finish();

        // �ر������
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mTweetEdit.getEditText().getWindowToken(),0);

    	
    }
    
    private void updateProgress(String progress) {
        mProgressText.setText(progress);
    }

    private void onSendFailure() {
        dialog.setMessage(getString(R.string.page_status_unable_to_update));
        dialog.dismiss();
        updateProgress(getString(R.string.page_status_unable_to_update));
        enableEntry();
    }
    private void enableEntry() {
        mTweetEdit.setEnabled(true);
        chooseImagesButton.setEnabled(true);
    }

    private void disableEntry() {
        mTweetEdit.setEnabled(false);
        chooseImagesButton.setEnabled(false);
    }

    private void openPhotoLibraryMenu() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        //��������
        startActivityForResult(intent, REQUEST_PHOTO_LIBRARY);
    }    
    
    private void openImageCaptureMenu() {
        try {
            // TODO: API < 1.6, images size too small
            File mImageFile = new File(FileHelper.getBasePath(), "upload.jpg");
            //������������ȡ���ս��ʱ��
            mImageUri = Uri.fromFile(mImageFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
            //�������������ճ���
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }    
    
    /**
     * ���պ����ѡȡ������startActivityForResult ��Ļص�����
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, ">>> onActivityResult... to getPic");
        
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //�������Ƿ��ܴ�����Ӧ�÷���ͼƬ��ַ
        	Bundle bundle = data.getExtras();
        	if(bundle!=null){
        		Uri newShot = bundle.getParcelable(MediaStore.EXTRA_OUTPUT);
        		String newFile = newShot.getPath();
        		Log.d(TAG, ">>> NEW Shot is: "+newFile);
        	}
            Log.d(TAG, ">>> SAVED IMG PATH IS: "+mImageUri.getPath());
        	//��ȡ������ص�ͼƬ����ʵǰ���Ѿ�������        	 
            getPic(mImageUri);
            
        } else if (requestCode == REQUEST_PHOTO_LIBRARY
                && resultCode == RESULT_OK) {
            
            //��ȡ��᷵�ص�ͼƬ
            getPic(data.getData());
            
        }
    }
    
    
    
    
} //end of class