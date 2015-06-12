package gmsyrimis.c4q.nyc.cammy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VanillaActivity extends Activity {

    private ImageView pictureHolder;
    private RelativeLayout vanillaLayout;
    private Bitmap bitmap;
    private Button shareBt;
    private Button saveBt;
    private EditText topRowEditText;
    private EditText bottomEditText;



    private String imageUri = "";
    public static String IMAGE_URI_KEY = "uri";
    private String topText;
    public static String TOP_TEXT_KEY = "top";
    private String bottomText;
    public static String BOTTOM_TEXT_KEY = "bottom";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vanilla);

        //View
        vanillaLayout = (RelativeLayout) findViewById(R.id.vanilla_holder);
        pictureHolder =(ImageView)findViewById(R.id.vanilla_custom_image);
        topRowEditText = (EditText) findViewById(R.id.vanilla_top_text);
        bottomEditText = (EditText) findViewById(R.id.vanilla_bottom_text);
        if (savedInstanceState == null) {
            imageUri = getIntent().getStringExtra(IMAGE_URI_KEY);
            topText = "";
            bottomText = "";
        } else {
            imageUri = savedInstanceState.getString(IMAGE_URI_KEY);
            topText = savedInstanceState.getString(TOP_TEXT_KEY);
            bottomText = savedInstanceState.getString(BOTTOM_TEXT_KEY);
        }

        //Set Values to Strings
        topRowEditText.setText(topText);
        bottomEditText.setText(bottomText);


        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(imageUri));
        } catch (IOException e) {
            e.printStackTrace();
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, 330, 210, true);
        pictureHolder.setImageDrawable(new FakeBitmapDrawable(bitmap, 0));


        DisplayMetrics metrics = getBaseContext().getResources().getDisplayMetrics();
        bitmap = Bitmap.createScaledBitmap(bitmap, metrics.widthPixels, metrics.heightPixels, true);
        vanillaLayout.setBackground(new FakeBitmapDrawable(bitmap, 0));

        shareBt = (Button) findViewById(R.id.btShare);
        saveBt =(Button) findViewById(R.id.save_btn);

        saveBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri resultUri = makeViewBitmap(vanillaLayout, vanillaLayout.getWidth(), vanillaLayout.getHeight());
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(resultUri);
                sendBroadcast(mediaScanIntent);
            }
        });

        // SHARE BUTTON
        shareBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri resultUri = makeViewBitmap(vanillaLayout, vanillaLayout.getWidth(), vanillaLayout.getHeight());
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, resultUri);
                intent.putExtra(Intent.EXTRA_TEXT, "");
                Intent chooser = Intent.createChooser(intent, "Send Picture");
                startActivity(chooser);
            }
        });


    }


    public Uri makeViewBitmap(View view, int width, int height) {
        // VIEW TO BITMAP
        Bitmap viewBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(viewBitmap);
        view.layout(0, 0, view.getLayoutParams().width, view.getLayoutParams().height);
        view.draw(c);

        // FILE SETUP
        String uniqueIdentifier = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss").format(new Date());
        String fileName = "Snapmeme" + uniqueIdentifier;
        File fileDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        // CREATING TEMP FILE
        File imageFILE = null;
        try {
            imageFILE = File.createTempFile(fileName, ".jpg", fileDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // STREAM INTO TEMP FILE
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(imageFILE);
            viewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        // URI TO BE PASSED
        return Uri.fromFile(imageFILE);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(IMAGE_URI_KEY, imageUri);
        outState.putString(TOP_TEXT_KEY, topText);
        outState.putString(BOTTOM_TEXT_KEY, bottomText);
    }
}
