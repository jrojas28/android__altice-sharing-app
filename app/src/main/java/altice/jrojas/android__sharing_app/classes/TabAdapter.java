package altice.jrojas.android__sharing_app.classes;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import altice.jrojas.android__sharing_app.activities.ArticleFeedFragment;
import altice.jrojas.android__sharing_app.activities.NewArticleFragment;
import altice.jrojas.android__sharing_app.activities.SignInFragment;

/**
 * Created by jaime on 6/7/2018.
 */

public class TabAdapter extends FragmentPagerAdapter {
    private static final String TAG = "TAB_ADAPTER";
    private static final int FRAGMENT_COUNT = 3;

    public TabAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        Log.wtf(TAG, String.valueOf(position));
        switch(position) {
            case 0:
                Log.wtf(TAG, "Entering Case 0: Article Feed");
                return ArticleFeedFragment.newInstance();
            case 1:
                Log.wtf(TAG, "Entering Case 1: New Article");
                return NewArticleFragment.newInstance();
            case 2:
                Log.wtf(TAG, "Entering Case 2: Sign In");
                return SignInFragment.newInstance();
            default:
                Log.wtf(TAG, "Entering Case Default");
                return ArticleFeedFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return FRAGMENT_COUNT;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position) {
            case 0:
                return "Articulos";
            case 1:
                return "Crear Articulo";
            case 2:
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                return user != null ? "Mi Perfil" : "Iniciar Sesion";
            default:
                return "Ultimos Articulos";
        }
    }
}
