// Rich Hanson 2017
// some my work except stuff taken from various sources on StackOverflow
// Credit where credit is due

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.content.pm.PackageManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.Manifest;
import android.support.v4.content.PermissionChecker;
import android.support.v4.app.ActivityCompat;
import android.util.logs

class photogallery{
	private ContentResolver contentResolver;
    private Activity _activity;

	// get permission
	public void getPermission() {
		_activity=BBAndroidGame.AndroidGame().GetActivity();
		contentResolver = _activity.getContentResolver();

		if (Build.VERSION.SDK_INT >= 23) {
			if (PermissionChecker.checkSelfPermission(_activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(_activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
			}
		}
	}

	// check permission
	public int checkPermission() {
		_activity=BBAndroidGame.AndroidGame().GetActivity();
		contentResolver = _activity.getContentResolver();

		if (Build.VERSION.SDK_INT >= 23) {
			if (PermissionChecker.checkSelfPermission(_activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
				return 1;
			}
		}
		return -1;
	}
	
	// get list of photos from gallery
	public String[] getPhotos(){
		_activity=BBAndroidGame.AndroidGame().GetActivity();
		contentResolver = _activity.getContentResolver();

		if (Build.VERSION.SDK_INT >= 23) {
			if (PermissionChecker.checkSelfPermission(_activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(_activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
			}
		}

		// which image properties are we querying
		String[] projection = new String[] {
				MediaStore.Images.Media._ID,
				MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
				MediaStore.Images.Media.DATE_TAKEN,
				MediaStore.Images.ImageColumns.DATA
		};

		// content:// style URI for the "primary" external storage volume
		Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

		// Make the query.
		Cursor cur = contentResolver.query(images,
				projection, // Which columns to return
				null,       // Which rows to return (all rows)
				null,       // Selection arguments (none)
				null        // Ordering
				);

		ArrayList<String> result = new ArrayList<String>();

		if (cur.moveToFirst()) {
			String bucket;
			String date;
			String data;
			int bucketColumn = cur.getColumnIndex(
				MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

			int dateColumn = cur.getColumnIndex(
				MediaStore.Images.Media.DATE_TAKEN);

			int dataColumn = cur.getColumnIndex(
				MediaStore.Images.ImageColumns.DATA);

			do {
				// Get the field values
				bucket = cur.getString(bucketColumn);
				date = cur.getString(dateColumn);
				data = cur.getString(dataColumn);
			
				result.add(data);

			} while (cur.moveToNext());

		}
		String[] newresult = new String[result.size()];
		newresult = result.toArray(newresult);

		return newresult;
		}

    private Bitmap getBM(String uri) {
        return getBM(uri,-1,-1);
    }

	private Bitmap getBitmapFromURL(String src) {
		try {
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (IOException e) {
			// Log exception
			return null;
		}
	}

    private Bitmap getBM(String uri, int width, int height) {
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(uri);
		} catch (IOException e) {
			e.printStackTrace();
		}  
		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 
											   ExifInterface.ORIENTATION_UNDEFINED);

		_activity=BBAndroidGame.AndroidGame().GetActivity();
        contentResolver = _activity.getContentResolver();

        BitmapFactory.Options options=new BitmapFactory.Options();
		// full size? doubt it. massive memory
        if (width>-1) {
	        options.inJustDecodeBounds=true;
	        BitmapFactory.decodeFile(uri,options);
	        options.inSampleSize=getScale(options.outWidth,options.outHeight,width,height);
        }
        options.inJustDecodeBounds=false;
 
        Bitmap bitmap = null;
        bitmap = BitmapFactory.decodeFile(uri,options);

		bitmap = rotateBitmap(bitmap, orientation);

        return bitmap;
    }

    private int getScale(int originalWidth,int originalHeight, int requiredWidth, int requiredHeight) {
        //a scale of 1 means the original dimensions 
        //of the image are maintained
        int scale=1;
            
        //calculate scale only if the height or width of 
        //the image exceeds the required value.
        if((originalWidth>requiredWidth) || (originalHeight>requiredHeight)) {
            //calculate scale with respect to
            //the smaller dimension
            if(originalWidth<originalHeight)
            	// portrait
                scale=Math.round((float)originalWidth/requiredWidth);
            else
            	// landscape
                scale=Math.round((float)originalHeight/requiredHeight);
            }
           
        return scale;
        }

	public int[] loadPhotoData(String url,int sizex, int sizey) {
        Uri uri = Uri.parse("file://" +url);
        Bitmap bitmap;

		//  can load from local resource and http resource
		if (url.contains("http:") || url.contains("https:")) {
			bitmap = getBitmapFromURL(url);
		} else {
			bitmap = getBM(url,sizex,sizey);
		}
		
		if (bitmap == null) {
			return (new int[1]);
		}
		
        Bitmap newbm = Bitmap.createBitmap(sizex, sizey, Bitmap.Config.ARGB_8888);
        Canvas newcn = new Canvas(newbm);

        Rect src = new Rect(0, 0, sizex, sizey);
        Rect dest = new Rect(src);

		int scaledHeight;
		int scaledWidth;
		Bitmap scaledbitmap;

		if (bitmap.getWidth() > bitmap.getHeight()) {
	        scaledHeight = sizey;
	        scaledWidth = (int)(bitmap.getWidth() / ((float)bitmap.getHeight() / scaledHeight));
		} else {
	        scaledWidth = sizex;
	        scaledHeight = (int)(bitmap.getHeight() / ((float)bitmap.getWidth() / scaledWidth));
		}
	    scaledbitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);

        src.offset((scaledWidth-sizex)/2,(scaledHeight-sizey)/2);
        newcn.drawBitmap(scaledbitmap, src, dest, null);

        int[] result= new int[sizex*sizey];
        newbm.getPixels(result,0,sizex,0,0,sizex,sizey);

		newbm = null;
		newcn = null;
		
        return result;
    }

	private static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

		android.graphics.Matrix matrix = new android.graphics.Matrix();
		switch (orientation) {
			case ExifInterface.ORIENTATION_NORMAL:
				return bitmap;
			case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
				matrix.setScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				matrix.setRotate(180);
				break;
			case ExifInterface.ORIENTATION_FLIP_VERTICAL:
				matrix.setRotate(180);
				matrix.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_TRANSPOSE:
				matrix.setRotate(90);
				matrix.postScale(-1, 1);
				break;
		   case ExifInterface.ORIENTATION_ROTATE_90:
			   matrix.setRotate(90);
			   break;
		   case ExifInterface.ORIENTATION_TRANSVERSE:
			   matrix.setRotate(-90);
			   matrix.postScale(-1, 1);
			   break;
		   case ExifInterface.ORIENTATION_ROTATE_270:
			   matrix.setRotate(-90);
			   break;
		   default:
			   return bitmap;
		}
		try {
			Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			bitmap.recycle();
			return bmRotated;
		}
		catch (OutOfMemoryError e) {
			e.printStackTrace();
			return null;
		}
	}
    
    public void loadPhoto(String url,String imagename,int sizex, int sizey) {
        _activity=BBAndroidGame.AndroidGame().GetActivity();
        contentResolver = _activity.getContentResolver();
        Uri uri = Uri.parse("file://" +url);

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri);
        } catch (FileNotFoundException e) {
                android.util.Log.e("CerberusX", "Error with bitmap "+url, e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
                android.util.Log.e("CerberusX", "Could not create file.", e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

		Bitmap newbm = Bitmap.createBitmap(sizex, sizey, Bitmap.Config.ARGB_8888);
		Canvas newcn = new Canvas(newbm);

        int scaleType = 1;
        int scaledWidth;
        int scaledHeight;

		scaledHeight = sizey;
		scaledWidth = (int)(bitmap.getWidth() / ((float)bitmap.getHeight() / scaledHeight));

		String path = "";
		File f=Environment.getExternalStorageDirectory();
		if( f!=null ) {
			path = f+"/" + imagename;
		}
		File file = new File( path );
		
		try {
			if(file.exists() == false) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}

		} catch (IOException e) {
			android.util.Log.e("CerberusX", "Could not create file.", e);
		}
		
		try {
			FileOutputStream out = new FileOutputStream(file);
			android.util.Log.i("[Monkey]", "ScaleType: " + scaleType);
			if(scaleType == 0) {
				bitmap.compress(Bitmap.CompressFormat.JPEG, 99, out);
				android.util.Log.i("[Monkey]", "Save original Bitmap.");
			} else {
				Bitmap scaledbitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);

				Rect src = new Rect(0, 0, sizex, sizey);
				Rect dest = new Rect(src);
				src.offset((scaledWidth-sizex)/2,0);
				newcn.drawBitmap(scaledbitmap, src, dest, null);

				newbm.compress(Bitmap.CompressFormat.JPEG, 99, out);
				android.util.Log.i("[Monkey]", "Save scaled Bitmap.");
			}
                
        } catch (IOException e) {
			android.util.Log.e("CerberusX", "Could not save scaled image.", e);
		}

    }
}