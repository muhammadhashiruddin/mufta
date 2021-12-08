package android.example.mufta;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.example.mufta.adapters.DrawerAdapter;
import android.example.mufta.adapters.SimpleItem;
import android.example.mufta.adapters.SpaceItem;
import android.example.mufta.fragments.AboutUsFragment;
import android.example.mufta.fragments.PermissionsFragment;
import android.example.mufta.fragments.SearchFragment;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.Arrays;

import static android.example.mufta.ImmutableConstants.REQUEST_CODE;
import static android.example.mufta.fragments.SearchFragment.broadcaster1;

public class MainActivity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener {

    private final static int POS_BACK = 0;
    private final static int POS_SEARCH = 1;
    private final static int POS_PERMISSIONS = 2;
    private final static int POS_ABOUT_US = 3;
    private final static int POS_EXIT = 5;
    int i;
    SharedPreferences.Editor editor;

    private String[] screenTitles;
    private Drawable[] screenIcons;

    private SlidingRootNav slidingRootNav;

    private Toolbar toolbar;
    private RelativeLayout fLayout;
    private MenuItem itemMenu;
    private RelativeLayout.LayoutParams layoutParams;
    private boolean backVisible = false;
    private int height = 0;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.main_activity);

        toolbar = findViewById(R.id.toolbaar);
        setSupportActionBar(toolbar);

        slidingRootNav = new SlidingRootNavBuilder(this)
                .withDragDistance(180)
                .withRootViewScale(0.75f)
                .withRootViewElevation(25)
                .withToolbarMenuToggle(toolbar)
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.drawer_menu_layout)
                .addDragListener(progress -> {
                    if(i!=0)
                        return;
                    if (progress == 1) {
                        backDorp(1);
                    } else if (progress == 0)
                        backDorp(2);
                })
                .inject();

        adapterInit();
    }

    private void adapterInit() {
        screenTitles = loadScreenTitles();
        screenIcons = loadIcons();

        DrawerAdapter adapter = new DrawerAdapter(Arrays.asList(
                createItemFor(POS_BACK),
                createItemFor(POS_SEARCH).setChecked(true),
                createItemFor(POS_PERMISSIONS),
                createItemFor(POS_ABOUT_US),
                new SpaceItem(250),
                createItemFor(POS_EXIT)
        ));
        adapter.setListener(this);
        RecyclerView list = findViewById(R.id.drawer_recycler);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        adapter.setSelected(POS_SEARCH);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_item, menu);
        itemMenu = menu.findItem(R.id.more);
        backDorp(2);
        return true;
    }

    private Drawable[] loadIcons() {
        TypedArray ta = getResources().obtainTypedArray(R.array.id_activity_screen_icon);
        Drawable[] icons = new Drawable[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            int id = ta.getResourceId(i, 0);
            if (id != 0) {
                icons[i] = ContextCompat.getDrawable(this, id);
            }
        }
        ta.recycle();
        return icons;
    }

    @Override
    public void onBackPressed() {
        if(slidingRootNav.isMenuClosed())
            slidingRootNav.openMenu();
        else
            slidingRootNav.closeMenu();
    }

    private String[] loadScreenTitles() {
        return getResources().getStringArray(R.array.id_activity_screen_title);
    }

    private SimpleItem createItemFor(int position) {
        return new SimpleItem(screenIcons[position], screenTitles[position])
                .withIconTint(color(R.color.recycler_txt_unselected))
                .withTitleTint(color(R.color.recycler_txt_unselected))
                .withSelectedIconTint(color(R.color.recycler_txt_selected))
                .withSelectedTitleTint(color(R.color.recycler_txt_selected));
    }

    @ColorInt
    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
    }

    @Override
    public void onItemSelected(int position) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (position == POS_BACK) {
            slidingRootNav.closeMenu();
        } else if (position == POS_SEARCH) {
            i = 0;
            SearchFragment searchFragment = new SearchFragment();
            transaction.replace(R.id.container, searchFragment);
            toolbar.setBackground(new ColorDrawable(color(R.color.main_bg)));
        } else if (position == POS_PERMISSIONS) {
            i = -1;
            PermissionsFragment permissionsFragment = new PermissionsFragment();
            transaction.replace(R.id.container, permissionsFragment);

        } else if (position == POS_ABOUT_US) {
            i = -1;
            AboutUsFragment aboutUsFragment = new AboutUsFragment();
            transaction.replace(R.id.container, aboutUsFragment);
        } else if (position == POS_EXIT) {
            finish();
        }

        slidingRootNav.closeMenu();
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(i==0) {
            if (item.getItemId() == R.id.more) {
                if (backVisible)
                    backDorp(4);
                else
                    backDorp(3);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void backDorp(int caseId) {
        fLayout = findViewById(R.id.fr_layout);
        LinearLayout bLayout = findViewById(R.id.bk_layout);
        itemMenu.setIcon(ContextCompat.getDrawable(this,
                backVisible ? R.drawable.ic_baseline_expand_less_24 : R.drawable.ic_baseline_expand_more_24));
        layoutParams = (RelativeLayout.LayoutParams) fLayout.getLayoutParams();
        switch (caseId) {
            case 1:
                valueAnimator(height, 0);
                break;
            case 2:
                valueAnimator(height, 200);
                break;
            case 3:
                backVisible = !backVisible;
                valueAnimator(height, bLayout.getHeight());
                break;
            case 4:
                backVisible = !backVisible;
                valueAnimator(height, 200);
                break;
        }
    }

    void valueAnimator(int i, int j) {
        ValueAnimator var = ValueAnimator.ofInt(i, j);
        var.setDuration(400);
        var.addUpdateListener(animation -> {
            layoutParams.setMargins(0,
                    (Integer) animation.getAnimatedValue(),
                    0,
                    0);
            fLayout.setLayoutParams(layoutParams);
        });
        height = j;
        var.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

            switch (response.getType()) {
                case EMPTY:
                case UNKNOWN:
                case CODE:
                default:
                case ERROR:
                    new AlertDialog.Builder(this)
                            .setTitle("Spotify Authentication failed")
                            .setMessage("Authentication is required in order " +
                                    "to fetch the songs from Spotify")

                            .setPositiveButton(android.R.string.yes, (dialog, which) -> finish())

                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    break;

                case TOKEN:
                    SharedPreferences prefs = getSharedPreferences("packageName", MODE_PRIVATE);
                    editor = prefs.edit();
                    ImmutableConstants.accessToken = response.getAccessToken();
                    editor.putString("access_token", ImmutableConstants.accessToken);
                    editor.apply();
                    if(i==0)
                        broadcaster1.setRedirect(true);
                    break;
            }
        }
    }

}