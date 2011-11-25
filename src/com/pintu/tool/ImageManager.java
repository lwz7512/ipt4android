package com.pintu.tool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.pintu.PintuApp;
import com.pintu.R;
import com.pintu.http.HttpException;
import com.pintu.http.Response;


/**
 * Manages retrieval and storage of icon images. Use the put method to download
 * and store images. Use the get method to retrieve images from the manager.
 */
public class ImageManager  {
	
	private static final String TAG = "ImageManager";
	
	//lazyImageLoader使用的静态变量
    public static Bitmap mDefaultBitmap = ImageManager
    		.drawableToBitmap(PintuApp.mContext.getResources().getDrawable(R.drawable.greenframe));
    public static final int DEFAULT_COMPRESS_QUALITY = 90;    
    
    //品图移动客户端上传图片大小限制：960/640
    //这个尺寸应该比较合适了，大了传送时间长，小了屏幕看不全
    //2011/11/25
    private static final int IMAGE_MAX_WIDTH  = 960;
    private static final int IMAGE_MAX_HEIGHT = 640;    

    private Context mContext;
    // In memory cache.
    private Map<String, SoftReference<Bitmap>> mCache;
    // MD5 hasher.
    private MessageDigest mDigest;
    
    
    


	public ImageManager(Context context) {
        mContext = context;
        mCache = new HashMap<String, SoftReference<Bitmap>>();

        try {
            mDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // This shouldn't happen.
            throw new RuntimeException("No MD5 algorithm.");
        }
    }

    /**
     * 有点冗余的查找方法：
     * 在没有执行get方法的情况下比较有效
     * 
     * @param file file URL/file PATH
     * @param bitmap
     * @param quality
     * @throws HttpException 
     */
    public Bitmap safeGet(String file) throws HttpException {
        Bitmap bitmap = lookupFile(file); // first try file.
        
        if (bitmap != null) {
            synchronized (this) { 
            	//找到文件了才缓存，而不是下载完缓存
                mCache.put(file, new SoftReference<Bitmap>(bitmap));
            }
            return bitmap;
        } else { //get from web
        	String url = file;
        	//TODO, 真正的下载文件方法，并写文件；
            bitmap = downloadImage2(url);
            //这里不做缓存，思路有点怪异啊
            return bitmap;
        }
    }
    
    /**
     * 从缓存器和文件系统中读取图片
     * @param file file URL/file PATH
     * @param bitmap
     * @param quality
     */
    public Bitmap get(String file) {
        SoftReference<Bitmap> ref;
        Bitmap bitmap;

        // Look in memory first.
        synchronized (this) {
            ref = mCache.get(file);
        }

        if (ref != null) {
            bitmap = ref.get();

            if (bitmap != null) {
                return bitmap;
            }
        }

        // Now try file.
        bitmap = lookupFile(file);

        if (bitmap != null) {
            synchronized (this) {
            	//找到文件了才缓存，而不是下载完缓存
                mCache.put(file, new SoftReference<Bitmap>(bitmap));
            }
            return bitmap;
        }

        //莫名其妙的丢失  
        //upload: see profileImageCacheManager line 96
        Log.w(TAG, "Image is missing: " + file);
        
        //找不着先用默认图片顶着
        return mDefaultBitmap;
    }
	
	


    // MD5 hases are used to generate filenames based off a URL.
    private String getMd5(String url) {
        mDigest.update(url.getBytes());

        return getHashString(mDigest);
    }

