package ru.arlen.androidaidl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends FragmentActivity implements IActivityCallbacks {
    private static final String ACTION_AIDL = "ru.arlen.aidl.IDataInterface";
    public static final String TEXT_ARG = "TEXT";
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

    @Override
    public void pressSendButton(String text) {
        try {
            iDataInterface.saveText(text);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bundle = new Bundle();
                    String loaded = iDataInterface.loadText();
                    bundle.putString(TEXT_ARG, loaded);
                    ReadFragment readFragment = new ReadFragment();
                    readFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction()
                                               .replace(R.id.fragmentRead, readFragment)
                                               .commit();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }, 2000);
    }
}
