package ru.arlen.androidaidl;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {
    private static final String ACTION_AIDL = "ru.arlen.aidl.IDataInterface";
    private IDataInterface iDataInterface;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iDataInterface = IDataInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iDataInterface = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView textOutput = findViewById(R.id.textOutput);
        final TextView textInput = findViewById(R.id.textInput);

        View send = findViewById(R.id.sendBtn);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String text = textInput.getText().toString().trim();
                    if (!text.isEmpty()) {
                        iDataInterface.saveText(text);
                        Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                        textOutput.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String loaded = iDataInterface.loadText();
                                    if (!loaded.isEmpty()) {
                                        String newText = getResources().getString(R.string.text_view_text) + loaded;
                                        textOutput.setText(newText);
                                    }
                                } catch (RemoteException e) {
                                    Log.w("MainActivity", e.getMessage());
                                }

                            }
                        }, 2000);
                    }
                } catch (RemoteException e) {
                    Log.w("MainActivity", e.getMessage());
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent explicitIntent = createExplicitIntent(this, new Intent(ACTION_AIDL));
        if (explicitIntent != null) {
            bindService(explicitIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(serviceConnection);
    }

    private Intent createExplicitIntent(Context context, Intent intent) {
        // Получить все службы, которые могут соответствовать указанному Intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(intent, 0);

        // Список найденных служб по интенту должен содержать лишь 1 элемент
        if (resolveInfo == null || resolveInfo.size() != 1) {
            // иначе служба на "приложении-сервере" не запущена и мы должны вернуть null
            return null;
        }
        // Получаем информацию о компоненте и создаем ComponentName для Intent
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Повторно используем старый интент
        Intent explicitIntent = new Intent(intent);
        // явно задаем компонент для обработки Intent
        explicitIntent.setComponent(component);

        return explicitIntent;
    }
}