    private String getHashString(MessageDigest digest) {
    	StringBuilder builder = new StringBuilder();
    	
    	for (byte b : digest.digest()) {
    		builder.append(Integer.toHexString((b >> 4) & 0xf));
    		builder.append(Integer.toHexString(b & 0xf));
    	}
    	
    	return builder.toString();
    }
    // Looks to see if an image is in the file system.
    private Bitmap lookupFile(String url) {
        String hashedUrl = getMd5(url);
        FileInputStream fis = null;

        try {
            fis = mContext.openFileInput(hashedUrl);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            // Not there.
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // Ignore.
                }
            }
        }
    }
    

    
    private Bitmap downloadImage2(String url) throws HttpException {
        Log.d(TAG, "[NEW]Fetching image: " + url);
        final Response res = PintuApp.mApi.getImgByUrl(url);
        String file = writeToFile(res.asStream(), getMd5(url));
        return BitmapFactory.decodeFile(file);
    }    
    
 
     /**
     * 普通的写文件操作：
     * 根据下载的字节流和加密字符串写文件
     * @param is
     * @param filename URL地址加密后的字符串
     * @return
     */
    private String writeToFile(InputStream is, String filename) {
        Log.d("LDS", "new write to file");
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            in  = new BufferedInputStream(is);
            out = new BufferedOutputStream(
                    mContext.openFileOutput(filename, Context.MODE_PRIVATE));
            byte[] buffer = new byte[1024];
            int l;
            while ((l = in.read(buffer)) != -1) {
                out.write(buffer, 0, l);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (in  != null) in.close();
                if (out != null) {
                    Log.d("LDS", "new write to file to -> " + filename);
                    out.flush();
                    out.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return mContext.getFilesDir() + "/" + filename;
    }
    
    
    
    /**
     * 判断缓存着中是否存在该文件对应的bitmap
     */
    public boolean isContains(String file) {
    	return mCache.containsKey(file);
    }

    public void clear() {
        String[] files = mContext.fileList();

        for (String file : files) {
            mContext.deleteFile(file);
        }

        synchronized (this) {
            mCache.clear();
        }
    }
 
    
    /**
     * Compress and resize the Image
     * 从相册或者相机中传递过来的图片文件：
     * 为减小上传文件尺寸，做无损压缩处理
     * @param targetFile
     * @param quality, 0~100, recommend 100
     * @return
     * @throws IOException
     */
    public File compressImage(File targetFile, int quality)  {
        String filepath = targetFile.getAbsolutePath();

        // 1. Calculate scale
        int scale = 1;
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, o);
        if (o.outWidth > IMAGE_MAX_WIDTH || o.outHeight > IMAGE_MAX_HEIGHT) {        	
        	 scale = (int) Math.pow( 2.0,
                     (int) Math.round(Math.log(IMAGE_MAX_WIDTH
                             / (double) Math.max(o.outHeight, o.outWidth))
                             / Math.log(0.5)));
        	Log.d(TAG, scale + " scale");
        }

        // 2. File -> Bitmap (Returning a smaller image)
        o.inJustDecodeBounds = false;
        o.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeFile(filepath, o);

        // 3. Bitmap -> File
        String compressedFile = writeCompressedFile(filepath, bitmap, quality);
        File compressedImage = mContext.getFileStreamPath(compressedFile);
        
        return compressedImage;
    }
    
    /**
     * 将Bitmap写入应用目录.
     * @param file URL/PATH
     * @param bitmap
     * @param quality
     */
    private String writeCompressedFile(String file, Bitmap bitmap, int quality) {
        if (bitmap == null) {
            Log.w(TAG, "Can't write file. Bitmap is null.");
            return null;
        }
        String hashedUrl = null;
        //分析文件名
        String imgType = null;
        if(file.endsWith("jpg")){
        	imgType = ".jpg";
        }else if(file.endsWith("png")){
        	imgType = ".png";
        }
        BufferedOutputStream bos = null;
        try {
        	hashedUrl = getMd5(file);
            //补充文件名后缀，否则上传时出错
            if(imgType!=null) hashedUrl += imgType;
            bos = new BufferedOutputStream(
                    mContext.openFileOutput(hashedUrl, Context.MODE_PRIVATE));
            //压缩文件到私有目录下
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos); // PNG
            Log.d(TAG, "Writing file: " + file);
        } catch (IOException ioe) {
            Log.e(TAG, ioe.getMessage());
        } finally {
            try {
                if (bos != null) {
                    bitmap.recycle();
                    bos.flush();
                    bos.close();
                }
                //bitmap.recycle();
            } catch (IOException e) {
                Log.e(TAG, "Could not close file.");
            }
        }
        return hashedUrl;
    }
    
    private static Bitmap drawableToBitmap(Drawable drawable) {
    	Bitmap bitmap = Bitmap
    			.createBitmap(
    					drawable.getIntrinsicWidth(),
    					drawable.getIntrinsicHeight(),
    					drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
    							: Bitmap.Config.RGB_565);
    	Canvas canvas = new Canvas(bitmap);
    	drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable
    			.getIntrinsicHeight());
    	drawable.draw(canvas);
    	return bitmap;
    }
  
    
    
// ---------------------Currently  useless methods, DO NOT DELETE! ----------------------
    
    
    /**
     * 下载远程图片 -> 转换为Bitmap -> 写入缓存器.
     * @param url
     * @param quality image quality 1～100
     * @throws HttpException 
     */
