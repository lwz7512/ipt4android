package com.pintu.http;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

import android.util.Log;

public class CountingMultiPartEntity extends MultipartEntity {

	private final ProgressListener listener;

	public CountingMultiPartEntity(final ProgressListener listener) {
		super();
		this.listener = listener;
	}

	public CountingMultiPartEntity(final HttpMultipartMode mode,
			final ProgressListener listener) {
		super(mode);
		this.listener = listener;
	}

	public CountingMultiPartEntity(HttpMultipartMode mode,
			final String boundary, final Charset charset,
			final ProgressListener listener) {
		super(mode, boundary, charset);
		this.listener = listener;
	}

	@Override
	public void writeTo(final OutputStream outstream) throws IOException {
		super.writeTo(new CountingOutputStream(outstream, this.listener));
	}

	/**
	 * 提供已上传字节数通知的接口方法
	 * @author lwz
	 *
	 */
	public static interface ProgressListener {
		public void transferred(long num);
		public void setTotalFileSize(long size);
	}

	public static class CountingOutputStream extends FilterOutputStream {

		private final ProgressListener listener;
		private long transferred;

		public CountingOutputStream(final OutputStream out,
				final ProgressListener listener) {
			super(out);
			this.listener = listener;
			this.transferred = 0;
		}

		public void write(byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
			this.transferred += len;			
//			Log.d("write len: ", String.valueOf(this.transferred));
			this.listener.transferred(this.transferred);
		}

		public void write(int b) throws IOException {
			out.write(b);
			this.transferred++;
//			Log.d("write b: ", String.valueOf(this.transferred));
			this.listener.transferred(this.transferred);
		}
	}
}