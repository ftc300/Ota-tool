package inshow.carl.com.ota_tool;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.URISyntaxException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import inshow.carl.com.ota_tool.entity.FileEntity;
import inshow.carl.com.ota_tool.tools.Utils;

import static inshow.carl.com.ota_tool.tools.Const.PERMISSION_REQ;

public class MainActivity extends BasicAct {

    @InjectView(R.id.file_name)
    TextView fileName;
    @InjectView(R.id.select_file)
    Button selectFile;
    private static final int FILE_SELECT_CODE = 44;
    private static final String TAG = "MainActivity";
    @InjectView(R.id.switchDfu)
    Button switchDfu;
    @InjectView(R.id.activity_main)
    LinearLayout activityMain;
    @InjectView(R.id.file_path)
    TextView filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.ACCESS_COARSE_LOCATION ,Manifest.permission.WRITE_EXTERNAL_STORAGE ,}, PERMISSION_REQ);
        }
        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });
        switchDfu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchTo(WeChatCaptureActivity.class);
            }
        });
        loadFileInfo();
    }


    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/zip");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d(TAG, "File Uri: " + uri.toString());
                    // Get the path
                    String path = null;
                    try {
                        path = Utils.getPath(this, uri);
                        Log.d(TAG, "File Path: " + path);
                        if (null != path) {
                            final File file = new File(path);
                            fileName.setText(file.getName());
                            filePath.setText(path);
                            saveSelectFileInfo(path, file.getName());
                        }

                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void saveSelectFileInfo(String path, String name) {
        FileEntity fileEntity = new FileEntity(path, name);
        fileEntity.save();
    }

    private void loadFileInfo() {
        FileEntity f = FileEntity.last(FileEntity.class);
        if (null != f) {
            fileName.setText(f.fileName);
            filePath.setText(f.filePath);
        }
    }


}
