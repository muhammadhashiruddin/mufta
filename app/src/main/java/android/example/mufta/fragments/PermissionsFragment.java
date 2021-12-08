package android.example.mufta.fragments;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.example.mufta.ImmutableConstants;
import android.example.mufta.MainActivity;
import android.example.mufta.R;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static android.example.mufta.ImmutableConstants.spotifyAuthenticationRequest;
import static android.example.mufta.ImmutableConstants.storagePermissions;

public class PermissionsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_permissions, container, false);

        Button spotifyPermission = root.findViewById(R.id.spotifyPermission);
        Button storagePermission = root.findViewById(R.id.storagePermission);
        Button bgPermission = root.findViewById(R.id.bgPermission);

        spotifyPermission.setOnClickListener(v -> spotifyAuthenticationRequest(PermissionsFragment.this.getActivity()));

        storagePermission.setOnClickListener(v -> storagePermissions(PermissionsFragment.this.getActivity()));

        bgPermission.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS);
            Uri uri = Uri.fromParts("package",
                    PermissionsFragment.this.getActivity().getPackageName(),
                    null);
            intent.setData(uri);
            startActivity(intent);
        });

        return root;
    }
}