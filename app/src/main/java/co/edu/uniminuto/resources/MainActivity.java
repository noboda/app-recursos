package co.edu.uniminuto.resources;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // UI
    private TextView tvAndroidVersionLB;
    private TextView tvBatteryLevel;
    private ProgressBar pbBattery;
    private TextView tvConnection;
    private Button onFlash;
    private Button offFlash;
    private EditText etNombreArchivo;
    private Button btnGuardarArchivo;
    private Button btnBluetooth;
    private Button btnCamera;

    // Servicios
    private CameraManager cameraManager;
    private String cameraId;
    private ConnectivityManager conexion;
    private IntentFilter batteryFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initView();
        initCamera();
        eventsClick();

        batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(broReceiver, batteryFilter);
    }

    // Ir al activity de bluetooth
    private void openBluetoothActivity(View view) {
        Intent intent = new Intent(this, MainActivityBluetooth.class);
        startActivity(intent);
    }

    // Ir al activity de la camara
    private void openCameraActivity(View view) {
        Intent intent = new Intent(this, MainActivityCamera.class);
        startActivity(intent);
    }

    // Guardar Archivo
    private void saveFile(View view) {
        String nameFile = etNombreArchivo.getText().toString().trim();

        if (nameFile.isEmpty()) {
            etNombreArchivo.setError("Ingresa un nombre valido");
            return;
        }

        // Datos del archivo txt
        String versionOS = Build.VERSION.RELEASE;
        int batteryLevel = pbBattery.getProgress();
        String content = "Nombre del estudiante: Luis David\n" + "Nivel de bateria " + batteryLevel + "%\n" + "Version de Android: " + versionOS + "\n";

        try {
            File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloads, nameFile + ".txt");

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();
            Toast.makeText(this, "Archivo guardado en: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e("SaveFile", "Error al guardar el archivo", e);
            Toast.makeText(this, "Error al guardar el archivo", Toast.LENGTH_SHORT).show();
        }

    }

    // Inicializar camara
    private void initCamera() {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            Log.e("CamInit", "No se pudo acceder a la cámara", e);
        }
    }

    // Batería
    BroadcastReceiver broReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int levelBattery = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            pbBattery.setProgress(levelBattery);
            tvBatteryLevel.setText("Nivel batería: " + levelBattery + "%");
        }
    };

    // Flash
    private void offLight(View view) {
        try {
            cameraManager.setTorchMode(cameraId, false);
        } catch (CameraAccessException e) {
            Log.i("Linterna:", e.getMessage());
        }
    }

    private void onLight(View view) {
        try {
            cameraManager.setTorchMode(cameraId, true);
        } catch (CameraAccessException e) {
            Log.i("Linterna:", e.getMessage());
        }
    }

    // Conexion
    private void checkConnection() {
        try {
            conexion = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (conexion != null) {
                NetworkInfo networkInfo = conexion.getActiveNetworkInfo();
                boolean stateNet = networkInfo != null && networkInfo.isConnectedOrConnecting();
                if (stateNet) {
                    tvConnection.setText("Estado: Conectado");
                } else {
                    tvConnection.setText("Estado: Desconectado");
                }
            }
        } catch (Exception e) {
            Log.i("Conexion:", e.getMessage());
        }
    }

    // Version Android
    @Override
    protected void onResume() {
        super.onResume();
        String versionOS = Build.VERSION.RELEASE;
        int versionSDK = Build.VERSION.SDK_INT;
        tvAndroidVersionLB.setText("Versión SO: " + versionOS + " / SDK: " + versionSDK);
        checkConnection();
    }

    // Eventos clicks
    private void eventsClick() {
        onFlash.setOnClickListener(this::onLight);
        offFlash.setOnClickListener(this::offLight);
        btnGuardarArchivo.setOnClickListener(this::saveFile);
        btnBluetooth.setOnClickListener(this::openBluetoothActivity);
        btnCamera.setOnClickListener(this::openCameraActivity);
    }

    // Inicializacion de vistas
    private void initView() {
        this.tvAndroidVersionLB = findViewById(R.id.tvAndroidVersionLB);
        this.tvBatteryLevel = findViewById(R.id.tvBatteryLevel);
        this.pbBattery = findViewById(R.id.pbBattery);
        this.tvConnection = findViewById(R.id.tvState);
        this.onFlash = findViewById(R.id.btnActivar);
        this.offFlash = findViewById(R.id.btnDesactivar);
        this.etNombreArchivo = findViewById(R.id.etNombreArchivo);
        this.btnGuardarArchivo = findViewById(R.id.btnGuardarArchivo);
        this.btnBluetooth = findViewById(R.id.btnBluetooth);
        this.btnCamera = findViewById(R.id.btnCamera);
    }

}