//    public void put(String url, int quality, boolean forceOverride) throws HttpException {
//        if (!forceOverride && contains(url)) {
//            // Image already exists.
//            return;
//
//        }
//
//        Bitmap bitmap = downloadImage(url);
//        if (bitmap != null) {
//            put(url, bitmap, quality); // file cache
//        } else {
//            Log.w(TAG, "Retrieved bitmap is null.");
//        }
//    }
    
    /**
     * 重载 put(String url, int quality)
     * @param url
     * @throws HttpException 
     */
//    public void put(String url) throws HttpException {
//        put(url, DEFAULT_COMPRESS_QUALITY, false);
//    }
    
    /**
     * 将本地File -> 转换为Bitmap -> 写入缓存器.
     * 如果图片大小超过MAX_WIDTH/MAX_HEIGHT, 则将会对图片缩放.
     * 
     * @param file
     * @param quality 图片质量(0~100)
     * @param forceOverride 
     * @throws IOException
     */
//    public void put(File file, int quality, boolean forceOverride) throws IOException {
//        if (!file.exists()) {
//            Log.w(TAG, file.getName() + " is not exists.");
//            return;
//        }
//        if (!forceOverride && contains(file.getPath())) {
//            // Image already exists.
//            Log.d(TAG, file.getName() + " is exists");
//            return;
//        }
//
//        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
//        //bitmap = resizeBitmap(bitmap, MAX_WIDTH, MAX_HEIGHT);
//
//        if (bitmap == null) {
//            Log.w(TAG, "Retrieved bitmap is null.");
//        } else {
//            put(file.getPath(), bitmap, quality);
//        }
//    }
    

//  public Bitmap get(File file) {
//      return get(file.getPath());
//  }
    
    /**
     * 保持长宽比缩小Bitmap
     * 
     * @param bitmap
     * @param maxWidth
     * @param maxHeight
     * @param quality 1~100
     * @return
     */
//    public Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
//        
//        int originWidth  = bitmap.getWidth();
//        int originHeight = bitmap.getHeight();
//        
//        // no need to resize
//        if (originWidth < maxWidth && originHeight < maxHeight) { 
//            return bitmap;
//        }
//        
//        int newWidth  = originWidth;
//        int newHeight = originHeight;
//        
//        // 若图片过宽, 则保持长宽比缩放图片
//        if (originWidth > maxWidth) {
//            newWidth = maxWidth;
//            
//            double i = originWidth * 1.0 / maxWidth;
//            newHeight = (int) Math.floor(originHeight / i);
//        
//            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
//        }
//        
//        // 若图片过长, 则从中部截取
//        if (newHeight > maxHeight) {
//            newHeight = maxHeight;
//            
//        	int half_diff = (int)((originHeight - maxHeight)  / 2.0);
//            bitmap = Bitmap.createBitmap(bitmap, 0, half_diff, newWidth, newHeight);
//        }
//        
//        Log.d(TAG, newWidth + " width");
//        Log.d(TAG, newHeight + " height");
//        
//        return bitmap;
//    }

//  public void cleanup(HashSet<String> keepers) {
//  String[] files = mContext.fileList();
//  HashSet<String> hashedUrls = new HashSet<String>();
//
//  for (String imageUrl : keepers) {
//      hashedUrls.add(getMd5(imageUrl));
//  }
//
//  for (String file : files) {
//      if (!hashedUrls.contains(file)) {
//          Log.d(TAG, "Deleting unused file: " + file);
//          mContext.deleteFile(file);
//      }
//  }
//}

    /**
     * Downloads a file
     * @param url
     * @return
     * @throws HttpException 
     */
//    public Bitmap downloadImage(String url) throws HttpException {
//        Log.d(TAG, "Fetching image: " + url);
//        Response res = PintuApp.mApi.getImgByUrl(url);
//        return BitmapFactory.decodeStream(new BufferedInputStream(res.asStream()));
//    }

//    public void setContext(Context context) {
//        mContext = context;
//    }


//  public boolean contains(String url) {
//      return get(url) != mDefaultBitmap;
//  }   
    
    /** 以前用该方法是配合downloadImage，后来不用了
	 * 重载 put(String file, Bitmap bitmap, int quality)
	 * @param filePath file path
	 * @param bitmap
	 * @param quality 1~100
	 */
//	public void put(String file, Bitmap bitmap) {
//		synchronized (this) {
//			mCache.put(file, new SoftReference<Bitmap>(bitmap));
//		}
//		writeFile(file, bitmap, DEFAULT_COMPRESS_QUALITY);
//	}
    
}
