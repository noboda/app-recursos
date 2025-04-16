package co.edu.uniminuto.resources;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Set;


public class MainActivityBluetooth extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 2;
    private BluetoothAdapter bluetoothAdapter;
    private TextView tvBluetoothStatus;
    private Button btnToggleBluetooth;
    private Button btnListDevices;
    private ListView lvPairedDevices;
    private ActivityResultLauncher<Intent> btLauncher;

    private ArrayAdapter<String> deviceAdapter;
    private ArrayList<String> deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_bluetooth);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;


        });
        initViews();
        activityResult();
        bluetoothSetup();
        eventsClick();
    }


    private void activityResult () {
        btLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Toast.makeText(this, "Bluetooth activado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Bluetooth no fue activado", Toast.LENGTH_SHORT).show();
                    }
                    updateBluetoothStatus();
                }
        );
    }


    private void bluetoothSetup() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d("Bluetooth", "Adapter: " + bluetoothAdapter);
        if (bluetoothAdapter == null) {
            Log.e("Bluetooth", "No hay soporte para Bluetooth");
            tvBluetoothStatus.setText("El dispositivo no soporta Bluetooth");
            btnToggleBluetooth.setEnabled(false);
            btnListDevices.setEnabled(false);
        } else {
            Log.d("Bluetooth", "Bluetooth soportado, estado: " + bluetoothAdapter.isEnabled());
            updateBluetoothStatus();
        }
    }

    // Eventos click
    private void eventsClick() {
        btnToggleBluetooth.setOnClickListener(v -> toggleBluetooth());
        btnListDevices.setOnClickListener(v -> checkPermissionsAndListDevices());
    }

    // Activar/Desactivar Bluetooth
    private void toggleBluetooth() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // Activar Bluetooth
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                            != PackageManager.PERMISSION_GRANTED) {
                requestBluetoothPermissions();
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                btLauncher.launch(enableBtIntent);
            }
        } else {
            // Desactivar Bluetooth
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestBluetoothPermissions();
                    return;
                }
            }

            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        == PackageManager.PERMISSION_GRANTED) {
                    boolean disableResult = bluetoothAdapter.disable();
                    if (disableResult) {
                        Toast.makeText(this, "Bluetooth desactivado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al desactivar Bluetooth", Toast.LENGTH_SHORT).show();
                    }
                    updateBluetoothStatus();
                }
            } catch (SecurityException e) {
                Toast.makeText(this, "Permiso denegado: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                requestBluetoothPermissions();
            }
        }
    }

    private void checkPermissionsAndListDevices() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            requestBluetoothPermissions();
        } else {
            listPairedDevices();
        }
    }

    private void requestBluetoothPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_CONNECT
                },
                REQUEST_BLUETOOTH_PERMISSIONS);
    }

    // Listar dispositivos emparejados
    private void listPairedDevices() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth no está activado", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        deviceList.clear();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceList.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            deviceList.add("No hay dispositivos emparejados");
        }

        deviceAdapter.notifyDataSetChanged();
    }

    private void updateBluetoothStatus() {
        if (bluetoothAdapter == null) return;

        if (bluetoothAdapter.isEnabled()) {
            tvBluetoothStatus.setText("Bluetooth: ACTIVADO");
            btnToggleBluetooth.setText("Desactivar Bluetooth");
        } else {
            tvBluetoothStatus.setText("Bluetooth: DESACTIVADO");
            btnToggleBluetooth.setText("Activar Bluetooth");
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                    toggleBluetooth();
                } else {
                    listPairedDevices();
                }
            } else {
                Toast.makeText(this, "Los permisos son necesarios para esta función", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Inicializador
    private void initViews() {
            this.tvBluetoothStatus = findViewById(R.id.tvBluetoothStatus);
            this.btnToggleBluetooth = findViewById(R.id.btnToggleBluetooth);
            this.btnListDevices = findViewById(R.id.btnListDevices);
            this.lvPairedDevices = findViewById(R.id.lvPairedDevices);
            this.deviceList = new ArrayList<>();
            this.deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
            this.lvPairedDevices.setAdapter(deviceAdapter);
        }
    }
